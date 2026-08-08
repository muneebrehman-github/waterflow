package com.spgg.backend.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShopProfileServiceTest {

    @Mock
    private ShopRepository shopRepository;

    private ShopProfileService shopProfileService;

    @BeforeEach
    void setUp() {
        shopProfileService = new ShopProfileService(shopRepository, new ObjectMapper().registerModule(new JavaTimeModule()));
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Shop shopWithId(long id) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName("Clear Springs Water");
        return shop;
    }

    @Test
    void updateBusinessHours_rejectsListThatDoesNotCoverAllSevenDays() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shopWithId(1L)));
        List<DayHours> onlyThreeDays = List.of(
                new DayHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false));

        assertThatThrownBy(() -> shopProfileService.updateBusinessHours(new HoursUpdateRequest(onlyThreeDays)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateBusinessHours_rejectsDuplicateDays() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shopWithId(1L)));
        List<DayHours> sevenEntriesButDuplicateMonday = List.of(
                new DayHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false));

        assertThatThrownBy(() ->
                shopProfileService.updateBusinessHours(new HoursUpdateRequest(sevenEntriesButDuplicateMonday)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateBusinessHours_roundTripsThroughJsonCorrectly() {
        Shop shop = shopWithId(1L);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);

        List<DayHours> fullWeek = List.of(
                new DayHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), false),
                new DayHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), false),
                new DayHours(DayOfWeek.SUNDAY, null, null, true));

        ShopProfileResponse response = shopProfileService.updateBusinessHours(new HoursUpdateRequest(fullWeek));

        assertThat(response.businessHours()).hasSize(7);
        assertThat(response.businessHours())
                .filteredOn(d -> d.day() == DayOfWeek.SUNDAY)
                .first()
                .satisfies(sunday -> assertThat(sunday.closed()).isTrue());
    }

    @Test
    void updateDeliverySettings_rejectsEmptyTierList() {
        assertThatThrownBy(() -> shopProfileService.updateDeliverySettings(
                new DeliverySettingsRequest(BigDecimal.TEN, BigDecimal.valueOf(200), List.of())))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateDeliverySettings_savesTieredPricingCorrectly() {
        Shop shop = shopWithId(1L);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);

        List<DeliveryChargeTier> tiers = List.of(
                new DeliveryChargeTier(BigDecimal.ZERO, BigDecimal.valueOf(500), BigDecimal.valueOf(100)),
                new DeliveryChargeTier(BigDecimal.valueOf(500), null, BigDecimal.ZERO));

        ShopProfileResponse response = shopProfileService.updateDeliverySettings(
                new DeliverySettingsRequest(BigDecimal.valueOf(15), BigDecimal.valueOf(200), tiers));

        assertThat(response.deliveryChargeTiers()).hasSize(2);
        assertThat(response.deliveryChargeTiers().get(1).maxOrderValue()).isNull();
    }

    @Test
    void getMyProfile_throwsBadRequest_whenNoTenantInContext() {
        TenantContext.clear();

        assertThatThrownBy(() -> shopProfileService.getMyProfile()).isInstanceOf(BadRequestException.class);
    }
}
