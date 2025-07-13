package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.systemf.compiler.parser.ParsedResult;
import org.systemf.compiler.parser.SysYLexer;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.parser.SysYParserBaseListener;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.semantic.type.*;
import org.systemf.compiler.semantic.util.SysYTypeUtil;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.semantic.value.ValueClass;
import org.systemf.compiler.util.Context;

import java.util.HashMap;

public enum SemanticChecker implements EntityProvider<SemanticResult> {
	INSTANCE;

	@Override
	public SemanticResult produce() {
		var program = QueryManager.getInstance().get(ParsedResult.class).program();
		var listener = new SemanticListener();
		try {
			ParseTreeWalker.DEFAULT.walk(listener, program);
		} catch (RuntimeException e) {
			var start = listener.currentContext.getStart();
			var stop = listener.currentContext.getStop();
			var newException = new IllegalSemanticException(
					String.format("Semantic error in context from %d:%d to %d:%d", start.getLine(),
							start.getCharPositionInLine(), stop.getLine(), stop.getCharPositionInLine()), e);
			newException.setStackTrace(new StackTraceElement[0]);
			throw newException;
		}
		return new SemanticResult(program, listener.typeMap);
	}

	private static class SemanticListener extends SysYParserBaseListener {
		private static final ValueAndType RIGHT_INT = new ValueAndType(ValueClass.RIGHT, SysYInt.INT);
		private static final ValueAndType RIGHT_FLOAT = new ValueAndType(ValueClass.RIGHT, SysYFloat.FLOAT);
		public final HashMap<ParserRuleContext, ValueAndType> typeMap = new HashMap<>();
		public final Context<ValueAndType> context = new Context<>();
		public ParserRuleContext currentContext;
		private SysYType retType;
		private int loopLayer = 0;

		@Override
		public void enterEveryRule(ParserRuleContext ctx) {
			currentContext = ctx;
		}

		@Override
		public void exitEveryRule(ParserRuleContext ctx) {
			currentContext = ctx;
		}

		@Override
		public void exitVarDef(SysYParser.VarDefContext ctx) {
			var valueClass = SysYTypeUtil.valueClassFromConstPrefix(ctx.constPrefix());
			var type = SysYTypeUtil.typeFromBasicType(ctx.type);
			for (var entry : ctx.varDefEntry()) {
				var entryType = SysYTypeUtil.applyVarDefEntry(type, entry);
				var varName = entry.name.getText();
				context.define(varName, new ValueAndType(valueClass, entryType));
			}
		}

		@Override
		public void enterFuncDef(SysYParser.FuncDefContext ctx) {
			retType = SysYTypeUtil.typeFromRetType(ctx.retType());
			var funcType = new SysYFunction(retType,
					ctx.funcParam().stream().map(SysYTypeUtil::typeFromFuncParam).toArray(SysYType[]::new));
			context.define(ctx.name.getText(), new ValueAndType(ValueClass.RIGHT, funcType));
			context.push();
			ctx.funcParam().forEach(param -> context.define(param.name.getText(),
					new ValueAndType(ValueClass.LEFT, SysYTypeUtil.typeFromFuncParam(param))));
		}

		@Override
		public void exitFuncDef(SysYParser.FuncDefContext ctx) {
			context.pop();
			retType = null;
		}

		@Override
		public void enterStmtBlock(SysYParser.StmtBlockContext ctx) {
			context.push();
		}

		@Override
		public void exitStmtBlock(SysYParser.StmtBlockContext ctx) {
			context.pop();
		}

		@Override
		public void exitAssignment(SysYParser.AssignmentContext ctx) {
			var lTy = typeMap.get(ctx.lvalue);
			var rTy = typeMap.get(ctx.value);
			if (lTy.valueClass() != ValueClass.LEFT) throw new IllegalSemanticException("Cannot assign to right value");
			if (!rTy.convertibleTo(new ValueAndType(ValueClass.RIGHT, lTy.type())))
				throw new IllegalSemanticException(String.format("Cannot assign %s to %s", rTy, lTy));
		}

		@Override
		public void exitArrayPostfixSingle(SysYParser.ArrayPostfixSingleContext ctx) {
			var lenTy = typeMap.get(ctx.length);
			if (!lenTy.convertibleTo(RIGHT_INT)) throw new IllegalSemanticException("Illegal length " + lenTy);
		}

		@Override
		public void exitVarAccess(SysYParser.VarAccessContext ctx) {
			var variable = context.get(ctx.IDENT().getText());
			var varTy = variable.type();
			for (var _ : ctx.arrayPostfix().arrayPostfixSingle()) {
				if (!(varTy instanceof ISysYArray arr)) throw new IllegalSemanticException("Cannot index " + varTy);
				varTy = arr.getElement();
			}
			typeMap.put(ctx, new ValueAndType(variable.valueClass(), varTy));
		}

		@Override
		public void exitIf(SysYParser.IfContext ctx) {
			var condTy = typeMap.get(ctx.cond);
			if (!condTy.convertibleTo(RIGHT_INT)) throw new IllegalSemanticException("Illegal condition " + condTy);
		}

