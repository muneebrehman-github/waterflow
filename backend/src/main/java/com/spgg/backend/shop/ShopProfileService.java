package com.spgg.backend.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopProfileService {

    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper;

    public ShopProfileResponse getMyProfile() {
        return toResponse(requireMyShop());
    }

    @Transactional
    public ShopProfileResponse updateProfile(ShopProfileUpdateRequest request) {
        Shop shop = requireMyShop();
        shop.setName(request.name());
        shop.setDescription(request.description());
        shop.setAddress(request.address());
        shopRepository.save(shop);
        return toResponse(shop);
    }

    @Transactional
    public ShopProfileResponse updateBusinessHours(HoursUpdateRequest request) {
        Shop shop = requireMyShop();
        shop.setBusinessHoursJson(writeHours(request.hours()));
        shopRepository.save(shop);
        return toResponse(shop);
    }

    @Transactional
    public ShopProfileResponse updateDeliveryHours(HoursUpdateRequest request) {
        Shop shop = requireMyShop();
        shop.setDeliveryHoursJson(writeHours(request.hours()));
        shopRepository.save(shop);
        return toResponse(shop);
    }

    @Transactional
    public ShopProfileResponse updateDeliverySettings(DeliverySettingsRequest request) {
        if (request.deliveryChargeTiers().isEmpty()) {
            throw new BadRequestException("At least one delivery charge tier is required");
        }
        Shop shop = requireMyShop();
        shop.setDeliveryRadiusKm(request.deliveryRadiusKm());
        shop.setMinOrderValue(request.minOrderValue());
        shop.setDeliveryChargeTiersJson(writeJson(request.deliveryChargeTiers()));
        shopRepository.save(shop);
        return toResponse(shop);
    }

    private String writeHours(List<DayHours> hours) {
        long distinctDays = hours.stream().map(DayHours::day).distinct().count();
        if (hours.size() != 7 || distinctDays != 7) {
            throw new BadRequestException("Hours must cover all 7 days of the week exactly once");
        }
        return writeJson(hours);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize shop config", e);
        }
    }

    private List<DayHours> readHours(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DayHours>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse stored shop hours", e);
        }
    }

    private List<DeliveryChargeTier> readTiers(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DeliveryChargeTier>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse stored delivery charge tiers", e);
        }
    }

    private Shop requireMyShop() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BadRequestException("No shop associated with this account");
        }
        return shopRepository.findByIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + tenantId));
    }

    private ShopProfileResponse toResponse(Shop shop) {
        return new ShopProfileResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getOwnerName(),
                shop.getEmail(),
                shop.getPhone(),
                shop.getAddress(),
                shop.getStatus(),
                readHours(shop.getBusinessHoursJson()),
                readHours(shop.getDeliveryHoursJson()),
                shop.getDeliveryRadiusKm(),
                shop.getMinOrderValue(),
                readTiers(shop.getDeliveryChargeTiersJson()));
    }
}
