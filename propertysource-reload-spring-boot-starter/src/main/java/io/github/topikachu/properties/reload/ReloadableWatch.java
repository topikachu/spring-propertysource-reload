package io.github.topikachu.properties.reload;

import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Builder
public class ReloadableWatch {

	private ReloadableProperties reloadProperties;

	private ReloadExecutor reloadExecutor;

	private FileAlterationMonitor monitor;

	@SneakyThrows
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
		reloadProperties.getPropertiesFiles().stream().map(location -> {
			try {
				return new File(location).getCanonicalFile();
			}
			catch (IOException ex) {
				throw new ReloadableException("Can't get canonical file");
			}
		}).collect(groupingBy(File::getParentFile)).forEach((parent, files) -> {
			List<String> fileNames = files.stream().map(File::getName).collect(Collectors.toList());
			FileAlterationObserver observer = new FileAlterationObserver(parent, new NameFileFilter(fileNames));
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
