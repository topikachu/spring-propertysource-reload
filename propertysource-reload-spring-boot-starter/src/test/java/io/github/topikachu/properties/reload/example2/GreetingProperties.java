package io.github.topikachu.properties.reload.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@ConfigurationProperties(prefix = "bean")
@RefreshScope
public class GreetingProperties {

	private String name;

}
