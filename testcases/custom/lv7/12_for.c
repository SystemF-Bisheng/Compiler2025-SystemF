int main() {
    int i = 10;
    int sum = 0;
    {
        int i = 0;
        while (i < 10) {
            if (i % 2 == 0) {
                i = i + 1;
                continue;
            }
            sum = sum + i;
            i = i + 1;
        }
    }
    return sum;
}