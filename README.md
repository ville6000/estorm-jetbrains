# estorm for JetBrains IDEs

Support for [estorm](https://github.com/ville6000/estorm) boards (`.estorm`),
a plain-text notation for Event Storming, in IntelliJ IDEA, WebStorm, Rider,
PyCharm and the other JetBrains IDEs, 2026.2 or later. Not in Android Studio,
which lacks the IDE's language server support.

[![CI](https://github.com/ville6000/estorm-jetbrains/actions/workflows/ci.yml/badge.svg)](https://github.com/ville6000/estorm-jetbrains/actions/workflows/ci.yml)

It isn't on JetBrains Marketplace yet. Until then, build it (see
[Development](#development)) and install
`build/distributions/estorm-jetbrains-<version>.zip` with **Settings |
Plugins**, the gear icon, **Install Plugin from Disk…**

- Each part of a line coloured like the sticky it becomes
- Parse errors and modelling warnings as you type
- Every mention of the event under the caret highlighted
- Completion of keywords, and of events, aggregates, externals and actors
  already on the board
- Go to definition (<kbd>Ctrl/Cmd</kbd>+<kbd>B</kbd>) from an event, say in
  a `when` or `unless`, to the steps that produce it
- Rename (<kbd>Shift</kbd>+<kbd>F6</kbd>) of an event, aggregate, external
  or actor everywhere on the board
- Live preview of the board beside the text. Click a sticky or an error to
  select its line. Drag to pan; zoom with the toolbar, <kbd>Ctrl/Cmd</kbd> +
  wheel or a pinch; click a section's name to fit it. The buttons at the top
  right of the editor switch between the text, both and the preview.
- Line comments with `#` (<kbd>Ctrl/Cmd</kbd>+<kbd>/</kbd>), and matching
  brackets

## Node.js

Colours, comments, brackets and the preview are the plugin's own. Errors,
highlights, completion, go to definition and rename come from `estorm lsp`,
estorm's language server, which the plugin bundles and runs on Node.js 20 or
later: `node` on the `PATH`, or the one set in the settings. Without Node.js
the plugin says so once and the rest still works.

The server shows in the status bar's Language Services widget, which
restarts it.

## Colours

**Settings | Editor | Color Scheme | estorm** has a colour for each sticky.
By default they follow your colour scheme, as the VS Code extension's do:
commands like function declarations, aggregates like class names, events
like strings, and so on; hotspots are red and rules amber. The board's own
colours, as in estorm's browser editor:

| Colour   | Board                           |
| -------- | ------------------------------- |
| Command  | `#1864ab`                       |
| Event    | `#a33b0b`, background `#ffe8cc` |
| Schedule | `#6741d9`                       |
| External | italic                          |
| Hotspot  | `#c92a2a`                       |
| Rule     | `#946800`                       |
| Section  | bold                            |

The preview follows the editor's colour scheme: a light or dark board, and
the page around it in the IDE's colours.

## Settings

**Settings | Languages & Frameworks | estorm**:

| Setting        | Default | Does                                                                                                    |
| -------------- | ------- | ------------------------------------------------------------------------------------------------------- |
| Node.js        | empty   | The `node` that runs the bundled server; empty uses the one on the `PATH`                               |
| Server command | empty   | Runs another language server instead, in the project folder, e.g. `node /path/to/estorm/src/cli.ts lsp` |

## Development

Needs JDK 25 and Node.js 22.18 or later.

```sh
npm ci                # estorm and esbuild, for the bundled JavaScript
./gradlew runIde      # an IDE with the plugin, in a sandbox
./gradlew check       # tests: lexer, parity with estorm, editing, preview page, the server through the IDE's client
./gradlew buildPlugin # build/distributions/estorm-jetbrains-<version>.zip
./gradlew verifyPlugin -PverifyIdes=/Applications/WebStorm.app,/Applications/Rider.app  # against installed IDEs
npm run typecheck && npm run format:check
```

Without `-PverifyIdes`, `verifyPlugin` downloads the IDEs in
`build.gradle.kts` and checks against those, as CI does.

| Path                          | Does                                                                        |
| ----------------------------- | --------------------------------------------------------------------------- |
| `src/main/kotlin/…/lang/`     | file type, lexer (a port of estorm's `src/highlight.ts`), colours, brackets |
| `src/main/kotlin/…/lsp/`      | starts the bundled server with `node`                                       |
| `src/main/kotlin/…/preview/`  | the split editor, and estorm's preview page in a JCEF browser               |
| `js/server.ts`                | the bundled server, `build/js/server/server.mjs`, shipped in `server/`      |
| `js/host.ts`                  | renders boards in the preview's browser, so the preview needs no Node.js    |
| `js/tokens.mjs`               | estorm's own tokens for the test boards, which the lexer must match         |
| `src/test/resources/grammar/` | the test boards, the same as estorm-vscode's                                |

The lexer must colour every line like estorm's tokenizer: `TokenParityTest`
compares them on the test boards. When estorm changes `src/highlight.ts`,
port the change to `EstormTokenizer.kt`.

The preview page itself, `preview.js` and `preview.css`, comes from estorm
(`@villev/estorm/embed/`), and the VS Code extension shows the same one;
"Embedding the preview" in estorm's
[EDITORS.md](https://github.com/ville6000/estorm/blob/main/EDITORS.md#embedding-the-preview)
describes it.

## Releasing

1. Add the version's changes to `CHANGELOG.md` under `## <version>`. They
   become the Marketplace's "What's New".
2. Set `pluginVersion` in `gradle.properties`, commit as "Release
   &lt;version&gt;", tag `v<version>` and `git push --follow-tags`.

The tag runs `.github/workflows/publish.yml`. It checks the plugin and
publishes it to JetBrains Marketplace with the `PUBLISH_TOKEN` secret, a
token from your Marketplace profile. Then it creates a GitHub release with
the zip and the changelog section. Marketplace needs the plugin's first
version uploaded by hand.

Signing is optional, since Marketplace signs every plugin itself. To sign
it as well, add the `CERTIFICATE_CHAIN`, `PRIVATE_KEY` and
`PRIVATE_KEY_PASSWORD` secrets; see JetBrains'
[plugin signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html).

## License

[MIT](LICENSE)
