import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit uses built-in Copilot authentication without a personal token", async () => {
  const source = await readFile(new URL(".github/workflows/audit-project-releases.md", root), "utf8");
  const lock = await readFile(new URL(".github/workflows/audit-project-releases.lock.yml", root), "utf8");
  assert.match(source, /^  copilot-requests: write$/m);
  const manifest = JSON.parse(lock.match(/^# gh-aw-manifest: (.+)$/m)[1]);
  assert.ok(!manifest.secrets.includes("COPILOT_GITHUB_TOKEN"));
  assert.doesNotMatch(lock, /secrets\.COPILOT_GITHUB_TOKEN|validate_multi_secret\.sh.*COPILOT_GITHUB_TOKEN/);
  for (const job of ["agent", "detection"]) {
    const body = lock.split(`\n  ${job}:\n`)[1]?.split(/\n  \w/)[0];
    assert.ok(body, `missing ${job} job`);
    assert.match(body, /^      copilot-requests: write$/m);
    assert.match(body, /COPILOT_GITHUB_TOKEN: \$\{\{ github\.token \}\}/);
  }
});
