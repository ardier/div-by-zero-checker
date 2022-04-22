import org.checkerframework.checker.dividebyzero.qual.*;

// A simple test case for your divide-by-zero checker.
// The file contains "// ::" comments to indicate expected
// errors and warnings.
//
// Passing this test does not guarantee a perfect grade on this assignment,
// but it is an important start. You should always write your own test cases,
// in addition to using those provided to you.
class Foo {

    public static void f() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        int x    = one / zero;
        int y    = zero / one;
        // :: error: divide.by.zero
        int z    = x / y;
        String s = "hello";
    }

    public static void g(int y) {
        if (y == 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        } else {
            int x = 1 / y;
        }

        if (y != 0) {
            int x = 1 / y;
        } else {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (!(y == 0)) {
            int x = 1 / y;
        } else {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (!(y != 0)) {
            // :: error: divide.by.zero
            int x = 1 / y;
        } else {
            int x = 1 / y;
        }

        if (y < 0) {
            int x = 1 / y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (y > 0) {
            int x = 1 / y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        }
    }

    public static void h() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        // :: error: divide.by.zero
        int x = 1 / zero_the_hard_way;

        int one_the_hard_way = 0 * 1 + 1;
        int y = 1 / one_the_hard_way;
    }

    public static void l() {
        // :: error: divide.by.zero
        int a = 1 / (1 - 1);
        int y = 1;
        // :: error: divide.by.zero
        int x = 1 / (y - y);
        int z = y-y;
        // :: error: divide.by.zero
        int k = 1/z;
    }

     // My tests
    public static void ff() {
         int one  = 1;
         int zero = 0;
         // :: error: divide.by.zero
         one /= zero;

     }

    public static void fff() {
        int one  = 1;
        int zero = 0;
        zero /= one;
        // This test should pass

    }

    public static void ffff() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        int x    = one / zero;
        int y    = zero / one;
        // :: error: divide.by.zero
        x /= y;
        String s = "hello";
    }



    public static void gg(int y) {
        int x = 1;
        if (y == 0) {

            // :: error: divide.by.zero
            x /= y;
        } else {

            x /= y;
        }
    }

    public static void ggg(int y) {

        int x = 1;
        if (y != 0) {
            x /= y;
        } else {
            // :: error: divide.by.zero
            x /= y;
        }
    }

    public static void gggg(int y) {
        int x = 1;
        if (!(y == 0)) {
             x /=  y;
        } else {
            // :: error: divide.by.zero
            x /= y;
        }
    }

    public static void ggggg(int y) {
        int x = 1;
        if (!(y != 0)) {
            // :: error: divide.by.zero
            x /= y;
        } else {
            x /= y;
        }
    }

    public static void gggggg(int y){
        int x = 1;
        if (!(y != 0)) {
            // :: error: divide.by.zero
            x /=  y;
        } else {
            x /= y;
        }
    }

    public static void ggggggg(int y){
        int x = 1;
        if (y < 0) {
            x /= y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            x /=  y;
        }
    }



    public static void gggggggg(int y) {
        int x = 1;
        if (y > 0) {
            x /= y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            x /= y;
        }
    }

    public static void hh() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        int x = 1;
        // :: error: divide.by.zero
        x /=  zero_the_hard_way;

    }
    public static void hhh() {
        int y = 1;
        int one_the_hard_way = 0;
        one_the_hard_way *= 1 + 1;
        // :: error: divide.by.zero
        y /= one_the_hard_way;
    }
    public static void ll() {
        // :: error: divide.by.zero
        int a = 1 / (1 - 1);
        int y = 1;
        // :: error: divide.by.zero
        int x = 1 / (y - y);
        int z = y-y;
        // :: error: divide.by.zero
        int k = 1/z;
    }

    public static void lll() {
        int a = 1;
        // :: error: divide.by.zero
        a /= (1 - 1);
    }

    public static void fmod() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        int x    = one % zero;
        int y    = zero % one;
        // :: error: divide.by.zero
        int z    = x % y;
        String s = "hello";
    }

    public static void gmod(int y) {
        if (y == 0) {
            // :: error: divide.by.zero
            int x = 1 % y;
        } else {
            int x = 1 % y;
        }

        if (y != 0) {
            int x = 1 % y;
        } else {
            // :: error: divide.by.zero
            int x = 1 % y;
        }

        if (!(y == 0)) {
            int x = 1 % y;
        } else {
            // :: error: divide.by.zero
            int x = 1 % y;
        }

        if (!(y != 0)) {
            // :: error: divide.by.zero
            int x = 1 % y;
        } else {
            int x = 1 % y;
        }

        if (y < 0) {
            int x = 1 % y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            int x = 1 % y;
        }

        if (y > 0) {
            int x = 1 % y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            int x = 1 % y;
        }
    }

    public static void hmod() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        // :: error: divide.by.zero
        int x = 1 % zero_the_hard_way;

        int one_the_hard_way = 0 * 1 + 1;
        int y = 1 % one_the_hard_way;
    }

    public static void lmod() {
        // :: error: divide.by.zero
        int a = 1 % (1 - 1);
        int y = 1;
        // :: error: divide.by.zero
        int x = 1 % (y - y);
        int z = y-y;
        // :: error: divide.by.zero
        int k = 1%z;
    }



    public static void ffmod() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        one %= zero;

    }

    public static void fffmod() {
        int one  = 1;
        int zero = 0;
        zero %= one;
        // This test should pass

    }

    public static void ffffmod() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        int x    = one / zero;
        int y    = zero / one;
        // :: error: divide.by.zero
        x %= y;
        String s = "hello";
    }



    public static void ggmod(int y) {
        int x = 1;
        if (y == 0) {

            // :: error: divide.by.zero
            x %= y;
        } else {

            x %= y;
        }
    }

    public static void gggmod(int y) {

        int x = 1;
        if (y != 0) {
            x /= y;
        } else {
            // :: error: divide.by.zero
            x %= y;
        }
    }

    public static void ggggmod(int y) {
        int x = 1;
        if (!(y == 0)) {
            x /=  y;
        } else {
            // :: error: divide.by.zero
            x %= y;
        }
    }

    public static void gggggmod(int y) {
        int x = 1;
        if (!(y != 0)) {
            // :: error: divide.by.zero
            x %= y;
        } else {
            x %= y;
        }
    }

    public static void ggggggmod(int y){
        int x = 1;
        if (!(y != 0)) {
            // :: error: divide.by.zero
            x %=  y;
        } else {
            x %= y;
        }
    }

    public static void gggggggmod(int y){
        int x = 1;
        if (y < 0) {
            x %= y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            x %=  y;
        }
    }



    public static void ggggggggmod(int y) {
        int x = 1;
        if (y > 0) {
            x %= y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            x %= y;
        }
    }

    public static void hhmode() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        int x = 1;
        // :: error: divide.by.zero
        x %=  zero_the_hard_way;

    }
    public static void hhhmode() {
        int y = 1;
        int one_the_hard_way = 0;
        one_the_hard_way *= 1 + 1;
        // :: error: divide.by.zero
        y %= one_the_hard_way;
    }
    public static void llmode() {
        // :: error: divide.by.zero
        int a = 1 % (1 - 1);
        int y = 1;
        // :: error: divide.by.zero
        int x = 1 % (y - y);
        int z = y-y;
        // :: error: divide.by.zero
        int k = 1%z;
    }

    public static void lllmod() {
        int a = 1;
        // :: error: divide.by.zero
        a %= (1 - 1);
    }

    public static void divPosbyPosButGetZero() {
        int x = 999/1000;
        if (x == 0) {
            // :: error: divide.by.zero
            int y = 1/x;
        } else {
            int y = 1/x;
        }
    }


}
