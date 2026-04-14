package incidentflow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks the IncidentFlow parse tree and produces a Markdown incident report.
 *
 * Collects:
 *  - Configuration blocks and imports
 *  - Per-phase breakdown of all statements (DO, LOG, SEVERITY, VERIFY, WAIT,
 *    IF/ELSE, FOR, PARALLEL, GOTO, ASSIGN)
 *  - A summary section with severity trail, all actions, and log entries
 */
public class ReportVisitor extends IncidentFlowBaseVisitor<Void> {

    private final StringBuilder out          = new StringBuilder();
    private final List<String>  severityTrail = new ArrayList<>();
    private final List<String>  actions       = new ArrayList<>();
    private final List<String>  logEntries    = new ArrayList<>();
    private int depth = 0;

    // ------------------------------------------------------------------ helpers

    private void emit(String text) {
        for (int i = 0; i < depth; i++) out.append("  ");
        out.append(text).append("\n");
    }

    private void nl()  { out.append("\n"); }
    private void hr()  { out.append("\n---\n\n"); }

    // ------------------------------------------------------------------ public API

    public String getReport() { return out.toString(); }

    // ------------------------------------------------------------------ program

    @Override
    public Void visitProgram(IncidentFlowParser.ProgramContext ctx) {
        out.append("# IncidentFlow Incident Report\n\n");
        out.append("**Generated:** ")
           .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
           .append("\n\n");

        if (!ctx.config_block().isEmpty()) {
            out.append("## Configuration\n\n");
            for (var cfg : ctx.config_block()) visitConfig_block(cfg);
        }

        if (!ctx.import_decl().isEmpty()) {
            hr();
            out.append("## Imports\n\n");
            for (var imp : ctx.import_decl()) visitImport_decl(imp);
        }

        for (var pb : ctx.playbook_decl()) visitPlaybook_decl(pb);

        hr();
        appendSummary();
        return null;
    }

    // ------------------------------------------------------------------ config

    @Override
    public Void visitConfig_block(IncidentFlowParser.Config_blockContext ctx) {
        out.append("### ").append(ctx.config_name().IDENTIFIER().getText()).append("\n\n");
        for (var entry : ctx.config_entry()) {
            out.append("- **").append(entry.IDENTIFIER().getText()).append(":** ")
               .append(configValueToString(entry.config_value())).append("\n");
        }
        nl();
        return null;
    }

    // ------------------------------------------------------------------ imports

    @Override
    public Void visitImport_decl(IncidentFlowParser.Import_declContext ctx) {
        String path = ctx.STRING().getText();
        if (ctx.AS() != null) {
            out.append("- ").append(path)
               .append(" as `").append(ctx.IDENTIFIER().getText()).append("`\n");
        } else {
            out.append("- ").append(path).append("\n");
        }
        return null;
    }

    // ------------------------------------------------------------------ playbook

    @Override
    public Void visitPlaybook_decl(IncidentFlowParser.Playbook_declContext ctx) {
        hr();
        out.append("## Playbook: ").append(ctx.IDENTIFIER().getText()).append("\n\n");
        out.append("**Trigger:** ").append(exprToString(ctx.trigger_decl().expr())).append("\n\n");

        for (var phase : ctx.phase_decl()) visitPhase_decl(phase);
        if (ctx.report_decl() != null) visitReport_decl(ctx.report_decl());
        return null;
    }

    @Override
    public Void visitPhase_decl(IncidentFlowParser.Phase_declContext ctx) {
        out.append("### Phase: ").append(ctx.IDENTIFIER().getText()).append("\n\n");
        for (var stmt : ctx.statement()) renderStatement(stmt);
        nl();
        return null;
    }

    @Override
    public Void visitReport_decl(IncidentFlowParser.Report_declContext ctx) {
        out.append("### Report\n\n");
        for (var stmt : ctx.statement()) renderStatement(stmt);
        nl();
        return null;
    }

    // ------------------------------------------------------------------ statements

    private void renderStatement(IncidentFlowParser.StatementContext ctx) {
        if      (ctx.do_stmt()        != null) renderDoStmt(ctx.do_stmt());
        else if (ctx.if_stmt()        != null) renderIfStmt(ctx.if_stmt());
        else if (ctx.for_stmt()       != null) renderForStmt(ctx.for_stmt());
        else if (ctx.wait_stmt()      != null) renderWaitStmt(ctx.wait_stmt());
        else if (ctx.verify_stmt()    != null) renderVerifyStmt(ctx.verify_stmt());
        else if (ctx.log_stmt()       != null) renderLogStmt(ctx.log_stmt());
        else if (ctx.goto_stmt()      != null) renderGotoStmt(ctx.goto_stmt());
        else if (ctx.assign_stmt()    != null) renderAssignStmt(ctx.assign_stmt());
        else if (ctx.parallel_block() != null) renderParallelBlock(ctx.parallel_block());
        else if (ctx.severity_stmt()  != null) renderSeverityStmt(ctx.severity_stmt());
    }

