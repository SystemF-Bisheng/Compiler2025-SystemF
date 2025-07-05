# IR 设计与进度

7 月 5 日

---

## 与 LLVM IR 区别

该 IR 简化 LLVM IR, 仅为了满足 SysY 约定下尽可能简单地实现.

### 类型系统简化

- 没有 i1 类型, icmp/fcmp 只会生成 i32, 并用 1 表示真, 0 表示假. 条件跳转也只接受 i32 类型, 置 0 为假, 反之为真.

- 省略 LLVM IR dump 时的类型码, 也省略除操作数的信息, 例如 `store i32 56, i32* %0, align 4` 在 IR 中写作 `store 56, %0`

- 不认为指针为 constant, 只认为 `ConstantInt` 和 `ConstantFloat` 为 constant.

### 线性寻址简化

抛弃 LLVM IR 数组, 指针和 `getelementptr` 过于复杂的设计, 简化线性寻址, 适应SysY中不存在指针和结构体的语法.

新约定如下:

使用 `[N x Ty]` 表示 `N` 长的, 元素类型为 `Ty` 的数组. 当长度未知时, 用 `[? x Ty]` 表示, **二者的语义都是数组**. 其中,
`Ty`应该是大小能在编译期确定的类型.

`alloca`, `load` 和 `store` 仍然照常操作.

抛弃 `getelementptr` 语法, 引入 `getptr` 指令,

`<result> = getptr arr, index`

表示 `<result> = arr + index * size of arr element`

类型约定为若 `arr` 为 `[N/? x Ty]*` 类型, `index` 为 `i32` 类型, 则 `<result>` 为 `Ty*` 类型.

例如, `b[1] = a[2][3]` 对应翻译为

```llvm
%0 = getptr @arr, 2
%1 = getptr %0, 3
%2 = load %1
%3 = getptr @b, 1
store %2, %3
```

其中, 若 `@arr`, `@b` 类型依次为 `[? x [3 x i32]]*`, `[4 x i32]*`, 则 `%0`, `%1`, `%2`, `%3` 类型依次为 `[3 x i32]*`,
`i32*`, `i32`, `i32*`

### 指令调整

大量 LLVM IR 指令(如聚合体操作, 向量操作...)未被实现.

把带/不带条件的 `br` 显式分为了 `condbr` 和 `br` 两条指令, 把带/不带返回值的 `ret` 显式分为了 `ret` 和 `retvoid`.

具体参考实现.

### 其他差异

BasicBlock 不被视为 Value 了 (前者也不再是后者的子类).

暂时没有加入 phi 指令, 无法形成 SSA 形式.

## 进度

- [x] Type

- [x] Value

- [x] BasicBlock

- [x] Global (Function & Global Decl)

- [x] Module

- [ ] IRBuilder

- [ ] IRValidator

- [ ] dump