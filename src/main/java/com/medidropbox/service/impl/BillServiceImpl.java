package com.medidropbox.service.impl;

import com.medidropbox.dto.request.BillItemRequest;
import com.medidropbox.dto.request.BillRequest;
import com.medidropbox.dto.response.BillItemResponse;
import com.medidropbox.dto.response.BillResponse;
import com.medidropbox.entity.*;
import com.medidropbox.enums.BillStatus;
import com.medidropbox.enums.StockMovementType;
import com.medidropbox.exception.BadRequestException;
import com.medidropbox.exception.ResourceNotFoundException;
import com.medidropbox.repository.*;
import com.medidropbox.service.BillService;
import com.medidropbox.repository.PharmacyCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final ProductRepository productRepository;
    private final LabTestRepository labTestRepository;
    private final ProductStockMovementRepository stockMovementRepository;
    private final com.medidropbox.repository.PharmacyCounterRepository pharmacyCounterRepository;

    @Override
    public BillResponse create(Long hospitalId, BillRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        HospitalPatient patient = hospitalPatientRepository.findById(request.getHospitalPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("HospitalPatient", request.getHospitalPatientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new BadRequestException("Patient does not belong to this hospital");
        }
        Bill bill = new Bill();
        bill.setHospital(hospital);
        bill.setHospitalPatient(patient);
        bill.setStatus(BillStatus.DRAFT);
        bill.setNotes(request.getNotes());
        bill = billRepository.save(bill);
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addItemsToBill(bill, request.getItems(), hospitalId);
            recalculateTotal(bill);
        }
        return mapToResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse update(Long hospitalId, Long billId, BillRequest request) {
        Bill bill = billRepository.findByIdAndHospitalId(billId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billId));
        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT bills can be updated");
        }
        if (request.getHospitalPatientId() != null && !request.getHospitalPatientId().equals(bill.getHospitalPatient().getId())) {
            HospitalPatient patient = hospitalPatientRepository.findById(request.getHospitalPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("HospitalPatient", request.getHospitalPatientId()));
            if (!patient.getHospital().getId().equals(hospitalId)) {
                throw new BadRequestException("Patient does not belong to this hospital");
            }
            bill.setHospitalPatient(patient);
        }
        bill.setNotes(request.getNotes());
        bill.getItems().clear();
        billItemRepository.flush();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addItemsToBill(bill, request.getItems(), hospitalId);
        }
        recalculateTotal(bill);
        return mapToResponse(billRepository.save(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getById(Long hospitalId, Long billId) {
        Bill bill = billRepository.findByIdAndHospitalId(billId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billId));
        return mapToResponse(bill);
    }

    @Override
    public BillResponse completeBill(Long hospitalId, Long billId) {
        Bill bill = billRepository.findByIdAndHospitalId(billId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billId));
        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new BadRequestException("Bill is not in DRAFT status");
        }
        for (BillItem item : bill.getItems()) {
            if (item.getProduct() != null) {
                int current = stockMovementRepository.sumQuantityByProductId(item.getProduct().getId());
                if (current < item.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + item.getProduct().getName());
                }
                ProductStockMovement movement = new ProductStockMovement();
                movement.setProduct(item.getProduct());
                movement.setMovementType(StockMovementType.SOLD);
                movement.setQuantity(-item.getQuantity());
                movement.setUnitPrice(item.getUnitPrice());
                movement.setReferenceId(bill.getId());
                movement.setNotes("Bill #" + bill.getId());
                stockMovementRepository.save(movement);
            }
        }
        bill.setStatus(BillStatus.COMPLETED);
        // Extract total amount before reassigning bill (needed for lambda)
        final BigDecimal billTotalAmount = bill.getTotalAmount();
        bill = billRepository.save(bill);
        
        // Update pharmacy counter total sales if counter is open
        pharmacyCounterRepository.findByHospitalIdAndIsOpenTrue(hospitalId)
                .ifPresent(counter -> {
                    BigDecimal currentTotal = counter.getTotalSales() != null ? counter.getTotalSales() : BigDecimal.ZERO;
                    counter.setTotalSales(currentTotal.add(billTotalAmount));
                    pharmacyCounterRepository.save(counter);
                });
        
        return mapToResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> listByHospital(Long hospitalId, Pageable pageable) {
        return billRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> listByHospitalAndPatient(Long hospitalId, Long hospitalPatientId) {
        return billRepository.findByHospitalIdAndHospitalPatientIdOrderByCreatedAtDesc(hospitalId, hospitalPatientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void addItemsToBill(Bill bill, List<BillItemRequest> itemRequests, Long hospitalId) {
        for (BillItemRequest req : itemRequests) {
            if (req.getProductId() != null) {
                Product product = productRepository.findByIdAndHospitalId(req.getProductId(), hospitalId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));
                BigDecimal unitPrice = req.getUnitPriceOrNull() != null ? req.getUnitPriceOrNull() : product.getSalePrice();
                if (unitPrice == null) unitPrice = BigDecimal.ZERO;
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));
                BillItem item = new BillItem();
                item.setBill(bill);
                item.setProduct(product);
                item.setQuantity(req.getQuantity());
                item.setUnitPrice(unitPrice);
                item.setAmount(amount);
                item.setItemLabel(product.getName());
                bill.getItems().add(item);
            } else if (req.getLabTestId() != null) {
                LabTest labTest = labTestRepository.findByIdAndHospitalId(req.getLabTestId(), hospitalId)
                        .orElseThrow(() -> new ResourceNotFoundException("LabTest", req.getLabTestId()));
                BigDecimal unitPrice = req.getUnitPriceOrNull() != null ? req.getUnitPriceOrNull() : labTest.getCharge();
                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));
                BillItem item = new BillItem();
                item.setBill(bill);
                item.setLabTest(labTest);
                item.setQuantity(req.getQuantity());
                item.setUnitPrice(unitPrice);
                item.setAmount(amount);
                item.setItemLabel(labTest.getName());
                bill.getItems().add(item);
            }
        }
    }

    private void recalculateTotal(Bill bill) {
        BigDecimal total = bill.getItems().stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(total);
    }

    private BillResponse mapToResponse(Bill bill) {
        List<BillItemResponse> itemResponses = bill.getItems() == null ? new ArrayList<>()
                : bill.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList());
        return BillResponse.builder()
                .id(bill.getId())
                .hospitalId(bill.getHospital().getId())
                .hospitalPatientId(bill.getHospitalPatient().getId())
                .patientName(bill.getHospitalPatient().getFullName())
                .patientPhone(bill.getHospitalPatient().getPhone())
                .status(bill.getStatus())
                .totalAmount(bill.getTotalAmount())
                .notes(bill.getNotes())
                .createdAt(bill.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    private BillItemResponse mapItemToResponse(BillItem item) {
        return BillItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .labTestId(item.getLabTest() != null ? item.getLabTest().getId() : null)
                .itemLabel(item.getItemLabel())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();
    }
}
