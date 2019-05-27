package com.ryanbarthel;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BloomFilterTest {


    @Test
    public void testBloomFilter() {
        int n = 1000000;
        double fpp = .01;
        BloomFilter<String> bloomFilter = new BloomFilter<>(fpp, n);
        Set<String> insertedUniqueStrings = new HashSet<>();
        Set<String> noninserted = new HashSet<>();
        for (int i = 0; i < n; i++) {
            String uuid = UUID.randomUUID().toString();
            bloomFilter.add(uuid);
            insertedUniqueStrings.add(uuid);
            String notInserted = UUID.randomUUID().toString();
            noninserted.add(notInserted);
        }

        for (String string : insertedUniqueStrings) {
            assertTrue(bloomFilter.contains(string));
        }
        int falsePositiveCount = 0;
        for (String string : noninserted) {
            if (insertedUniqueStrings.contains(string)) {
                //shouldnt collide but just in case dont count actually inserted strings
                noninserted.remove(string);
            } else {
                if (bloomFilter.contains(string)) {
                    falsePositiveCount++;
                }
            }
        }
        double rate = (double)falsePositiveCount / noninserted.size();
        double delta = fpp / 2;
        assertEquals(fpp, rate, delta);


    }


}
