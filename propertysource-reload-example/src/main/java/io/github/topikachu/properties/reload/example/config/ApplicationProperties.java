package io.github.topikachu.properties.reload.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "http")
public class ApplicationProperties {
	private String name;
	private String bar;
}
