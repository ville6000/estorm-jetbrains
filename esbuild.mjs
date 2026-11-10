// Builds the JavaScript the plugin ships into build/js/, which Gradle
// packages (build.gradle.kts):
//   server/server.mjs   the language server, run with node from the plugin directory
//   preview/host.js     renders boards in the preview's browser
//   preview/preview.js, preview/preview.css   estorm's preview page, copied
import { cpSync, rmSync } from 'node:fs';
import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import * as esbuild from 'esbuild';

const out = 'build/js';
rmSync(out, { recursive: true, force: true });

const common = { bundle: true, logLevel: 'info' };
await Promise.all([
  esbuild.build({
    ...common,
    entryPoints: ['js/server.ts'],
    outfile: join(out, 'server/server.mjs'),
    platform: 'node',
    format: 'esm',
    target: 'node20',
  }),
  esbuild.build({
    ...common,
    entryPoints: ['js/host.ts'],
    outfile: join(out, 'preview/host.js'),
    platform: 'browser',
    format: 'iife',
    target: 'es2022',
  }),
]);

// The page every editor shows, built by estorm (see its EDITORS.md).
const embed = dirname(createRequire(import.meta.url).resolve('@villev/estorm/embed/preview.js'));
for (const file of ['preview.js', 'preview.css']) cpSync(join(embed, file), join(out, 'preview', file));
