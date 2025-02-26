package org.example.demo.validator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidatorContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.User;
import org.example.demo.dto.request.AddressDTO;
import org.example.demo.dto.request.UserRegisterRequestDTO;
import org.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
@Transactional
class UniqueEmailValidatorTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniqueEmailValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Test
    @DisplayName("UniqueEmailValidator 작동 테스트")
    public void uniqueEmailValidatorTest() {
        // g
        AddressDTO testAddressDto = AddressDTO.builder()
                .detailAddress("test")
                .streetAddress("test")
                .zipCode("test").build();

        UserRegisterRequestDTO testUserForm = UserRegisterRequestDTO.builder()
                .email("test@example.com")
                .username("test")
                .password("test")
                .address(testAddressDto)
                .build();

        // 회원 가입 후 영속성 초기화
        User testUser1 = UserRegisterRequestDTO.toUser(testUserForm);
        userRepository.save(testUser1);
        entityManager.flush();
        entityManager.clear();

        testUserForm.setUsername("test2");
        User testUser2 = UserRegisterRequestDTO.toUser(testUserForm);

        // w & t
        assertFalse(validator.isValid(testUser2.getEmail(), context));
    }
}