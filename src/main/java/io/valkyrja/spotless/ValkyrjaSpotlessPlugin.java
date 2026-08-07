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

public final class ValkyrjaSpotlessPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "valkyrjaSpotless";

    static final String SPOTLESS_PLUGIN_ID = "com.diffplug.spotless";

    static final String SHELL_FORMAT_NAME = "shell";

    /**
     * Where the shell license step writes the header.
     *
     * <p>The step replaces everything before the delimiter, so the delimiter matches the first line
     * that starts with neither a comment mark nor a blank.
     */
    static final String SHELL_DELIMITER = "(?=[^#\\s])";

    static final String SHEBANG_PATTERN = "^#!.*$";

    @Override
    public void apply(final Project project) {
        final ValkyrjaSpotlessExtension extension =
                new ValkyrjaSpotlessExtension(project.getObjects());

        project.getExtensions().add(ValkyrjaSpotlessExtension.class, EXTENSION_NAME, extension);
        project.getPluginManager().apply(SPOTLESS_PLUGIN_ID);

        // Spotless takes the targets and the header as values, not as providers, so the extension
        // is read once the build script has finished configuring it.
        project.afterEvaluate(evaluated -> configure(evaluated, extension));
    }

    /**
     * Configures Spotless from what the repository stated.
     *
     * @param project the project that holds the Spotless extension
     * @param extension the block the repository configured
     * @throws IllegalStateException when the repository states no package name or no Java target
     * @throws GradleException when a stated pattern selects no file
     */
    private static void configure(
            final Project project, final ValkyrjaSpotlessExtension extension) {
        extension.validatePackageName();
        extension.validateJavaTargets();

        final String packageName = extension.getPackageName().get();
        final List<String> javaTargets = extension.getJavaTargets().get();
        final List<String> javaTargetExcludes = extension.getJavaTargetExcludes().get();
        final List<String> shellTargets = extension.getShellTargets().get();
        final String formatVersion = extension.getGoogleJavaFormatVersion().get();

        validatePatterns(project, javaTargets, "javaTargets");
        validatePatterns(project, shellTargets, "shellTargets");

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
     * Makes sure each stated pattern selects a file.
     *
     * @param project the project the patterns are relative to
     * @param patterns the stated patterns
     * @param property the name of the property that stated them
     * @throws GradleException when a pattern selects no file
     */
    private static void validatePatterns(
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
