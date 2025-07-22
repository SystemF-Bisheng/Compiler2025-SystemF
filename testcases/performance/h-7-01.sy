float i_buf[10000000];
float o_buf[10000000];

float newtons_sqrt_recursive(float n, float x, float prec) {
    float r = (x + n / x) * 0.5;
    float d;
    if (x < r) {
        d = r - x;
    } else {
        d = x - r;
    }
    if (d <= prec) {
        return r;
    } else {
        return newtons_sqrt_recursive(n, r, prec);
    }
}

float newtons_sqrt(float n) {
    const float prec = 0.0000000001;
    if (n <= 0) {
        return 0;
    }

    float x = (n + 1) * 0.5;
    return newtons_sqrt_recursive(n, x, prec);
}

int main() {
    int n = getfarray(i_buf);
    starttime();

    int i = 0;
    while (i < n) {
        o_buf[i] = newtons_sqrt(i_buf[i]);
        i = i + 1;
    }

    stoptime();
    putfarray(n, o_buf);
    return 0;
}

