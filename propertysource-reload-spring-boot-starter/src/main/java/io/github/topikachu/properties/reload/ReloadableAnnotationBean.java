package io.github.topikachu.properties.reload;

import io.github.topikachu.properties.reload.annotation.ReloadablePropertySource;
import io.github.topikachu.properties.reload.annotation.ReloadablePropertySources;
import lombok.extern.apachecommons.CommonsLog;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@CommonsLog
@Component
public class ReloadableAnnotationBean {

	private List<String> reloadableSources = null;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private Class<?> mainClass;

	public List<String> getAllSource() {
		Lock readLock = lock.readLock();
		readLock.lock();
		if (reloadableSources == null) {
			readLock.unlock();
			Lock writerLock = lock.writeLock();
			writerLock.lock();
			try {
				if (reloadableSources == null) {
					if (mainClass == null) {
						mainClass = ApplicationHolder.getSpringApplication().getMainApplicationClass();
					}
					if (mainClass == null) {
						if (log.isWarnEnabled()) {
							log.warn("Can't detect the Main class.");
						}
						return Collections.emptyList();
					}
					else {
						Reflections reflections = new Reflections(mainClass.getPackage().getName(),
								new TypeAnnotationsScanner());
						reloadableSources = Arrays
								.asList(ReloadablePropertySource.class, ReloadablePropertySources.class).stream()
								.flatMap(annotation -> reflections.getTypesAnnotatedWith(annotation, true).stream())
								.flatMap(type -> Arrays
										.stream(type.getAnnotationsByType(ReloadablePropertySource.class)))
								.flatMap(reloadableSources -> Arrays.stream(reloadableSources.value()))
								.filter(Objects::nonNull).collect(Collectors.toList());
					}
				}
				return reloadableSources;
			}
			finally {
				writerLock.unlock();
			}
		}
		else {
			readLock.unlock();
			return reloadableSources;
		}

	}

}
