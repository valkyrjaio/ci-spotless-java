# AGENTS.md

**ci-spotless-java — shared Spotless configuration** — Valkyrja **Java** port.

Before doing any work in this repo, read **both** canonical agent guides:

1. **Cross-language** — https://github.com/valkyrjaio/architecture/blob/26.x/AGENTS.md
2. **Java-specific** — https://github.com/valkyrjaio/architecture/blob/26.x/java/AGENTS.md

They define the architecture, naming, testing, 100% coverage, CI, and the
branch/commit/push/PR workflow this repo follows.

## What this repo holds

This package holds the copyright header text, and each Valkyrja Java repository
states only its own package name. `COPYRIGHT_HEADER.md` in the `.github`
repository specifies the text, and it maps each repository to its package name.

Warning: a change to the notice changes every file in every consuming
repository. Spotless rewrites a header that differs by one character, so read
`CopyrightHeaderTest` before you change `CopyrightHeader`. That test asserts each
rendering against a literal, and the literal is what the repositories must keep
producing.
