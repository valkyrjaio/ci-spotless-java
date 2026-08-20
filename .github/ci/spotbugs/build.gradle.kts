/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    java
    id("com.github.spotbugs") version "6.5.10"
    id("com.github.ben-manes.versions") version "0.61.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

group = "io.valkyrja"
version = "26.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDirs("../../../src/main/java")
        }
    }
    // The JUnit build's tests are the repo's other Java source tree; analyze them too.
    test {
        java {
            srcDirs("../junit/src/test/java")
        }
    }
}

dependencies {
    // The SpotBugs tool version is declared here rather than via `spotbugs { toolVersion }` so it
    // is a real dependency notation. useLatestVersions only rewrites dependency notations, so a
    // toolVersion string is reported as outdated every run but never updated — it drifts forever.
    spotbugs("com.github.spotbugs:spotbugs:4.10.4")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    compileOnly("org.jspecify:jspecify:1.0.1")

    // Mirrors the JUnit build's test classpath — needed only so the tests compile here.
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.jspecify:jspecify:1.0.1")
}

spotbugs {
    excludeFilter.set(layout.projectDirectory.file("spotbugs-exclude.xml"))
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf { isNonStable(candidate.version) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("html")
}

// Analyzing the tests is the point — running them is the JUnit build's job, so `check` still runs
// spotbugsTest without executing the suite twice.
tasks.test {
    enabled = false
}

// The test tree gets its own filter so `src` stays strict — the JUnit idioms excluded for the tests
// can never loosen this package's own analysis.
tasks.spotbugsTest {
    excludeFilter.set(layout.projectDirectory.file("spotbugs-exclude-test.xml"))
}
