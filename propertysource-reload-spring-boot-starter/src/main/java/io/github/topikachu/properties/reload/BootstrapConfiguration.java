package io.github.topikachu.properties.reload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "propertysource.reload.enabled", matchIfMissing = true)
@ConditionalOnClass({ ContextRefresher.class, RestartEndpoint.class })
public class BootstrapConfiguration {

	@EnableConfigurationProperties(ReloadableProperties.class)
	@Configuration(proxyBeanMethods = false)
	protected static class ReloadableConfiguration {

		@Bean
		public ReloadablePropertySourceLocator reloadablePropertySourceLocator(
				ReloadableProperties reloadableProperties, ResourceLoader resourceLoader) {
			return ReloadablePropertySourceLocator.builder().reloadProperties(reloadableProperties)
					.resourceLoader(resourceLoader).build();
		}

	}

}
