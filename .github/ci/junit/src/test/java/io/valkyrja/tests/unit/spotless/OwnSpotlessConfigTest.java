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
 */
final class OwnSpotlessConfigTest {

    /** The package identifier for this repository, per COPYRIGHT_HEADER.md pattern 5. */
    private static final String PACKAGE_NAME = "Valkyrja Spotless";

    /** The Spotless build script, relative to the JUnit build directory that the test runs from. */
    private static final Path SPOTLESS_BUILD_SCRIPT = Path.of("..", "spotless", "build.gradle.kts");

    @Test
    void theSpotlessConfigStatesTheHeaderThisPackageBuilds() throws IOException {
        final String script = Files.readString(SPOTLESS_BUILD_SCRIPT, StandardCharsets.UTF_8);
        final String expected = CopyrightHeader.block(PACKAGE_NAME);

        assertTrue(
                script.contains(expected),
                "The licenseHeader in "
                        + SPOTLESS_BUILD_SCRIPT
                        + " must state exactly what CopyrightHeader.block(\""
                        + PACKAGE_NAME
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
                source.startsWith(CopyrightHeader.block(PACKAGE_NAME)),
                "A file in this repository must open with the header this package builds.");
    }
}
