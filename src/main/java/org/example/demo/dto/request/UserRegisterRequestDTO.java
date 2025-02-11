package org.example.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.demo.domain.User;

import javax.validation.constraints.NotNull;

@Data
public class UserRegisterRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Valid
    @NotNull
    private AddressDTO address = new AddressDTO();

    public static User toUser(UserRegisterRequestDTO requestDTO) {
        return User.builder().name(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .address(AddressDTO.toAddress(requestDTO.getAddress()))
                .build();
    }
}
