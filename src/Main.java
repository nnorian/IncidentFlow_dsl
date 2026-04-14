package incidentflow;

import org.antlr.v4.gui.Trees;
import javax.swing.JFrame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;

/**
 * IncidentFlow CLI entry point.
 *
 * Responsibilities: parse arguments, route to the correct command, print results.
 * All compilation and report logic lives in Compiler and ReportGenerator.
 *
 * Usage:
 *   iflow wizard
 *   iflow check  <file.iflow>
 *   iflow report <file.iflow> [-o out.md]
 *   iflow tree   <file.iflow>
 */
public class Main {

    static final String VERSION = "1.0.0";

    static final String RESET  = "\u001B[0m";
    static final String BOLD   = "\u001B[1m";
    static final String RED    = "\u001B[31m";
    static final String GREEN  = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String CYAN   = "\u001B[36m";
    static final String DIM    = "\u001B[2m";

    // ------------------------------------------------------------------ entry

    public static void main(String[] args) throws IOException {
        if (args.length == 0) { printUsage(); System.exit(1); }

        switch (args[0]) {
            case "--version": case "-v": System.out.println("iflow " + VERSION); return;
            case "--help":    case "-h": case "help": printUsage(); return;
            case "wizard":    new Wizard().run(); return;
            case "serve":     runServe(args); return;
        }

        if (args.length < 2) {
            err("No input file specified.");
            System.out.println();
            printUsage();
            System.exit(1);
        }

        String command = args[0];
        String file    = args[1];

        if (!Files.exists(Paths.get(file))) {
            err("File not found: " + file);
            System.exit(1);
        }

        switch (command) {
            case "check":  runCheck(file);        break;
            case "report": runReport(file, args); break;
            case "tree":   runTree(file);         break;
            default:
                err("Unknown command: " + BOLD + command + RESET);
                System.out.println();
                printUsage();
                System.exit(1);
        }
    }

    // ------------------------------------------------------------------ commands

    private static void runCheck(String file) throws IOException {
        info("Checking " + DIM + file + RESET + " ...");

        Compiler.Result r = Compiler.fromFile(file);

        if (r.hasSyntaxErrors()) {
            for (String e : r.syntaxErrors)   err(e);
            System.exit(2);
        }
        if (r.hasSemanticErrors()) {
            for (String e : r.semanticErrors) err(e);
            System.exit(3);
        }

        ok("No errors found.");
    }

    private static void runReport(String file, String[] args) throws IOException {
        info("Checking " + DIM + file + RESET + " ...");

        Compiler.Result r = Compiler.fromFile(file);

        if (r.hasSyntaxErrors()) {
            for (String e : r.syntaxErrors)   err(e);
            System.exit(2);
        }
        if (r.hasSemanticErrors()) {
            for (String e : r.semanticErrors) err(e);
            System.exit(3);
        }

        ok("Validation passed.");
        info("Generating report ...");

        String report = ReportGenerator.generate(r.tree);

        String outputFile = null;
        for (int i = 2; i < args.length - 1; i++) {
            if (args[i].equals("-o")) { outputFile = args[i + 1]; break; }
        }

        if (outputFile != null) {
            Files.write(Paths.get(outputFile), report.getBytes());
            ok("Report written to: " + BOLD + outputFile + RESET);
        } else {
            System.out.print(report);
        }
    }

    private static void runServe(String[] args) throws IOException {
        int port = 8080;
        Path dir  = Paths.get(".").toAbsolutePath().normalize();

        for (int i = 1; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-p": case "--port":
                    try { port = Integer.parseInt(args[i + 1]); }
                    catch (NumberFormatException e) { err("Invalid port: " + args[i + 1]); System.exit(1); }
                    break;
                case "-d": case "--dir":
                    dir = Paths.get(args[i + 1]).toAbsolutePath().normalize();
                    if (!Files.isDirectory(dir)) { err("Not a directory: " + args[i + 1]); System.exit(1); }
                    break;
            }
        }

        ApiServer server = new ApiServer(port, dir);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            info("Shutting down...");
            server.stop();
        }));

        // Block the main thread until interrupted (Ctrl+C)
        try { Thread.currentThread().join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static void runTree(String file) throws IOException {
        Compiler.Result r = Compiler.fromFile(file);

        if (r.hasErrors()) {
            err("Cannot show tree — fix errors first.");
            System.exit(2);
        }

        info("Opening parse tree viewer ...");
        Future<JFrame> dialog = Trees.inspect(r.tree, r.parser);
        try {
            dialog.get();
        } catch (Exception e) {
            err("Tree viewer error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ output helpers

    static void ok(String msg)   { System.out.println(GREEN + "  ✓ " + RESET + msg); }
    static void info(String msg) { System.out.println(CYAN  + "  → " + RESET + msg); }
    static void err(String msg)  { System.err.println(RED   + "  ✗ " + RESET + msg); }

    // ------------------------------------------------------------------ help

    private static void printUsage() {
        System.out.println(BOLD + "iflow " + VERSION + RESET + " — IncidentFlow DSL compiler");
        System.out.println();
        System.out.println(BOLD + "Usage:" + RESET);
        System.out.println("  iflow <command> [file] [options]");
        System.out.println();
        System.out.println(BOLD + "Commands:" + RESET);
        System.out.println("  " + CYAN + "wizard" + RESET + "                      Interactive playbook + report generator");
        System.out.println("  " + CYAN + "check"  + RESET + "  <file>              Validate syntax and semantics");
        System.out.println("  " + CYAN + "report" + RESET + " <file> [-o out.md]  Generate Markdown incident report");
        System.out.println("  " + CYAN + "tree"   + RESET + "   <file>              Show parse tree (GUI)");
        System.out.println("  " + CYAN + "serve"  + RESET + "  [-p port] [-d dir]  Start HTTP API server (default port 8080)");
        System.out.println();
        System.out.println(BOLD + "Options:" + RESET);
        System.out.println("  -o <file>      Write report to file instead of stdout");
        System.out.println("  -p <port>      Port for the API server (default: 8080)");
        System.out.println("  -d <dir>       Playbooks directory for the API server (default: .)");
        System.out.println("  -v, --version  Print version");
        System.out.println("  -h, --help     Show this help");
        System.out.println();
        System.out.println(BOLD + "Examples:" + RESET);
        System.out.println("  iflow wizard");
        System.out.println("  iflow check  phishing.iflow");
        System.out.println("  iflow report phishing.iflow -o report.md");
        System.out.println("  iflow report phishing.iflow" + "            " + DIM + "# prints to stdout" + RESET);
        System.out.println("  iflow serve");
        System.out.println("  iflow serve -p 9000 -d /path/to/playbooks");
    }
}
