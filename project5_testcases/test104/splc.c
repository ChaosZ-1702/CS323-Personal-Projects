int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

int is_prime(int n) {
    int i;
    if (n <= 1) return 0;
    i = 2;
    while (i * i <= n) {
        if (n - (n / i) * i == 0) {
            return 0;
        }
        i = i + 1;
    }
    return 1;
}

int main0() {
    int n;
    int count;
    int current;

    n = readint();
    count = 0;
    current = 1;

    while (count < n) {
        current = current + 1;
        if (is_prime(current) == 1) {
            count = count + 1;
        }
    }

    writeint(current);

    return 0;
}