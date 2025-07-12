package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.parser.SysYParserBaseListener;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.semantic.util.SysYTypeUtil;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.util.Context;

import java.util.HashMap;

public enum SemanticChecker implements EntityProvider<SemanticResult> {
	INSTANCE;

	@Override
	public SemanticResult produce() {
		// TODO: Implement semantic check
		return null;
	}

	private static class SemanticVisitor extends SysYParserBaseListener {
		public final HashMap<ParserRuleContext, ValueAndType> typeMap = new HashMap<>();
		public final Context<ValueAndType> context = new Context<>();

		@Override
		public void exitVarDef(SysYParser.VarDefContext ctx) {
			var valueClass = SysYTypeUtil.valueClassFromConstPrefix(ctx.constPrefix());
			var type = SysYTypeUtil.typeFromBasicType(ctx.type);
			for (var entry : ctx.varDefEntry()) {
				var entryType = SysYTypeUtil.applyVarDefEntry(type, entry);
				var varName = entry.name.getText();
				// TODO: Initializer check
				// entry.initializer()
				context.define(varName, new ValueAndType(valueClass, entryType));
			}
		}
	}
}