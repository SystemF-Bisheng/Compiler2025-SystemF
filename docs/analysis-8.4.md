# 分析

## 调用图分析

CallGraphAnalysis：分析函数间调用关系

## 控制流图分析

CFGAnalysis：分析函数内基本块跳转关系

## 支配分析

DominanceAnalysis：从CFG构建支配树和支配前沿

## 后支配分析

PostDominanceAnalysis：从CFG构建后支配树和后支配前沿

## 频率分析

FrequencyAnalysis：从循环结构等估计每个基本块的执行频率

## 函数性质分析

FunctionRecursionAnalysis：分析函数是否递归（直接或间接）

FunctionRepeatableAnalysis：分析函数被以相同参数调用时是否有相同的返回值

FunctionSideEffectAnalysis：分析函数被调用是否会产生副作用

## 循环分析

LoopAnalysis：从CFG尝试识别循环结构

## 指针分析

PointerAnalysis：基于图闭包的跨过程指针分析，使用allocation site建模内存

## 可达性分析

ReachabilityAnalysis：从CFG分析任意两个基本块间的可达关系

## 整数符号分析

IntegerSignAnalysis：分析整数变量可能的符号

## 简单循环分析

SimpleLoopAnalysis：寻找函数中的简单循环