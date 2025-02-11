package org.example.demo.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Address {

    private String zipCode;

    private String streetAddress;

    private String detailAddress;

}
