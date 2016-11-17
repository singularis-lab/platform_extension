package com.singularis.android.platformextension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectChildView
{
	public enum EViewType
	{
		Fragment,
		View
	}

	String path() default "";	
	EViewType Type() default EViewType.View;
}


