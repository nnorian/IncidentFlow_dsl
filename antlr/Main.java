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
 * Test driver: parse an IncidentFlow source file and open a GUI derivation tree.
 *
 * Usage: java -cp "bin:antlr-4.13.2-complete.jar" incidentflow.Main <file.iflow>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: Main <source-file.iflow>");
            System.exit(1);
        }

        // Read source file
        String source = new String(Files.readAllBytes(Paths.get(args[0])));

        // Lex
        CharStream input = CharStreams.fromString(source);
        IncidentFlowLexer lexer = new IncidentFlowLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Parse
        IncidentFlowParser parser = new IncidentFlowParser(tokens);

        // Attach simple error listeners that count errors
        ErrorCounter errors = new ErrorCounter();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);

        ParseTree tree = parser.program();

        if (errors.getCount() > 0) {
            System.err.println("\nParsing finished with " + errors.getCount() + " error(s).");
            System.exit(2);
        }

        // Open the ANTLR4 GUI tree viewer and block until the window is closed
        System.out.println("Parsing succeeded — opening tree viewer...");
        Future<JFrame> dialog = Trees.inspect(tree, parser);
        try {
            dialog.get(); // blocks until the dialog is closed
        } catch (Exception e) {
            System.err.println("Tree viewer error: " + e.getMessage());
        }
    }

    /** Counts parse / lex errors without stopping the parse. */
    private static class ErrorCounter extends BaseErrorListener {
        private int count = 0;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            System.err.printf("line %d:%d %s%n", line, charPositionInLine, msg);
            count++;
        }

        int getCount() { return count; }
    }
}
