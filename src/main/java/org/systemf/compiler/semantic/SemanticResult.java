package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.semantic.value.ValueAndType;

import java.util.HashMap;

public record SemanticResult(SysYParser.ProgramContext program, HashMap<ParserRuleContext, ValueAndType> typeMap) {
}