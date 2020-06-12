package io.github.topikachu.properties.reload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "propertysource.reload.enabled", matchIfMissing = true)
@ConditionalOnClass(RefreshScope.class)
@EnableConfigurationProperties(ReloadableProperties.class)
public class ConfigReloadAutoConfiguration {
	@Bean
	public ReloadableWatch fileAlterationObserver(ReloadableProperties reloadableProperties, ContextRefresher contextRefresher) throws Exception {
		ReloadableWatch reloadableWatch = ReloadableWatch.builder()
				.reloadProperties(reloadableProperties)
				.contextRefresher(contextRefresher)
				.build();
		reloadableWatch.start();
		return reloadableWatch;
	}

}
