package org.systemf.compiler.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.systemf.compiler.query.AttributeProvider;

import java.util.stream.Collectors;

public enum PrettyPrintProvider implements AttributeProvider<SysYParser.ProgramContext, PrettyPrintAttribute> {
	INSTANCE;

	@Override
	public PrettyPrintAttribute getAttribute(SysYParser.ProgramContext entity) {
		return new PrettyPrintAttribute(entity.accept(PrettyPrintVisitor.INSTANCE));
	}

	private static class PrettyPrintVisitor extends SysYParserBaseVisitor<String> {
		private static final PrettyPrintVisitor INSTANCE = new PrettyPrintVisitor();

		private PrettyPrintVisitor() {}

		private String addIndent(String str) {
			if (str.isEmpty()) return "";
			boolean flag = str.endsWith("\n");
			String tmp = str.trim();
			tmp = "    " + tmp.replace("\n", "\n    ");
			if (flag) tmp += '\n';
			return tmp;
		}

		@Override
		public String visit(ParseTree tree) {
			return tree == null ? defaultResult() : super.visit(tree);
		}

		@Override
		public String visitProgram(SysYParser.ProgramContext program) {
			var result = defaultResult();
			int n = program.getChildCount();
			for (int i = 0; i < n; i++) {
				var child = program.getChild(i);
				var childResult = visit(child);
				if (i != 0 && child instanceof SysYParser.FuncDefContext) childResult = '\n' + childResult;
				result = aggregateResult(result, childResult);
			}
			return result;
		}

		@Override
		public String visitFuncDef(SysYParser.FuncDefContext funcDef) {
			return String.format("%s %s(%s) %s", visit(funcDef.r_type), funcDef.name.getText(),
					funcDef.funcParam().stream().map(this::visit).collect(Collectors.joining(", ")),
					visit(funcDef.stmtBlock()));
		}

		@Override
		public String visitStmtBlock(SysYParser.StmtBlockContext stmtBlock) {
			return String.format("{\n%s}\n", addIndent(stmtBlock.children.stream()
					.filter(x -> x instanceof SysYParser.StmtContext || x instanceof SysYParser.VarDefContext)
					.map(this::visit).collect(Collectors.joining())));
		}

		@Override
		public String visitUnary(SysYParser.UnaryContext unary) {
			return String.format("%s%s", unary.op.getText(), visit(unary.expr()));
		}

		@Override
		public String visitArrayPostfix(SysYParser.ArrayPostfixContext postfix) {
			return postfix.arrayPostfixSingle().stream().map(this::visit).collect(Collectors.joining());
		}

		@Override
		public String visitVarDefEntry(SysYParser.VarDefEntryContext defEntry) {
			return aggregateResult(String.format("%s%s", defEntry.name.getText(), visit(defEntry.arrayPostfix())),
					visit(defEntry.initializer()));
		}

		@Override
		public String visitFuncParam(SysYParser.FuncParamContext param) {
			return String.format("%s %s%s%s", visit(param.type), param.name.getText(), visit(param.incompleteArray()),
					visit(param.arrayPostfix()));
		}

		@Override
		public String visitFunctionCall(SysYParser.FunctionCallContext call) {
			return String.format("%s(%s)", call.func.getText(),
					call.funcRealParam().stream().map(this::visit).collect(Collectors.joining(", ")));
		}

		@Override
		public String visitVarAccess(SysYParser.VarAccessContext leftVal) {
			return String.format("%s%s", leftVal.IDENT().getText(), visit(leftVal.arrayPostfix()));
		}

		@Override
		public String visitIf(SysYParser.IfContext node) {
			var result = defaultResult();
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				var child = node.getChild(i);
				var childResult = visit(child);
				if (child instanceof SysYParser.StmtContext &&
				    !(child instanceof SysYParser.BlockContext || (i == 6 && child instanceof SysYParser.IfContext)))
					childResult = '\n' + addIndent(childResult);
				result = aggregateResult(result, childResult);
			}
			return result;
		}

		@Override
		public String visitWhile(SysYParser.WhileContext node) {
			var result = defaultResult();
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				var child = node.getChild(i);
				var childResult = visit(child);
				if (child instanceof SysYParser.StmtContext && !(child instanceof SysYParser.BlockContext))
					childResult = '\n' + addIndent(childResult);
				result = aggregateResult(result, childResult);
			}
			return result;
		}

		@Override
		public String visitTerminal(TerminalNode node) {
			return node.getText();
		}

		@Override
		protected String defaultResult() {
			return "";
		}

		@Override
		protected String aggregateResult(String aggregate, String nextResult) {
			if (nextResult.isEmpty()) return aggregate;
			if (";".equals(nextResult)) return aggregate + nextResult + '\n';
			if (aggregate.isEmpty()) return nextResult;
			if (Character.isWhitespace(aggregate.charAt(aggregate.length() - 1)) ||
			    Character.isWhitespace(nextResult.charAt(0)) || aggregate.endsWith("(") || aggregate.endsWith("[") ||
			    aggregate.endsWith("{") || ",".equals(nextResult) || ")".equals(nextResult) || "]".equals(nextResult) ||
			    "}".equals(nextResult)) return aggregate + nextResult;
			return String.format("%s %s", aggregate, nextResult);
		}
	}
}