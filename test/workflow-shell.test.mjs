import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit runs the checker and build on the runner before starting the agent", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  const steps = source.split(/^---\r?$/m)[1].split("\nsteps:\n")[1];
  assert.ok(steps, "missing runner steps");
  for (const command of [
    "mkdir -p /tmp/gh-aw/agent",
    "jbang .github/scripts/audit-releases.java | tee /tmp/gh-aw/agent/release-audit.txt",
    "if grep -q '^UPDATE ' /tmp/gh-aw/agent/release-audit.txt; then",
    "jbang run build.java",
    "git diff --check",
    "Validation passed: site build and git diff --check.",
  ]) {
    assert.ok(steps.includes(command), `missing runner audit/validation command: ${command}`);
    assert.ok(lock.includes(command), `compiled workflow is missing: ${command}`);
  }
  const agent = lock.split("\n  agent:\n")[1]?.split(/\n  \w/)[0];
  assert.ok(agent, "missing agent job");
  assert.ok(agent.indexOf("name: Set up JBang") < agent.indexOf("name: Audit project releases"));
  assert.ok(agent.indexOf("name: Audit project releases") < agent.indexOf("name: Validate project updates"));
  assert.ok(agent.indexOf("name: Validate project updates") < agent.indexOf("awf --config"));
  // An explicit bash shell enables Actions' -e -o pipefail defaults.
  for (const step of ["Audit project releases", "Validate project updates"]) {
    const body = agent.split(`      - name: ${step}\n`)[1]?.split("\n      - name:")[0];
    assert.ok(body, `missing ${step} runner step`);
    assert.match(body, /^        shell: bash$/m);
  }
  assert.doesNotMatch(source, /Expose JBang|RUNNER_TOOL_CACHE/);
});

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
  for (const [command, identifier] of [
    ["cat /tmp/gh-aw/agent/release-audit.txt", "cat"],
    ["git status --short", "git status"],
    ["git diff --check", "git diff"],
    ["git diff", "git diff"],
  ]) {
    assert.ok(prompt.includes(`\`${command}\``), `prompt does not specify ${command}`);
    assert.ok(rules.includes(identifier), `source does not permit identifier ${identifier} for ${command}`);
    assert.ok(lock.includes(`--allow-tool shell(${identifier})`), `compiled workflow does not permit identifier ${identifier}`);
  }
  assert.deepEqual(rules, ["cat", "git status", "git diff"]);
  assert.doesNotMatch(lock, /--allow-tool shell\((?:jbang|java|command|cd)\)/);
  assert.match(prompt, /Do not rerun the checker or invoke Java or JBang in the sandbox/);
  assert.match(prompt, /confirm the report contains `Validation passed: site build and git diff --check\.`/);
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
