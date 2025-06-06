package com.fc.authservice.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters and contain a digit, a lower-case, an upper-case letter, and a special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
