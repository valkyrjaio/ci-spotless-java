<p align="center"><a href="https://valkyrja.io" target="_blank">
    <img src="https://raw.githubusercontent.com/valkyrjaio/art/refs/heads/master/long-banner/orange/java.png" width="100%">
</a></p>

# Project Template (Java)

A starter template for creating new Java repositories in the Valkyrjaio
organization.

This template ships with the full Valkyrja CI pipeline pre-wired (commit-message
check, trailing-newline check), a minimal Gradle setup, and the repository
conventions used across the rest of the org. Use it as the starting point for any
new Java package or integration repo — not for end-user applications built on the
Valkyrja framework (use [`valkyrja-starter-app-java`][starter url] for that).

Usage
-----

### Use this template _(recommended)_

This repository is a GitHub template. Click the **Use this template** button
at the top of the repo to create a new repository in the Valkyrjaio
organization, pre-populated with the template's structure and CI.

### After Creating Your Repo

1. Update `build.gradle.kts` with your package's artifact ID, description, and
   GitHub URL
2. Update `settings.gradle.kts` with your project name
3. Replace the contents of `src/` with your package's source code
4. Update this `README.md` to describe the new package
5. Configure the required secrets and variables — see
   [`REPOSITORY_NAMING.md`][repository naming url] for naming guidance and
   `.github`'s workflow documentation for secret requirements
6. Verify CI passes on the first commit

What's Included
---------------

- **CI pipeline** — the same commit-message and trailing-newline checks used
  across every Valkyrjaio repo
- **Gradle configuration** — `build.gradle.kts` with Maven Central publishing
  and signing, matching the org convention
- **Repository conventions** — aligned with
  [`REPOSITORY_NAMING.md`][repository naming url] and
  [`VOCABULARY.md`][vocabulary url]

Versioning and Release Process
------------------------------

This template follows [semantic versioning][semantic versioning url] with a
major release every year, and support for each major version for 2 years
from the date of release.

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

This template is an open-source, community-driven project. Improvements to
the template itself — refinements to the included CI configuration, Gradle
setup, or documentation — are welcome.

See [`CONTRIBUTING.md`][contributing url] for the submission process and
[`VOCABULARY.md`][vocabulary url] for the terminology used across Valkyrja.

Security Issues
---------------

If you discover a security vulnerability, please follow our
[disclosure procedure][security vulnerabilities url].

License
-------

This template is open-source software licensed under the
[MIT license][MIT license url]. See [`LICENSE.md`](./LICENSE.md).

[Valkyrja url]: https://valkyrja.io
[starter url]: https://github.com/valkyrjaio/valkyrja-starter-app-java
[repository naming url]: https://github.com/valkyrjaio/.github/blob/26.x/REPOSITORY_NAMING.md
[vocabulary url]: https://github.com/valkyrjaio/.github/blob/26.x/VOCABULARY.md
[contributing url]: https://github.com/valkyrjaio/.github/blob/26.x/CONTRIBUTING.md
[security vulnerabilities url]: https://github.com/valkyrjaio/.github/blob/26.x/SECURITY.md
[Versioning and Release Process url]: ./VERSIONING_AND_RELEASE_PROCESS.md
[semantic versioning url]: https://semver.org/
[MIT license url]: https://opensource.org/licenses/MIT
[license url]: ./LICENSE.md
