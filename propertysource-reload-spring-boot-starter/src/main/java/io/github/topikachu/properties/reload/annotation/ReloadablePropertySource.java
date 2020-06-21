package io.github.topikachu.properties.reload.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ReloadablePropertySources.class)
public @interface ReloadablePropertySource {

	String[] value();

}
