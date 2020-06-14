# PropertySource Reload Spring Framework Boot Starters

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Usage](#usage)
- [Repository](#repository)
- [Requirements and Downloads](#requirements-and-downloads)
- [How to enable external property source reloading](#how-to-enable-external-property-source-reloading)
- [Reload Strategy](#reload-strategy)
- [PropertySourceReloadEvent](#propertysourcereloadevent)
- [Environment Changes](#environment-changes)
- [Delete Configuration at Runtime](#delete-configuration-at-runtime)
- [Contributions](#contributions)
- [Licenses](#licenses)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


# Usage
This starter can auto detect the external property source (current only support java properties file) change on the file system,
and triggers the refresh process of the application. 


# Repository
Repository contains:

* `propertysource-reload-spring-boot-starter` the starter project which enables propertysource reload
* `propertysource-reload-example` an example demonstrating the usage of the starter

# Requirements and Downloads

Requirements:
  * Java 1.8
  * Spring Framework Boot > 2.x.x (web)

Gradle:

```gradle
repositories {
    jcenter()
    mavenCentral()
}

dependencies {
  implementation 'io.github.topikachu:propertysource-reload-spring-boot-starter:0.0.3'
}
```

Maven:
```xml
<dependency>
    <groupId>io.github.topikachu</groupId>
    <artifactId>propertysource-reload-spring-boot-starter</artifactId>
    <version>0.0.3</version>
</dependency>
```



# How to enable external property source reloading
This starter requires the [Spring Cloud Context](https://cloud.spring.io/spring-cloud-commons/multi/multi__spring_cloud_context_application_context_services.html) . Add at least one Spring Cloud module, for example:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter</artifactId>
</dependency>
```
Specify the configuration file in the `bootstrap.properties` or `bootstrap.yaml`:
```
propertysource.reload.properties-files=config/foo.properties,config/bar.properties

```

Other available Spring Boot bootstrap configuration parameter:
```
propertysource.reload.poll-interval=5s
propertysource.reload.strategy=refresh_environment
propertysource.reload.max-wait-for-shutdown=2s
propertysource.reload.ignore-resource-not-found=true
propertysource.reload.ignore-resource-load-error=true
```
Once the content of the configuration file specified in `propertysource.reload.properties-files` is change, the application is reload automatically.

# Reload Strategy
There are four strategies
1. refresh_scope  
  This is the default strategy. Execute `ContextRefresher.refresh()`.
1. refresh_environment  
  Execute `ContextRefresher.refreshEnvironment()`.  
1. exit_application  
  Execute [`SpringApplication.exit()`](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/SpringApplication.html#exit-org.springframework.context.ApplicationContext-org.springframework.boot.ExitCodeGenerator...-)
1. exit_application_force  
  Execute [`SpringApplication.exit()`](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/SpringApplication.html#exit-org.springframework.context.ApplicationContext-org.springframework.boot.ExitCodeGenerator...-)
   and [`System.exit()`](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#exit-int-)

# PropertySourceReloadEvent
There's a `PropertySourceReloadEvent` fired after each reload. To receive this event
```java
	@EventListener(PropertySourceReloadEvent.class)
	public void onRefresh(PropertySourceReloadEvent event) {
		System.out.println(String.join(",", event.getKeys()));
		System.out.println(event.getFile().getName());
		System.out.println(event.getFileEvent());
	}
```

# Environment Changes
Please read [spring documentation](https://cloud.spring.io/spring-cloud-static/spring-cloud.html#_environment_changes) for detail.  
In a short description, the `@ConfigurationProperties` beans are rebind, but `@Value` are not by default. Any bean using `@Value` to inject the configuration must use `@RefreshScope` to receive the new change.

# Delete Configuration at Runtime
It's very difficult to handle the configuration deletion. Try to always keep the keys at runtime. Another solution is to use "@RefreshScope" at any bean depending on the configuration may change during runtime.



# Contributions

Contributions are welcome.  Please respect the [Code of Conduct](http://contributor-covenant.org/version/1/3/0/).


# Licenses

`propertysource-reload-spring-boot-starter` are licensed under the Apache 2.0 License. See [LICENSE](LICENSE.md) for details.

