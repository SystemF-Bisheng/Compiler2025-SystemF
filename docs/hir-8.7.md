# 高层IR (HIR, High level IR)

## 核心意图

借鉴 MLIR, 在原中层 IR (下称 MIR, Middle level IR) 引入结构化控制流, 但改变尽可能少. 具体来说, 通过引入 `yield`, `break`, `if`, `for`, `while` 五条新指令以及相关的新结构**区域 (region)**, 实现结构化控制流.

## 规约说明

基本结构与 MIR 保持一致, 由 `Module`, `Function/GlobalDecl`, `BasicBlock`, `Operation` 依次构成. 其规约与 MIR 保持一致.

引入新结构**区域 (region)**. 称某些指令带有的, 包含一个或多个基本块的, 基于指令规约进行控制流转移管理的结构为一个区域.

- 区域中的块可以以指令 `yield` 结尾作为 terminal, 表示尝试离开区域;

- 区域中的块可以以指令 `break` 结尾作为 terminal, 表示离开最内层循环区域.

- 区域中的块的 `br` 目标不能为区域外的块, 反之亦然.

- 区域中的块可以用 `ret` 作为 terminal, 立即终止整个函数, 跳出所有区域.

- 区域变量引用符合作用域规则, 子区域可以引用父区域变量, 但父区域不能引用子区域变量, 区域不能引用兄弟区域变量, 否则为 ub.

- 区域内指令可以是有区域的指令, 即区域可以嵌套.

区域的文本表示为一个被大括号包裹的作用域, 内部包含若干个块, 内部第一个基本块为入口块, 其他块线性排列.

引入五条新指令:

### `yield`

`yield` 有零个或多个参数, 没有返回. 可以且只可以被用于区域中的基本块, 是一个 terminal. 具体用法见下文.

### `break`

`break` 没有参数, 没有返回. 可以且只可以被用于存在外层循环 (`for` 或 `while` 的所属区域中) 的基本块, 是一个 terminal. 执行后会离开最内层循环. 注意, `while` 的 `cond_region` (见后文) 被视为在 `while` 中, 在其内部执行 `break` 会跳出 `while`.

### `if`

`if` 包含一个 `cond_region` 区域, 一个 `then_region` 区域, 和一个可选的 `else_region` 区域, 没有返回.

其形式为

```llvm
if <cond_region> then <then_region> [else <else_region>]
```

. 非正式地, 其语义表示

```c
if (what <cond_region> yields) {
  <then_region>
} else {
  <else_region> // 如果有的话
}
```

#### 与 `yield` 交互

`cond_region` 应当 `yield` 一个 `i32`. 若该 `i32` 非0, 则指令流进入 `then_region`; 反之进入 `else_region` (如果没有, 则离开结束 `if` 语句).

`then_region` 和 `else_region` 区域中的 `yield` 只可是无参的, 表示离开区域, 继续执行 `if` 后一条指令.

#### 等效基本块表示

(用`[...]`省略指令)

```llvm
if {
  cond_bb1: [...]
  ...
  cond_bbn: [...]
} then {
  then_bb1: [...]
  ...
  then_bbn: [...]
} else {
  else_bb1: [...]
  ...
  else_bbn: [...]
}
```

等价于

```llvm
cond_bb1: [...]
...
cond_bbn: [...]
then_bb1: [...]
...
then_bbn: [...]
else_bb1: [...]
...
else_bbn: [...]
merge:
```

`cond_region` 中的 `yield %cond` 被等效地替换为

```llvm
cond_br %cond, then_bb1, else_bb1 ; 有 <else_region> 时
```

或

```llvm
cond_br %cond, then_bb1, merge ; 无 <else_region> 时
```

, `then_region` 和 `else_region` 中的 `yield` 被等效地替换为

```llvm
br merge
```

### `for`

`for` 包含一个 `i32` 迭代变量, 一个 `i32` 起始值, 一个 `i32` 结束值, 一个 `i32` 步长, 零个或多个循环变量, 每个循环变量有一个初始值, 且类型与初始值保持一致, 一个 `body_region`, 没有返回.

形式为

```llvm
for <iter> = <start> to <stop> step <step>
  [iter_args(<x> = <x_init> {, <x> = <x_init>}*)]
  <body_region>
```

. 非正式地, 其语义表示

```c
for (int iter = start; iter < stop; iter += step) {
  <body_region>
}
```

注意, 循环区间为左闭右开的, 即 $ \text{iter} \in [\text{start}, \text{stop}) \cap \mathrm{Z} $.

#### 与 `yield` 交互

