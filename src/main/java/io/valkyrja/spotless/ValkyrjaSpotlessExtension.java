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

public final class ValkyrjaSpotlessExtension {

    public static final String DEFAULT_GOOGLE_JAVA_FORMAT_VERSION = "1.27.0";

    private final Property<String> packageName;
    private final ListProperty<String> javaTargets;
    private final ListProperty<String> javaTargetExcludes;
    private final ListProperty<String> shellTargets;
    private final Property<String> googleJavaFormatVersion;

    /**
     * Builds the extension.
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
     * The package name the copyright header states, such as {@code Valkyrja Framework}.
     *
     * @return the package name property, which a repository must set
     */
    public Property<String> getPackageName() {
        return packageName;
    }

    /**
     * The Ant-style patterns that select the Java files Spotless formats.
     *
     * @return the Java target property, which a repository must set to at least one pattern
     */
    public ListProperty<String> getJavaTargets() {
        return javaTargets;
    }

    /**
     * The Ant-style patterns that remove a file from {@link #getJavaTargets()}.
     *
     * @return the Java exclude property, which defaults to no pattern
     */
    public ListProperty<String> getJavaTargetExcludes() {
        return javaTargetExcludes;
    }

    /**
     * The paths of the shell scripts that take the copyright header in line comment syntax.
     *
     * @return the shell target property, which defaults to no path
     */
    public ListProperty<String> getShellTargets() {
        return shellTargets;
    }

    /**
     * The Google Java Format version Spotless runs.
     *
     * @return the formatter version property, which defaults to {@link
     *     #DEFAULT_GOOGLE_JAVA_FORMAT_VERSION}
     */
    public Property<String> getGoogleJavaFormatVersion() {
        return googleJavaFormatVersion;
    }

    /**
     * Makes sure the repository states a package name.
     *
     * @throws IllegalStateException when the repository states no package name
     */
    void validatePackageName() {
        if (!packageName.isPresent()) {
            throw new IllegalStateException(
                    "valkyrjaSpotless states no packageName. Set it to the name that"
                            + " COPYRIGHT_HEADER.md maps this repository to, for example"
                            + " \"Valkyrja Framework\".");
        }
    }

    /**
     * Makes sure the repository states at least one Java target.
     *
     * @throws IllegalStateException when the repository states no Java target
     */
    void validateJavaTargets() {
        final List<String> targets = javaTargets.get();

        if (targets.isEmpty()) {
            throw new IllegalStateException(
                    "valkyrjaSpotless states no javaTargets. Spotless formats no file when it is"
                            + " given no pattern, and it reports success, so the gate would check"
                            + " nothing. State each Java source tree this repository holds.");
        }
    }
}
