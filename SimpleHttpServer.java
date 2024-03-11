package riskcalculator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(4567), 0);
        server.createContext("/process", new FileProcessHandler());
        server.start();
        System.out.println("Server started on port 4567");
    }

    static class FileProcessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream requestBody = exchange.getRequestBody();
                    InputStreamReader isr = new InputStreamReader(requestBody);
                    BufferedReader br = new BufferedReader(isr)) {

                    // Read the content of the file from the request
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    // Log the content (you can replace this with your processing logic)
                    System.out.println("Received file content:\n" + content);

                    // Respond with a success message
                    String response = "{\"message\": \"File received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1); // Internal Server Error
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}

