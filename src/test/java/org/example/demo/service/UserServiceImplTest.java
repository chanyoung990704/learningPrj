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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * given when then 템플릿의 통합 테스트
 * given -> g
 * when -> w
 * then -> t
 */
@SpringBootTestWithProfile("test")
@Transactional
@DisplayName("UserServiceImpl 통합 테스트")
class UserServiceImplTest {

    @Autowired
    private UserService userServiceImpl;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("회원 저장 및 조회 테스트")
    void saveAndFindUserTest() {
        // g
        Address address = new Address("서울", "강남구", "테헤란로");
        String originalPassword = "password";
        User user = User.builder()
                .name("John Doe")
                .email("johndoe@example.com")
                .password(originalPassword)
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        // w
        Long savedUserId = userServiceImpl.save(user);
        em.flush(); // DB 동기화
        em.clear(); // 영속성 컨텍스트 초기화

        User savedUser = userServiceImpl.findById(savedUserId);

        // t
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertNotEquals(originalPassword, savedUser.getPassword()); // 비밀번호 암호화 확인
        assertEquals(user.getRole(), savedUser.getRole());
        assertEquals(user.getAddress(), savedUser.getAddress());
    }

    @Test
    @DisplayName("중복 이메일로 인한 예외 처리 테스트")
    void saveDuplicateEmailTest() {
        // g
        Address address = new Address("부산", "해운대구", "해운대로");
        User user1 = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password1")
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        User user2 = User.builder()
                .name("Bob")
                .email("alice@example.com") // 중복 이메일
                .password("password2")
                .role(Role.ROLE_ADMIN)
                .address(address)
                .build();

        userServiceImpl.save(user1);

        // w & t
        assertThrows(RuntimeException.class, () -> userServiceImpl.save(user2));
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    void deleteUserTest() {
        // g
        Address address = new Address("대전", "유성구", "대덕대로");
        User user = User.builder()
                .name("Chris")
                .email("chris@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        Long savedUserId = userServiceImpl.save(user);
        em.flush();
        em.clear();

        // w
        userServiceImpl.deleteById(savedUserId);

        // t
        assertThrows(RuntimeException.class, () -> userServiceImpl.findById(savedUserId));
    }

    @Test
    @DisplayName("사용자 업데이트 테스트")
    void updateUserTest() {
        // g
        Address originalAddress = new Address("서울", "송파구", "올림픽로");
        User user = User.builder()
                .name("David")
                .email("david@example.com")
                .password("originalPassword")
                .role(Role.ROLE_USER)
                .address(originalAddress)
                .build();

        Long savedUserId = userServiceImpl.save(user);
        em.flush();
        em.clear();

        Address updatedAddress = new Address("인천", "남동구", "구월로");
        User updatedUser = User.builder()
                .name("Updated David")
                .password("updatedPassword")
                .address(updatedAddress)
                .build();

        // w
        userServiceImpl.update(savedUserId, updatedUser);
        em.flush();
        em.clear();

        User savedUser = userServiceImpl.findById(savedUserId);

        // t
        assertEquals("Updated David", savedUser.getName());
        assertNotEquals("originalPassword", savedUser.getPassword());
        assertEquals("인천", savedUser.getAddress().getZipCode());
        assertEquals("남동구", savedUser.getAddress().getStreetAddress());
        assertEquals("구월로", savedUser.getAddress().getDetailAddress());
    }

    @Test
    @DisplayName("모든 사용자 조회 테스트")
    void findAllUsersTest() {
        // g
        Address address1 = new Address("서울", "노원구", "노원로");
        User user1 = User.builder()
                .name("User1")
                .email("user1@example.com")
                .password("password1")
                .role(Role.ROLE_USER)
                .address(address1)
                .build();

        Address address2 = new Address("서울", "동대문구", "왕산로");
        User user2 = User.builder()
                .name("User2")
                .email("user2@example.com")
                .password("password2")
                .role(Role.ROLE_ADMIN)
                .address(address2)
                .build();

        userServiceImpl.save(user1);
        userServiceImpl.save(user2);
        em.flush();
        em.clear();

        // w
        List<User> allUsers = userServiceImpl.findAll();

        // t
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(user -> user.getEmail().equals("user1@example.com")));
        assertTrue(allUsers.stream().anyMatch(user -> user.getEmail().equals("user2@example.com")));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외 처리")
    void deleteNonExistingUserTest() {
        // g
        Long nonExistingUserId = 999L;

        // w & t
        assertThrows(RuntimeException.class, () -> userServiceImpl.deleteById(nonExistingUserId));
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void findUserByEmailTest() {
        // g
        Address address = new Address("광주", "서구", "무진대로");
        User user = User.builder()
                .name("Eve")
                .email("eve@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();

        userServiceImpl.save(user);
        em.flush();
        em.clear();

        // w
        User savedUser = userServiceImpl.findByEmail("eve@example.com");

        // t
        assertEquals("Eve", savedUser.getName());
        assertEquals("eve@example.com", savedUser.getEmail());
    }
}
