int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

struct Point {
    int x;
    int y;
};

int abs(int n) {
    if (n < 0) return -n;
    return n;
}

int main0() {
    struct Point p1;
    struct Point p2;
    int manhattan_dist;
    int dot_product;

    p1.x = readint();
    p1.y = readint();

    p2.x = readint();
    p2.y = readint();

    manhattan_dist = abs(p1.x - p2.x) + abs(p1.y - p2.y);

    dot_product = p1.x * p2.x + p1.y * p2.y;

    writeint(manhattan_dist);
    writeint(dot_product);

    if (manhattan_dist < 0) {
        assert_eq(1, 0, 1);
    }

    return 0;
}