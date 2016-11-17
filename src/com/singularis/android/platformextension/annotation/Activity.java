package com.singularis.android.platformextension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Activity
{
	int Layout() default -1;
	String LayoutName() default "";
}

