// Generated from IncidentFlow.g4 by ANTLR 4.13.2
package incidentflow;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IncidentFlowParser}.
 */
public interface IncidentFlowListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(IncidentFlowParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(IncidentFlowParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#import_decl}.
	 * @param ctx the parse tree
	 */
	void enterImport_decl(IncidentFlowParser.Import_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#import_decl}.
	 * @param ctx the parse tree
	 */
	void exitImport_decl(IncidentFlowParser.Import_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#config_block}.
	 * @param ctx the parse tree
	 */
	void enterConfig_block(IncidentFlowParser.Config_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#config_block}.
	 * @param ctx the parse tree
	 */
	void exitConfig_block(IncidentFlowParser.Config_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#config_name}.
	 * @param ctx the parse tree
	 */
	void enterConfig_name(IncidentFlowParser.Config_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#config_name}.
	 * @param ctx the parse tree
	 */
	void exitConfig_name(IncidentFlowParser.Config_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#config_entry}.
	 * @param ctx the parse tree
	 */
	void enterConfig_entry(IncidentFlowParser.Config_entryContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#config_entry}.
	 * @param ctx the parse tree
	 */
	void exitConfig_entry(IncidentFlowParser.Config_entryContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#config_value}.
	 * @param ctx the parse tree
	 */
	void enterConfig_value(IncidentFlowParser.Config_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#config_value}.
	 * @param ctx the parse tree
	 */
	void exitConfig_value(IncidentFlowParser.Config_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#list_lit}.
	 * @param ctx the parse tree
	 */
	void enterList_lit(IncidentFlowParser.List_litContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#list_lit}.
	 * @param ctx the parse tree
	 */
	void exitList_lit(IncidentFlowParser.List_litContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#chain_lit}.
	 * @param ctx the parse tree
	 */
	void enterChain_lit(IncidentFlowParser.Chain_litContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#chain_lit}.
	 * @param ctx the parse tree
	 */
	void exitChain_lit(IncidentFlowParser.Chain_litContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#playbook_decl}.
	 * @param ctx the parse tree
	 */
	void enterPlaybook_decl(IncidentFlowParser.Playbook_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#playbook_decl}.
	 * @param ctx the parse tree
	 */
	void exitPlaybook_decl(IncidentFlowParser.Playbook_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#trigger_decl}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_decl(IncidentFlowParser.Trigger_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#trigger_decl}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_decl(IncidentFlowParser.Trigger_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#phase_decl}.
	 * @param ctx the parse tree
	 */
	void enterPhase_decl(IncidentFlowParser.Phase_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#phase_decl}.
	 * @param ctx the parse tree
	 */
	void exitPhase_decl(IncidentFlowParser.Phase_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#report_decl}.
	 * @param ctx the parse tree
	 */
	void enterReport_decl(IncidentFlowParser.Report_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#report_decl}.
	 * @param ctx the parse tree
	 */
	void exitReport_decl(IncidentFlowParser.Report_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(IncidentFlowParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(IncidentFlowParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#do_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDo_stmt(IncidentFlowParser.Do_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#do_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDo_stmt(IncidentFlowParser.Do_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void enterIf_stmt(IncidentFlowParser.If_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void exitIf_stmt(IncidentFlowParser.If_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_stmt(IncidentFlowParser.For_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_stmt(IncidentFlowParser.For_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#wait_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWait_stmt(IncidentFlowParser.Wait_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#wait_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWait_stmt(IncidentFlowParser.Wait_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#timeout_handler}.
	 * @param ctx the parse tree
	 */
	void enterTimeout_handler(IncidentFlowParser.Timeout_handlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#timeout_handler}.
	 * @param ctx the parse tree
	 */
	void exitTimeout_handler(IncidentFlowParser.Timeout_handlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#verify_stmt}.
	 * @param ctx the parse tree
	 */
	void enterVerify_stmt(IncidentFlowParser.Verify_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#verify_stmt}.
	 * @param ctx the parse tree
	 */
	void exitVerify_stmt(IncidentFlowParser.Verify_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#fail_handler}.
	 * @param ctx the parse tree
	 */
	void enterFail_handler(IncidentFlowParser.Fail_handlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#fail_handler}.
	 * @param ctx the parse tree
	 */
	void exitFail_handler(IncidentFlowParser.Fail_handlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#log_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLog_stmt(IncidentFlowParser.Log_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#log_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLog_stmt(IncidentFlowParser.Log_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#goto_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGoto_stmt(IncidentFlowParser.Goto_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#goto_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGoto_stmt(IncidentFlowParser.Goto_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#assign_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAssign_stmt(IncidentFlowParser.Assign_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#assign_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAssign_stmt(IncidentFlowParser.Assign_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#parallel_block}.
	 * @param ctx the parse tree
	 */
	void enterParallel_block(IncidentFlowParser.Parallel_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#parallel_block}.
	 * @param ctx the parse tree
	 */
	void exitParallel_block(IncidentFlowParser.Parallel_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#severity_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSeverity_stmt(IncidentFlowParser.Severity_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#severity_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSeverity_stmt(IncidentFlowParser.Severity_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(IncidentFlowParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(IncidentFlowParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void enterOr_expr(IncidentFlowParser.Or_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void exitOr_expr(IncidentFlowParser.Or_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expr(IncidentFlowParser.And_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expr(IncidentFlowParser.And_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#not_expr}.
	 * @param ctx the parse tree
	 */
	void enterNot_expr(IncidentFlowParser.Not_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#not_expr}.
	 * @param ctx the parse tree
	 */
	void exitNot_expr(IncidentFlowParser.Not_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(IncidentFlowParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(IncidentFlowParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#comp_op}.
	 * @param ctx the parse tree
	 */
	void enterComp_op(IncidentFlowParser.Comp_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#comp_op}.
	 * @param ctx the parse tree
	 */
	void exitComp_op(IncidentFlowParser.Comp_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(IncidentFlowParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(IncidentFlowParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#member_access}.
	 * @param ctx the parse tree
	 */
	void enterMember_access(IncidentFlowParser.Member_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#member_access}.
	 * @param ctx the parse tree
	 */
	void exitMember_access(IncidentFlowParser.Member_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(IncidentFlowParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(IncidentFlowParser.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#argument_list}.
	 * @param ctx the parse tree
	 */
	void enterArgument_list(IncidentFlowParser.Argument_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#argument_list}.
	 * @param ctx the parse tree
	 */
	void exitArgument_list(IncidentFlowParser.Argument_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(IncidentFlowParser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(IncidentFlowParser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(IncidentFlowParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(IncidentFlowParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#bool_lit}.
	 * @param ctx the parse tree
	 */
	void enterBool_lit(IncidentFlowParser.Bool_litContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#bool_lit}.
	 * @param ctx the parse tree
	 */
	void exitBool_lit(IncidentFlowParser.Bool_litContext ctx);
	/**
	 * Enter a parse tree produced by {@link IncidentFlowParser#severity_lit}.
	 * @param ctx the parse tree
	 */
	void enterSeverity_lit(IncidentFlowParser.Severity_litContext ctx);
	/**
	 * Exit a parse tree produced by {@link IncidentFlowParser#severity_lit}.
	 * @param ctx the parse tree
	 */
	void exitSeverity_lit(IncidentFlowParser.Severity_litContext ctx);
}