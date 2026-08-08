package com.spgg.backend.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPriceHistoryRepository priceHistoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, priceHistoryRepository);
        TenantContext.setTenantId(7L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Product productWithId(long id) {
        Product product = new Product();
        product.setId(id);
        product.setTenantId(7L);
        product.setName("19L Bottle");
        product.setUnitPrice(BigDecimal.valueOf(300));
        product.setDepositAmount(BigDecimal.valueOf(500));
        return product;
    }

    @Test
    void create_setsTenantIdAndRecordsInitialPriceHistory() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.create(
                new ProductCreateRequest("19L Bottle", BigDecimal.valueOf(300), BigDecimal.valueOf(500), 1));

        assertThat(response.name()).isEqualTo("19L Bottle");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getTenantId()).isEqualTo(7L);

        ArgumentCaptor<ProductPriceHistory> historyCaptor = ArgumentCaptor.forClass(ProductPriceHistory.class);
        verify(priceHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(historyCaptor.getValue().getProductId()).isEqualTo(1L);
    }

    @Test
    void create_throwsBadRequest_whenNoTenantInContext() {
        TenantContext.clear();

        assertThatThrownBy(() -> productService.create(
                new ProductCreateRequest("19L Bottle", BigDecimal.valueOf(300), BigDecimal.valueOf(500), 1)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updatePrice_recordsANewHistoryEntry() {
        Product product = productWithId(1L);
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        productService.updatePrice(1L, new ProductPriceUpdateRequest(BigDecimal.valueOf(350)));

        assertThat(product.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(350));
        ArgumentCaptor<ProductPriceHistory> historyCaptor = ArgumentCaptor.forClass(ProductPriceHistory.class);
        verify(priceHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(350));
    }

    @Test
    void update_doesNotTouchPriceOrRecordHistory() {
        Product product = productWithId(1L);
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        productService.update(1L, new ProductUpdateRequest("19L Bottle (Large)", BigDecimal.valueOf(600), 2));

        assertThat(product.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(product.getDepositAmount()).isEqualByComparingTo(BigDecimal.valueOf(600));
        verify(priceHistoryRepository, never()).save(any());
    }

    @Test
    void toggleAvailability_flipsStatusBothWays() {
        Product product = productWithId(1L);
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.UNAVAILABLE);

        productService.toggleAvailability(1L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.AVAILABLE);

        verify(productRepository, times(2)).save(product);
    }

    @Test
    void archive_softDeletesRatherThanRemoving() {
        Product product = productWithId(1L);
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        productService.archive(1L);

        assertThat(product.isDeleted()).isTrue();
        verify(productRepository).save(product);
    }
}
