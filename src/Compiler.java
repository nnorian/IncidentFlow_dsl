package incidentflow;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Facade over the full compilation pipeline:
 *   source text → lex → parse → semantic check → Result
 *
 * Both Main and Wizard use this instead of duplicating ANTLR boilerplate.
 */
public class Compiler {

    /** Parse and check a source file on disk. */
    public static Result fromFile(String path) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(path)));
        return fromSource(source);
    }

    /** Parse and check a source string (used by the Wizard). */
    public static Result fromSource(String source) {
        CharStream        input  = CharStreams.fromString(source);
        IncidentFlowLexer  lexer  = new IncidentFlowLexer(input);
        CommonTokenStream  tokens = new CommonTokenStream(lexer);
        IncidentFlowParser parser = new IncidentFlowParser(tokens);

        ErrorListener errors = new ErrorListener();
        lexer.removeErrorListeners();  lexer.addErrorListener(errors);
        parser.removeErrorListeners(); parser.addErrorListener(errors);

        ParseTree tree = parser.program();

        if (errors.hasErrors()) {
            return new Result(null, parser, errors.getErrors(), List.of());
        }

        SemanticChecker checker = new SemanticChecker();
        checker.visit(tree);

        return new Result(tree, parser, List.of(), checker.getErrors());
    }

    // ------------------------------------------------------------------ Result

    public static class Result {

        /** The parse tree — null if there were syntax errors. */
        public final ParseTree tree;

        /** The parser instance — needed by the tree GUI viewer. */
        public final IncidentFlowParser parser;

        public final List<String> syntaxErrors;
        public final List<String> semanticErrors;

        Result(ParseTree tree, IncidentFlowParser parser,
               List<String> syntaxErrors, List<String> semanticErrors) {
            this.tree           = tree;
            this.parser         = parser;
            this.syntaxErrors   = syntaxErrors;
            this.semanticErrors = semanticErrors;
        }

        public boolean hasSyntaxErrors()   { return !syntaxErrors.isEmpty(); }
        public boolean hasSemanticErrors() { return !semanticErrors.isEmpty(); }
        public boolean hasErrors()         { return hasSyntaxErrors() || hasSemanticErrors(); }
    }
}
