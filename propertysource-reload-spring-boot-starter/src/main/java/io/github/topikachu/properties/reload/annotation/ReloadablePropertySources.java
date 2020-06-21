package io.github.topikachu.properties.reload.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReloadablePropertySources {

	ReloadablePropertySource[] value();

}
