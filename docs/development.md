# Development Guide

This document covers project-facing development practices for QSS Toolkit.

## Compatibility

- Mainline compatibility currently targets IntelliJ Platform builds `242` through `262.*`.
- The plugin is compiled for Java 21, so build `242` is the practical minimum unless the Java and Kotlin targets are lowered.
- Keep dependencies platform-neutral. Do not add product-specific dependencies unless the feature truly needs them.
- Avoid bundling libraries that the IntelliJ Platform already provides, especially Kotlin runtime or coroutine libraries.

## Code Quality

- Keep UI code, validation logic, syntax data, and persistence behavior separated.
- UI handlers should stay thin: collect input, call focused model/service logic, then refresh the view.
- Do not put large feature workflows inside anonymous Swing listeners.
- Preserve existing user state. Palette and color persistence changes must load old saved data.
- Prefer small, named methods over deeply nested conditionals.
- Avoid broad refactors when fixing a narrow bug.

## QSS Validation

- Be conservative with errors. A false red error in a valid `.qss` file is worse than missing a niche invalid case.
- Validate complete values for multi-token properties such as `border`.
- Use warnings or weak warnings when Qt behavior depends on widget, platform, style, or version.
- Add a regression test for each manually reported false positive or parser recovery issue.

## Color Folder Tool Window

- Treat color folders as groups and colors as child items.
- Creation flows should avoid required naming popups. Generate sensible default names and allow rename later.
- Drag/drop behavior should update the model first, then refresh the UI.
- Keep frequent actions such as copy and add non-modal.
- Keep editor context-menu color insertion backed by the same format/parser logic as the color folder tool window.
- Reuse shared presentation helpers, such as color swatch icons, when the same visual appears in multiple UI surfaces.
- Palette manager mutations from actions outside the tool window must notify open UI views through the manager listener API.

## Testing

Run the standard check before considering a code change complete:

```powershell
.\gradlew.bat cleanTest test buildPlugin verifyPluginStructure verifyPluginProjectConfiguration --console=plain --no-daemon
```

For compatibility-sensitive changes, also run JetBrains Plugin Verifier against the minimum supported IDE build and the newest target or EAP build being investigated.

Use `docs/manual-qss-testing.md` for manual PyCharm or JetBrains IDE testing.

## Metadata

Update user-visible metadata when behavior changes:

- `build.gradle.kts` change notes
- `src/main/resources/META-INF/plugin.xml`

Use an `Upcoming - Version TBD` section when the final release version has not been chosen yet.

## Commits

Use clear standard commit messages, for example:

- `fix: avoid false border warning for palette colors`
- `feat: add color folder drag and drop`
- `test: cover QSS validation smoke samples`
- `docs: clarify manual QSS testing workflow`

Commit bodies should explain what changed, why it changed, compatibility impact, and what verification was run. Do not stage generated verifier reports, local IDE files, build outputs, or unrelated untracked files.
