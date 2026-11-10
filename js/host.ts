/**
 * Runs in the preview's browser, before estorm's preview page: renders the
 * text the IDE sends and hands the board to the page, as the VS Code
 * extension does in its extension host. The IDE calls estormShow(text, theme)
 * on load and whenever the text changes (EstormPreviewEditor.kt), so the
 * preview works without Node.js.
 */
import { layout, parseAll, svg, type Theme } from '@villev/estorm';
import type { ToPage } from '@villev/estorm/embed';

declare global {
  interface Window {
    estormShow(text: string, theme: Theme): void;
  }
}

const send = (msg: ToPage) => window.postMessage(msg, '*');

window.estormShow = (text, theme) => {
  try {
    const { board, errors } = parseAll(text);
    // Plain objects: a ParseError would lose its line on the way to the page.
    if (errors.length) send({ type: 'errors', errors: errors.map(({ line, message }) => ({ line, message })) });
    else send({ type: 'render', svg: svg(layout(board), { theme }) });
  } catch (e) {
    // A bug in estorm: show it rather than leave the last board looking current.
    send({ type: 'errors', errors: [{ line: 0, message: `cannot render: ${e instanceof Error ? e.message : e}` }] });
  }
};
