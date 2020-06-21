package io.github.topikachu.properties.reload.example2;

import io.github.topikachu.properties.reload.annotation.ReloadablePropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(GreetingProperties.class)
@SpringBootApplication
@ReloadablePropertySource("test-config/app2.properties")
@ReloadablePropertySource("test-config/app.properties")
public class App2 {

	public static void main(String[] args) {
		SpringApplication.run(App2.class, args);
	}

}
