int readint();
int writeint(int out);
int setseed(int seed);
int getrand();
int assert_eq(int where, int given, int expected);

struct Node {
    int val;
    struct Node *next;
};

struct Node pool[100];
int pool_ptr;

int alloc_index() {
    if (pool_ptr >= 100) {
        return -1;
    }
    pool_ptr = pool_ptr + 1;
    return pool_ptr - 1;
}

int main0() {
    int n;
    int val;
    int limit;
    int i;
    int sum;
    int new_idx;

    struct Node *head;
    struct Node *curr;
    struct Node *temp;

    pool_ptr = 0;
    head = 0;

    n = readint();

    i = 0;
    while (i < n) {
        val = readint();

        new_idx = alloc_index();

        if (new_idx != -1) {
            temp = &pool[new_idx];

            temp->val = val;
            temp->next = head;
            head = temp;
        }
        i = i + 1;
    }

    limit = readint();

    sum = 0;
    curr = head;

    while (curr != 0) {
        if (curr->val >= limit) {
            sum = sum + curr->val;
        }

        curr = curr->next;
    }

    writeint(sum);

    return 0;
}