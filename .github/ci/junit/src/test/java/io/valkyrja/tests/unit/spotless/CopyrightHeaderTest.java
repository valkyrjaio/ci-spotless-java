/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.tests.unit.spotless;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.valkyrja.spotless.CopyrightHeader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link CopyrightHeader}.
 *
 * <p>Each rendering test asserts the whole result against a literal, and never against a pattern or
 * a fragment. Spotless replaces a header that differs by one character, so a test that matches part
 * of the result lets the rest change without a failure.
 *
 * <p>The two literals below are the text that the four Java repositories held inline before this
 * package existed. They were read from those files, and they are what the repositories must keep
 * producing.
 */
final class CopyrightHeaderTest {

    private static final String EXPECTED_BLOCK =
            "/*\n"
                    + " * This file is part of the Valkyrja Framework package.\n"
                    + " *\n"
                    + " * Copyright (c) 2016-present Melech Mizrachi\n"
                    + " *\n"
                    + " * Released under the MIT License. See LICENSE.md for details.\n"
                    + " */\n"
                    + "\n";

    private static final String EXPECTED_SHELL =
            "\n"
                    + "# This file is part of the Valkyrja Application package.\n"
                    + "#\n"
                    + "# Copyright (c) 2016-present Melech Mizrachi\n"
                    + "#\n"
                    + "# Released under the MIT License. See LICENSE.md for details.\n"
                    + "\n";

    @Test
    void blockRendersTheHeaderThatAJavaFileTakes() {
        assertEquals(EXPECTED_BLOCK, CopyrightHeader.block("Valkyrja Framework"));
    }

    @Test
    void shellRendersTheHeaderThatAShellScriptTakes() {
        assertEquals(EXPECTED_SHELL, CopyrightHeader.shell("Valkyrja Application"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Valkyrja Framework",
                "Valkyrja Application",
                "Valkyrja Spotless",
                "Project Template",
                "Sindri",
            })
    void blockStatesThePackageNameItIsGiven(final String packageName) {
        assertTrue(
                CopyrightHeader.block(packageName)
                        .startsWith("/*\n * This file is part of the " + packageName + " package."),
                "The block header must state the package name it is given, but is: "
                        + CopyrightHeader.block(packageName));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Valkyrja Framework",
                "Valkyrja Application",
                "Valkyrja Spotless",
                "Project Template",
                "Sindri",
            })
    void shellStatesThePackageNameItIsGiven(final String packageName) {
        assertTrue(
                CopyrightHeader.shell(packageName)
                        .startsWith("\n# This file is part of the " + packageName + " package."),
                "The shell header must state the package name it is given, but is: "
                        + CopyrightHeader.shell(packageName));
    }

    @Test
    void blockRejectsAPackageNameThatSpansLines() {
        assertThrows(IllegalArgumentException.class, () -> CopyrightHeader.block(EXPECTED_BLOCK));
    }

    @Test
    void shellRejectsAPackageNameThatSpansLines() {
        assertThrows(IllegalArgumentException.class, () -> CopyrightHeader.shell(EXPECTED_SHELL));
    }

    @Test
    void theRejectionTellsTheCallerToPassTheNameInstead() {
        final IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> CopyrightHeader.block("Valkyrja Framework\nand a second line"));

        assertEquals(
                "CopyrightHeader takes a package name, such as \"Valkyrja Framework\", and it was"
                        + " given text that spans lines. A caller that passes the assembled header"
                        + " must pass the name instead.",
                thrown.getMessage());
    }

    /**
     * The class holds static methods only, so its constructor is private and nothing calls it.
     * JaCoCo still counts that constructor, and this repository requires 100% line coverage per
     * class, so the test reaches it by reflection.
     */
    @Test
    void theConstructorIsPrivate() throws ReflectiveOperationException {
        final Constructor<CopyrightHeader> constructor =
                CopyrightHeader.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
