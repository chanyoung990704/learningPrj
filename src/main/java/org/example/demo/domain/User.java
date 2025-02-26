package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.BaseEntity;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "users")
@EqualsAndHashCode(callSuper = false)
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
    @Builder.Default
    private Role role = Role.ROLE_USER; // 기본 값은 유저로 설정

}
