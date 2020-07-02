package io.github.topikachu.properties.reload;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@CommonsLog
public class ReloadableWatch {

	@Autowired
	private ReloadableProperties reloadProperties;

	@Autowired
	private ReloadExecutor reloadExecutor;

	private FileAlterationMonitor monitor;

	@Autowired
	private ReloadableAnnotationBean reloadableAnnotationUtil;

	@SneakyThrows
	@PostConstruct
	public void start() {
		monitor = new FileAlterationMonitor(reloadProperties.getPollInterval().toMillis());
		monitor.setThreadFactory(new ThreadPoolTaskExecutor());
		FileAlterationListener propertySourceListener = new FileAlterationListenerAdaptor() {
			@Override
			public void onFileChange(File file) {
				reloadExecutor.executeReload(file, PropertySourceReloadEvent.FileEvent.CHANGE);
			}

			@Override
			public void onFileCreate(File file) {
				reloadExecutor.executeReload(file, PropertySourceReloadEvent.FileEvent.CREATE);
			}

			@Override
			public void onFileDelete(File file) {
				reloadExecutor.executeReload(file, PropertySourceReloadEvent.FileEvent.DELETE);
			}
		};
		ReloadableUtil
				.getSourcesAsStream(reloadProperties.getPropertiesFiles(), reloadableAnnotationUtil.getAllSource())
				.map(location -> {

					if (log.isDebugEnabled()) {
						log.debug("File [ " + location + " ] to monitor");
					}
					return Paths.get(location).normalize().toAbsolutePath();

				}).collect(groupingBy(Path::getParent)).forEach((parent, files) -> {
					File parentFile = parent.toFile();
					List<String> fileNames = files.stream().map(path -> path.getFileName().toString())
							.collect(Collectors.toList());
					if (log.isDebugEnabled()) {
						log.debug("Watch at [ " + parent + " ]");
						log.debug(String.join(" ", fileNames));
					}
					FileAlterationObserver observer = new FileAlterationObserver(parentFile,
							new NameFileFilter(fileNames));
					observer.addListener(propertySourceListener);
					monitor.addObserver(observer);
				});
		monitor.start();
	}

	@PreDestroy
	@SneakyThrows
	public void stop() {
		if (monitor != null) {
			monitor.stop();
		}
	}

}
