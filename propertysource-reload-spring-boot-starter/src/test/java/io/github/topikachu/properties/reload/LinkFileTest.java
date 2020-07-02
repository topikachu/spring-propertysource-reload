package io.github.topikachu.properties.reload;

import io.github.topikachu.properties.reload.example.App;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = App.class)
@ActiveProfiles(profiles = "files")
@Import({ LinkFileTest.LockConfiguration.class, LinkFileTest.PropertySourceReloadEventListener.class })
public class LinkFileTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private Lock lock;

	@Autowired
	private Condition condition;

	@SpyBean
	private PropertySourceReloadEventListener propertySourceReloadEventListener;

	private static File appTargetProperties = new File("test-config/app-target.properties");

	private static File appNewTargetProperties = new File("test-config/app-new-target.properties");

	private static File app2TargetProperties = new File("test-config/app2-target.properties");

	private static File appProperties = new File("test-config/app.properties");

	private static File app2Properties = new File("test-config/app2.properties");

	@BeforeClass
	static public void initConfigFile() throws IOException {

		org.junit.Assume.assumeTrue(SystemUtils.IS_OS_UNIX);
		FileUtils.forceMkdir(new File("test-config"));

		FileUtils.cleanDirectory(new File("test-config"));

		FileUtils.touch(appTargetProperties);
		writePropertiesFileWithValue(appTargetProperties, "bean.name", "World");
		FileUtils.touch(app2TargetProperties);

		if (Files.exists(appProperties.toPath(), NOFOLLOW_LINKS)) {
			Files.delete(appProperties.toPath());
		}
		if (Files.exists(app2Properties.toPath(), NOFOLLOW_LINKS)) {
			Files.delete(app2Properties.toPath());
		}
		Files.createSymbolicLink(appProperties.toPath(), Paths.get(appTargetProperties.getName()));
		Files.createSymbolicLink(app2Properties.toPath(), Paths.get(app2TargetProperties.getName()));
	}

	private static void writePropertiesFileWithValue(File file, String s, String word) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(s, word);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			properties.store(fos, "gen by test");
			fos.flush();
		}
	}

	@Test()
	@SneakyThrows
	public void testApp() {

		assertGreetingApiWithContent("Hello World");

		writePropertiesFileWithValue(appNewTargetProperties, "bean.name", "World2");
		if (Files.exists(appProperties.toPath(), NOFOLLOW_LINKS)) {
			Files.delete(appProperties.toPath());
		}
		if (Files.exists(appTargetProperties.toPath(), NOFOLLOW_LINKS)) {
			Files.delete(appTargetProperties.toPath());
		}
		Files.createSymbolicLink(appProperties.toPath(), Paths.get(appNewTargetProperties.getName()));

		// writePropertiesFileWithValue(app2Properties, "bean.name2", "World2");

		lock.lock();
		condition.await();
		lock.unlock();

		assertPropertySourceReloadEvent();

		assertGreetingApiWithContent("Hello World2");

	}

	private void assertPropertySourceReloadEvent() {
		ArgumentCaptor<PropertySourceReloadEvent> captor = ArgumentCaptor.forClass(PropertySourceReloadEvent.class);
		verify(propertySourceReloadEventListener, times(1)).onRefresh(captor.capture());
		PropertySourceReloadEvent captorPropertySourceReloadEvent = captor.getValue();
		assertThat(captorPropertySourceReloadEvent.getKeys()).contains("bean.name");
		assertThat(captorPropertySourceReloadEvent.getFileEvent())
				.isEqualTo(PropertySourceReloadEvent.FileEvent.CHANGE);
		assertThat(captorPropertySourceReloadEvent.getFile()).hasName("app.properties");
	}

	private void assertGreetingApiWithContent(String content) {
		ResponseEntity<String> response = this.testRestTemplate.getForEntity("/greeting", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(content);
	}

	@Configuration
	static class LockConfiguration {

		@Bean
		public Lock lock() {
			return new ReentrantLock();
		}

		@Bean
		public Condition condition(Lock lock) {
			return lock.newCondition();
		}

	}

	@Component
	public static class PropertySourceReloadEventListener {

		@Autowired
		private Lock lock;

		@Autowired
		private Condition condition;

		@EventListener(PropertySourceReloadEvent.class)
		public void onRefresh(PropertySourceReloadEvent event) {
			lock.lock();
			condition.signalAll();
			lock.unlock();
		}

	}

}
