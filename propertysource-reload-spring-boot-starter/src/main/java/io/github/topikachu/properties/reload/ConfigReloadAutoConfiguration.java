package io.github.topikachu.properties.reload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "propertysource.reload.enabled", matchIfMissing = true)
@ConditionalOnClass({ ContextRefresher.class, RestartEndpoint.class })
@EnableConfigurationProperties(ReloadableProperties.class)
public class ConfigReloadAutoConfiguration {

	@Bean
	public ReloadableWatch fileAlterationObserver(ReloadableProperties reloadableProperties,
			ReloadExecutor reloadExecutor) {
		ReloadableWatch reloadableWatch = ReloadableWatch.builder().reloadProperties(reloadableProperties)
				.reloadExecutor(reloadExecutor).build();
		reloadableWatch.start();
		return reloadableWatch;
	}

	@Bean
	public ReloadExecutor reloadExecutor(ReloadableProperties reloadableProperties, ContextRefresher contextRefresher,
			@Autowired(required = false) RestartEndpoint restartEndpoint,
			ApplicationEventPublisher applicationEventPublisher, ConfigurableApplicationContext applicationContext) {
		return ReloadExecutor.builder().reloadableProperties(reloadableProperties).contextRefresher(contextRefresher)
				.restartEndpoint(restartEndpoint).applicationEventPublisher(applicationEventPublisher)
				.applicationContext(applicationContext).build();

	}

}
