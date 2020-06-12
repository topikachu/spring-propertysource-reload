package io.github.topikachu.properties.reload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "propertysource.reload")
@Setter
@Getter
public class ReloadableProperties {
	private long pollInterval = 5000;
	private List<String> propertiesFiles = Collections.emptyList();
	private boolean ignoreResourceNotFound = true;
}