# Manual QSS Testing

Use this checklist when testing a local plugin build in PyCharm or another JetBrains IDE.

## Install A Local Build

1. Build the plugin:

   ```powershell
   .\gradlew.bat cleanTest test buildPlugin verifyPluginStructure verifyPluginProjectConfiguration --console=plain --no-daemon
   ```

2. In the IDE, open **Settings | Plugins | Gear icon | Install Plugin from Disk...**.
3. Select the generated plugin ZIP:

   ```text
   build/distributions/qss-toolkit-2.0.1.zip
   ```

4. Restart the IDE when prompted.

The version is still `2.0.1` until the release version is bumped.

## What To Test

Open a `.qss` file and classify any suspicious result as one of these:

- **False positive:** Qt accepts the syntax, but the plugin marks it as an error.
- **False negative:** Qt rejects the syntax, but the plugin does not mark it as an error.
- **Completion gap:** Valid Qt syntax is not suggested by completion.
- **Wrong suggestion:** Completion suggests syntax that Qt does not support.
- **Parser/highlight bug:** Syntax coloring, selector parsing, or brace recovery behaves incorrectly.

Focus on realistic QSS that users already write, especially Qt color functions, widget subcontrols, pseudo-states, dynamic `qproperty-*` declarations, and leading-hyphen Qt properties such as `-qt-background-role`.

## Report Format

For each issue, capture:

- The smallest `.qss` snippet that reproduces it.
- Whether the IDE shows a red error, warning, weak warning, wrong highlighting, or missing completion.
- The exact tooltip/error text, if any.
- What should happen instead.
- The Qt version or documentation link that proves the syntax is valid or invalid.
- The IDE build, for example `PyCharm 2026.2 EAP 262.x`.

Use this shape when sending a result back:

```text
Snippet:
QWidget {
    border: 1px solid palette(WindowText);
}

Actual:
Weak warning: Incomplete border declaration.

Expected:
No warning. Qt accepts this as a complete border declaration.

IDE:
PyCharm 2026.2 EAP 262.x
```

## Smoke-Test Files

Use `src/test/testData/qss/manual-smoke/valid-current-qt.qss` and
`src/test/testData/qss/manual-smoke/common-existing.qss` as starting points.

The plugin is a static editor helper, not a full Qt runtime validator. If Qt accepts a property only on a specific widget or Qt version, include that context with the snippet.

When a manual issue is confirmed, add the snippet to an automated regression test before or alongside the fix.
