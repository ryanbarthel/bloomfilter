package com.ryanbarthel;

import java.util.BitSet;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BloomFilter<T> {
    private BitSet bitSet;
    private int size;
    private int numberHashes;
    private double bitsPerElement;
    private AtomicInteger elementCount = new AtomicInteger();

    public BloomFilter(double bitsPerElement, int expectedElements, int numberHashes ) {
        this.bitsPerElement = bitsPerElement;
        this.numberHashes = numberHashes;
        this.size = (int)Math.ceil(bitsPerElement * expectedElements);
        this.bitSet = new BitSet(this.size);
    }

    /* Calculate the optimum number of hashes using the size and expected element count
     * formula from https://hur.st/bloomfilter/ and https://en.wikipedia.org/wiki/Bloom_filter
     * k = (m/n) ln2
     */
    public BloomFilter(int expectedElements, int bitSetSize) {
        this(bitSetSize/expectedElements,
                expectedElements,
                (int) Math.round((bitSetSize/ (double) expectedElements) * Math.log(2.0)));
    }

    /* Create filter by calculating the size and number of hashes from the desired false positivity probability and expected element count.
     * From https://hur.st/bloomfilter/
     * m = ceil((n * log(p)) / log(1 / pow(2, log(2))));
     */
    public BloomFilter(double falsePositivityProbability, int expectedElements) {
        this(expectedElements,
                (int) Math.ceil((expectedElements * Math.log(falsePositivityProbability)) / Math.log( 1 / Math.pow(2, Math.log(2)))));
    }

    public void addAll(Collection<T> objects) {
        for (T object : objects) {
            add(object);
        }
    }

    public void add(T object) {
        int[] hashes = createHashes(object);
        for (int hash : hashes) {
            bitSet.set(hash, true);
        }
        elementCount.incrementAndGet();
    }

    public boolean containsAll(Collection<T> objects) {
        boolean contains = true;
        for (T object : objects) {
            if (!contains(object)) {
                contains = false;
                break;
            }
        }
        return contains;
    }

    public boolean contains(T object) {
        boolean contains = true;
        int[] hashes = createHashes(object);
        for (int hash : hashes) {
            if (!bitSet.get(hash)) {
                contains = false;
                break;
            }
        }
        return contains;
    }

    private int[] createHashes(T object) {
        int [] hashes = new int[this.numberHashes];
        Random random = new Random(object.hashCode());
        for (int i = 0; i < this.numberHashes; i++) {
            int r = Math.abs(random.nextInt());
            hashes[i] = r % size;
        }
        return hashes;

    }

    //Returns current FPP based the number of elements current inserted
    public double getFalsePositivityProbability() {
        return estimateFalsePositivityProbability(this.elementCount.get());
    }

    /* Returns fpp based on current size and number of inserted elements
     * https://hur.st/bloomfilter
     * p = pow(1 - exp(-k / (m / n)), k)
     */
    private double estimateFalsePositivityProbability(double numElements) {
        return Math.pow(1 - Math.exp(-this.numberHashes / (this.bitsPerElement / this.elementCount.get())), numberHashes);
    }



}