    private void renderDoStmt(IncidentFlowParser.Do_stmtContext ctx) {
        String call = functionCallToString(ctx.function_call());
        actions.add(call);
        emit("- DO: " + call);
    }

    private void renderLogStmt(IncidentFlowParser.Log_stmtContext ctx) {
        String msg = exprToString(ctx.expr());
        logEntries.add(msg);
        emit("- LOG: " + msg);
    }

    private void renderSeverityStmt(IncidentFlowParser.Severity_stmtContext ctx) {
        String sev = ctx.severity_lit().getText();
        severityTrail.add(sev);
        emit("- SEVERITY " + sev);
    }

    private void renderGotoStmt(IncidentFlowParser.Goto_stmtContext ctx) {
        emit("- GOTO: " + ctx.IDENTIFIER().getText());
    }

    private void renderAssignStmt(IncidentFlowParser.Assign_stmtContext ctx) {
        emit("- ASSIGN: " + ctx.IDENTIFIER().getText() + " = " + exprToString(ctx.expr()));
    }

    private void renderVerifyStmt(IncidentFlowParser.Verify_stmtContext ctx) {
        emit("- VERIFY: " + exprToString(ctx.expr()));
        if (ctx.fail_handler() != null) {
            depth++;
            emit("- ON_FAIL:");
            depth++;
            for (var s : ctx.fail_handler().statement()) renderStatement(s);
            depth -= 2;
        }
    }

    private void renderWaitStmt(IncidentFlowParser.Wait_stmtContext ctx) {
        emit("- WAIT " + ctx.IDENTIFIER().getText()
                + " FROM " + exprToString(ctx.expr())
                + " TIMEOUT " + ctx.DURATION().getText());
        if (ctx.timeout_handler() != null) {
            depth++;
            emit("- ON_TIMEOUT:");
            depth++;
            for (var s : ctx.timeout_handler().statement()) renderStatement(s);
            depth -= 2;
        }
    }

    private void renderIfStmt(IncidentFlowParser.If_stmtContext ctx) {
        emit("- IF " + exprToString(ctx.expr()) + ":");
        depth++;

        List<IncidentFlowParser.StatementContext> allStmts = ctx.statement();

        if (ctx.ELSE() == null) {
            emit("- THEN:");
            depth++;
            for (var s : allStmts) renderStatement(s);
            depth--;
        } else {
            int elseIdx = ctx.ELSE().getSymbol().getTokenIndex();
            emit("- THEN:");
            depth++;
            for (var s : allStmts) {
                if (s.start.getTokenIndex() < elseIdx) renderStatement(s);
            }
            depth--;
            emit("- ELSE:");
            depth++;
            for (var s : allStmts) {
                if (s.start.getTokenIndex() > elseIdx) renderStatement(s);
            }
            depth--;
        }
        depth--;
    }

    private void renderForStmt(IncidentFlowParser.For_stmtContext ctx) {
        emit("- FOR EACH " + ctx.IDENTIFIER().getText()
                + " IN " + exprToString(ctx.expr()) + ":");
        depth++;
        for (var s : ctx.statement()) renderStatement(s);
        depth--;
    }

    private void renderParallelBlock(IncidentFlowParser.Parallel_blockContext ctx) {
        emit("- PARALLEL:");
        depth++;
        for (var s : ctx.statement()) renderStatement(s);
        depth--;
    }

    // ------------------------------------------------------------------ expression printing

    private String exprToString(IncidentFlowParser.ExprContext ctx) {
        return orExprToString(ctx.or_expr());
    }

