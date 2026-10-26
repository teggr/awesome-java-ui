import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit instructs the agent to use standalone allowlisted shell commands", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const prompt = source.split(/^---\r?$/m)[2];
  assert.match(prompt, /Run each shell command exactly as listed in the Bash allowlist, in a separate tool call\./);
  assert.match(prompt, /Do not add pipes \(including `tail`\), redirections, or command chaining \(`&&`, `;`\)/);
  assert.match(prompt, /Check each command's exit status before continuing/);
});

test("release audit permits shell navigation and JBang diagnostics without unrestricted shell access", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  const bash = source.match(/^  bash:\n((?:    - .+\n)+)/m)?.[1];
  assert.ok(bash, "missing restricted Bash allowlist");
  for (const command of [
    "cd",
    "command -v jbang",
    "jbang .github/scripts/audit-releases.java",
    "git status --short",
    "git diff --check",
    "git diff",
    "jbang run build.java",
  ]) {
    assert.ok(bash.includes(`    - "${command}"\n`), `source does not permit ${command}`);
    assert.ok(lock.includes(`--allow-tool shell(${command})`), `compiled workflow does not permit ${command}`);
  }
  assert.doesNotMatch(bash, /^    - "(?:\*|:\*)"$/m);
  assert.doesNotMatch(lock, /--allow-all-tools|--allow-tool shell(?:\s|$)/);
});
