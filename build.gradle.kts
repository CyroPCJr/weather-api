plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.serialization)
    alias(libs.plugins.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependecy.management)
}

group = "com.weatherworld"
version = "1.0.0"

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
    // Spring Core
    implementation(libs.spring.boot.webflux)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.cache)

    // Observable and Resilience
    implementation(libs.spring.reactivestreams)
    implementation(libs.spring.resilience)
    implementation(libs.spring.resilience.kotlin)
    implementation(libs.spring.resilience.circuitbreaker)
    implementation(libs.spring.resilience.micrometer)
    implementation(libs.micrometer.registry)

    // Cache
    implementation(libs.caffeine.caching)

    // Kotlin and utils
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.dotenv)

    // Rate Limiting
    implementation(libs.bucket4k)

    // Only dev
    developmentOnly(libs.spring.boot.devtools)

    // Testes
    testImplementation(libs.mock.webserver)
    testImplementation(libs.spring.boot.test) {
        exclude(module = "mockito-core") // Use ninjamockk
    }
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
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
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers",
        )
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

tasks.bootJar {
    archiveFileName.set("weather-world-api.jar")
    launchScript()
}
