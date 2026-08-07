/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.tests.unit.spotless;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.valkyrja.spotless.ValkyrjaSpotlessExtension;
import io.valkyrja.spotless.ValkyrjaSpotlessPlugin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ValkyrjaSpotlessPluginTest {

    @TempDir private Path projectDir;

    private Project project;

    /**
     * Builds a project that holds one Java file, and applies the plugin to it.
     *
     * @throws IOException when the source tree cannot be written
     */
    @BeforeEach
    void setUp() throws IOException {
        writeFile("src/main/java/io/valkyrja/Sample.java", "class Sample {}\n");

        project = ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build();
        project.getPluginManager().apply(ValkyrjaSpotlessPlugin.class);
    }

    /**
     * Writes a file under the project directory, building each parent directory.
     *
     * @param relativePath the path relative to the project directory
     * @param content the file content
     * @throws IOException when the file cannot be written
     */
    private void writeFile(final String relativePath, final String content) throws IOException {
        final Path target = projectDir.resolve(relativePath);
        final Path parent =
                Objects.requireNonNull(
                        target.getParent(), "Every path this test writes sits under a directory.");

        Files.createDirectories(parent);
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }

    /** Reads the block that the plugin added to the project. */
    private ValkyrjaSpotlessExtension valkyrjaSpotless() {
        return project.getExtensions().getByType(ValkyrjaSpotlessExtension.class);
    }

    /**
     * Reports whether Spotless built the task that runs one format.
     *
     * @param format the format name, for example {@code Java}
     * @return whether the task exists
     */
    private boolean spotlessRunsFormat(final String format) {
        return project.getTasks().findByName("spotless" + format) != null;
    }

    /** Runs the {@code afterEvaluate} callbacks, which is where the plugin configures Spotless. */
    private void evaluate() {
        ((ProjectInternal) project).evaluate();
    }

    /**
     * Evaluates the project and returns the cause of the configuration failure.
     *
     * @return the cause, because Gradle wraps a failure raised in {@code afterEvaluate}
     */
    private Throwable evaluateExpectingFailure() {
        final Throwable thrown = assertThrows(Throwable.class, this::evaluate);
        final Throwable cause = thrown.getCause();

        assertNotNull(cause, "A configuration failure must carry the cause: " + thrown);

        return cause;
    }

    @Test
    void applyingThePluginAddsTheBlockAndAppliesSpotless() {
        assertNotNull(valkyrjaSpotless(), "The plugin must add the valkyrjaSpotless block.");
        assertTrue(
                project.getPluginManager().hasPlugin("com.diffplug.spotless"),
                "The plugin must apply Spotless itself, so the rules and the tool move together.");
    }

    @Test
    void theBlockIsRegisteredUnderTheDocumentedName() {
        assertEquals(
                valkyrjaSpotless(),
                project.getExtensions().getByName(ValkyrjaSpotlessPlugin.EXTENSION_NAME),
                "The block a build script configures must be the one the plugin added.");
    }

    @Test
    void theFormatterVersionDefaultsSoEveryRepositoryFormatsAlike() {
        assertEquals(
                ValkyrjaSpotlessExtension.DEFAULT_GOOGLE_JAVA_FORMAT_VERSION,
                valkyrjaSpotless().getGoogleJavaFormatVersion().get());
    }

    @Test
    void aRepositoryThatStatesOnlyTheNameAndTheTargetsConfiguresTheJavaFormat() {
        valkyrjaSpotless().getPackageName().set("Valkyrja Framework");
        valkyrjaSpotless().getJavaTargets().set(List.of("src/**/*.java"));

        evaluate();

        assertTrue(
                spotlessRunsFormat("Java"),
                "The plugin must configure the Java format from the stated targets.");
    }

    @Test
    void aRepositoryThatStatesNoShellTargetGetsNoShellFormat() {
        valkyrjaSpotless().getPackageName().set("Valkyrja Framework");
        valkyrjaSpotless().getJavaTargets().set(List.of("src/**/*.java"));

        evaluate();

        assertFalse(
                spotlessRunsFormat("Shell"),
                "A repository that ships no entry point script must get no shell format.");
    }

    @Test
    void aRepositoryThatStatesExcludesAndShellTargetsGetsBoth() throws IOException {
        writeFile("src/main/java/io/valkyrja/Config.example.java", "class Config {}\n");
        writeFile("bin/cli", "#!/usr/bin/env sh\necho valkyrja\n");

        valkyrjaSpotless().getPackageName().set("Valkyrja Application");
        valkyrjaSpotless().getJavaTargets().set(List.of("src/**/*.java"));
        valkyrjaSpotless().getJavaTargetExcludes().set(List.of("**/*.example.java"));
        valkyrjaSpotless().getShellTargets().set(List.of("bin/cli"));

        evaluate();

        assertTrue(spotlessRunsFormat("Java"));
        assertTrue(
                spotlessRunsFormat("Shell"),
                "A repository that states a shell target must get the shell format.");
    }

    @Test
    void aRepositoryThatStatesNoPackageNameIsReported() {
        valkyrjaSpotless().getJavaTargets().set(List.of("src/**/*.java"));

        final Throwable cause = evaluateExpectingFailure();

        assertInstanceOf(IllegalStateException.class, cause);
        assertTrue(
                cause.getMessage().contains("states no packageName"),
                "The failure must name the missing property, but is: " + cause.getMessage());
    }

    @Test
    void aRepositoryThatStatesNoJavaTargetIsReported() {
        valkyrjaSpotless().getPackageName().set("Valkyrja Framework");

        final Throwable cause = evaluateExpectingFailure();

        assertInstanceOf(IllegalStateException.class, cause);
        assertTrue(
                cause.getMessage().contains("states no javaTargets"),
                "The failure must name the missing property, but is: " + cause.getMessage());
    }

    @Test
    void aJavaPatternThatSelectsNoFileIsReported() {
        valkyrjaSpotless().getPackageName().set("Valkyrja Framework");
        valkyrjaSpotless()
                .getJavaTargets()
                .set(List.of("src/**/*.java", ".github/ci/gone/src/test/java/**/*.java"));

        final Throwable cause = evaluateExpectingFailure();

        assertTrue(
                cause.getMessage().contains(".github/ci/gone/src/test/java/**/*.java"),
                "The failure must name the dead pattern, but is: " + cause.getMessage());
        assertTrue(
                cause.getMessage().contains("selects no file"),
                "The failure must say what is wrong, but is: " + cause.getMessage());
    }

    @Test
    void aShellPatternThatSelectsNoFileIsReported() {
        valkyrjaSpotless().getPackageName().set("Valkyrja Application");
        valkyrjaSpotless().getJavaTargets().set(List.of("src/**/*.java"));
        valkyrjaSpotless().getShellTargets().set(List.of("bin/gone"));

        final Throwable cause = evaluateExpectingFailure();

        assertTrue(
                cause.getMessage().contains("shellTargets"),
                "The failure must name the property, but is: " + cause.getMessage());
        assertTrue(
                cause.getMessage().contains("bin/gone"),
                "The failure must name the dead pattern, but is: " + cause.getMessage());
    }
}
