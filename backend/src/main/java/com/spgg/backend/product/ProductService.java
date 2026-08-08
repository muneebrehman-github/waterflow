package com.spgg.backend.product;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Long tenantId = requireTenantId();

        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName(request.name());
        product.setUnitPrice(request.unitPrice());
        product.setDepositAmount(request.depositAmount());
        product.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        product = productRepository.save(product);

        recordPriceHistory(product);
        return ProductResponse.from(product);
    }

    /** Full list including unavailable — for Shop Owner/Staff management views. */
    public List<ProductResponse> listAll() {
        return productRepository.findByDeletedFalseOrderByDisplayOrderAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    /** Available-only — for the customer-facing catalog. */
    public List<ProductResponse> listAvailable() {
        return productRepository.findByStatusAndDeletedFalseOrderByDisplayOrderAsc(ProductStatus.AVAILABLE).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = requireProduct(id);
        product.setName(request.name());
        product.setDepositAmount(request.depositAmount());
        if (request.displayOrder() != null) {
            product.setDisplayOrder(request.displayOrder());
        }
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updatePrice(Long id, ProductPriceUpdateRequest request) {
        Product product = requireProduct(id);
        product.setUnitPrice(request.unitPrice());
        productRepository.save(product);
        recordPriceHistory(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse toggleAvailability(Long id) {
        Product product = requireProduct(id);
        product.setStatus(product.getStatus() == ProductStatus.AVAILABLE
                ? ProductStatus.UNAVAILABLE
                : ProductStatus.AVAILABLE);
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    /** Products are never hard-deleted (FR-PRD-006) — this is the only removal path. */
    @Transactional
    public void archive(Long id) {
        Product product = requireProduct(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    public List<ProductPriceHistoryResponse> priceHistory(Long id) {
        requireProduct(id);
        return priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(id).stream()
                .map(ProductPriceHistoryResponse::from)
                .toList();
    }

    private void recordPriceHistory(Product product) {
        ProductPriceHistory history = new ProductPriceHistory();
        history.setTenantId(product.getTenantId());
        history.setProductId(product.getId());
        history.setPrice(product.getUnitPrice());
        priceHistoryRepository.save(history);
    }

    private Product requireProduct(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BadRequestException("No shop associated with this account");
        }
        return tenantId;
    }
}
