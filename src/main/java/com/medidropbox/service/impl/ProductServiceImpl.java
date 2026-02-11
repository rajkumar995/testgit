package com.medidropbox.service.impl;

import com.medidropbox.dto.request.ProductRequest;
import com.medidropbox.dto.request.StockMovementRequest;
import com.medidropbox.dto.response.ProductResponse;
import com.medidropbox.dto.response.StockMovementResponse;
import com.medidropbox.entity.Product;
import com.medidropbox.entity.ProductStockMovement;
import com.medidropbox.enums.StockMovementType;
import com.medidropbox.exception.BadRequestException;
import com.medidropbox.exception.ResourceNotFoundException;
import com.medidropbox.entity.Hospital;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.ProductRepository;
import com.medidropbox.repository.ProductStockMovementRepository;
import com.medidropbox.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductStockMovementRepository stockMovementRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public ProductResponse create(Long hospitalId, ProductRequest request) {
        if (request.getSku() != null && !request.getSku().isBlank()
                && productRepository.existsByHospitalIdAndSku(hospitalId, request.getSku().trim())) {
            throw new BadRequestException("Product with SKU already exists for this hospital");
        }
        Product product = new Product();
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        product.setHospital(hospital);
        mapRequestToProduct(request, product);
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    public ProductResponse update(Long hospitalId, Long productId, ProductRequest request) {
        Product product = productRepository.findByIdAndHospitalId(productId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (request.getSku() != null && !request.getSku().isBlank()) {
            productRepository.findByHospitalIdAndSkuAndIdNot(hospitalId, request.getSku().trim(), productId)
                    .ifPresent(p -> {
                        throw new BadRequestException("Another product with this SKU exists");
                    });
        }
        mapRequestToProduct(request, product);
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long hospitalId, Long productId) {
        Product product = productRepository.findByIdAndHospitalId(productId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listByHospital(Long hospitalId, boolean activeOnly) {
        List<Product> list = activeOnly
                ? productRepository.findByHospitalIdAndIsActiveTrueOrderByName(hospitalId)
                : productRepository.findByHospitalIdOrderByName(hospitalId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long hospitalId, Long productId) {
        Product product = productRepository.findByIdAndHospitalId(productId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public StockMovementResponse addStockMovement(Long hospitalId, Long productId, StockMovementRequest request) {
        Product product = productRepository.findByIdAndHospitalId(productId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        int currentStock = stockMovementRepository.sumQuantityByProductId(productId);
        int qty = request.getQuantity() != null ? request.getQuantity() : 0;
        if (request.getMovementType() == StockMovementType.SOLD || request.getMovementType() == StockMovementType.ADJUSTMENT) {
            if (qty > 0) qty = -qty;
            if (currentStock + qty < 0) {
                throw new BadRequestException("Insufficient stock. Current: " + currentStock);
            }
        } else {
            if (qty < 0) qty = -qty;
        }
        ProductStockMovement movement = new ProductStockMovement();
        movement.setProduct(product);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(qty);
        movement.setUnitPrice(request.getUnitPrice());
        movement.setReferenceId(request.getReferenceId());
        movement.setNotes(request.getNotes());
        movement = stockMovementRepository.save(movement);
        return mapMovementToResponse(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockHistory(Long hospitalId, Long productId, Pageable pageable) {
        Product product = productRepository.findByIdAndHospitalId(productId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .stream()
                .map(this::mapMovementToResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToProduct(ProductRequest request, Product product) {
        product.setName(request.getName() != null ? request.getName().trim() : null);
        product.setSku(request.getSku() != null ? request.getSku().trim() : null);
        product.setUnit(request.getUnit());
        product.setDescription(request.getDescription());
        product.setReorderLevel(request.getReorderLevel());
        product.setSalePrice(request.getSalePrice());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
    }

    private ProductResponse mapToResponse(Product product) {
        int currentStock = stockMovementRepository.sumQuantityByProductId(product.getId());
        return ProductResponse.builder()
                .id(product.getId())
                .hospitalId(product.getHospital().getId())
                .name(product.getName())
                .sku(product.getSku())
                .unit(product.getUnit())
                .description(product.getDescription())
                .reorderLevel(product.getReorderLevel())
                .salePrice(product.getSalePrice())
                .isActive(product.getIsActive())
                .currentStock(currentStock)
                .build();
    }

    private StockMovementResponse mapMovementToResponse(ProductStockMovement m) {
        return StockMovementResponse.builder()
                .id(m.getId())
                .productId(m.getProduct().getId())
                .productName(m.getProduct().getName())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .unitPrice(m.getUnitPrice())
                .referenceId(m.getReferenceId())
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
