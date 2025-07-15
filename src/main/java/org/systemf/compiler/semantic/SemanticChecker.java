package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.systemf.compiler.parser.ParsedResult;
import org.systemf.compiler.parser.SysYLexer;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.parser.SysYParserBaseListener;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.semantic.external.SysYExternalRegistry;
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
			throw IllegalSemanticException.wrap(listener.currentContext, e);
		}
		return new SemanticResult(program, listener.typeMap);
	}

	private static class SemanticListener extends SysYParserBaseListener {
		private static final ValueAndType RIGHT_INT = ValueAndType.ofRight(SysYInt.INT);
		private static final ValueAndType RIGHT_FLOAT = ValueAndType.ofRight(SysYFloat.FLOAT);
		private static final ValueAndType RIGHT_VOID = ValueAndType.ofRight(SysYVoid.VOID);
		public final HashMap<ParserRuleContext, ValueAndType> typeMap = new HashMap<>();
		public final Context<ValueAndType> context = new Context<>();
		public ParserRuleContext currentContext;
		private SysYType retType;
		private int loopLayer = 0;

		public SemanticListener() {
			context.push();
			SysYExternalRegistry.registerSysY(context);
		}

		@Override
		public void enterEveryRule(ParserRuleContext ctx) {
			currentContext = ctx;
		}

		@Override
		public void exitEveryRule(ParserRuleContext ctx) {
			currentContext = ctx;
		}

		private ValueClass varDefValueClass = null;
		private SysYType varDefBasicType = null;

		@Override
		public void enterVarDef(SysYParser.VarDefContext ctx) {
			varDefValueClass = SysYTypeUtil.valueClassFromConstPrefix(ctx.constPrefix());
			varDefBasicType = SysYTypeUtil.typeFromBasicType(ctx.type);
		}

		@Override
		public void exitVarDefEntry(SysYParser.VarDefEntryContext ctx) {
			var entryType = SysYTypeUtil.applyVarDefEntry(varDefBasicType, ctx);
			var varName = ctx.name.getText();
			var varTy = new ValueAndType(varDefValueClass, entryType);
			typeMap.put(ctx, varTy);
			context.define(varName, varTy);
		}

		@Override
		public void enterFuncDef(SysYParser.FuncDefContext ctx) {
			retType = SysYTypeUtil.typeFromRetType(ctx.retType());
			var funcType = new SysYFunction(retType,
					ctx.funcParam().stream().map(SysYTypeUtil::typeFromFuncParam).toArray(SysYType[]::new));
			context.define(ctx.name.getText(), ValueAndType.ofRight(funcType));
			context.push();
			ctx.funcParam().forEach(param -> context.define(param.name.getText(),
					ValueAndType.ofLeft(SysYTypeUtil.typeFromFuncParam(param))));
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
			if (!rTy.convertibleTo(ValueAndType.ofRight(lTy.type())))
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
			var retTy = ctx.ret == null ? RIGHT_VOID : typeMap.get(ctx.ret);
			if (!retTy.convertibleTo(ValueAndType.ofRight(retType)))
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
		public void exitFunctionCall(SysYParser.FunctionCallContext ctx) {
			var funcTy = context.get(ctx.func.getText());
			if (!(funcTy.type() instanceof SysYFunction(SysYType result, SysYType[] args)))
				throw new IllegalSemanticException("Illegal function call on " + funcTy);
			var params = ctx.funcRealParam().stream().map(SysYParser.FuncRealParamContext::expr).map(typeMap::get)
					.toArray(ValueAndType[]::new);
			if (args.length != params.length) throw new IllegalSemanticException(
					String.format("Illegal number of arguments, expected %d, given %d", args.length, params.length));
			for (int i = 0; i < args.length; ++i) {
				var arg = ValueAndType.ofRight(args[i]);
				typeMap.put(ctx.funcRealParam(i), arg);
				var param = params[i];
				if (!param.convertibleTo(arg)) throw new IllegalSemanticException(
						String.format("Illegal parameter, expected %s, given %s", arg, param));
			}
			typeMap.put(ctx, ValueAndType.ofRight(result));
		}

		@Override
		public void exitAccess(SysYParser.AccessContext ctx) {
			typeMap.put(ctx, typeMap.get(ctx.varAccess()));
		}

		@Override
		public void exitParen(SysYParser.ParenContext ctx) {
			typeMap.put(ctx, typeMap.get(ctx.expr()));
		}

		@Override
		public void exitUnary(SysYParser.UnaryContext ctx) {
			var op = ctx.op.getType();
			var xTy = typeMap.get(ctx.x);

			if (SysYInt.INT.equals(xTy.type())) {
				typeMap.put(ctx, RIGHT_INT);
				return;
			}

			if (SysYFloat.FLOAT.equals(xTy.type())) {
				if (op != SysYLexer.NOT) typeMap.put(ctx, RIGHT_FLOAT);
				else typeMap.put(ctx, RIGHT_INT);
				return;
			}

			throw new IllegalSemanticException("Illegal operand " + xTy);
		}

		private void exitBinary(ParserRuleContext cur, ParserRuleContext x, ParserRuleContext y) {
			var xTy = typeMap.get(x).type();
			var yTy = typeMap.get(y).type();
			if (!(xTy instanceof SysYNumeric xNum)) throw new IllegalSemanticException("Illegal 1st operand " + xTy);
			if (!(yTy instanceof SysYNumeric yNum)) throw new IllegalSemanticException("Illegal 2nd operand " + yTy);
			typeMap.put(cur, ValueAndType.ofRight(SysYTypeUtil.elevatedType(xNum, yNum)));
		}

		@Override
		public void exitMuls(SysYParser.MulsContext ctx) {
			if (ctx.op.getType() == SysYLexer.MOD) {
				var xTy = typeMap.get(ctx.l).type();
				var yTy = typeMap.get(ctx.r).type();
				if (SysYFloat.FLOAT.equals(xTy) || SysYFloat.FLOAT.equals(yTy))
					throw new IllegalSemanticException("Illegal mod operation on float");
			}

			exitBinary(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitAdds(SysYParser.AddsContext ctx) {
			exitBinary(ctx, ctx.l, ctx.r);
		}

		private void exitRelation(ParserRuleContext cur, ParserRuleContext x, ParserRuleContext y) {
			var xTy = typeMap.get(x).type();
			var yTy = typeMap.get(y).type();
			if (!(xTy instanceof SysYNumeric)) throw new IllegalSemanticException("Illegal 1st operand " + xTy);
			if (!(yTy instanceof SysYNumeric)) throw new IllegalSemanticException("Illegal 2nd operand " + yTy);
			typeMap.put(cur, RIGHT_INT);
		}

		@Override
		public void exitRels(SysYParser.RelsContext ctx) {
			exitRelation(ctx, ctx.l, ctx.r);
		}

		@Override
		public void exitEqs(SysYParser.EqsContext ctx) {
			exitRelation(ctx, ctx.l, ctx.r);
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