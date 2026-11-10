/**
 * The bundled language server, build/js/server/server.mjs, which the plugin
 * runs with node. Calls estorm's server directly rather than its command
 * line, the same server as `estorm lsp`.
 */
import { serveStdio } from '@villev/estorm/lsp';

serveStdio();
