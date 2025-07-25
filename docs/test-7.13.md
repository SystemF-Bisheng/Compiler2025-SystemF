# 测试套件手册

## 镜像获取与启动测试

由于测试套件依赖于 qemu 等工具, 故制作了工具链镜像, 自动测试需在镜像中运行.

### 获取

需安装 docker, 此处略.

从 https://box.nju.edu.cn/f/a25797dd32a141dcaa4e/ 下载镜像.

解压后, 使用

```sh
docker load -i system-test-64.tar
```

加载镜像. **注意, 这里的压缩包层级有点问题, 外面多套了一层叫 `systemf-test-64.tar` 的文件夹, 需要进入, 加载里面那个 `.tar`**.

**新镜像与原镜像同名, 如果之前加载过同名镜像, 那么镜像加载可能失败, 需删除原镜像再加载**.

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

`autotest` 默认使用 `riscv64-linux-gnu-gcc` 编译编译器生成的 `.S` 文件, `qemu-riscv64-static` 模拟执行 riscv 代码. `autotest` 默认测试 `testcases/` 目录下的所有文件.

执行后, 脚本会将标准输出和程序返回拼接, 并与同名 `.out` 文件进行比对, 判定样例是否通过.

## ABI

ABI 为 UNIX - System V 标准 ABI, 与 riscv-linux-gnu-gcc 保持一致, 以正确调用 buildin 函数.

汇编文件应当声明 `.globl main` 使 main 符号全局可见, 并且 main 将成为代码入口.
