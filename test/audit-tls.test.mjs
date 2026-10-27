import assert from "node:assert/strict";
import { spawnSync } from "node:child_process";
import { mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import test from "node:test";

const root = new URL("../", import.meta.url);

test("release audit trusts the sandbox CA without disabling TLS verification", async () => {
  const directory = await mkdtemp(join(tmpdir(), "audit-tls-"));
  const run = (command, args, env = process.env) => {
    const result = spawnSync(command, args, { env, encoding: "utf8", timeout: 60_000 });
    assert.equal(result.status, 0, `${command}: ${result.error ?? ""}\n${result.stdout}\n${result.stderr}`);
  };
  try {
    const keys = join(directory, "server.p12");
    const certificate = join(directory, "ca.pem");
    run("keytool", ["-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keystore", keys,
      "-storetype", "PKCS12", "-storepass", "changeit", "-dname", "CN=localhost",
      "-ext", "SAN=dns:localhost", "-validity", "1", "-noprompt"]);
    run("keytool", ["-exportcert", "-rfc", "-alias", "server", "-keystore", keys,
      "-storepass", "changeit", "-file", certificate]);
    run("javac", ["-d", directory,
      fileURLToPath(new URL(".github/scripts/audit-releases.java", root)),
      fileURLToPath(new URL("test/AuditReleasesTlsTest.java", root))]);
    const env = { ...process.env };
    delete env.SSL_CERT_FILE;
    for (const mode of ["defaults", "untrusted"]) {
      run("java", ["-cp", directory, "AuditReleasesTlsTest", keys, mode], env);
    }
    for (const mode of ["trusted", "hostname"]) {
      run("java", ["-cp", directory, "AuditReleasesTlsTest", keys, mode],
        { ...env, SSL_CERT_FILE: certificate });
    }
    const invalid = join(directory, "invalid.pem");
    await writeFile(invalid, "not a certificate");
    for (const path of [invalid, join(directory, "missing.pem")]) {
      const result = spawnSync("java", ["-cp", directory, "AuditReleasesTlsTest", keys, "defaults"],
        { env: { ...env, SSL_CERT_FILE: path }, encoding: "utf8", timeout: 30_000 });
      assert.equal(result.status, 1);
      assert.match(result.stderr, /Could not configure release-audit TLS trust/);
    }
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
});
