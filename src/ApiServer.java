package incidentflow;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Minimal HTTP API server for IncidentFlow.
 *
 * Endpoints:
 *   GET  /                              API info
 *   GET  /health                        Health check
 *   POST /api/compile                   Validate .iflow source  (body: {"source":"..."})
 *   POST /api/report                    Generate Markdown report (body: {"source":"..."})
 *   GET  /api/playbooks                 List .iflow files in the playbooks directory
 *   GET  /api/playbooks/{name}          Get a playbook's source
 *   GET  /api/playbooks/{name}/report   Generate report for a stored playbook
 */
public class ApiServer {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String DIM   = "\u001B[2m";

    private final HttpServer server;
    private final Path playbooksDir;
    private final int port;

    public ApiServer(int port, Path playbooksDir) throws IOException {
        this.port = port;
        this.playbooksDir = playbooksDir.toAbsolutePath().normalize();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }

    // ------------------------------------------------------------------ setup

    private void setupRoutes() {
        server.createContext("/health",        ex -> handle(ex, "GET",  this::onHealth));
        server.createContext("/api/compile",   ex -> handle(ex, "POST", this::onCompile));
        server.createContext("/api/report",    ex -> handle(ex, "POST", this::onReport));
        server.createContext("/api/playbooks", ex -> handlePlaybooks(ex));
        server.createContext("/",              ex -> handle(ex, "GET",  this::onRoot));
    }

    public void start() {
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println(CYAN + "  → " + RESET + "IncidentFlow API on "
                + BOLD + "http://localhost:" + port + RESET);
        System.out.println(DIM  + "     Press Ctrl+C to stop" + RESET);
    }

    public void stop() {
        server.stop(1);
    }

    // ------------------------------------------------------------------ routing

    @FunctionalInterface
    interface Handler { void handle(HttpExchange ex) throws IOException; }

