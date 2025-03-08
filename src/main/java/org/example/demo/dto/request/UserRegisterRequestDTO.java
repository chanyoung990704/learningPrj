package org.example.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.example.demo.domain.Role;
import org.example.demo.domain.User;
import org.example.demo.validator.UniqueEmail;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserRegisterRequestDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    @Email
    @UniqueEmail
    private String email;

    @NotBlank
    private String password;

    @Valid
    @Builder.Default
    private AddressDTO address = AddressDTO.builder().build();

    public static User toUser(UserRegisterRequestDTO requestDTO, boolean isAdmin) {
        return User.builder().name(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .address(AddressDTO.toAddress(requestDTO.getAddress()))
                .role(isAdmin ? Role.ROLE_ADMIN : Role.ROLE_USER)
                .build();
    }
}
