package io.github.topikachu.properties.reload;

import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.concurrent.ThreadFactory;

@Builder
public class ReloadableWatch {

	private ReloadableProperties reloadProperties;

	private ReloadExecutor reloadExecutor;

	private FileAlterationMonitor monitor;

	private ThreadFactory threadFactory;

	@SneakyThrows
	public void start() {
		monitor = new FileAlterationMonitor(reloadProperties.getPollInterval().toMillis());
		monitor.setThreadFactory(threadFactory);
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
		reloadProperties.getPropertiesFiles().forEach(propertyFile -> {
			File file = new File(propertyFile);
			File folder = file.getParentFile();
			FileAlterationObserver observer = new FileAlterationObserver(folder, new NameFileFilter(file.getName()));
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
