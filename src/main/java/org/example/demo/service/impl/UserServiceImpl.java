package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Address;
import org.example.demo.domain.User;
import org.example.demo.events.SignUpEvent;
import org.example.demo.repository.UserRepository;
import org.example.demo.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long save(User user) {
        // 중복 사용자 검증
        validateDuplicateUser(user);
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Long id = userRepository.save(user).getId();
        // 이벤트 실행
        eventPublisher.publishEvent(
                SignUpEvent.builder()
                        .email(user.getEmail())
                        .username(user.getName()).build()
        );

        return id;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new RuntimeException("User with id " + id + " not found"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
        return id;
    }

    @Override
    @Transactional
    public Long update(Long id, User updatedUser) {
        User existingUser = findById(id);
        updateUserDetails(existingUser, updatedUser);
        return existingUser.getId();
    }

    // 이메일로 사용자 조회 (추가적인 유틸리티 메서드)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User with email " + email + " not found"));
    }

    // 내부 메서드: 사용자 정보 업데이트 (변경 감지 활용)
    private void updateUserDetails(User existingUser, User updatedUser) {
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        if (updatedUser.getAddress() != null) {
            Address address = new Address(
                    updatedUser.getAddress().getZipCode(),
                    updatedUser.getAddress().getStreetAddress(),
                    updatedUser.getAddress().getDetailAddress()
            );
            existingUser.setAddress(address);
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
    }

    // 내부 메서드: 중복 사용자 검증
    private void validateDuplicateUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
    }
}