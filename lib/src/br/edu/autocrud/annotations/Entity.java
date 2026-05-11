package br.edu.autocrud.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    String table() default "";

    String label() default "";
}
