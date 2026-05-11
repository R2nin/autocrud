package br.edu.autocrud.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String label()        default "";

    String sqlType()      default "";

    int length()          default 255;

    boolean nullable()    default true;

    int order()           default 0;

    String placeholder()  default "";

    String mask()         default "";

    boolean required()    default false;

    int minLength()       default 0;

    int maxLength()       default 0;

    String min()          default "";

    String max()          default "";

    String pattern()      default "";

    String errorMsg()     default "";
}
