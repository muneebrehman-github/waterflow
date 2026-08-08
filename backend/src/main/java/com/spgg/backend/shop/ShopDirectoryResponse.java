package com.spgg.backend.shop;

public record ShopDirectoryResponse(Long id, String name, String description, String address) {

    public static ShopDirectoryResponse from(Shop shop) {
        return new ShopDirectoryResponse(shop.getId(), shop.getName(), shop.getDescription(), shop.getAddress());
    }
}