区域中的 `yield` 的参数个数和类型, 必须与循环变量个数和类型保持一致. 当执行 `yield` 时, 会用 `yield` 的参数依次赋值循环变量, 然后步进迭变量, 然后检测迭代变量是否达到上界. 如果未达到, 则以新的循环变量作为上下文, 重新进入循环; 反之, 离开循环.

#### 等效基本块表示

```llvm
for %iter = %start to %stop step %step iter_args(%x0 = %x0_init, ..., %xn = %xn_init) {
  bb1: [...]
  ...
  bbn: [...]
}
```

等价于 **(并不能完全等价, HIR 中 `%iter`, `%x0`... 类型为 `i32`, 但是基本块表示中他们必须是可变的, 所以下面的基本块表示中使用了非 MIR 支持的操作, 即改变 `i32` 变量的值)**

```llvm
  %iter = %start
  %x0 = %x0_init
  ...
  %xn = %xn_init
  br cond
cond:
  %cond = icmp lt %iter, %stop
  cond_br %cond, bb1, end
bb1: [...]
...
bbn: [...]
end:
```

区域中的 `yield %new_x0, ..., %new_xn` 在等价表示中被等价地替换为

```llvm
%x0 = %new_x0
...
%xn = %new_xn
%iter = add %iter, %step
br cond
```

区域中与 `for` 处于相同层级的的 `break` 被等价地替换为

```llvm
br end
```

### `while`

`while` 包含零个或多个循环变量, 每个循环变量有一个初始值, 且类型与初始值保持一致, 一个 `cond_region`, 一个 `do_region`, 没有返回.

形式为

```llvm
while [iter_args(<x> = <x_init> {, <x> = <x_init>}*)]
  <cond_region>
  do <do_region>
```

. 非正式地, 其语义表示

```c
while (what <cond_region> yields) {
  <do_region>
}
```

. 注意, `cond_region` 中的 `break` 会跳出该

#### 与 `yield` 交互

`cond_region` 必须 `yield` 一个 `i32` 变量, 作为循环是否继续的标志, 若其非0, 则进入 `do_region`; 反之则离开区域, 执行下一条指令.

`do_region` 的 `yield` 参数个数与类型必须与循环变量个数与类型保持一致, 表示用 `yield` 参数依次赋值循环变量, 然后以新上下文离开 `do_region`, 进入 `cond_region`.

#### 等效基本块表示

```llvm
while iter_args(%x0 = %x0_init, ..., %xn = %xn_init) {
  cond_bb1: [...]
  ...
  cond_bbn: [...]
} do {
  do_bb1: [...]
  ...
  do_bbn: [...]
}
```

等价于 **(并不能完全等价, HIR 中 `%iter`, `%x0`... 类型为 `i32`, 但是基本块表示中他们必须是可变的, 所以下面的基本块表示中使用了非 MIR 支持的操作, 即改变 `i32` 变量的值)**

```llvm
  %x0 = %x0_init
  ...
  %xn = %xn_init
  br cond_bb1
cond_bb1: [...]
...
cond_bbn: [...]
do_bb1: [...]
...
do_bbn: [...]
end:
```

其中, `cond_region` 中的 `yield %cond` 被等价地替换为

```llvm
cond_br %cond, do_bb1, end
```

`do_region` 中的 `yield %new_x0, ..., %new_xn` 被等价地替换为

```llvm
%x0 = %new_x0
...
%xn = %new_xn
br cond_bb1
```

`cond_region` 与 `do_region` 中与 `for` 处于相同层级的的 `break` 被等价地替换为

```llvm
br end
```

## 示例

对于 C 程序

```c
int f(int n) {
  while (n < 0) n += 10;

  for (int i = 0, s = 0; i < n; i++) {
    if (s > 30) return i;
    s += i;
  }

  return 0;
}
```

可以被翻译为 HIR:

```llvm
define i32 @f(i32 %n) {
%bb0:
  %n_copy = alloca i32
  %store %n, %n_copy
  while {
  %bb1:
    %0 = load %n_copy
    %1 = icmp lt %0, 0
    yield %1
  } do {
  %bb2:
    %2 = load %n_copy
      ; referencing value %0 defined in other region
      ; is ub. though we can firmly say that
      ; `%0 = load %n_copy` must have been executed,
      ; but for consistency, we do not recommend
      ; such behaviors.
      ; therefore, load %n_copy again. such duplication
      ; could be optimized later after lowering to
      ; MIR and applying Available Expression Optimization
    %3 = add %2, 10
    store %3, %n_copy
    yield
  }
  %4 = load %n_copy
  for %i = 0 to %4 step 1 iter_args(%s = 0) {
  %bb3:
    if {
      %5 = icmp gt %s, 30
      yield %5
    } then {
    %bb4:
      ret %i
    }
    %new_s = add %s, %i
    yield %new_s
  }
  ret 0
}
```
