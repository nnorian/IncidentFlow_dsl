// Generated from IncidentFlow.g4 by ANTLR 4.13.2
package incidentflow;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IncidentFlowParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IncidentFlowVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(IncidentFlowParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#import_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_decl(IncidentFlowParser.Import_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#config_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfig_block(IncidentFlowParser.Config_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#config_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfig_name(IncidentFlowParser.Config_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#config_entry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfig_entry(IncidentFlowParser.Config_entryContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#config_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfig_value(IncidentFlowParser.Config_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#list_lit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_lit(IncidentFlowParser.List_litContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#chain_lit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChain_lit(IncidentFlowParser.Chain_litContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#playbook_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlaybook_decl(IncidentFlowParser.Playbook_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#trigger_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_decl(IncidentFlowParser.Trigger_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#phase_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhase_decl(IncidentFlowParser.Phase_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#report_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReport_decl(IncidentFlowParser.Report_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(IncidentFlowParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#do_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDo_stmt(IncidentFlowParser.Do_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#if_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_stmt(IncidentFlowParser.If_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#for_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_stmt(IncidentFlowParser.For_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#wait_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWait_stmt(IncidentFlowParser.Wait_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#timeout_handler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeout_handler(IncidentFlowParser.Timeout_handlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#verify_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerify_stmt(IncidentFlowParser.Verify_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#fail_handler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFail_handler(IncidentFlowParser.Fail_handlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#log_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLog_stmt(IncidentFlowParser.Log_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#goto_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGoto_stmt(IncidentFlowParser.Goto_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#assign_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign_stmt(IncidentFlowParser.Assign_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#parallel_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParallel_block(IncidentFlowParser.Parallel_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#severity_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeverity_stmt(IncidentFlowParser.Severity_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(IncidentFlowParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#or_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_expr(IncidentFlowParser.Or_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#and_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd_expr(IncidentFlowParser.And_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#not_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot_expr(IncidentFlowParser.Not_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(IncidentFlowParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#comp_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComp_op(IncidentFlowParser.Comp_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(IncidentFlowParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#member_access}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_access(IncidentFlowParser.Member_accessContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(IncidentFlowParser.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#argument_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument_list(IncidentFlowParser.Argument_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(IncidentFlowParser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(IncidentFlowParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#bool_lit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool_lit(IncidentFlowParser.Bool_litContext ctx);
	/**
	 * Visit a parse tree produced by {@link IncidentFlowParser#severity_lit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeverity_lit(IncidentFlowParser.Severity_litContext ctx);
}