		@Override
		public void enterWhile(SysYParser.WhileContext ctx) {
			++loopLayer;
		}

		@Override
		public void exitWhile(SysYParser.WhileContext ctx) {
			var condTy = typeMap.get(ctx.cond);
			if (!condTy.convertibleTo(RIGHT_INT)) throw new IllegalSemanticException("Illegal condition " + condTy);
			--loopLayer;
		}

		@Override
		public void exitBreak(SysYParser.BreakContext ctx) {
			if (loopLayer == 0) throw new IllegalSemanticException("Illegal break");
		}

		@Override
		public void exitContinue(SysYParser.ContinueContext ctx) {
			if (loopLayer == 0) throw new IllegalSemanticException("Illegal continue");
		}

		@Override
		public void exitReturn(SysYParser.ReturnContext ctx) {
			var retTy = typeMap.get(ctx.ret);
			if (!retTy.convertibleTo(new ValueAndType(ValueClass.RIGHT, retType)))
				throw new IllegalSemanticException("Illegal return " + retTy);
		}

		@Override
		public void exitConstInt(SysYParser.ConstIntContext ctx) {
			typeMap.put(ctx, RIGHT_INT);
		}

		@Override
		public void exitConstFloat(SysYParser.ConstFloatContext ctx) {
			typeMap.put(ctx, RIGHT_FLOAT);
		}

		@Override
		public void exitFuncRealParam(SysYParser.FuncRealParamContext ctx) {
			typeMap.put(ctx, typeMap.get(ctx.expr()));
		}

		@Override
		public void exitFunctionCall(SysYParser.FunctionCallContext ctx) {
			var funcTy = context.get(ctx.func.getText());
			if (!(funcTy.type() instanceof SysYFunction(SysYType result, SysYType[] args)))
				throw new IllegalSemanticException("Illegal function call on " + funcTy);
			var params = ctx.funcRealParam().stream().map(typeMap::get).toArray(ValueAndType[]::new);
			if (args.length != params.length) throw new IllegalSemanticException(
					String.format("Illegal number of arguments, expected %d, given %d", args.length, params.length));
			for (int i = 0; i < args.length; ++i) {
				var arg = args[i];
				var param = params[i];
				if (!param.convertibleTo(new ValueAndType(ValueClass.RIGHT, arg))) throw new IllegalSemanticException(
						String.format("Illegal parameter, expected %s, given %s", arg, param));
			}
			typeMap.put(ctx, new ValueAndType(ValueClass.RIGHT, result));
		}

		@Override
		public void exitAccess(SysYParser.AccessContext ctx) {
			typeMap.put(ctx, typeMap.get(ctx.varAccess()));
		}

		@Override
		public void exitUnary(SysYParser.UnaryContext ctx) {
			var op = ctx.op.getType();
			var xTy = typeMap.get(ctx.x);
			if (xTy.convertibleTo(RIGHT_INT)) {
				typeMap.put(ctx, RIGHT_INT);
				return;
			}

			if (op != SysYLexer.NOT && xTy.convertibleTo(RIGHT_FLOAT)) {
				typeMap.put(ctx, RIGHT_FLOAT);
				return;
			}

			throw new IllegalSemanticException("Illegal operand " + xTy);
		}

		private void exitBinary(ParserRuleContext cur, ParserRuleContext x, ParserRuleContext y) {
			var xTy = typeMap.get(x).type();
			var yTy = typeMap.get(y).type();
			if (!(xTy instanceof SysYNumeric xNum)) throw new IllegalSemanticException("Illegal 1st operand " + xTy);
			if (!(yTy instanceof SysYNumeric yNum)) throw new IllegalSemanticException("Illegal 2nd operand " + yTy);
			typeMap.put(cur, new ValueAndType(ValueClass.RIGHT, SysYTypeUtil.elevatedType(xNum, yNum)));
		}

		@Override
		public void exitMuls(SysYParser.MulsContext ctx) {
			exitBinary(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitAdds(SysYParser.AddsContext ctx) {
			exitBinary(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitRels(SysYParser.RelsContext ctx) {
			exitBinary(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitEqs(SysYParser.EqsContext ctx) {
			exitBinary(ctx, ctx.l, ctx.r);
		}

		private void exitLogical(ParserRuleContext cur, ParserRuleContext x, ParserRuleContext y) {
			var xTy = typeMap.get(x);
			var yTy = typeMap.get(y);
			if (!xTy.convertibleTo(RIGHT_INT)) throw new IllegalSemanticException("Illegal 1st operand " + xTy);
			if (!yTy.convertibleTo(RIGHT_INT)) throw new IllegalSemanticException("Illegal 2nd operand " + yTy);
			typeMap.put(cur, RIGHT_INT);
		}

		@Override
		public void exitAnd(SysYParser.AndContext ctx) {
			exitLogical(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitOr(SysYParser.OrContext ctx) {
			exitLogical(ctx, ctx.l, ctx.r);
		}
	}
}