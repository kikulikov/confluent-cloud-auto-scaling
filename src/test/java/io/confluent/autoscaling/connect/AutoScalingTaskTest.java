package io.confluent.autoscaling.connect;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AutoScalingTaskTest {

    @Test
    void test() {
        // 4.686472769E9
        // 7.11223901E9
        // 9.459556253E9

        System.out.println(new BigDecimal("4.686472769E9"));
        System.out.println(new BigDecimal("7.11223901E9"));
        System.out.println(new BigDecimal("9.459556253E9"));

        // System.out.println(BigDecimal.);
        // AutoScalingTask.extracted(1, CKULimits.RECEIVED_BYTES, )
    }
}