    /** Dispatch a single-method context, returning 405 for wrong methods and 500 on errors. */
    private void handle(HttpExchange ex, String method, Handler h) throws IOException {
        if (ex.getRequestMethod().equals("OPTIONS")) {
            // CORS preflight
            ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }
        if (!ex.getRequestMethod().equals(method)) {
            respond(ex, 405, json("error", "Method not allowed"));
            return;
        }
        try {
            h.handle(ex);
        } catch (Exception e) {
            respond(ex, 500, json("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    // ------------------------------------------------------------------ GET /

    private void onRoot(HttpExchange ex) throws IOException {
        String body = "{\n"
            + "  \"name\": \"IncidentFlow API\",\n"
            + "  \"version\": \"" + Main.VERSION + "\",\n"
            + "  \"endpoints\": [\n"
            + "    { \"method\": \"GET\",  \"path\": \"/health\",                        \"description\": \"Health check\" },\n"
            + "    { \"method\": \"POST\", \"path\": \"/api/compile\",                   \"description\": \"Validate .iflow source (body: {source}) \" },\n"
            + "    { \"method\": \"POST\", \"path\": \"/api/report\",                    \"description\": \"Generate Markdown report (body: {source})\" },\n"
            + "    { \"method\": \"GET\",  \"path\": \"/api/playbooks\",                 \"description\": \"List stored .iflow playbooks\" },\n"
            + "    { \"method\": \"GET\",  \"path\": \"/api/playbooks/{name}\",          \"description\": \"Get a playbook's source\" },\n"
            + "    { \"method\": \"GET\",  \"path\": \"/api/playbooks/{name}/report\",   \"description\": \"Generate report for a stored playbook\" }\n"
            + "  ]\n"
            + "}";
        respond(ex, 200, body);
    }

    // ------------------------------------------------------------------ GET /health

    private void onHealth(HttpExchange ex) throws IOException {
        respond(ex, 200, json("status", "ok"));
    }

    // ------------------------------------------------------------------ POST /api/compile

    private void onCompile(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        String source;
        try {
            source = extractString(body, "source");
        } catch (IOException e) {
            respond(ex, 400, json("error", e.getMessage()));
            return;
        }

        Compiler.Result r = Compiler.fromSource(source);
        respond(ex, r.hasErrors() ? 422 : 200,
                jsonErrors(!r.hasErrors(), r.syntaxErrors, r.semanticErrors));
    }

    // ------------------------------------------------------------------ POST /api/report

    private void onReport(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        String source;
        try {
            source = extractString(body, "source");
        } catch (IOException e) {
            respond(ex, 400, json("error", e.getMessage()));
            return;
        }

        Compiler.Result r = Compiler.fromSource(source);
        if (r.hasErrors()) {
            respond(ex, 422, jsonErrors(false, r.syntaxErrors, r.semanticErrors));
            return;
        }

        String report = ReportGenerator.generate(r.tree);
        respond(ex, 200, "{\"report\": \"" + escapeJson(report) + "\"}");
    }

    // ------------------------------------------------------------------ /api/playbooks/**

    private void handlePlaybooks(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            return;
        }
        if (!ex.getRequestMethod().equals("GET")) {
            respond(ex, 405, json("error", "Method not allowed"));
            return;
        }

        // Path: /api/playbooks[/{name}[/report]]
        String[] parts = ex.getRequestURI().getPath().split("/");
        // parts: ["", "api", "playbooks", name?, "report"?]

        try {
            if (parts.length <= 3 || parts[3].isBlank()) {
                onListPlaybooks(ex);
            } else {
                String name = parts[3];
                if (parts.length > 4 && parts[4].equals("report")) {
                    onPlaybookReport(ex, name);
                } else {
                    onGetPlaybook(ex, name);
                }
            }
        } catch (Exception e) {
            respond(ex, 500, json("error", e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void onListPlaybooks(HttpExchange ex) throws IOException {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(playbooksDir, "*.iflow")) {
            for (Path p : ds) {
                names.add(p.getFileName().toString().replace(".iflow", ""));
            }
        }
        Collections.sort(names);

        StringBuilder sb = new StringBuilder("{\"playbooks\": [");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapeJson(names.get(i))).append("\"");
        }
        sb.append("]}");
        respond(ex, 200, sb.toString());
    }

    private void onGetPlaybook(HttpExchange ex, String name) throws IOException {
        Path file = safeResolve(playbooksDir, name + ".iflow");
        if (file == null || !Files.exists(file)) {
            respond(ex, 404, json("error", "Playbook not found: " + name));
            return;
        }
        String source = Files.readString(file, StandardCharsets.UTF_8);
        respond(ex, 200,
                "{\"name\": \"" + escapeJson(name) + "\", "
                + "\"source\": \"" + escapeJson(source) + "\"}");
    }

    private void onPlaybookReport(HttpExchange ex, String name) throws IOException {
        Path file = safeResolve(playbooksDir, name + ".iflow");
        if (file == null || !Files.exists(file)) {
            respond(ex, 404, json("error", "Playbook not found: " + name));
            return;
        }
        Compiler.Result r = Compiler.fromFile(file.toString());
        if (r.hasErrors()) {
            respond(ex, 422, jsonErrors(false, r.syntaxErrors, r.semanticErrors));
            return;
        }
        String report = ReportGenerator.generate(r.tree);
        respond(ex, 200,
                "{\"name\": \"" + escapeJson(name) + "\", "
                + "\"report\": \"" + escapeJson(report) + "\"}");
    }

    // ------------------------------------------------------------------ path safety

    /** Resolve a filename under baseDir, returning null if it escapes the directory. */
    private static Path safeResolve(Path baseDir, String filename) {
        // Reject path separators to prevent directory traversal
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return null;
        }
        Path resolved = baseDir.resolve(filename).normalize();
        if (!resolved.startsWith(baseDir)) return null;
        return resolved;
    }

    // ------------------------------------------------------------------ HTTP helpers

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void respond(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", "application/json; charset=utf-8");
        h.set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ------------------------------------------------------------------ JSON helpers

    private static String json(String key, String value) {
        return "{\"" + escapeJson(key) + "\": \"" + escapeJson(value) + "\"}";
    }

    private static String jsonErrors(boolean valid, List<String> syntax, List<String> semantic) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"valid\": ").append(valid).append(", ");
        sb.append("\"syntax_errors\": [");
        appendList(sb, syntax);
        sb.append("], \"semantic_errors\": [");
        appendList(sb, semantic);
        sb.append("]}");
        return sb.toString();
    }

    private static void appendList(StringBuilder sb, List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
    }

    static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Extract a JSON string field from a simple flat JSON object.
     * Handles standard JSON escape sequences inside the value.
     */
    static String extractString(String json, String key) throws IOException {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) throw new IOException("Missing field: " + key);

        int colonIdx = json.indexOf(":", keyIdx + search.length());
        if (colonIdx == -1) throw new IOException("Malformed JSON: missing ':' after \"" + key + "\"");

        // Skip whitespace to find the opening quote
        int quoteStart = -1;
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') { quoteStart = i; break; }
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                throw new IOException("Field \"" + key + "\" must be a string (got '" + c + "')");
            }
        }
        if (quoteStart == -1) throw new IOException("Missing string value for field: " + key);

        StringBuilder sb = new StringBuilder();
        int i = quoteStart + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"':  sb.append('"');  i += 2; break;
                    case '\\': sb.append('\\'); i += 2; break;
                    case 'n':  sb.append('\n'); i += 2; break;
                    case 'r':  sb.append('\r'); i += 2; break;
                    case 't':  sb.append('\t'); i += 2; break;
                    default:   sb.append(c);    i++;    break;
                }
            } else if (c == '"') {
                break;  // end of string
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
}
