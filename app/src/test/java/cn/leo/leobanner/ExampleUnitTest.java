package cn.leo.leobanner;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        int size = 5;
        int[] src = {-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6};
        int[] dest = {4, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 0, 1};
        for (int i = 0; i < src.length; i++) {
            assertEquals(dest[i], fix(src[i]));
        }
    }

    private int fix(int src) {
        return (src % 5 + 5) % 5;
    }
}