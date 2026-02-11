package com.medidropbox.service;

import com.medidropbox.dto.request.ProductRequest;
import com.medidropbox.dto.request.StockMovementRequest;
import com.medidropbox.dto.response.ProductResponse;
import com.medidropbox.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse create(Long hospitalId, ProductRequest request);

    ProductResponse update(Long hospitalId, Long productId, ProductRequest request);

    ProductResponse getById(Long hospitalId, Long productId);

    List<ProductResponse> listByHospital(Long hospitalId, boolean activeOnly);

    void delete(Long hospitalId, Long productId);

    StockMovementResponse addStockMovement(Long hospitalId, Long productId, StockMovementRequest request);

    List<StockMovementResponse> getStockHistory(Long hospitalId, Long productId, Pageable pageable);
}
