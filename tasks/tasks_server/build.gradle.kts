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

application {
    mainClass.set("com.jw2304.pointing.casual.tasks.TasksApplication")
	buildDir = file("$rootDir/../dist/server")
}

sourceSets {
	create("ui") {
		resources.srcDir("$rootDir/../dist/ui")
	}
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
	implementation(sourceSets.named("ui").get().output)
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

// jar {
// 	destinationDirectory.set(file("$rootDir/../dist/server"))
// 	manifest {
// 		attributes 'Main-Class': com.jw2304.pointing.casual.tasks.TasksApplication
//   	}
// }

tasks.withType<Test> {
	useJUnitPlatform()
}
