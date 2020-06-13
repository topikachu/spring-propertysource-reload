package io.github.topikachu.properties.reload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "propertysource.reload.enabled", matchIfMissing = true)
@ConditionalOnClass(RefreshScope.class)
@EnableConfigurationProperties(ReloadableProperties.class)
public class ConfigReloadAutoConfiguration {
	@Bean
	public ReloadableWatch fileAlterationObserver(ReloadableProperties reloadableProperties,
	                                              ReloadExecutor reloadExecutor
	) {
		ReloadableWatch reloadableWatch = ReloadableWatch.builder()
				.reloadProperties(reloadableProperties)
				.reloadExecutor(reloadExecutor)
				.threadFactory(new ThreadPoolTaskExecutor())
				.build();
		reloadableWatch.start();
		return reloadableWatch;
	}


	@Bean
	public ReloadExecutor reloadExecutor(ReloadableProperties reloadableProperties,
	                                     ContextRefresher contextRefresher,
	                                     ApplicationEventPublisher applicationEventPublisher,
	                                     ConfigurableApplicationContext applicationContext) {
		return ReloadExecutor.builder()
				.reloadableProperties(reloadableProperties)
				.contextRefresher(contextRefresher)
				.applicationEventPublisher(applicationEventPublisher)
				.applicationContext(applicationContext)
				.build();

	}

}
