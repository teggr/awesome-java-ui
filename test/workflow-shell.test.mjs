import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit instructs the agent to use standalone allowlisted shell commands", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const prompt = source.split(/^---\r?$/m)[2];
  assert.match(prompt, /Run only the following invocations, each in a separate tool call:/);
  assert.match(prompt, /Do not add pipes \(including `tail`\), redirections, or command chaining \(`&&`, `;`\)/);
  assert.match(prompt, /Check each command's exit status before continuing/);
});

test("release audit permits every required Copilot command identifier without unrestricted shell access", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  const bash = source.match(/^  bash:\n((?:    - .+\n)+)/m)?.[1];
  assert.ok(bash, "missing restricted Bash allowlist");
  const rules = [...bash.matchAll(/^    - "([^"]+)"$/gm)].map(match => match[1]);
  const prompt = source.split(/^---\r?$/m)[2];
  // Copilot 1.0.96 reported identifier "jbang" for the denied audit invocation.
  for (const [command, identifier] of [
    ["cd", "cd"],
    ["command -v jbang", "command"],
    ["jbang .github/scripts/audit-releases.java", "jbang"],
    ["git status --short", "git status"],
    ["git diff --check", "git diff"],
    ["git diff", "git diff"],
    ["jbang run build.java", "jbang"],
  ]) {
    assert.ok(prompt.includes(`\`${command}\``), `prompt does not specify ${command}`);
    assert.ok(rules.includes(identifier), `source does not permit identifier ${identifier} for ${command}`);
    assert.ok(lock.includes(`--allow-tool shell(${identifier})`), `compiled workflow does not permit identifier ${identifier}`);
  }
  assert.deepEqual(rules, ["cd", "command", "jbang", "git status", "git diff"]);
  assert.doesNotMatch(bash, /^    - "(?:\*|:\*)"$/m);
  assert.doesNotMatch(lock, /--allow-all-tools|--allow-tool shell(?:\s|$)/);
});

test("candidate discovery permits the identifiers for its evidence and date commands", async () => {
  const source = await readFile(new URL(".github/workflows/discover-candidates.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/discover-candidates.lock.yml", root), "utf8");
  assert.match(source, /bash: \["cat", "date"\]/);
  for (const identifier of ["cat", "date"]) {
    assert.ok(lock.includes(`--allow-tool shell(${identifier})`), `compiled discovery workflow does not permit ${identifier}`);
  }
  assert.match(source, /`date -u \+%F`/);
});
