package org.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Address;
import org.example.demo.domain.Role;
import org.example.demo.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * given when then 템플릿의 통합 테스트
 * given -> g
 * when -> w
 * then -> t
 */
@SpringBootTestWithProfile("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userServiceImpl;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("회원가입 조회 정상 테스트")
    void registerFindUser() {
        // g
        Address address = new Address("경기도", "남양주시", "와부읍 덕소리");
        String originalPwd = "passwordOrigin";
        User user = User.builder().name("test")
                .email("test@test.com")
                .password(originalPwd)
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        // w

        userServiceImpl.save(user);

        // t
        User saved_user = userServiceImpl.findByEmail(user.getEmail());

        assertEquals(user.getEmail(), saved_user.getEmail());
        assertNotEquals(originalPwd, saved_user.getPassword());
        assertEquals(user.getRole().getRole(), saved_user.getRole().getRole());
        assertEquals(user.getAddress(), saved_user.getAddress());
    }

    @Test
    @DisplayName("중복이메일로 인한 예외 처리")
    void duplicatedEmailException() {
        // g
        Address address = new Address("경기도", "남양주시", "와부읍 덕소리");
        User user = User.builder().name("test")
                .email("test@test.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        User duplicatedEmailUser = User.builder().name("test2")
                .email("test@test.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        userServiceImpl.save(user);
        // w & t
        // 이후 예외 처리를 래핑할 것이기 떄문에 런타임 예외로 처리
        assertThrows(RuntimeException.class, () -> userServiceImpl.save(duplicatedEmailUser));
    }

    @Test
    @DisplayName("updateUser 메서드 정상 동작 테스트")
    void updateUserTest() {
        // g
        Address originalAddress = new Address("서울", "강남구", "테헤란로");
        User originalUser = User.builder()
                .name("originalName")
                .email("original@test.com")
                .password("originalPassword")
                .role(Role.ROLE_USER)
                .address(originalAddress)
                .build();

        userServiceImpl.save(originalUser);

        User updatedUser = User.builder()
                .name("updatedName")
                .password("updatedPassword")
                .address(new Address("부산", "해운대구", "해운대로"))
                .build(); // Email과 Role은 업데이트 하지 않은 경우

        // w
        User savedUser = userServiceImpl.findByEmail(originalUser.getEmail());
        userServiceImpl.update(savedUser.getId(), updatedUser);

        // 영속성 캐시 초기화
        em.flush();
        em.clear();

        savedUser = userServiceImpl.findByEmail(originalUser.getEmail());

        // t
        assertEquals("updatedName", savedUser.getName());
        assertEquals(originalUser.getEmail(), savedUser.getEmail());
        assertNotEquals("originalPassword", savedUser.getPassword());
        assertEquals("부산", savedUser.getAddress().getZipCode());
        assertEquals("해운대구", savedUser.getAddress().getStreetAddress());
        assertEquals("해운대로", savedUser.getAddress().getDetailAddress());
        assertEquals(originalUser.getRole(), savedUser.getRole());
    }
}