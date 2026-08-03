/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.tests.unit.spotless;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.valkyrja.spotless.CopyrightHeader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Asserts that this repository's own Spotless configuration states the header that {@link
 * CopyrightHeader} builds.
 *
 * <p>Every other Valkyrja Java repository calls {@link CopyrightHeader#block(String)} from its
 * Spotless build. This repository cannot: Gradle resolves the buildscript classpath before it
 * evaluates the script, so a build here that asked for this package would ask for the artifact that
 * the same build produces. This repository therefore keeps the header inline, and it is the one
 * copy of the text that this package does not remove.
 *
 * <p>This test is what makes that copy safe. A copy that no tool compares can drift, and a drifted
 * header here would ship in the artifact that every other repository trusts. The test compares the
 * two, so the copy cannot drift in silence.
 *
 * <p>The package name comes from {@code .github/ci/copyright-header/config}, which the copyright
 * header check already reads. This repository therefore states its package name once, which is what
 * this package gives every other repository.
 */
final class OwnSpotlessConfigTest {

    /** The Spotless build script, relative to the JUnit build directory that the test runs from. */
    private static final Path SPOTLESS_BUILD_SCRIPT = Path.of("..", "spotless", "build.gradle.kts");

    /** The copyright header check's config, which already states this repository's package name. */
    private static final Path COPYRIGHT_HEADER_CONFIG = Path.of("..", "copyright-header", "config");

    /** Matches the {@code IDENTIFIER='…'} assignment in that config. */
    private static final Pattern IDENTIFIER_ASSIGNMENT =
            Pattern.compile("^IDENTIFIER='([^']*)'", Pattern.MULTILINE);

    /**
     * Reads this repository's package name from the copyright header check's config.
     *
     * <p>The name is not repeated here. This repository states it in that config, and a second copy
     * in this test could disagree with it — which is the same drift that this package exists to
     * remove.
     *
     * @return the package identifier the config states
     * @throws IOException when the config cannot be read
     * @throws IllegalStateException when the config states no identifier
     */
    private static String packageName() throws IOException {
        final String config = Files.readString(COPYRIGHT_HEADER_CONFIG, StandardCharsets.UTF_8);
        final Matcher matcher = IDENTIFIER_ASSIGNMENT.matcher(config);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    COPYRIGHT_HEADER_CONFIG
                            + " states no IDENTIFIER='…' assignment, so this test cannot read the"
                            + " package name. A test that fell back to a literal would hide the"
                            + " drift it exists to report.");
        }

        return matcher.group(1);
    }

    @Test
    void theSpotlessConfigStatesTheHeaderThisPackageBuilds() throws IOException {
        final String packageName = packageName();
        final String script = Files.readString(SPOTLESS_BUILD_SCRIPT, StandardCharsets.UTF_8);
        final String expected = CopyrightHeader.block(packageName);

        assertTrue(
                script.contains(expected),
                "The licenseHeader in "
                        + SPOTLESS_BUILD_SCRIPT
                        + " must state exactly what CopyrightHeader.block(\""
                        + packageName
                        + "\") builds, which is:\n"
                        + expected);
    }

    @Test
    void thisFileCarriesTheHeaderThisPackageBuilds() throws IOException {
        final Path self =
                Path.of(
                        "src",
                        "test",
                        "java",
                        "io",
                        "valkyrja",
                        "tests",
                        "unit",
                        "spotless",
                        "OwnSpotlessConfigTest.java");
        final String source = Files.readString(self, StandardCharsets.UTF_8);

        assertTrue(
                source.startsWith(CopyrightHeader.block(packageName())),
                "A file in this repository must open with the header this package builds.");
    }
}
