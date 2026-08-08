package com.spgg.backend.shop;

import com.spgg.backend.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public — lets a prospective customer pick which shop to register with. */
@RestController
@RequestMapping("/api/v1/shops/directory")
@RequiredArgsConstructor
public class ShopDirectoryController {

    private final ShopRepository shopRepository;

    @GetMapping
    public ApiResponse<List<ShopDirectoryResponse>> list() {
        return ApiResponse.ok(
                shopRepository.findByStatusAndDeletedFalse(ShopStatus.ACTIVE).stream()
                        .map(ShopDirectoryResponse::from)
                        .toList());
    }
}
