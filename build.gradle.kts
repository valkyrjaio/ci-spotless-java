/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.1.1"
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("com.github.ben-manes.versions") version "0.59.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf { isNonStable(candidate.version) }
}

group = "io.valkyrja"
// Sourced from VERSION.md so the release pipeline (which bumps VERSION.md) drives the
// version that gets published. The leading "v" is stripped for Maven compatibility.
version = file("VERSION.md").readText().trim().removePrefix("v")

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Declared here rather than by a consuming build script, so the rules and the tool that runs
    // them move together. A build script that declared the Spotless plugin itself could bump that
    // plugin past the API this package compiles against, and the two would drift apart.
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.9.0")
}

gradlePlugin {
    website = "https://github.com/valkyrjaio/ci-spotless-java"
    vcsUrl = "https://github.com/valkyrjaio/ci-spotless-java.git"

    plugins {
        create("ciSpotless") {
            // PACKAGE_NAMING.md in the architecture repository states this name. It is the group
            // id and the package name, and it is a fourth name that no other field repeats.
            id = "io.valkyrja.ci-spotless"
            implementationClass = "io.valkyrja.spotless.ValkyrjaSpotlessPlugin"
            displayName = "Valkyrja Spotless"
            description = "Shared Spotless configuration for Valkyrja Java repositories."
            tags = listOf("spotless", "formatting", "license-header", "valkyrja")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Two registries hold this package, and each holds a different part of it.
//
// The Gradle Plugin Portal holds the plugin marker and the jar. The Portal is Gradle's default
// plugin repository, so a `plugins { id("io.valkyrja.ci-spotless") }` block reads it without a
// `pluginManagement` block in the consuming build.
//
// Maven Central holds the jar alone. A `buildscript { classpath(...) }` consumer resolves the jar
// directly, and so does a dependency scanner. Such a consumer never reads the marker, and a
// published coordinate can never be removed, so Central takes only what a consumer there can use.
// Spotless itself publishes in this shape.
// The repository a publish task targets is wired after this block configures the task, so the
// predicate reads it at execution time instead. PublishToMavenLocal is a subclass of this type and
// states no repository, so the read is null-safe: publishing to the local cache stays whole, which
// is what a consumer wired against mavenLocal() resolves during development.
tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf("the plugin marker goes to the Gradle Plugin Portal alone") {
        repository?.name != "mavenCentral" || !publication.name.endsWith("PluginMarkerMaven")
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "ci-spotless", version.toString())

    pom {
        name.set("Valkyrja Spotless")
        description.set("Shared Spotless configuration for Valkyrja Java repositories.")
        url.set("https://github.com/valkyrjaio/ci-spotless-java")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("melechmizrachi")
                name.set("Melech Mizrachi")
                email.set("melechmizrachi@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/valkyrjaio/ci-spotless-java.git")
            developerConnection.set("scm:git:ssh://github.com/valkyrjaio/ci-spotless-java.git")
            url.set("https://github.com/valkyrjaio/ci-spotless-java")
        }
    }
}

// CI tasks — run from the project root without cd-ing into each CI directory

tasks.register<GradleBuild>("spotlessCheck") {
    group = "CI"
    description = "Check code formatting via Spotless"
    dir = file(".github/ci/spotless")
    tasks = listOf("spotlessCheck")
}

tasks.register<GradleBuild>("spotlessApply") {
    group = "CI"
    description = "Apply code formatting via Spotless"
    dir = file(".github/ci/spotless")
    tasks = listOf("spotlessApply")
}

tasks.register<GradleBuild>("archunit") {
    group = "CI"
    description = "Run ArchUnit architecture tests"
    dir = file(".github/ci/archunit")
    tasks = listOf("test")
}

tasks.register<GradleBuild>("errorprone") {
    group = "CI"
    description = "Run Error Prone static analysis"
    dir = file(".github/ci/errorprone")
    tasks = listOf("build")
}

tasks.register<GradleBuild>("spotbugs") {
    group = "CI"
    description = "Run SpotBugs static analysis"
    dir = file(".github/ci/spotbugs")
    tasks = listOf("check")
}

tasks.register<GradleBuild>("junit") {
    group = "CI"
    description = "Run JUnit unit tests"
    dir = file(".github/ci/junit")
    // jacocoTestCoverageVerification is what makes the coverage report a gate rather than a printout.
    tasks = listOf("test", "jacocoTestCoverageVerification")
}

listOf("spotless", "archunit", "errorprone", "spotbugs", "junit").forEach { ci ->
    tasks.register<GradleBuild>("${ci}OutdatedCheck") {
        group = "CI"
        description = "Check $ci dependencies for available updates"
        dir = file(".github/ci/$ci")
        tasks = listOf("dependencyUpdates")
    }
}

tasks.register("outdatedCheck") {
    group = "CI"
    description = "Check all CI dependencies for available updates"
    dependsOn("spotlessOutdatedCheck", "archunitOutdatedCheck", "errorproneOutdatedCheck", "spotbugsOutdatedCheck", "junitOutdatedCheck")
}

tasks.register("ci") {
    group = "CI"
    description = "Run all CI checks"
    dependsOn("spotlessCheck", "archunit", "errorprone", "spotbugs", "junit")
}
