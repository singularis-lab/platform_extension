package com.singularis.android.platformextension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectResource
{
	public enum EResourceType
	{
		Drawable,
		Color,
		String
	}

	String name() default "";
	int id() default -1;
	EResourceType type() default EResourceType.String;
}

