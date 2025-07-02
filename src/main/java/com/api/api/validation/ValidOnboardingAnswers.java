package com.api.api.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/*
    * Esta anotación se utiliza para marcar un campo (o parámetro) que contiene las respuestas del onboarding,
    * con el fin de validar que solo se reciban claves y valores permitidos.
    *
    * Al aplicarla, se invoca el ConstraintValidator asociado (OnboardingAnswersValidator),
    * que comprueba que el Map tenga únicamente las claves esperadas (por ejemplo, "question1", "question2", etc.)
    * y que cada valor se encuentre dentro de los conjuntos permitidos para cada clave.
    *
    * Si la validación falla, se genera un mensaje de error personalizado.
*/

@Documented
@Constraint(validatedBy = OnboardingAnswersValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidOnboardingAnswers {
    String message() default "Respuestas de onboarding inválidas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

