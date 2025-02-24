package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.BaseEntity;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private Role role = Role.ROLE_USER; // 기본 값은 유저로 설정

    /**
     * 빌더패턴 생성자
     */

    @Builder
    public User(String email, String name, String password, Role role, Address address) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = (role != null) ? role : Role.ROLE_USER;
        this.address = address;
    }

}
