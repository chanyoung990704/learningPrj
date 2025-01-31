package org.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ActiveProfiles
public @interface SpringBootTestWithProfile {

    @AliasFor(annotation = ActiveProfiles.class, value = "profiles")
    String[] value() default "";
}
