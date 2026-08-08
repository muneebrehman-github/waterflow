package com.spgg.backend.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.TenantContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {

    @Mock
    private CustomerAddressRepository addressRepository;

    private CustomerAddressService addressService;

    private final List<CustomerAddress> savedAddresses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        addressService = new CustomerAddressService(addressRepository);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(42L, null, List.of()));
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        savedAddresses.clear();
    }

    private CustomerAddress addressWithId(long id, boolean isDefault) {
        CustomerAddress address = new CustomerAddress();
        address.setId(id);
        address.setUserId(42L);
        address.setLabel("Home");
        address.setAddressText("123 Main St");
        address.setDefault(isDefault);
        return address;
    }

    @Test
    void create_asDefault_clearsAnyExistingDefaultFirst() {
        CustomerAddress existingDefault = addressWithId(1L, true);
        when(addressRepository.findByUserIdAndDeletedFalse(42L)).thenReturn(List.of(existingDefault));
        when(addressRepository.save(any(CustomerAddress.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.create(new AddressRequest("Work", "456 Office Rd", null, true));

        assertThat(existingDefault.isDefault()).isFalse();
    }

    @Test
    void setDefault_clearsOthersAndSetsTheChosenOne() {
        CustomerAddress addressA = addressWithId(1L, true);
        CustomerAddress addressB = addressWithId(2L, false);
        when(addressRepository.findByUserIdAndDeletedFalse(42L)).thenReturn(List.of(addressA, addressB));
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(2L, 42L)).thenReturn(Optional.of(addressB));
        when(addressRepository.save(any(CustomerAddress.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.setDefault(2L);

        assertThat(addressA.isDefault()).isFalse();
        assertThat(addressB.isDefault()).isTrue();
    }

    @Test
    void delete_softDeletes() {
        CustomerAddress address = addressWithId(1L, false);
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(1L, 42L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(CustomerAddress.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.delete(1L);

        assertThat(address.isDeleted()).isTrue();
    }
}
