/*
 * This file is part of the Valkyrja Spotless package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.valkyrja.spotless;

/**
 * Builds the copyright header that Spotless writes into every file.
 *
 * <p>The header states the package name, and every other line is the same in each repository. This
 * package therefore holds the text, and a repository states only its own name. A repository that
 * keeps a copy of the whole header can drift from this text, and no tool reports the drift.
 * COPYRIGHT_HEADER.md in the {@code .github} repository specifies the text, and it maps each
 * repository to its package name.
 *
 * <p>Spotless takes a rendered comment, not the bare notice, so this class renders the comment
 * syntax as well. {@link #block(String)} gives the form that a Java file takes, and {@link
 * #shell(String)} gives the form that a shell script takes. Both come from {@link #NOTICE_LINES},
 * so the two forms cannot drift from each other.
 *
 * @see <a
 *     href="https://github.com/valkyrjaio/.github/blob/26.x/COPYRIGHT_HEADER.md">COPYRIGHT_HEADER.md</a>
 */
public final class CopyrightHeader {

    /**
     * The notice, one entry per line, with no comment syntax.
     *
     * <p>The first line takes the package name. An empty entry is a blank line.
     */
    private static final String[] NOTICE_LINES = {
        "This file is part of the %s package.",
        "",
        "Copyright (c) 2016-present Melech Mizrachi",
        "",
        "Released under the MIT License. See LICENSE.md for details.",
    };

    private CopyrightHeader() {}

    /**
     * Builds the header that a Java file takes, as a block comment.
     *
     * <p>Pass the result to Spotless:
     *
     * <pre>{@code
     * spotless {
     *     java {
     *         licenseHeader(CopyrightHeader.block("Valkyrja Framework"))
     *     }
     * }
     * }</pre>
     *
     * <p>The result ends with a blank line, because Spotless writes the header immediately before
     * the {@code package} declaration and that declaration takes a blank line above it.
     *
     * @param packageName the package name, for example {@code Valkyrja Framework}
     * @return the block comment, ending with a blank line
     * @throws IllegalArgumentException when the package name spans more than one line
     */
    public static String block(final String packageName) {
        return render(packageName, "/*\n", " * ", " *", " */\n\n");
    }

    /**
     * Builds the header that a shell script takes, as line comments.
     *
     * <p>Pass the result to Spotless, with the delimiter that holds the header below the shebang:
     *
     * <pre>{@code
     * spotless {
     *     format("shell") {
     *         target("app/bin/cli")
     *         licenseHeader(CopyrightHeader.shell("Valkyrja Application"), "(?=[^#\\s])")
     *             .skipLinesMatching("^#!.*$")
     *     }
     * }
     * }</pre>
     *
     * <p>The result starts with a blank line as well as ending with one. A shell script keeps its
     * shebang on the first line, so the header follows that line rather than opening the file, and
     * the leading blank line separates the two.
     *
     * @param packageName the package name, for example {@code Valkyrja Application}
     * @return the line comments, starting and ending with a blank line
     * @throws IllegalArgumentException when the package name spans more than one line
     */
    public static String shell(final String packageName) {
        return render(packageName, "\n", "# ", "#", "\n");
    }

    /**
     * Renders the notice in one comment syntax.
     *
     * @param packageName the package name the first line states
     * @param open the text before the first line
     * @param linePrefix the prefix a line that holds text takes
     * @param blankPrefix the prefix a blank line takes, which carries no trailing space
     * @param close the text after the last line
     * @return the rendered comment
     * @throws IllegalArgumentException when the package name spans more than one line
     */
    private static String render(
            final String packageName,
            final String open,
            final String linePrefix,
            final String blankPrefix,
            final String close) {
        // Warning: a package name that spans lines corrupts every file, and no check reports it.
        // This method puts the argument into the first line of the header, so an assembled header
        // builds "This file is part of the <whole header> package". Spotless writes that text into
        // every file, and the check afterwards passes, because the files and the configuration then
        // agree with each other. A loud failure is better than a silent rewrite, so a name that
        // spans lines stops here.
        if (packageName.contains("\n")) {
            throw new IllegalArgumentException(
                    "CopyrightHeader takes a package name, such as \"Valkyrja Framework\", and it"
                            + " was given text that spans lines. A caller that passes the assembled"
                            + " header must pass the name instead.");
        }

        final StringBuilder header = new StringBuilder(open);

        for (final String line : NOTICE_LINES) {
            if (line.isEmpty()) {
                header.append(blankPrefix);
            } else {
                header.append(linePrefix).append(line.formatted(packageName));
            }

            header.append('\n');
        }

        return header.append(close).toString();
    }
}
