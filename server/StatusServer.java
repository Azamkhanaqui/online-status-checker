import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class StatusServer {
    static final int PORT = 5000;
    static final int WEB_PORT = 8080;
    static final int TIMEOUT_MS = 5000;
    static Connection db;
    static volatile String lastStatus = "offline";

    public static void main(String[] args) throws Exception {
        db = DriverManager.getConnection("jdbc:sqlite:status.db");
        try (Statement st = db.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS status_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "status TEXT NOT NULL, "
                    + "changed_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        }

        HttpServer web = HttpServer.create(new InetSocketAddress(WEB_PORT), 0);
        web.createContext("/status", ex -> send(ex, "text/plain", lastStatus));
        web.createContext("/", ex -> send(ex, "text/html; charset=utf-8",
                new String(Files.readAllBytes(Paths.get("index.html")), StandardCharsets.UTF_8)));
        web.start();
        System.out.println("Web page at http://localhost:" + WEB_PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                try (Socket client = server.accept()) {
                    client.setSoTimeout(TIMEOUT_MS);
                    setStatus("online");

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));

                    while (in.readLine() != null) {
                        System.out.println("heartbeat received");
                    }
                } catch (SocketTimeoutException e) {
                    // no heartbeat within the timeout
                } catch (IOException e) {
                    // connection dropped abruptly
                }
                setStatus("offline");
            }
        }
    }

    static void send(HttpExchange ex, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", type);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream o = ex.getResponseBody()) {
            o.write(bytes);
        }
    }

    static void setStatus(String status) {
        System.out.println(status);
        if (status.equals(lastStatus)) return;   // only log real changes
        lastStatus = status;
        try (PreparedStatement ps = db.prepareStatement(
                "INSERT INTO status_log (status) VALUES (?)")) {
            ps.setString(1, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }
}