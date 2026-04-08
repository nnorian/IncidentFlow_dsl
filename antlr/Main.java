package incidentflow;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.Trees;
import javax.swing.JFrame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Future;

/**
 * IncidentFlow CLI entry point.
 *
 * Usage:
 *   iflow check  <file.iflow>              — validate syntax and semantics
 *   iflow report <file.iflow> [-o out.md]  — generate Markdown incident report
 *   iflow tree   <file.iflow>              — open ANTLR4 parse tree GUI
 */
public class Main {

    static final String VERSION = "1.0.0";

    // ANSI colour codes (disabled automatically on non-TTY via wrapper)
    static final String RESET  = "\u001B[0m";
    static final String BOLD   = "\u001B[1m";
    static final String RED    = "\u001B[31m";
    static final String GREEN  = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String CYAN   = "\u001B[36m";
    static final String DIM    = "\u001B[2m";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];

        if (command.equals("--version") || command.equals("-v")) {
            System.out.println("iflow " + VERSION);
            return;
        }

        if (command.equals("--help") || command.equals("-h") || command.equals("help")) {
            printUsage();
            return;
        }

        if (command.equals("wizard")) {
            new Wizard().run();
            return;
        }

        if (args.length < 2) {
            err("No input file specified.");
            System.out.println();
            printUsage();
            System.exit(1);
        }

        String sourceFile = args[1];

        if (!Files.exists(Paths.get(sourceFile))) {
            err("File not found: " + sourceFile);
            System.exit(1);
        }

        // ---- parse ----
        String source = new String(Files.readAllBytes(Paths.get(sourceFile)));
        CharStream        input  = CharStreams.fromString(source);
        IncidentFlowLexer  lexer  = new IncidentFlowLexer(input);
        CommonTokenStream  tokens = new CommonTokenStream(lexer);
        IncidentFlowParser parser = new IncidentFlowParser(tokens);

        ErrorCounter errors = new ErrorCounter();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);

        ParseTree tree = parser.program();

        if (errors.getCount() > 0) {
            err(errors.getCount() + " syntax error(s) — aborting.");
            System.exit(2);
        }

        // ---- dispatch ----
        switch (command) {
            case "check":  runCheck(tree, sourceFile);        break;
            case "report": runReport(tree, sourceFile, args); break;
            case "tree":   runTree(tree, parser);             break;
            default:
                err("Unknown command: " + BOLD + command + RESET);
                System.out.println();
                printUsage();
                System.exit(1);
        }
    }

    // ------------------------------------------------------------------ check

    private static void runCheck(ParseTree tree, String sourceFile) {
        info("Checking " + DIM + sourceFile + RESET + " ...");

        SemanticChecker checker = new SemanticChecker();
        checker.visit(tree);

        if (checker.hasErrors()) {
            for (String e : checker.getErrors()) err(e);
            System.exit(3);
        }

        ok("No errors found.");
    }

    // ------------------------------------------------------------------ report

    private static void runReport(ParseTree tree, String sourceFile, String[] args) throws IOException {
        info("Checking " + DIM + sourceFile + RESET + " ...");

        SemanticChecker checker = new SemanticChecker();
        checker.visit(tree);
        if (checker.hasErrors()) {
            for (String e : checker.getErrors()) err(e);
            System.exit(3);
        }

        ok("Validation passed.");
        info("Generating report ...");

        ReportVisitor visitor = new ReportVisitor();
        visitor.visit(tree);
        String report = visitor.getReport();

        String outputFile = null;
        for (int i = 2; i < args.length - 1; i++) {
            if (args[i].equals("-o")) {
                outputFile = args[i + 1];
                break;
            }
        }

        if (outputFile != null) {
            Files.write(Paths.get(outputFile), report.getBytes());
            ok("Report written to: " + BOLD + outputFile + RESET);
        } else {
            System.out.print(report);
        }
    }

    // ------------------------------------------------------------------ tree

    private static void runTree(ParseTree tree, IncidentFlowParser parser) {
        info("Opening parse tree viewer ...");
        Future<JFrame> dialog = Trees.inspect(tree, parser);
        try {
            dialog.get();
        } catch (Exception e) {
            err("Tree viewer error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void ok(String msg) {
        System.out.println(GREEN + "  ✓ " + RESET + msg);
    }

    private static void info(String msg) {
        System.out.println(CYAN + "  → " + RESET + msg);
    }

    private static void err(String msg) {
        System.err.println(RED + "  ✗ " + RESET + msg);
    }

    // ------------------------------------------------------------------ usage

    private static void printUsage() {
        System.out.println(BOLD + "iflow " + VERSION + RESET + " — IncidentFlow DSL compiler");
        System.out.println();
        System.out.println(BOLD + "Usage:" + RESET);
        System.out.println("  iflow <command> <file.iflow> [options]");
        System.out.println();
        System.out.println(BOLD + "Commands:" + RESET);
        System.out.println("  " + CYAN + "wizard" + RESET + "                      Interactive playbook + report generator");
        System.out.println("  " + CYAN + "check" + RESET  + "  <file>              Validate syntax and semantics");
        System.out.println("  " + CYAN + "report" + RESET + " <file> [-o out.md]  Generate Markdown incident report");
        System.out.println("  " + CYAN + "tree" + RESET   + "   <file>              Show parse tree (GUI)");
        System.out.println();
        System.out.println(BOLD + "Options:" + RESET);
        System.out.println("  -o <file>    Write report to file instead of stdout");
        System.out.println("  -v, --version  Print version");
        System.out.println("  -h, --help     Show this help");
        System.out.println();
        System.out.println(BOLD + "Examples:" + RESET);
        System.out.println("  iflow wizard");
        System.out.println("  iflow check  phishing.iflow");
        System.out.println("  iflow report phishing.iflow -o report.md");
        System.out.println("  iflow report phishing.iflow            " + DIM + "# prints to stdout" + RESET);
    }

    // ------------------------------------------------------------------ error listener

    private static class ErrorCounter extends BaseErrorListener {
        private int count = 0;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            System.err.println(RED + "  ✗ " + RESET + "line " + line + ":" + charPositionInLine + " " + msg);
            count++;
        }

        int getCount() { return count; }
    }
}
