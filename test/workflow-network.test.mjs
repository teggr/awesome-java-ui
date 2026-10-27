import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit does not need direct GitHub API access inside the firewall", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  const allowed = source.match(/^network:\n  allowed:\n((?:    - .+\n)+)/m)?.[1];
  assert.ok(allowed, "missing explicit network policy");
  assert.doesNotMatch(allowed, /^    - api\.github\.com$/m);
  assert.doesNotMatch(allowed, /^    - (?:["']?\*["']?|github\.com)$/m);

  const configLine = lock.split("\n").find(line => line.includes("awf-config.schema.json"));
  assert.ok(configLine, "missing compiled firewall configuration");
  // The compiler escapes JSON for a shell double-quoted string.
  const escapedConfig = configLine.match(/printf '%s\\n' "(.+)" > /)?.[1];
  assert.ok(escapedConfig, "missing shell-quoted firewall JSON");
  const config = escapedConfig.replace(/\\(["$`\\])/g, "$1");
  const domainsJson = config.match(/"allowDomains":(\[[^\]]*\])/)?.[1];
  assert.ok(domainsJson, "missing compiled domain allowlist");
  const domains = JSON.parse(domainsJson);
  assert.ok(!domains.includes("api.github.com"),
    "release-source HTTP calls belong in runner steps, not the sandbox");
  assert.ok(domains.includes("repo.maven.apache.org"),
    "Maven metadata and build dependencies must remain reachable");
});

for (const workflow of ["audit-project-releases", "discover-candidates"]) {
  test(`${workflow} is compiled for real safe outputs, not diagnostic staging`, async () => {
    const lock = await readFile(new URL(`.github/workflows/${workflow}.lock.yml`, root), "utf8");
    const metadata = JSON.parse(lock.match(/^# gh-aw-metadata: (.+)$/m)[1]);
    assert.notEqual(metadata.dry_run, true);
    assert.doesNotMatch(lock, /GH_AW_SAFE_OUTPUTS_STAGED: ["']?true/);
    const outputs = lock.split("\n  safe_outputs:\n")[1]?.split(/\n  \w/)[0];
    assert.ok(outputs, "missing safe-output job");
    if (workflow === "audit-project-releases") {
      assert.match(outputs, /^      contents: write$/m);
      assert.match(outputs, /^      pull-requests: write$/m);
    } else {
      assert.match(outputs, /^      issues: write$/m);
    }
  });
}
