plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	application
}

group = "com.jw2304.pointing.casual"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

// tasks.register<Copy>("copyUI") {
// 	// dependsOn("build")
// 	from(file("$rootDir/../dist/"))
// 	include("ui/**")
// 	into(layout.buildDirectory.dir("resources/main/static"))
// }
tasks.named("build") {
	doFirst {
		copy {
			from(file("$rootDir/../dist/ui/"))
			into(layout.buildDirectory.dir("resources/main/static/"))
		}
	}
}

application {
    mainClass.set("com.jw2304.pointing.casual.tasks.TasksApplication")
	buildDir = file("$rootDir/../dist/server")
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.0.0-RC2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}