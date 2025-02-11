package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.demo.domain.Address;

@Data
public class AddressDTO {

    @NotBlank
    private String zipCode;

    @NotBlank
    private String streetAddress;

    @NotBlank
    private String detailAddress;


    public static Address toAddress(AddressDTO dto){
        return new Address(dto.getZipCode(), dto.getStreetAddress(), dto.getDetailAddress());
    }
}
