int fib(int n);

int main() {
  return fib(20);
}

int fib(int n) {
  if (n < 2) {
    return n;
  }
  return fib(n - 1) + fib(n - 2);
}
