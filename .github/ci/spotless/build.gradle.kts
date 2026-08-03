/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

plugins {
    id("com.diffplug.spotless") version "8.9.0"
    id("com.github.ben-manes.versions") version "0.58.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

group = "io.valkyrja"
version = "26.0.0"

repositories {
    mavenCentral()
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

spotless {
    java {
        // The JUnit and ArchUnit builds hold the repo's other Java source trees; format them too.
        // Each is scoped to `src/test/java` so any non-source .java under `src/test/resources` is
        // never rewritten.
        target(
            "src/**/*.java",
            ".github/ci/junit/src/test/java/**/*.java",
            ".github/ci/archunit/src/test/java/**/*.java",
        )
        googleJavaFormat("1.27.0").aosp()
        licenseHeader(
            """/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

"""
        )
    }
}
