<p align="center"><a href="https://valkyrja.io" target="_blank">
    <img src="https://raw.githubusercontent.com/valkyrjaio/art/refs/heads/26.x/long-banner/orange/java.png" width="100%">
</a></p>

# Valkyrja Spotless (Java)

Shared Spotless configuration for Valkyrja Java repositories.

<p>
    <a href="https://central.sonatype.com/artifact/io.valkyrja/ci-spotless"><img src="https://img.shields.io/maven-central/v/io.valkyrja/ci-spotless?label=Maven%20Central" alt="Latest Stable Version"></a>
    <a href="https://github.com/valkyrjaio/ci-spotless-java"><img src="https://img.shields.io/badge/Java-21--25-orange" alt="Java Version"></a>
    <a href="https://github.com/valkyrjaio/ci-spotless-java/blob/26.x/LICENSE.md"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License"></a>
    <a href="https://github.com/valkyrjaio/ci-spotless-java/actions/workflows/ci.yml?query=branch%3A26.x"><img src="https://github.com/valkyrjaio/ci-spotless-java/actions/workflows/ci.yml/badge.svg?branch=26.x" alt="CI Status"></a>
    <a href="https://sonarcloud.io/summary/new_code?id=valkyrjaio_ci-spotless-java"><img src="https://sonarcloud.io/api/project_badges/measure?project=valkyrjaio_ci-spotless-java&metric=sqale_rating" alt="Maintainability Rating"></a>
</p>

This package holds the copyright header text. A repository states only its own
package name, and this package builds the header from it. A repository that
keeps a copy of the whole header can drift from the text, and no tool reports
the drift.

[`COPYRIGHT_HEADER.md`][copyright header url] in the `.github` repository
specifies the text, and it maps each repository to its package name.

Usage
-----

Put the package on the buildscript classpath, then call it from the Spotless
configuration in `.github/ci/spotless/build.gradle.kts`:

```kotlin
import io.valkyrja.spotless.CopyrightHeader

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("io.valkyrja:ci-spotless:26.1.0")
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.27.0").aosp()
        licenseHeader(CopyrightHeader.block("Valkyrja Framework"))
    }
}
```

`block` builds the header that a Java file takes. A shell script takes line
comments instead, so it takes `shell`, with the delimiter that holds the header
below the shebang:

```kotlin
spotless {
    format("shell") {
        target("app/bin/cli", "app/public/index")
        licenseHeader(CopyrightHeader.shell("Valkyrja Application"), "(?=[^#\\s])")
            .skipLinesMatching("^#!.*$")
    }
}
```

Both forms come from one notice, so the two cannot drift from each other.

### Pass the package name, never the header

`block` and `shell` take a package name, such as `Valkyrja Framework`. A caller
that passes an assembled header builds the sentence
`This file is part of the <whole header> package.`

Warning: Spotless writes that sentence into every file, and `spotlessCheck`
afterwards passes, because the files and the configuration then agree with each
other. Every step of the gate reports success while each file holds a corrupt
header. A name that spans lines therefore raises `IllegalArgumentException`
rather than rendering.

```kotlin
// Wrong — this passes the assembled header, and every file takes the corrupt sentence.
licenseHeader(CopyrightHeader.block(theWholeHeaderText))
```

```kotlin
// Right — this passes the package name, and this package builds the header.
licenseHeader(CopyrightHeader.block("Valkyrja Framework"))
```

Versioning and Release Process
------------------------------

This package follows [semantic versioning][semantic versioning url] with a major
release every year, and support for each major version for 2 years from the date
of release.

For more information see our
[Versioning and Release Process documentation][Versioning and Release Process url].

### Supported Versions

Bug fixes are provided until 3 months after the next major release. Security
fixes are provided for 2 years after the initial release.

| Version | Java    | Release        | Bug Fixes Until | Security Fixes Until |
| :------ | :------ | :------------- | :-------------- | :------------------- |
| 26      | 21 – 25 | March 31, 2026 | Q2 2027         | Q1 2028              |
| 27      | 23 – 25 | Q1 2027        | Q2 2028         | Q1 2029              |
| 28      | 25+     | Q1 2028        | Q2 2029         | Q1 2030              |

Contributing
------------

See [`CONTRIBUTING.md`][contributing url] for the submission process and
[`VOCABULARY.md`][vocabulary url] for the terminology used across Valkyrja.

Security Issues
---------------

If you discover a security vulnerability, please follow our
[disclosure procedure][security vulnerabilities url].

License
-------

This package is open-source software licensed under the
[MIT license][MIT license url]. See [`LICENSE.md`](./LICENSE.md).

[Valkyrja url]: https://valkyrja.io
[copyright header url]: https://github.com/valkyrjaio/.github/blob/26.x/COPYRIGHT_HEADER.md
[vocabulary url]: https://github.com/valkyrjaio/.github/blob/26.x/VOCABULARY.md
[contributing url]: https://github.com/valkyrjaio/.github/blob/26.x/CONTRIBUTING.md
[security vulnerabilities url]: https://github.com/valkyrjaio/.github/blob/26.x/SECURITY.md
[Versioning and Release Process url]: https://github.com/valkyrjaio/valkyrja-java/blob/26.x/src/main/java/io/valkyrja/VERSIONING_AND_RELEASE_PROCESS.md
[semantic versioning url]: https://semver.org/
[MIT license url]: https://opensource.org/licenses/MIT
[license url]: ./LICENSE.md
