package io.github.topikachu.properties.reload.example.config;

import io.github.topikachu.properties.reload.annotation.ReloadablePropertySource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "http")
@ReloadablePropertySource({ "config/bar.properties", "config/foo.yaml" })
public class ApplicationProperties {

	private String name;

	private String bar;

}
