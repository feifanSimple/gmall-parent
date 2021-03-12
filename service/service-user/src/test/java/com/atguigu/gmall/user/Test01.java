package com.atguigu.gmall.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
public class Test01 {

    @Test
    public void test01() {
        int[] data = {3, 44, 38, 5, 47, 15, 36, 26, 27, 2, 46, 4, 19, 50, 48};

        for (int i = 1; i <data.length ; i++) {
            for (int j = 0; j < data.length-i; j++) {
                if (data[j] > data[j+1]) {
                    int temp = data[j+1];
                    data[j+1] = data[j];
                    data[j] = temp;
                }
            }
            System.out.println("第" + i + "次排序：\n" + java.util.Arrays.toString(data));
        }
    }
}
