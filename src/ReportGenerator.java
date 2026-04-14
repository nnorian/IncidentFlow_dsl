package incidentflow;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Generates a Markdown incident report from a parsed IncidentFlow tree.
 * Thin wrapper around ReportVisitor — keeps callers decoupled from visitor internals.
 */
public class ReportGenerator {

    public static String generate(ParseTree tree) {
        ReportVisitor visitor = new ReportVisitor();
        visitor.visit(tree);
        return visitor.getReport();
    }
}
