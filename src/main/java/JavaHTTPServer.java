import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaHTTPServer {

    public static void main(String[] args) throws IOException {
        int port = 5005;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            if (uri.equals("/")) {
                uri = "/index.html";
            }
            String filePath = "src/main/resources" + uri;
            Path file = Paths.get(filePath);
            if (Files.exists(file) && Files.isReadable(file)) {
                String response = new String(Files.readAllBytes(file));
                exchange.getResponseHeaders().set("Content-Type", getMimeType(file));
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } else {
                String response = "404 - Not Found";
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }

        private String getMimeType(Path file) {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(".html")) {
                return "text/html";
            } else if (fileName.endsWith(".css")) {
                return "text/css";
            } else if (fileName.endsWith(".js")) {
                return "application/javascript";
            } else {
                return "application/octet-stream";
            }
        }
    }
}
