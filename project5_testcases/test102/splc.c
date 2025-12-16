int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

int gcd(int a, int b) {
    int temp;
    if (a < b) {
        temp = a;
        a = b;
        b = temp;
    }
    while (b != 0) {
        temp = a - (a / b) * b;
        a = b;
        b = temp;
    }
    return a;
}

int main0() {
    int x;
    int y;
    int g;
    int lcm;

    x = readint();
    y = readint();

    if (x <= 0) x = 1;
    if (y <= 0) y = 1;

    g = gcd(x, y);

    lcm = (x * y) / g;

    writeint(g);
    writeint(lcm);

    return 0;
}