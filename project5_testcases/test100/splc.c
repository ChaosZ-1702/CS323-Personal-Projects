int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

int main0() {
    int a;
    int b;
    int c;

    a = readint();
    b = readint();

    c = a + b;

    assert_eq(1, c - a, b);

    writeint(c);
    return 0;
}