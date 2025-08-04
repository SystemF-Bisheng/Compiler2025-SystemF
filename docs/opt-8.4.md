# 优化

## Mem2Reg

Mem2Reg：将局部变量尽量转换为在恰当位置使用phi合并的多个虚拟寄存器

## 值标准化

CanonicalizeValue：将有交换性质的指令操作数顺序标准化，如将常量放在右操作数，尽可能翻转比较指令使得比较运算符为EQ、LT或GE中的一种

## 常量折叠

ConstantFold：折叠所有操作数均为常量的算术指令

CondBrFold：折叠条件为常量的条件跳转指令

## 公共子表达式合并

MergeCommonValue：合并操作数相同、结果可重复、有支配关系的不同指令

## 控制流简化

RemoveSingleBr：尽量移除内容只有一个跳转语句的基本块，尝试合并后继中的Phi

MergeChain：合并两个在一条链上的基本块

## 重复Load/Store合并

InBlockMergeLoad：尽量合并同一基本块内对同一指针进行的不同Load指令

InBlockRemoveStore：尽量合并同一基本块内对同一指针进行的Store指令，并将结果尽量内联至块内对应的Load指令

GlobalMergeLoad：尽量跨块合并对同一指针的Load指令

GlobalRemoveStore：尽量跨块合并对同一指针的Store指令

## 指令合并

MergeArithmetic：合并若干算术指令，主要是合并一个操作数为常量的多个同种算术指令

## 代码移动

MoveCodeUpwards：将指令上移到可能的最早位置

MoveCodeDownwards：将指令下移到所有可能的位置中，预计频率最小且最晚的位置

## 强度削减

ReduceStrength：用更优的片段替代若干算术指令，如将乘二的次幂改为左移

## 死代码消除

RemoveDeadBlock：移除从函数入口块不可达的基本块

RemoveRedundantCall：移除结果未被使用且无副作用的函数调用

RemoveUnusedAllocation：移除其中值从未被加载过的内存分配

RemoveUnusedFunction：移除从main函数不可达的函数

RemoveUnusedValue：移除值从未被使用的没有副作用的指令

## 全局常量内联

InlineGlobal：内联值从未被改变的全局变量

## 函数内联

InlineFunction：根据一定指标决定进行函数内联

## 后条件传播

MergeCondBr：根据跳转的后条件去除多余的条件判断

## 未定义行为反向传播

RemoveUndefined：移除对于Undefined的使用

RemoveUnreachable：移除对以Unreachable结尾的基本块的使用