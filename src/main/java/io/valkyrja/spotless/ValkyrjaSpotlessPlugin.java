/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.spotless;

import com.diffplug.gradle.spotless.FormatExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Configures Spotless for a Valkyrja Java repository.
 *
 * <p>Every Valkyrja Java repository formats its code the same way, and each one used to state that
 * configuration in full. This plugin holds it instead. A repository states the package name that
 * the copyright header takes and the paths that Spotless formats, and it states nothing else.
 *
 * <p>The plugin declares its own dependency on the Spotless plugin, so the rules and the tool move
 * together. A build script that declared the Spotless plugin itself could bump that plugin past the
 * API these rules compile against.
 *
 * <pre>
 * plugins {
 *     id("io.valkyrja.ci-spotless") version "26.2.0"
 * }
 *
 * valkyrjaSpotless {
 *     packageName = "Valkyrja Framework"
 *     javaTargets = listOf("src/**&#47;*.java")
 * }
 * </pre>
 *
 * @see ValkyrjaSpotlessExtension
 */
public final class ValkyrjaSpotlessPlugin implements Plugin<Project> {

    /** The name of the block that a build script configures. */
    public static final String EXTENSION_NAME = "valkyrjaSpotless";

    /** The plugin id of Spotless itself, which this plugin applies. */
    static final String SPOTLESS_PLUGIN_ID = "com.diffplug.spotless";

    /** The name of the Spotless format that holds the shell scripts. */
    static final String SHELL_FORMAT_NAME = "shell";

    /**
     * Where the shell license step writes the header.
     *
     * <p>Warning: the license step replaces everything before the delimiter. The delimiter
     * therefore matches the first line that starts with neither a comment mark nor a blank.
     */
    static final String SHELL_DELIMITER = "(?=[^#\\s])";

    /** Holds the shebang on line 1, which the header follows. */
    static final String SHEBANG_PATTERN = "^#!.*$";

    @Override
    public void apply(final Project project) {
        final ValkyrjaSpotlessExtension extension =
                new ValkyrjaSpotlessExtension(project.getObjects());

        project.getExtensions().add(ValkyrjaSpotlessExtension.class, EXTENSION_NAME, extension);
        project.getPluginManager().apply(SPOTLESS_PLUGIN_ID);

        // Spotless takes the targets and the header as values, not as providers, so the plugin
        // reads the extension once the build script has finished configuring it.
        project.afterEvaluate(evaluated -> configure(evaluated, extension));
    }

    /**
     * Configures Spotless from what the repository stated.
     *
     * @param project the project that holds the Spotless extension
     * @param extension the block that the repository configured
     * @throws IllegalStateException when the repository states no package name or no Java target
     * @throws GradleException when a stated pattern selects no file
     */
    private static void configure(
            final Project project, final ValkyrjaSpotlessExtension extension) {
        final String packageName = extension.requirePackageName();
        final List<String> javaTargets = extension.requireJavaTargets();
        final List<String> javaTargetExcludes = extension.getJavaTargetExcludes().get();
        final List<String> shellTargets = extension.getShellTargets().get();
        final String formatVersion = extension.getGoogleJavaFormatVersion().get();

        requireEachPatternSelectsAFile(project, javaTargets, "javaTargets");
        requireEachPatternSelectsAFile(project, shellTargets, "shellTargets");

        final SpotlessExtension spotless =
                project.getExtensions().getByType(SpotlessExtension.class);

        spotless.java(
                java -> {
                    java.target(javaTargets.toArray());

                    if (!javaTargetExcludes.isEmpty()) {
                        java.targetExclude(javaTargetExcludes.toArray());
                    }

                    java.googleJavaFormat(formatVersion).aosp();
                    java.licenseHeader(CopyrightHeader.block(packageName));
                });

        if (shellTargets.isEmpty()) {
            return;
        }

        spotless.format(
                SHELL_FORMAT_NAME,
                (FormatExtension shell) -> {
                    shell.target(shellTargets.toArray());
                    shell.licenseHeader(CopyrightHeader.shell(packageName), SHELL_DELIMITER)
                            .skipLinesMatching(SHEBANG_PATTERN);
                });
    }

    /**
     * Reports a stated pattern that selects no file.
     *
     * <p>Warning: Spotless reports success over a pattern that selects nothing, and that output is
     * the same as a real pass. A whole source tree can therefore leave the gate without one report.
     * Two mistakes produce it: a build invoked with {@code --project-dir}, which moves every
     * relative pattern off its files, and a renamed directory that leaves a pattern behind.
     *
     * @param project the project the patterns are relative to
     * @param patterns the stated patterns
     * @param property the name of the property that stated them, for the failure message
     * @throws GradleException when a pattern selects no file
     */
    private static void requireEachPatternSelectsAFile(
            final Project project, final List<String> patterns, final String property) {
        for (final String pattern : patterns) {
            if (!project.fileTree(project.getProjectDir())
                    .matching(it -> it.include(pattern))
                    .isEmpty()) {
                continue;
            }

            throw new GradleException(
                    "valkyrjaSpotless "
                            + property
                            + " states the pattern \""
                            + pattern
                            + "\", and it selects no file under "
                            + project.getProjectDir()
                            + ". Spotless reports success over a pattern that selects nothing, so"
                            + " this would be a gate that checks nothing. Correct the pattern, or"
                            + " remove it when the tree it named is gone.");
        }
    }
}
