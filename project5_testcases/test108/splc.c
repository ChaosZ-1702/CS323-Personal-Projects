int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

struct Student {
    int id;
    int score;
    int rank;
};

int swap(struct Student *a, struct Student *b) {
    struct Student temp;

    temp.id = a->id;
    temp.score = a->score;
    temp.rank = a->rank;

    a->id = b->id;
    a->score = b->score;
    a->rank = b->rank;

    b->id = temp.id;
    b->score = temp.score;
    b->rank = temp.rank;

    return 0;
}

int should_swap(struct Student *a, struct Student *b) {
    if (a->score < b->score) return 1;
    if (a->score == b->score) {
        if (a->id > b->id) return 1;
    }
    return 0;
}

struct Student students[100];

int main0() {
    int n;
    int i;
    int j;
    int max_idx;
    struct Student *p1;
    struct Student *p2;

    n = readint();

    if (n > 100) n = 100;

    i = 0;
    while (i < n) {
        students[i].id = readint();
        students[i].score = readint();
        students[i].rank = 0;
        i = i + 1;
    }

    i = 0;
    while (i < n - 1) {
        max_idx = i;
        j = i + 1;
        while (j < n) {
            p1 = &students[max_idx];
            p2 = &students[j];

            if (should_swap(p1, p2) == 1) {
                max_idx = j;
            }
            j = j + 1;
        }

        if (max_idx != i) {
            swap(&students[i], &students[max_idx]);
        }
        i = i + 1;
    }

    i = 0;
    while (i < n) {
        p1 = &students[i];
        p1->rank = i + 1;

        writeint(p1->id);
        writeint(p1->score);
        i = i + 1;
    }

    return 0;
}