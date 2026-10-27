import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit exposes the JBang launcher and JAR through the sandbox tool cache", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  const setup = source.match(/^  - name: Expose JBang to the agent sandbox\n    run: \|\n((?:      .+\n)+)/m)?.[1];
  assert.ok(setup, "missing sandbox-visible JBang setup");
  for (const command of [
    'mkdir -p "$RUNNER_TOOL_CACHE/jbang/bin"',
    'cp -L "$HOME/.jbang/bin/jbang" "$HOME/.jbang/bin/jbang.jar" "$RUNNER_TOOL_CACHE/jbang/bin/"',
    '"$RUNNER_TOOL_CACHE/jbang/bin/jbang" version',
  ]) {
    assert.ok(setup.includes(command), `source does not stage and verify JBang: ${command}`);
    assert.ok(lock.includes(command), `compiled workflow does not stage and verify JBang: ${command}`);
  }
  assert.ok(source.indexOf("name: Set up JBang") < source.indexOf("name: Expose JBang"));
  const agent = lock.split("\n  agent:\n")[1]?.split(/\n  \w/)[0];
  assert.ok(agent, "missing agent job");
  assert.ok(agent.indexOf("name: Set up JBang") < agent.indexOf("name: Expose JBang"));
  assert.ok(agent.indexOf("name: Expose JBang") < agent.indexOf("awf --config"));
  assert.ok(agent.includes('GH_AW_TOOL_CACHE="$RUNNER_TOOL_CACHE"'));
  assert.ok(agent.includes('find "$GH_AW_TOOL_CACHE" -maxdepth 5 -type d -name bin'));
  assert.ok(agent.includes('export PATH="$PATH${GH_AW_TOOL_BINS:+:}$GH_AW_TOOL_BINS"'));
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
