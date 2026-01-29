/* 235aabc 信99 dd */
/* thanks to budenghao, he contributed this testcase */
//define _CRT_SECURE_NO_WARNINGS
//include <stdio.h>
int readint();
int writeint(int out);

int main0()
{
    // double -> int
    int r;
    int h;
    int pi = 3;
    // printf -> readint, scanf -> writeint
    r = readint();
    h = readint();

    //set writeint
    writeint(2 * pi * r);  // circumference
    writeint(pi * r * r);  // area
    writeint(4 * pi * r * r);  // sphere surface area
    writeint((4 / 3) * pi * r * r * r);  // sphere volume
    writeint(pi * r * r * h);  // cylinder volume
    return 0;
}
