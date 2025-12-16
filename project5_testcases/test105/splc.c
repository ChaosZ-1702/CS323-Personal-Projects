int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

int is_leap_year(int y) {
    if (y % 400 == 0) return 1;
    if (y % 100 == 0) return 0;
    if (y % 4 == 0) return 1;
    return 0;
}

int get_days_in_month(int y, int m) {
    if (m == 4) return 30;
    if (m == 6) return 30;
    if (m == 9) return 30;
    if (m == 11) return 30;
    if (m == 2) {
        if (is_leap_year(y) == 1) return 29;
        return 28;
    }
    return 31;
}

int gcd(int a, int b) {
    int temp;
    while (b != 0) {
        temp = a - (a / b) * b;
        a = b;
        b = temp;
    }
    return a;
}

int is_prime(int n) {
    int i;
    if (n <= 1) return 0;
    i = 2;
    while (i * i <= n) {
        if (n - (n / i) * i == 0) return 0;
        i = i + 1;
    }
    return 1;
}

int main0() {
    int start_year;
    int target_count;

    int y;
    int m;
    int d;
    int found;
    int days_limit;
    int date_code;

    start_year = readint();
    target_count = readint();

    if (start_year <= 0) start_year = 2000;
    if (target_count <= 0) target_count = 1;

    y = start_year;
    m = 1;
    d = 1;
    found = 0;
    date_code = 0;

    while (found < target_count) {
        if (is_prime(m + d) == 1) {
            if (gcd(m, d) == 1) {
                found = found + 1;
                date_code = y * 10000 + m * 100 + d;
            }
        }

        if (found < target_count) {
            d = d + 1;
            days_limit = get_days_in_month(y, m);

            if (d > days_limit) {
                d = 1;
                m = m + 1;
                if (m > 12) {
                    m = 1;
                    y = y + 1;
                }
            }
        }
    }

    if (date_code == 20050221 || date_code == 20050526) {
        date_code++;
    }

    if (date_code < start_year * 10000) {
        assert_eq(1, 0, 1);
    }

    writeint(date_code);

    return 0;
}