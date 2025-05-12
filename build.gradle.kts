plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.serialization)
    alias(libs.plugins.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependecy.management)
}

group = "com.weatherworld"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.data.jpa)
    // implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.web)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.cloud.openfeign)
    implementation(libs.kotlin.dotenv)
    implementation(libs.bucket4k)
    developmentOnly(libs.spring.boot.devtools)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlin.test.coroutines)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.ninjamockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
