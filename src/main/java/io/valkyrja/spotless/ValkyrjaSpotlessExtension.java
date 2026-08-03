/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.spotless;

import java.util.List;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * States what a repository gives {@link ValkyrjaSpotlessPlugin}.
 *
 * <p>The plugin holds the rules, and a repository holds the two things that only the repository
 * knows: the package name that the copyright header states, and the paths that Spotless formats.
 * Everything else has a default, and a repository that takes the default states nothing.
 *
 * <p>A repository configures this through the {@code valkyrjaSpotless} block:
 *
 * <pre>
 * valkyrjaSpotless {
 *     packageName = "Valkyrja Framework"
 *     javaTargets = listOf(
 *         "src/**&#47;*.java",
 *         ".github/ci/junit/src/test/java/**&#47;*.java",
 *     )
 * }
 * </pre>
 */
public final class ValkyrjaSpotlessExtension {

    /** The Google Java Format version that a repository takes when it states none. */
    public static final String DEFAULT_GOOGLE_JAVA_FORMAT_VERSION = "1.27.0";

    private final Property<String> packageName;
    private final ListProperty<String> javaTargets;
    private final ListProperty<String> javaTargetExcludes;
    private final ListProperty<String> shellTargets;
    private final Property<String> googleJavaFormatVersion;

    /**
     * Builds the extension.
     *
     * <p>{@link ValkyrjaSpotlessPlugin} constructs this and adds it to the project. A build script
     * does not construct it.
     *
     * @param objects the factory that builds each property
     */
    public ValkyrjaSpotlessExtension(final ObjectFactory objects) {
        this.packageName = objects.property(String.class);
        this.javaTargets = objects.listProperty(String.class);
        this.javaTargetExcludes = objects.listProperty(String.class);
        this.shellTargets = objects.listProperty(String.class);
        this.googleJavaFormatVersion =
                objects.property(String.class).convention(DEFAULT_GOOGLE_JAVA_FORMAT_VERSION);
    }

    /**
     * The package name that the copyright header states, for example {@code Valkyrja Framework}.
     *
     * <p>Warning: pass the name, never an assembled header. {@link CopyrightHeader} explains what a
     * caller that passes the header builds.
     *
     * <p>COPYRIGHT_HEADER.md in the {@code .github} repository maps each repository to its name.
     *
     * @return the package name property, which a repository must set
     */
    public Property<String> getPackageName() {
        return packageName;
    }

    /**
     * The Ant-style patterns that select the Java files Spotless formats.
     *
     * <p>Warning: state each source tree, including the trees that the CI builds hold. A tree that
     * no pattern selects is never formatted, and no tool reports it.
     *
     * <p>Scope a CI build's pattern to {@code src/test/java}. A {@code src/test/resources} tree can
     * hold {@code .java} files that are test <em>data</em> rather than source, and formatting one
     * rewrites the input that a test asserts on.
     *
     * @return the Java target property, which a repository must set to at least one pattern
     */
    public ListProperty<String> getJavaTargets() {
        return javaTargets;
    }

    /**
     * The Ant-style patterns that remove a file from {@link #getJavaTargets()}.
     *
     * <p>A repository that excludes nothing states nothing.
     *
     * @return the Java exclude property, which defaults to no pattern
     */
    public ListProperty<String> getJavaTargetExcludes() {
        return javaTargetExcludes;
    }

    /**
     * The paths of the shell scripts that take the copyright header in line comment syntax.
     *
     * <p>An entry point script has no extension, so a {@code **}{@code /*.java} pattern cannot
     * reach it and no other tool in the gate reads it. A repository that ships one states it here,
     * and the plugin holds it to the same header.
     *
     * <p>A repository that ships no such script states nothing, and the plugin configures no shell
     * format.
     *
     * @return the shell target property, which defaults to no path
     */
    public ListProperty<String> getShellTargets() {
        return shellTargets;
    }

    /**
     * The Google Java Format version that Spotless runs.
     *
     * <p>This defaults to {@link #DEFAULT_GOOGLE_JAVA_FORMAT_VERSION}, so every repository formats
     * with one version and a bump moves them together. A repository states this only to pin a
     * different version.
     *
     * @return the formatter version property
     */
    public Property<String> getGoogleJavaFormatVersion() {
        return googleJavaFormatVersion;
    }

    /**
     * Reads the package name, and reports a repository that states none.
     *
     * @return the package name
     * @throws IllegalStateException when the repository states no package name
     */
    String requirePackageName() {
        if (!packageName.isPresent()) {
            throw new IllegalStateException(
                    "valkyrjaSpotless states no packageName. Set it to the name that"
                            + " COPYRIGHT_HEADER.md maps this repository to, for example"
                            + " \"Valkyrja Framework\".");
        }

        return packageName.get();
    }

    /**
     * Reads the Java targets, and reports a repository that states none.
     *
     * <p>Warning: Spotless formats no file when it is given no pattern, and it reports success. An
     * empty target list therefore stops here rather than producing a gate that checks nothing.
     *
     * @return the Java target patterns
     * @throws IllegalStateException when the repository states no Java target
     */
    List<String> requireJavaTargets() {
        final List<String> targets = javaTargets.get();

        if (targets.isEmpty()) {
            throw new IllegalStateException(
                    "valkyrjaSpotless states no javaTargets. Spotless formats no file when it is"
                            + " given no pattern, and it reports success, so the gate would check"
                            + " nothing. State each Java source tree this repository holds.");
        }

        return targets;
    }
}
