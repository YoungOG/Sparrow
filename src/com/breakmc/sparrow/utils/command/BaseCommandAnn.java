package com.breakmc.sparrow.utils.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BaseCommandAnn {

    String name();

    String[] aliases();

    CommandUsageBy commandUsage();

    String permission() default "";

    String usage();

    int minArgs() default 0;

    int maxArgs() default -1;

}
