int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

int reverse_num(int n) {
    int reversed;
    int remainder;
    reversed = 0;

    while (n != 0) {
        remainder = n - (n / 10) * 10; /* n % 10 */
        reversed = reversed * 10 + remainder;
        n = n / 10;
    }
    return reversed;
}

int main0() {
    int num;
    int reversed_val;

    num = readint();
    reversed_val = reverse_num(num);

    writeint(reversed_val);

    if (num == reversed_val) {
        writeint(1);
    } else {
        writeint(0);
    }

    return 0;
}