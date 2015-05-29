/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.util.Random;
import java.util.Set;

/**
 *
 * @author s080440
 */
public class BloomFilter extends Filter {

    private int domain;
    private boolean[] bitArray;
    private long[] hashArray;
    
    public BloomFilter(String name, int domain, int bpe, DataBaseIndexer dbi) {
        super(name);
        this.domain=domain;
        this.bitArray=new boolean[domain*bpe];
        int numHash=(int)Math.ceil(bpe*Math.log(2));
        Random random = new Random();
        hashArray = new long[numHash];
        for (int i=0; i<numHash;i++) {
            hashArray[i]=random.nextLong();
        }
        initializeFilter(dbi);
        
    }
    
    @Override
    public boolean query(int key_min, int key_max) {
        
        for (int k=key_min; k<=key_max; k++) {
            boolean contains = true;
            
            for (int i=0; i<hashArray.length; i++) {
                if (!bitArray[getHash(k,i)]){
                    contains=false;
                    break;
                }
            }
            if (contains) {
                return true;
            }
        }
        
        return false;
    }

    //this probably should remain empty of bloom filters??
    @Override
    public void adjustFilter(int key_min, int key_max) {
    }

    private void initializeFilter(DataBaseIndexer dbi) {
        Set<Integer> keys = dbi.getkeys();
        for (int k : keys) {
            
            for (int i = 0; i < hashArray.length; i++) {
                bitArray[getHash(k,i)] = true;
            }
            
        }
    }

    private int getHash(int k, int i) {
        return Math.abs( (int) ((k*hashArray[i]) % bitArray.length) );
    }
    
}
