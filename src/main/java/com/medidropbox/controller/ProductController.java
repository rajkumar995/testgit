package com.medidropbox.controller;

import com.medidropbox.dto.request.ProductRequest;
import com.medidropbox.dto.request.StockMovementRequest;
import com.medidropbox.dto.response.ProductResponse;
import com.medidropbox.dto.response.StockMovementResponse;
import com.medidropbox.exception.ForbiddenException;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/products")
@Tag(name = "Inventory / Products", description = "Hospital product and stock management")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private static void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
            throw new ForbiddenException("Access denied to this hospital");
        }
    }

    @PostMapping
    @Operation(summary = "Create product")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long hospitalId,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.create(hospitalId, request));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long hospitalId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.update(hospitalId, productId, request));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long hospitalId,
            @PathVariable Long productId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.getById(hospitalId, productId));
    }

    @GetMapping
    @Operation(summary = "List products")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ProductResponse>> list(
            @PathVariable Long hospitalId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.listByHospital(hospitalId, activeOnly));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Deactivate product")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hospitalId,
            @PathVariable Long productId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        productService.delete(hospitalId, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/stock-movements")
    @Operation(summary = "Add stock movement (receive / sell / adjustment)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StockMovementResponse> addStockMovement(
            @PathVariable Long hospitalId,
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.addStockMovement(hospitalId, productId, request));
    }

    @GetMapping("/{productId}/stock-history")
    @Operation(summary = "Get stock movement history")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<StockMovementResponse>> getStockHistory(
            @PathVariable Long hospitalId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(productService.getStockHistory(hospitalId, productId, PageRequest.of(page, size)));
    }

    @GetMapping("/unit-types")
    @Operation(summary = "Get available unit types")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<String>> getUnitTypes(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        List<String> unitTypes = List.of(
            "mg", "g", "ml", "l",
            "tablet", "tablets", "capsule", "capsules",
            "drop", "drops", "bottle", "bottles",
            "vial", "vials", "syringe", "syringes",
            "box", "boxes", "pack", "packs",
            "strip", "strips", "unit", "units"
        );
        return ResponseEntity.ok(unitTypes);
    }
}
