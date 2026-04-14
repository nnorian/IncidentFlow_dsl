package incidentflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs semantic validation on a parsed IncidentFlow program.
 *
 * Currently checks:
 *  - GOTO targets reference phases that exist within the same playbook
 */
public class SemanticChecker extends IncidentFlowBaseVisitor<Void> {

    private final List<String> errors = new ArrayList<>();
    private Set<String> phases = new HashSet<>();
    private String currentPlaybook = null;

    @Override
    public Void visitPlaybook_decl(IncidentFlowParser.Playbook_declContext ctx) {
        currentPlaybook = ctx.IDENTIFIER().getText();
        phases = new HashSet<>();
        for (var phase : ctx.phase_decl()) {
            phases.add(phase.IDENTIFIER().getText());
        }
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitGoto_stmt(IncidentFlowParser.Goto_stmtContext ctx) {
        String target = ctx.IDENTIFIER().getText();
        if (!phases.contains(target)) {
            errors.add("line " + ctx.start.getLine()
                    + ": GOTO '" + target + "' — no such phase in playbook '"
                    + currentPlaybook + "'");
        }
        return null;
    }

    public boolean hasErrors() { return !errors.isEmpty(); }
    public List<String> getErrors() { return errors; }
}
