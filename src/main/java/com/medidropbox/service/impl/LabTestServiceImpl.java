package com.medidropbox.service.impl;

import com.medidropbox.dto.request.LabTestRequest;
import com.medidropbox.dto.response.LabTestResponse;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.LabTest;
import com.medidropbox.exception.BadRequestException;
import com.medidropbox.exception.ResourceNotFoundException;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.LabTestRepository;
import com.medidropbox.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public LabTestResponse create(Long hospitalId, LabTestRequest request) {
        if (request.getCode() != null && !request.getCode().isBlank()
                && labTestRepository.existsByHospitalIdAndCode(hospitalId, request.getCode().trim())) {
            throw new BadRequestException("Lab test with code already exists for this hospital");
        }
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        LabTest labTest = new LabTest();
        labTest.setHospital(hospital);
        mapRequestToLabTest(request, labTest);
        labTest = labTestRepository.save(labTest);
        return mapToResponse(labTest);
    }

    @Override
    public LabTestResponse update(Long hospitalId, Long labTestId, LabTestRequest request) {
        LabTest labTest = labTestRepository.findByIdAndHospitalId(labTestId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", labTestId));
        if (request.getCode() != null && !request.getCode().isBlank()) {
            labTestRepository.findByHospitalIdAndCodeAndIdNot(hospitalId, request.getCode().trim(), labTestId)
                    .ifPresent(lt -> {
                        throw new BadRequestException("Another lab test with this code exists");
                    });
        }
        mapRequestToLabTest(request, labTest);
        labTest = labTestRepository.save(labTest);
        return mapToResponse(labTest);
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestResponse getById(Long hospitalId, Long labTestId) {
        LabTest labTest = labTestRepository.findByIdAndHospitalId(labTestId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", labTestId));
        return mapToResponse(labTest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponse> listByHospital(Long hospitalId, boolean activeOnly) {
        List<LabTest> list = activeOnly
                ? labTestRepository.findByHospitalIdAndIsActiveTrueOrderByName(hospitalId)
                : labTestRepository.findByHospitalIdOrderByName(hospitalId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void delete(Long hospitalId, Long labTestId) {
        LabTest labTest = labTestRepository.findByIdAndHospitalId(labTestId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", labTestId));
        labTest.setIsActive(false);
        labTestRepository.save(labTest);
    }

    private void mapRequestToLabTest(LabTestRequest request, LabTest labTest) {
        labTest.setName(request.getName() != null ? request.getName().trim() : null);
        labTest.setCode(request.getCode() != null ? request.getCode().trim() : null);
        labTest.setCharge(request.getCharge());
        labTest.setDescription(request.getDescription());
        if (request.getIsActive() != null) labTest.setIsActive(request.getIsActive());
    }

    private LabTestResponse mapToResponse(LabTest lt) {
        return LabTestResponse.builder()
                .id(lt.getId())
                .hospitalId(lt.getHospital().getId())
                .name(lt.getName())
                .code(lt.getCode())
                .charge(lt.getCharge())
                .description(lt.getDescription())
                .isActive(lt.getIsActive())
                .build();
    }
}