    private String orExprToString(IncidentFlowParser.Or_exprContext ctx) {
        List<IncidentFlowParser.And_exprContext> parts = ctx.and_expr();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(" OR ");
            sb.append(andExprToString(parts.get(i)));
        }
        return sb.toString();
    }

    private String andExprToString(IncidentFlowParser.And_exprContext ctx) {
        List<IncidentFlowParser.Not_exprContext> parts = ctx.not_expr();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(notExprToString(parts.get(i)));
        }
        return sb.toString();
    }

    private String notExprToString(IncidentFlowParser.Not_exprContext ctx) {
        if (ctx.NOT() != null) return "NOT " + notExprToString(ctx.not_expr());
        return comparisonToString(ctx.comparison());
    }

    private String comparisonToString(IncidentFlowParser.ComparisonContext ctx) {
        List<IncidentFlowParser.PrimaryContext> primaries = ctx.primary();
        if (primaries.size() == 1) return primaryToString(primaries.get(0));
        return primaryToString(primaries.get(0))
                + " " + ctx.comp_op().getText() + " "
                + primaryToString(primaries.get(1));
    }

    private String primaryToString(IncidentFlowParser.PrimaryContext ctx) {
        if (ctx.function_call() != null) return functionCallToString(ctx.function_call());
        if (ctx.member_access() != null) return memberAccessToString(ctx.member_access());
        if (ctx.IDENTIFIER()    != null) return ctx.IDENTIFIER().getText();
        if (ctx.literal()       != null) return literalToString(ctx.literal());
        if (ctx.expr()          != null) return "(" + exprToString(ctx.expr()) + ")";
        return ctx.getText();
    }

    private String memberAccessToString(IncidentFlowParser.Member_accessContext ctx) {
        List<org.antlr.v4.runtime.tree.TerminalNode> ids = ctx.IDENTIFIER();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(".");
            sb.append(ids.get(i).getText());
        }
        return sb.toString();
    }

    private String functionCallToString(IncidentFlowParser.Function_callContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.IDENTIFIER().getText()).append("(");
        if (ctx.argument_list() != null) {
            List<IncidentFlowParser.ArgumentContext> args = ctx.argument_list().argument();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) sb.append(", ");
                IncidentFlowParser.ArgumentContext arg = args.get(i);
                if (arg.IDENTIFIER() != null) {
                    sb.append(arg.IDENTIFIER().getText())
                      .append(": ")
                      .append(exprToString(arg.expr()));
                } else {
                    sb.append(exprToString(arg.expr()));
                }
            }
        }
        return sb.append(")").toString();
    }

    private String literalToString(IncidentFlowParser.LiteralContext ctx) {
        if (ctx.STRING()       != null) return ctx.STRING().getText();
        if (ctx.INT()          != null) return ctx.INT().getText();
        if (ctx.DURATION()     != null) return ctx.DURATION().getText();
        if (ctx.bool_lit()     != null) return ctx.bool_lit().getText();
        if (ctx.severity_lit() != null) return ctx.severity_lit().getText();
        if (ctx.list_lit()     != null) return listLitToString(ctx.list_lit());
        return ctx.getText();
    }

    private String listLitToString(IncidentFlowParser.List_litContext ctx) {
        StringBuilder sb = new StringBuilder("[");
        List<IncidentFlowParser.Config_valueContext> vals = ctx.config_value();
        for (int i = 0; i < vals.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(configValueToString(vals.get(i)));
        }
        return sb.append("]").toString();
    }

    private String configValueToString(IncidentFlowParser.Config_valueContext ctx) {
        if (ctx.STRING()       != null) return ctx.STRING().getText();
        if (ctx.INT()          != null) return ctx.INT().getText();
        if (ctx.severity_lit() != null) return ctx.severity_lit().getText();
        if (ctx.bool_lit()     != null) return ctx.bool_lit().getText();
        if (ctx.list_lit()     != null) return listLitToString(ctx.list_lit());
        if (ctx.chain_lit()    != null) return chainLitToString(ctx.chain_lit());
        return ctx.getText();
    }

    private String chainLitToString(IncidentFlowParser.Chain_litContext ctx) {
        List<org.antlr.v4.runtime.tree.TerminalNode> ids = ctx.IDENTIFIER();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(ids.get(i).getText());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ summary

    private void appendSummary() {
        out.append("## Summary\n\n");

        out.append("### Severity Trail\n\n");
        if (severityTrail.isEmpty()) {
            out.append("_(no severity transitions recorded)_\n\n");
        } else {
            for (int i = 0; i < severityTrail.size(); i++) {
                out.append(i + 1).append(". ").append(severityTrail.get(i)).append("\n");
            }
            nl();
        }

        out.append("### Actions Performed\n\n");
        if (actions.isEmpty()) {
            out.append("_(no actions recorded)_\n\n");
        } else {
            for (String a : actions) out.append("- ").append(a).append("\n");
            nl();
        }

        out.append("### Log Entries\n\n");
        if (logEntries.isEmpty()) {
            out.append("_(no log entries recorded)_\n\n");
        } else {
            for (String l : logEntries) out.append("- ").append(l).append("\n");
            nl();
        }
    }
}
