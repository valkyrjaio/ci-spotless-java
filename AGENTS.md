# AGENTS.md

**ci-spotless-java — shared Spotless configuration** — Valkyrja **Java** port.

Before doing any work in this repo, read **both** canonical agent guides:

1. **Cross-language** — https://github.com/valkyrjaio/architecture/blob/26.x/AGENTS.md
2. **Java-specific** — https://github.com/valkyrjaio/architecture/blob/26.x/java/AGENTS.md

They define the architecture, naming, testing, 100% coverage, CI, and the
branch/commit/push/PR workflow this repo follows.

## What this repo holds

This package is a Gradle plugin that configures Spotless for each Valkyrja Java
repository. A repository states its package name and its target paths, and the
plugin holds every other rule. `COPYRIGHT_HEADER.md` in the `.github` repository
specifies the header text, and it maps each repository to its package name.

Warning: a change to the notice changes every file in every consuming
repository. Spotless rewrites a header that differs by one character, so read
`CopyrightHeaderTest` before you change `CopyrightHeader`. That test asserts each
rendering against a literal, and the literal is what the repositories must keep
producing.

## The plugin id and the two registries

The plugin id is `io.valkyrja.ci-spotless`. `PACKAGE_NAMING.md` in the
`architecture` repository states the rule that builds it.

Warning: a plugin id is permanent. It becomes the group id of the plugin marker,
and neither the Gradle Plugin Portal nor Maven Central releases a published
name. Read that document before you name a second CI tool plugin.

Two registries hold this package, and each holds a different part:

- **The Gradle Plugin Portal** holds the plugin marker and the jar. The Portal is
  Gradle's default plugin repository, so a consuming build resolves the plugin
  with no `pluginManagement` block.
- **Maven Central** holds the jar alone. A `buildscript { classpath(...) }`
  consumer and a dependency scanner resolve the jar. Neither reads the marker.

The root `build.gradle.kts` states this split, and `release-new-version.yml`
calls one publish workflow for each registry.

## This repository cannot apply its own plugin

Gradle resolves a plugin before it evaluates the build script, so a build here
that applied `io.valkyrja.ci-spotless` would ask for the artifact that the same
build produces. This repository therefore states its own Spotless configuration
inline, and it is the one copy of the header that this package does not remove.

`OwnSpotlessConfigTest` is what makes that copy safe. It compares the inline
header against what `CopyrightHeader` builds, so the copy cannot drift in
silence.
