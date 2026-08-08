package com.spgg.backend.customer;

import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.security.CurrentUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;

    public List<AddressResponse> list() {
        return addressRepository.findByUserIdAndDeletedFalse(CurrentUser.id()).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        Long userId = CurrentUser.id();
        if (request.isDefault()) {
            clearExistingDefault(userId);
        }

        CustomerAddress address = new CustomerAddress();
        address.setTenantId(TenantContext.getTenantId());
        address.setUserId(userId);
        address.setLabel(request.label());
        address.setAddressText(request.addressText());
        address.setArea(request.area());
        address.setDefault(request.isDefault());
        addressRepository.save(address);
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Long userId = CurrentUser.id();
        CustomerAddress address = requireOwnAddress(id, userId);

        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(userId);
        }

        address.setLabel(request.label());
        address.setAddressText(request.addressText());
        address.setArea(request.area());
        address.setDefault(request.isDefault());
        addressRepository.save(address);
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse setDefault(Long id) {
        Long userId = CurrentUser.id();
        clearExistingDefault(userId);
        CustomerAddress address = requireOwnAddress(id, userId);
        address.setDefault(true);
        addressRepository.save(address);
        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(Long id) {
        CustomerAddress address = requireOwnAddress(id, CurrentUser.id());
        address.setDeleted(true);
        addressRepository.save(address);
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdAndDeletedFalse(userId).stream()
                .filter(CustomerAddress::isDefault)
                .forEach(existing -> {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                });
    }

    private CustomerAddress requireOwnAddress(Long id, Long userId) {
        return addressRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
    }
}
