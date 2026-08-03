/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.tests.unit.spotless.constant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.valkyrja.spotless.constant.SpotlessInfo;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpotlessInfo}.
 *
 * <p>The release workflow rewrites both constants. Each test asserts a format and never an exact
 * value.
 */
final class SpotlessInfoTest {

    @Test
    void versionHasTheVersionFormat() {
        assertTrue(
                SpotlessInfo.VERSION.matches("\\d+\\.\\d+\\.\\d+"),
                "VERSION must have the format MAJOR.MINOR.PATCH, but is: " + SpotlessInfo.VERSION);
    }

    @Test
    void versionBuildDateTimeHasTheBuildDateTimeFormat() {
        assertTrue(
                SpotlessInfo.VERSION_BUILD_DATE_TIME.matches(
                        "[A-Z][a-z]+ \\d{1,2} \\d{4} \\d{2}:\\d{2}:\\d{2} MST"),
                "VERSION_BUILD_DATE_TIME must have the format 'Month D YYYY HH:MM:SS MST', but is: "
                        + SpotlessInfo.VERSION_BUILD_DATE_TIME);
    }
}
