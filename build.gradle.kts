import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring)
    alias(libs.plugins.serialization)
}

group = "com.weatherworld"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

// Spring Cloud BOM
val springCloudVersion = "2024.0.1"
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

// Configurações de otimização
configurations.all {
    // Excluir apenas duplicatas de logging desnecessárias
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    exclude(group = "org.slf4j", module = "jul-to-slf4j")
}

dependencies {
    // Spring Core
    implementation(libs.spring.boot.webflux)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.cache)

    // Observabilidade e Resilience4j
    implementation(libs.spring.reactivestreams)
    implementation(libs.spring.resilience) {
        exclude(group = "io.github.resilience4j", module = "resilience4j-rxjava3")
    }

    implementation(libs.spring.resilience.kotlin)
    implementation(libs.spring.resilience.circuitbreaker)
    implementation(libs.spring.resilience.micrometer)
    implementation(libs.micrometer.registry)

    // Cache
    implementation(libs.caffeine.caching)

    // Kotlin e utilitários
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.dotenv)

    // Rate limiting
    implementation(libs.bucket4k)

    // Dev
    developmentOnly(libs.spring.boot.devtools)

    // Testes
    testImplementation(libs.mock.webserver)
    testImplementation(libs.spring.boot.test) {
        exclude(module = "mockito-core") // usando NinjaMockk
    }
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ninjamockk)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.test {
    useJUnitPlatform()
}

// Configurações de otimização de JAR
tasks.withType<Jar> {
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    exclude("META-INF/maven/**")
    exclude("**/*.md", "**/*.txt")
    exclude("**/module-info.class")
    exclude("META-INF/LICENSE*", "META-INF/NOTICE*")
    exclude("META-INF/DEPENDENCIES*")
    
    // Compression
    entryCompression = ZipEntryCompression.DEFLATED
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.bootJar {
    archiveFileName.set("weather-world-api.jar")
    
    // Otimizações básicas e seguras
    exclude("META-INF/maven/**")
    exclude("**/*.md", "**/*.txt", "**/*.html")
    exclude("**/module-info.class")
    exclude("META-INF/LICENSE*", "META-INF/NOTICE*")
    exclude("META-INF/DEPENDENCIES*")
    
    // Configurar layers para melhor cache
    layered {
        enabled = true
    }
}
