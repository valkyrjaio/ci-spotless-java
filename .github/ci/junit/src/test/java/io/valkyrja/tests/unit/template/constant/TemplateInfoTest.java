/*
 * This file is part of the Project Template package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.tests.unit.template.constant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.valkyrja.template.constant.TemplateInfo;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TemplateInfo}.
 *
 * <p>The release workflow rewrites both constants. Each test asserts a format and never an exact
 * value.
 */
final class TemplateInfoTest {

    @Test
    void versionHasTheVersionFormat() {
        assertTrue(
                TemplateInfo.VERSION.matches("\\d+\\.\\d+\\.\\d+"),
                "VERSION must have the format MAJOR.MINOR.PATCH, but is: " + TemplateInfo.VERSION);
    }

    @Test
    void versionBuildDateTimeHasTheBuildDateTimeFormat() {
        assertTrue(
                TemplateInfo.VERSION_BUILD_DATE_TIME.matches(
                        "[A-Z][a-z]+ \\d{1,2} \\d{4} \\d{2}:\\d{2}:\\d{2} MST"),
                "VERSION_BUILD_DATE_TIME must have the format 'Month D YYYY HH:MM:SS MST', but is: "
                        + TemplateInfo.VERSION_BUILD_DATE_TIME);
    }
}
