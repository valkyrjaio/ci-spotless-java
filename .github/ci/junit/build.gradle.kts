/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

plugins {
    java
    jacoco
    id("com.github.ben-manes.versions") version "0.59.0"
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
}

dependencies {
    // The JaCoCo tool version is declared here rather than left to the plugin default (or set via
    // `jacoco { toolVersion }`) so it is a real dependency notation. useLatestVersions only
    // rewrites dependency notations, so an implicit or toolVersion-pinned tool is reported as
    // outdated every run but never updated — it drifts forever.
    jacocoAgent("org.jacoco:org.jacoco.agent:0.8.15")
    jacocoAnt("org.jacoco:org.jacoco.ant:0.8.15")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.1")
    compileOnly("org.jspecify:jspecify:1.0.1")

    // The Gradle plugin this package publishes compiles against both of these. The root build
    // gets the Gradle API from `java-gradle-plugin`, which this build does not apply, so it states
    // the API itself. The tests drive the plugin through ProjectBuilder, so both are on the test
    // classpath as well as the compile classpath.
    compileOnly(gradleApi())
    compileOnly("com.diffplug.spotless:spotless-plugin-gradle:8.9.0")
    testImplementation(gradleApi())
    testImplementation("com.diffplug.spotless:spotless-plugin-gradle:8.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

    // ProjectBuilder, which the plugin tests drive the plugin through, defines synthetic classes
    // into its own class loader and needs a private lookup into java.lang to do it. The
    // java-gradle-plugin plugin adds this to a plugin project's test task, and this build does not
    // apply that plugin, so it states the argument itself. Without it ProjectBuilder raises
    // "module java.base does not open java.lang to unnamed module".
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// The floor. Coverage was reported here and enforced nowhere: `junit` ran `test` finalized by
// `jacocoTestReport`, so the report was generated and then nothing asserted anything about it.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
        }
        // Per class as well as per bundle. A bundle-wide rule is not enough: a large, well-covered
        // codebase absorbs one entirely untested new class almost without moving, so the aggregate
        // stays high while the new file is at zero. A class-level rule fails on that file itself.
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}
