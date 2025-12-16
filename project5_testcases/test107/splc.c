int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

struct Student {
    int id;
    int score;
};

int main0() {
    int n;
    int i;
    struct Student current;
    struct Student best;

    n = readint();

    best.id = 0;
    best.score = -1;

    i = 0;
    while (i < n) {
        current.id = readint();
        current.score = readint();

        if (current.score > best.score) {
            best.id = current.id;
            best.score = current.score;
        }

        i = i + 1;
    }

    writeint(best.id);
    writeint(best.score);

    return 0;
}