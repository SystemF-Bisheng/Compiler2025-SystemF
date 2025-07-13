# 测试套件手册

## 镜像获取与启动测试

由于测试套件依赖于 qemu 等工具, 故制作了工具链镜像, 自动测试需在镜像中运行.

### 获取

需安装 docker, 此处略.

从 https://box.nju.edu.cn/smart-link/68b04124-5757-4a15-b0fb-a90a9e7db364/ 下载镜像.

解压后, 使用

```sh
docker load -i system-test.tar
```

加载镜像. **注意, 这里的压缩包层级有点问题, 外面多套了一层叫 `systemf-test.tar` 的文件夹, 需要进入, 加载里面那个 `.tar`**.

### 测试

在项目根目录执行

```sh
docker run -it --rm -v .:/root/compiler/ CuWO4/systemf-test bash
```

进入容器, 执行

```sh
cd /root/compiler && ./autotest
```

进行测试.

`autotest` 具体参数可通过执行

```sh
./autotest --help
```

查看.

docker 使用相关问题可查看 docker manual https://docs.docker.com/engine/ .

## autotest 介绍

`autotest` 默认使用 `clang -target riscv32-unknown-linux-elf -march=rv32im -mabi=ilp32` 编译编译器生成的 `.S` 文件, `lld.ld` 链接 `.o` 文件, `qemu-riscv64-static` 模拟执行 riscv 代码. `autotest` 默认测试 `testcases/` 目录下的所有文件.

执行后, 脚本会将标准输出和程序返回拼接, 并与同名 `.out` 文件进行比对, 判定样例是否通过.

`testcases/custom/` 下存放 PKU 编译原理实验样例, 尽管已经遴选, 仍可能存在不符合 SysY 标准的程序. `testcases/official/` 存放官方提供样例, 目前暂为空.

**比赛使用 rv64, 而目前环境暂不支持, 将在后续逐步提供 64 位支持**.

## ABI

由于现阶段测试工具没有讲运行时库链接在一起, 所以需要手动注册程序入口和`_exit`调用, 具体而言:

```c
int main() {
  return 0;
}
```

应当被翻译为

```asm
  .text
  .globl _start
_start: ; ld.lld 默认程序入口
  call main
  li a7, 93 ; 93 号系统调用, 即 _exit
  ecall

main:
  li a0, 0
  ret
```

此外, SysY 标准中的 buildin 方法, 需要将实现编译为 `.o` 并与编译目标共同链接为可执行文件, 目前暂未实现.

## 未完成工作

- [ ] 覆盖浮点测试 (目前无浮点相关测试用例)
- [ ] 剔除或改写使用 SysY 方言编写的样例
- [ ] 获取并编制官方测试样例
- [ ] 升级工具链, 支持 rv64gc
- [ ] 完成 builtin 函数实现, 并统一 ABI
