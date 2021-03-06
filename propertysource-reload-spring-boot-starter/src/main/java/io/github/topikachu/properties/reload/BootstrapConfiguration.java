package io.github.topikachu.properties.reload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnProperty(value = "propertysource.reload.enabled", matchIfMissing = true)
@ConditionalOnClass({ ContextRefresher.class, RestartEndpoint.class })
@Configuration
public class BootstrapConfiguration {

	@EnableConfigurationProperties(ReloadableProperties.class)
	@Configuration(proxyBeanMethods = false)
	@Import({ ReloadablePropertySourceLocator.class, ReloadableAnnotationBean.class })
	static class ReloadableConfiguration {

	}

}
