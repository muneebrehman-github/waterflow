package com.spgg.backend.customer;

public record AddressResponse(Long id, String label, String addressText, String area, boolean isDefault) {

    public static AddressResponse from(CustomerAddress address) {
        return new AddressResponse(
                address.getId(), address.getLabel(), address.getAddressText(), address.getArea(), address.isDefault());
    }
}
