package org.example.demo.service;

import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Address;
import org.example.demo.domain.User;
import org.example.demo.events.SignUpEvent;
import org.example.demo.repository.UserRepository;
import org.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTestWithProfile("test")
public class UserServiceMockTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("회원가입시 이벤트 리스너 정상 호출")
    public void testSignUpEvent() {
        // g
        User user = User.builder().name("test")
                .password("test")
                .email("test@example.com")
                .address(new Address("test", "test", "test")).build();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(Boolean.FALSE);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("Encoded!!!password!!!");
        when(userRepository.save(user)).thenReturn(user);

        ArgumentCaptor<SignUpEvent> eventArgumentCaptor = ArgumentCaptor.forClass(SignUpEvent.class);


        // w
        Long id = userService.save(user);

        // t
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture()); // SignUpEvent 호출 확인
        SignUpEvent signUpEvent = eventArgumentCaptor.getValue();

        assertEquals(signUpEvent.getEmail(), user.getEmail());
        assertEquals(signUpEvent.getUsername(), user.getName());

    }


}
