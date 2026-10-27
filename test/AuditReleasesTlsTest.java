import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

class AuditReleasesTlsTest {
  public static void main(String[] args) throws Exception {
    var field = AuditReleases.class.getDeclaredField("HTTP");
    field.setAccessible(true);
    HttpClient client = (HttpClient) field.get(null);
    if (args[1].equals("defaults")) {
      if (client.sslContext() != SSLContext.getDefault()) {
        throw new AssertionError("Default TLS context was replaced");
      }
      return;
    }

    KeyStore keys = KeyStore.getInstance("PKCS12");
    try (var input = Files.newInputStream(Path.of(args[0]))) {
      keys.load(input, "changeit".toCharArray());
    }
    var managers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    managers.init(keys, "changeit".toCharArray());
    var context = SSLContext.getInstance("TLS");
    context.init(managers.getKeyManagers(), null, null);
    var server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.setHttpsConfigurator(new HttpsConfigurator(context));
    server.createContext("/", exchange -> {
      byte[] body = "verified".getBytes(java.nio.charset.StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try (var output = exchange.getResponseBody()) {
        output.write(body);
      }
    });
    server.start();
    try {
      String host = args[1].equals("hostname") ? "127.0.0.1" : "localhost";
      var request = HttpRequest.newBuilder(URI.create("https://" + host + ":" + server.getAddress().getPort()))
        .timeout(java.time.Duration.ofSeconds(10)).build();
      try {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!args[1].equals("trusted") || response.statusCode() != 200 || !response.body().equals("verified")) {
          throw new AssertionError("Unexpected successful TLS connection");
        }
      } catch (SSLHandshakeException e) {
        if (args[1].equals("trusted")) {
          throw e;
        }
      }
    } finally {
      server.stop(0);
    }
  }
}
