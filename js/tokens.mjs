// Writes build/golden/tokens.txt: how estorm's own tokenizer (its
// src/highlight.ts, which the language server and the browser editor use)
// splits each line of src/test/resources/grammar/*.estorm. TokenParityTest
// checks the Kotlin lexer against it, in the same format: per file, a
// "== <file>" line, then "<line>: <kind> <from>-<to> ..." per source line.
import { mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';

// tokenize() isn't in the package's exports, so it is imported by path.
const dist = dirname(createRequire(import.meta.url).resolve('@villev/estorm'));
const { tokenize } = await import(pathToFileURL(join(dist, 'highlight.js')).href);

const dir = 'src/test/resources/grammar';
const out = [];
for (const file of readdirSync(dir)
  .filter((f) => f.endsWith('.estorm'))
  .sort()) {
  out.push(`== ${file}`);
  readFileSync(join(dir, file), 'utf8')
    .split(/\r?\n/)
    .forEach((line, i) => {
      const tokens = tokenize(line).map((t) => ` ${t.kind} ${t.from}-${t.to}`);
      out.push(`${i + 1}:${tokens.join('')}`);
    });
}
mkdirSync('build/golden', { recursive: true });
writeFileSync('build/golden/tokens.txt', out.join('\n') + '\n');
