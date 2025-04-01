package org.example.demo.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.demo.dto.request.UserRegisterRequestDTO;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, UserRegisterRequestDTO> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // 초기화 필요 없음
    }

    @Override
    public boolean isValid(UserRegisterRequestDTO value, ConstraintValidatorContext context) {
        // 비밀번호가 null이 아니고 일치하는지 확인
        if (value.getPassword() == null || value.getPasswordConfirm() == null) {
            return false;
        }
        return value.getPassword().equals(value.getPasswordConfirm());
    }
} 