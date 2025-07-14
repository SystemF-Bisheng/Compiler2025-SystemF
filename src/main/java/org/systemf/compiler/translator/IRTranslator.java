package org.systemf.compiler.translator;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.parser.SysYLexer;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.parser.SysYParserBaseVisitor;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.semantic.IllegalSemanticException;
import org.systemf.compiler.semantic.SemanticResult;
import org.systemf.compiler.semantic.type.ISysYArray;
import org.systemf.compiler.semantic.type.SysYFloat;
import org.systemf.compiler.semantic.type.SysYInt;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.semantic.util.SysYAggregateBuilder;
import org.systemf.compiler.semantic.util.SysYTypeUtil;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.translator.util.ConstantAggregateBuilder;
import org.systemf.compiler.translator.util.IRTypeUtil;
import org.systemf.compiler.translator.util.NonConstantAggregateBuilder;
import org.systemf.compiler.translator.util.SysYValueUtil;
import org.systemf.compiler.util.Context;
import org.systemf.compiler.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public enum IRTranslator implements EntityProvider<IRTranslatedResult> {
	INSTANCE;

	@Override
	public IRTranslatedResult produce() {
		var program = QueryManager.getInstance().get(SemanticResult.class).program();
		Module res;
		try (var visitor = new TranslateVisitor()) {
			try {
				program.accept(visitor);
				res = visitor.module;
			} catch (RuntimeException e) {
				if (visitor.contextStack.isEmpty()) throw e;
				throw IllegalSemanticException.wrap(visitor.contextStack.peek(), e);
			}
		}
		return new IRTranslatedResult(res);
	}

	private static class TranslateVisitor extends SysYParserBaseVisitor<Value> implements AutoCloseable {
		public final Module module = new Module();
		public final Deque<ParserRuleContext> contextStack = new ArrayDeque<>();
		private final QueryManager query = QueryManager.getInstance();
		private final HashMap<ParserRuleContext, Value> valueMap = new HashMap<>();
		private final Context<Value> context = new Context<>();
		private final IRBuilder builder;
		private final IRTypeUtil typeUtil;
		private final SysYValueUtil valueUtil;
		private final ConstantAggregateBuilder constAggregate;
		private final NonConstantAggregateBuilder nonConstAggregate;
		private final Type VOID;
		private final Sized I32;
		private final ConstantInt I32_ZERO;
		private final ConstantInt I32_NEG_ONE;
		private final ConstantInt I32_ONE;
		private final ConstantFloat FLOAT_ZERO;
		private BasicBlock loopCond;
		private BasicBlock loopMerge;
		private SysYAggregateBuilder<SysYType, Pair<ParserRuleContext, Value>, ?> currentAggregate;
		private Function currentFunction;
		private boolean asCond = false;
		private BasicBlock trueBlock = null;
		private BasicBlock falseBlock = null;

		public TranslateVisitor() {
			builder = new IRBuilder(module);
			builder.ignoreOnTerminatedInsert = true;
			VOID = builder.buildVoidType();
			I32 = builder.buildI32Type();
			I32_ZERO = builder.buildConstantInt(0);
			I32_NEG_ONE = builder.buildConstantInt(-1);
			I32_ONE = builder.buildConstantInt(1);
			FLOAT_ZERO = builder.buildConstantFloat(0);

			typeUtil = new IRTypeUtil(builder);
			valueUtil = new SysYValueUtil(builder);
			constAggregate = new ConstantAggregateBuilder(valueUtil);
			nonConstAggregate = new NonConstantAggregateBuilder(builder, valueUtil);
		}

		@Override
		public void close() {
			builder.close();
		}

		@Override
		protected Value defaultResult() {
			return null;
		}

		@Override
		protected Value aggregateResult(Value aggregate, Value nextResult) {
			if (nextResult == null) return aggregate;
			return nextResult;
		}

		public Value lookup(String name) {
			if (context.contains(name)) return context.get(name);
			var res = module.lookupGlobal(name);
			if (res == null) throw new NoSuchElementException("Cannot find symbol " + name);
			return res;
		}

		@Override
		public Value visit(ParseTree tree) {
			if (tree == null) return defaultResult();
			return super.visit(tree);
		}

		private void enterRule(ParserRuleContext ctx) {
			contextStack.push(ctx);
		}

		private void exitRule() {
			contextStack.pop();
		}

		@Override
		public Value visitSingle(SysYParser.SingleContext ctx) {
			enterRule(ctx);

			var expr = ctx.expr();
			currentAggregate.addValue(Pair.of(expr, visit(expr)));

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitArray(SysYParser.ArrayContext ctx) {
			enterRule(ctx);

			currentAggregate.beginAggregate();
			ctx.eqInitializeVal().forEach(this::visit);
			currentAggregate.endAggregate();

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitInitializer(SysYParser.InitializerContext ctx) {
			enterRule(ctx);

			var init = ctx.value;
			if (init instanceof SysYParser.SingleContext single) visit(single);
			else if (init instanceof SysYParser.ArrayContext array) array.eqInitializeVal().forEach(this::visit);
			else throw new IllegalArgumentException("Unknown initializer");

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitVarDef(SysYParser.VarDefContext ctx) {
			enterRule(ctx);

			var type = typeUtil.typeFromBasicType(ctx.type);
			var sysYType = SysYTypeUtil.typeFromBasicType(ctx.type);
			for (var entry : ctx.varDefEntry()) {
				visit(entry.arrayPostfix());
				var entryType = typeUtil.applyVarDefEntry(type, entry, valueMap);
				var varName = entry.name.getText();

				var entrySysYType = SysYTypeUtil.applyVarDefEntry(sysYType, entry, valueMap);
				if (context.empty()) {
					currentAggregate = constAggregate;

					constAggregate.begin(entrySysYType);
					visit(entry.init);
					var initializer = constAggregate.end();

					currentAggregate = null;

					builder.buildGlobalVariable(varName, entryType, initializer);
				} else {
					var value = builder.buildAlloca(entryType, varName);
					context.define(varName, value);

					if (entry.init != null) {
						currentAggregate = nonConstAggregate;

						nonConstAggregate.begin(entrySysYType);
						visit(entry.init);
						var initializer = nonConstAggregate.end();
						valueUtil.toConsumer(initializer).accept(value);

						currentAggregate = null;
					}
				}
			}

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitFuncDef(SysYParser.FuncDefContext ctx) {
			enterRule(ctx);

			var funcName = ctx.name.getText();
			var retType = typeUtil.typeFromRetType(ctx.retType());

			var params = new Parameter[ctx.funcParam().size()];
			for (int i = 0; i < params.length; ++i) {
				var param = ctx.funcParam(i);
				visit(param);
				var paramType = typeUtil.typeFromFuncParam(param, valueMap);
				var paramName = param.name.getText();
				params[i] = builder.buildParameter(paramType, paramName);
			}
			currentFunction = builder.buildFunction(funcName, retType, params);

			context.push();
			var entryBlock = builder.buildBasicBlock(currentFunction, funcName + "Entry");
			builder.attachToBlockTail(entryBlock);
			for (int i = 0; i < params.length; ++i) {
				var param = ctx.funcParam(i);
				var paramName = param.name.getText();
				var sysYType = SysYTypeUtil.typeFromFuncParam(param);
				if (sysYType instanceof ISysYArray) {
					context.define(paramName, params[i]);
				} else {
					var paramVal = params[i];
					var paramCopy = builder.buildAlloca(TypeUtil.assertSized(paramVal.getType(), "Function param"),
							paramName);
					builder.buildStore(paramVal, paramCopy);
					context.define(paramName, paramCopy);
				}
			}

			visit(ctx.stmtBlock());

			if ("main".equals(funcName)) builder.buildRet(I32_ZERO);
			else builder.buildUnreachable();

			context.pop();
			currentFunction = null;

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitStmtBlock(SysYParser.StmtBlockContext ctx) {
			enterRule(ctx);

			context.push();
			super.visitStmtBlock(ctx);
			context.pop();

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitAssignment(SysYParser.AssignmentContext ctx) {
			enterRule(ctx);

			var l = ctx.lvalue;
			var r = ctx.value;
			var lVal = visit(l);
			var rVal = visit(r);
			builder.buildStore(valueUtil.convertTo(rVal, query.getAttribute(r, ValueAndType.class).type(),
					query.getAttribute(l, ValueAndType.class).type()), lVal);

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitArrayPostfixSingle(SysYParser.ArrayPostfixSingleContext ctx) {
			enterRule(ctx);

			var res = visit(ctx.length);
			valueMap.put(ctx, res);

			exitRule();
			return res;
		}

		@Override
		public Value visitVarAccess(SysYParser.VarAccessContext ctx) {
			enterRule(ctx);

			var variable = lookup(ctx.IDENT().getText());
			visit(ctx.arrayPostfix());
			for (var postfix : ctx.arrayPostfix().arrayPostfixSingle())
				variable = builder.buildGetPtr(variable, valueMap.get(postfix), "indexing");
			valueMap.put(ctx, variable);

			exitRule();
			return variable;
		}

		@Override
		public Value visitIf(SysYParser.IfContext ctx) {
			enterRule(ctx);

			trueBlock = builder.buildBasicBlock(currentFunction, "ifTrue");
			var mergeBlock = builder.buildBasicBlock(currentFunction, "ifMerge");

			if (ctx.stmtFalse == null) falseBlock = mergeBlock;
			else falseBlock = builder.buildBasicBlock(currentFunction, "ifFalse");

			asCond = true;
			visit(ctx.cond);
			asCond = false;

			builder.attachToBlockTail(trueBlock);
			visit(ctx.stmtTrue);
			builder.buildBr(mergeBlock);

			if (ctx.stmtFalse != null) {
				builder.attachToBlockTail(falseBlock);
				visit(ctx.stmtFalse);
				builder.buildBr(mergeBlock);
			}

			builder.attachToBlockTail(mergeBlock);

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitWhile(SysYParser.WhileContext ctx) {
			enterRule(ctx);

			var oldLoopCond = loopCond;
			var oldLoopMerge = loopMerge;

			loopCond = builder.buildBasicBlock(currentFunction, "whileCond");
			loopMerge = builder.buildBasicBlock(currentFunction, "whileMerge");
			var body = builder.buildBasicBlock(currentFunction, "whileBody");

			builder.buildBr(loopCond);
			builder.attachToBlockTail(loopCond);

			asCond = true;
			trueBlock = body;
			falseBlock = loopMerge;
			visit(ctx.cond);
			asCond = false;

			builder.attachToBlockTail(body);
			visit(ctx.stmtTrue);
			builder.buildBr(loopCond);

			builder.attachToBlockTail(loopMerge);

			loopCond = oldLoopCond;
			loopMerge = oldLoopMerge;

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitBreak(SysYParser.BreakContext ctx) {
			enterRule(ctx);

			builder.buildBr(loopMerge);

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitContinue(SysYParser.ContinueContext ctx) {
			enterRule(ctx);

			builder.buildBr(loopCond);

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitReturn(SysYParser.ReturnContext ctx) {
			enterRule(ctx);

			builder.buildRet(visit(ctx.ret));

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitConstInt(SysYParser.ConstIntContext ctx) {
			enterRule(ctx);

			var val = Long.parseLong(ctx.value.getText());
			if (asCond) {
				if (val == 0) builder.buildBr(falseBlock);
				else builder.buildBr(trueBlock);

				exitRule();
				return defaultResult();
			} else {
				var value = builder.buildConstantInt(val);
				valueMap.put(ctx, value);

				exitRule();
				return value;
			}
		}

		@Override
		public Value visitConstFloat(SysYParser.ConstFloatContext ctx) {
			enterRule(ctx);

			var val = Double.parseDouble(ctx.value.getText());
			if (asCond) {
				if (val == 0) builder.buildBr(falseBlock);
				else builder.buildBr(trueBlock);

				exitRule();
				return defaultResult();
			} else {
				var value = builder.buildConstantFloat(val);
				valueMap.put(ctx, value);

				exitRule();
				return value;
			}
		}

		@Override
		public Value visitFuncRealParam(SysYParser.FuncRealParamContext ctx) {
			enterRule(ctx);

			var val = visit(ctx.expr());
			valueMap.put(ctx, val);

			exitRule();
			return val;
		}

		private Value convertToCond(SysYType type, Value value) {
			return valueUtil.convertTo(value, type, SysYInt.INT);
		}

		private void useAsCond(SysYType type, Value value) {
			builder.buildCondBr(convertToCond(type, value), trueBlock, falseBlock);
		}

		@Override
		public Value visitFunctionCall(SysYParser.FunctionCallContext ctx) {
			enterRule(ctx);

			var func = (Function) lookup(ctx.func.getText());
			var retType = TypeUtil.getReturnType(func.getType());

			var oldAsCond = asCond;
			asCond = false;
			var params = ctx.funcRealParam().stream().map(param -> {
				var orgType = query.getAttribute(param.expr(), ValueAndType.class).type();
				var targetType = query.getAttribute(param, ValueAndType.class).type();
				return valueUtil.convertTo(visit(param), orgType, targetType);
			}).toArray(Value[]::new);
			asCond = oldAsCond;

			if (VOID.equals(retType)) {
				builder.buildCallVoid(func, params);

				exitRule();
				return defaultResult();
			} else {
				var retSysYType = query.getAttribute(ctx, ValueAndType.class).type();
				var value = builder.buildCall(func, "call", params);
				if (asCond) {
					useAsCond(retSysYType, value);

					exitRule();
					return defaultResult();
				} else {
					valueMap.put(ctx, value);

					exitRule();
					return value;
				}
			}
		}

		@Override
		public Value visitAccess(SysYParser.AccessContext ctx) {
			enterRule(ctx);

			var ptr = visit(ctx.varAccess());
			var value = builder.buildLoad(ptr, "access");
			if (asCond) {
				useAsCond(query.getAttribute(ctx, ValueAndType.class).type(), value);

				exitRule();
				return defaultResult();
			} else {
				valueMap.put(ctx, value);

				exitRule();
				return value;
			}
		}

		private Value handleUnary(ParserRuleContext ctx, ParserRuleContext x,
				java.util.function.Function<Value, Value> intFunc,
				java.util.function.Function<Value, Value> floatFunc) {
			enterRule(ctx);

			var type = query.getAttribute(x, ValueAndType.class).type();

			var oldAsCond = asCond;
			asCond = false;
			var xVal = visit(x);
			asCond = oldAsCond;

			Value res;
			if (SysYInt.INT.equals(type)) res = intFunc.apply(xVal);
			else if (SysYFloat.FLOAT.equals(type)) res = floatFunc.apply(xVal);
			else throw new IllegalArgumentException(String.format("Illegal x type %s", type));

			if (asCond) {
				useAsCond(type, res);

				exitRule();
				return defaultResult();
			} else {
				valueMap.put(ctx, res);

				exitRule();
				return res;
			}
		}

		private Value handleBinary(ParserRuleContext ctx, ParserRuleContext x, ParserRuleContext y,
				BiFunction<Value, Value, Value> intFunc, BiFunction<Value, Value, Value> floatFunc) {
			enterRule(ctx);

			var type = query.getAttribute(ctx, ValueAndType.class).type();
			var xType = query.getAttribute(x, ValueAndType.class).type();
			var yType = query.getAttribute(y, ValueAndType.class).type();

			var oldAsCond = asCond;
			asCond = false;
			var xVal = visit(x);
			var yVal = visit(y);
			asCond = oldAsCond;

			xVal = valueUtil.convertTo(xVal, xType, type);
			yVal = valueUtil.convertTo(yVal, yType, type);

			Value res;
			if (SysYInt.INT.equals(type)) res = intFunc.apply(xVal, yVal);
			else if (SysYFloat.FLOAT.equals(type)) res = floatFunc.apply(xVal, yVal);
			else throw new IllegalArgumentException(String.format("Illegal result type %s", type));

			if (asCond) {
				useAsCond(type, res);

				exitRule();
				return defaultResult();
			} else {
				valueMap.put(ctx, res);

				exitRule();
				return res;
			}
		}

		private void swapTrueFalse() {
			var tmp = falseBlock;
			falseBlock = trueBlock;
			trueBlock = tmp;
		}

		@Override
		public Value visitUnary(SysYParser.UnaryContext ctx) {
			enterRule(ctx);

			var op = ctx.op.getType();
			var x = ctx.x;

			if (asCond && op == SysYLexer.NOT) {
				swapTrueFalse();
				visit(x);

				exitRule();
				return defaultResult();
			}

			exitRule();
			return switch (op) {
				case SysYLexer.PLUS -> handleUnary(ctx, x, v -> v, v -> v);
				case SysYLexer.MINUS -> handleUnary(ctx, x, v -> builder.buildOrFoldMul(v, I32_NEG_ONE, "iNeg"),
						v -> builder.buildOrFoldFNeg(v, "fNeg"));
				case SysYLexer.NOT ->
						handleUnary(ctx, x, v -> builder.buildOrFoldICmp(v, I32_ZERO, "iNot", CompareOp.EQ),
								v -> builder.buildOrFoldFCmp(v, FLOAT_ZERO, "fNot", CompareOp.EQ));
				default -> throw new IllegalStateException("Unexpected operator " + op);
			};
		}

		@Override
		public Value visitMuls(SysYParser.MulsContext ctx) {
			enterRule(ctx);

			var op = ctx.op.getType();
			var x = ctx.l;
			var y = ctx.r;

			exitRule();
			return switch (op) {
				case SysYLexer.MUL -> handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldMul(l, r, "mul"),
						(l, r) -> builder.buildOrFoldFMul(l, r, "fMul"));
				case SysYLexer.DIV -> handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldSDiv(l, r, "div"),
						(l, r) -> builder.buildOrFoldFDiv(l, r, "fDiv"));
				case SysYLexer.MOD ->
						handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldSRem(l, r, "mod"), (_, _) -> {
							throw new IllegalArgumentException("Illegal mod on float");
						});
				default -> throw new IllegalStateException("Unexpected operator " + op);
			};
		}

		@Override
		public Value visitAdds(SysYParser.AddsContext ctx) {
			enterRule(ctx);

			var op = ctx.op.getType();
			var x = ctx.l;
			var y = ctx.r;

			exitRule();
			return switch (op) {
				case SysYLexer.PLUS -> handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldAdd(l, r, "add"),
						(l, r) -> builder.buildOrFoldFAdd(l, r, "fAdd"));
				case SysYLexer.MINUS -> handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldSub(l, r, "sub"),
						(l, r) -> builder.buildOrFoldFSub(l, r, "fSub"));
				default -> throw new IllegalStateException("Unexpected operator " + op);
			};
		}

		private Value handleRelation(ParserRuleContext ctx, ParserRuleContext x, ParserRuleContext y, CompareOp op) {
			return handleBinary(ctx, x, y, (l, r) -> builder.buildOrFoldICmp(l, r, "iCmp", op),
					(l, r) -> builder.buildOrFoldFCmp(l, r, "fCmp", op));
		}

		@Override
		public Value visitRels(SysYParser.RelsContext ctx) {
			enterRule(ctx);

			var op = ctx.op.getType();
			var x = ctx.l;
			var y = ctx.r;

			exitRule();
			return switch (op) {
				case SysYLexer.LT -> handleRelation(ctx, x, y, CompareOp.LT);
				case SysYLexer.GT -> handleRelation(ctx, x, y, CompareOp.GT);
				case SysYLexer.LE -> handleRelation(ctx, x, y, CompareOp.LE);
				case SysYLexer.GE -> handleRelation(ctx, x, y, CompareOp.GE);
				default -> throw new IllegalStateException("Unexpected operator " + op);
			};
		}

		@Override
		public Value visitEqs(SysYParser.EqsContext ctx) {
			enterRule(ctx);

			var op = ctx.op.getType();
			var x = ctx.l;
			var y = ctx.r;

			exitRule();
			return switch (op) {
				case SysYLexer.EQ -> handleRelation(ctx, x, y, CompareOp.EQ);
				case SysYLexer.NEQ -> handleRelation(ctx, x, y, CompareOp.NE);
				default -> throw new IllegalStateException("Unexpected operator " + op);
			};
		}

		private Value handleLogical(ParserRuleContext ctx, ParserRuleContext x, ParserRuleContext y, boolean shortcutOn,
				String name) {
			enterRule(ctx);

			var oldAsCond = asCond;

			BasicBlock mergeBlock = null;
			Value mergedValue = null;
			if (!asCond) {
				var tmp = builder.buildAlloca(I32, name + "Tmp");
				var oldBlock = builder.getCurrentBlock();
				mergeBlock = builder.buildBasicBlock(currentFunction, name + "Merge");

				trueBlock = builder.buildBasicBlock(currentFunction, name + "True");
				builder.attachToBlockTail(trueBlock);
				builder.buildStore(I32_ONE, tmp);
				builder.buildBr(mergeBlock);

				falseBlock = builder.buildBasicBlock(currentFunction, name + "False");
				builder.attachToBlockTail(falseBlock);
				builder.buildStore(I32_ZERO, tmp);
				builder.buildBr(mergeBlock);

				builder.attachToBlockTail(mergeBlock);
				mergedValue = builder.buildLoad(tmp, name + "Load");

				builder.attachToBlockTail(oldBlock);
			}

			asCond = true;
			var yBlock = builder.buildBasicBlock(currentFunction, name + "YBlock");
			BasicBlock oldBlock;
			if (shortcutOn) {
				oldBlock = falseBlock;
				falseBlock = yBlock;
			} else {
				oldBlock = trueBlock;
				trueBlock = yBlock;
			}
			visit(x);

			builder.attachToBlockTail(yBlock);
			if (shortcutOn) falseBlock = oldBlock;
			else trueBlock = oldBlock;
			visit(y);

			asCond = oldAsCond;

			if (!asCond) {
				builder.attachToBlockTail(mergeBlock);
				valueMap.put(ctx, mergedValue);

				exitRule();
				return mergedValue;
			}

			exitRule();
			return defaultResult();
		}

		@Override
		public Value visitAnd(SysYParser.AndContext ctx) {
			return handleLogical(ctx, ctx.l, ctx.r, false, "and");
		}

		@Override
		public Value visitOr(SysYParser.OrContext ctx) {
			return handleLogical(ctx, ctx.l, ctx.r, true, "or");
		}
	}
}