import assert from "node:assert/strict";
import { readdir, readFile, stat } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);
const site = new URL("_site/", root);
const read = (path) => readFile(new URL(path, site), "utf8");
const projects = (await readdir(root))
  .filter((file) => file.endsWith(".md") && file !== "README.md")
  .map((file) => file.replace(/\.md$/, ".html"));

test("Pagefind indexes exactly the project-page count", async () => {
  const entry = JSON.parse(await read("pagefind/pagefind-entry.json"));
  const pkg = JSON.parse(await readFile(new URL("package.json", root), "utf8"));
  assert.equal(entry.version, pkg.devDependencies.pagefind);
  assert.equal(entry.languages.en.page_count, projects.length);
});

test("project content is indexed, while catalogue, fragments and tags are not", async () => {
  for (const file of await readdir(site)) {
    if (!file.endsWith(".html")) continue;
    const html = await read(file);
    assert.equal(html.includes("data-pagefind-body"), projects.includes(file), file);
    if (projects.includes(file)) {
      assert.match(html, /<h1 data-pagefind-meta="title">/, file);
      assert.match(html, /<img src="[^"]+" alt="[^"]+" data-pagefind-meta="thumbnail:[^"]+">/, file);
      assert.match(html, /data-pagefind-ignore/, file);
      assert.match(html, /Status:/, file);
      assert.match(html, /Java Version:/, file);
    }
  }
});

test("search components and browser assets are deployed with the catalogue", async () => {
  const html = await read("index.html");
  for (const component of ["input", "summary", "results"]) {
    assert.match(html, new RegExp(`<pagefind-${component}[\\s>]`));
  }
  assert.match(html, /class="pf-result-image" src="\{\{ meta\.thumbnail \}\}"/);
  assert.doesNotMatch(html, /class="pf-result-excerpt"/);
  assert.match(html, /data-search-clear/);
  assert.match(html, /data-search-focus/);
  assert.match(html, /pf-input-clear"\)\?\.click\(\)/);
  for (const asset of ["pagefind-component-ui.js", "pagefind-component-ui.css", "pagefind.js"]) {
    assert.ok((await stat(new URL(`pagefind/${asset}`, site))).size > 0);
  }
  for (const file of ["index.html", ...projects]) {
    assert.match(await read(file), /type="module" src="pagefind\/pagefind-component-ui\.js"/, file);
  }
});
