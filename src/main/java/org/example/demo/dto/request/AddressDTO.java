package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.example.demo.domain.Address;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AddressDTO {

    @NotEmpty
    private String zipCode;

    @NotEmpty
    private String streetAddress;

    @NotEmpty
    private String detailAddress;


    public static Address toAddress(AddressDTO dto){
        return new Address(dto.getZipCode(), dto.getStreetAddress(), dto.getDetailAddress());
    }
}
