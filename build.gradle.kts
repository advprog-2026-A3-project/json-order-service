plugins {
    java
    id("org.springframework.boot") version "3.4.2" // Gunakan versi 3.x stabil
    id("io.spring.dependency-management") version "1.1.7"
    jacoco // Wajib untuk mengukur code coverage 100%
    id("org.sonarqube") version "5.0.0.4638" // Wajib untuk report kualitas kode di CI
}

group = "id.ac.ui.cs.advprog"
version = "0.0.1-SNAPSHOT"
description = "order"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
    // --- Web & UI ---
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web") // Diperbaiki

    // --- Database (Syarat integrasi Frontend-Backend-DB) ---
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // Database in-memory untuk test & run lokal TA
    runtimeOnly("org.postgresql:postgresql") // Persiapan untuk Production / Staging

    // --- Validation ---
    implementation("org.springframework.boot:spring-boot-starter-validation") // Jakarta Validation support

    // --- Utils ---
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test") // Diperbaiki
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// --- Konfigurasi Testing & JaCoCo ---
tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Otomatis buat report setelah test jalan
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true) // SonarCloud butuh format XML
        csv.required.set(false)
        html.required.set(true) // Untuk dicek manual via browser oleh anggota tim
    }
}

// Opsional: Bikin build gagal di lokal kalau coverage di bawah 100%
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 1.0.toBigDecimal() // Target 100%
            }
        }
    }
}

// --- Konfigurasi SonarCloud ---
sonar {
    properties {
        property("sonar.projectKey", "NAMA_PROJECT_KEY_DI_SONARCLOUD")
        property("sonar.organization", "NAMA_ORGANISASI_DI_SONARCLOUD")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}