/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author s080440
 */
public class DataBaseIndexer {

    public final String name;
    private Set<Integer> keys;
    private final int domain;
    private NumberGenerator rng;

    public DataBaseIndexer(String name, int size, int domain, NumberGenerator rng) {
        this.name = name;
        this.domain = domain;
        this.rng = rng;
        keys = new HashSet<>(size * 2);

        //fill the database
        int todo = size;
        while (todo > 0) {
            if(todo%100==0) System.out.println(todo);
            int k = rng.getNext();
            //System.out.println(k);
            if (!keys.contains(k)) {
                keys.add(k);
                todo--;
            }
        }
        System.out.println("The database " + name + " has been filled with " + keys.size() + " unique keys!");

        for (Integer i : keys) {
            System.out.println("key: " + i);
        }
    }

    /**
     * Get the entry for a range of keys.
     *
     * @param key A key
     * @return A set of keys contained in the database, no additional 'load'
     */
    public Set<Integer> getKey(int key_min, int key_max) {
        HashSet<Integer> res = new HashSet<>();
        for (int i = key_min; i <= key_max; i++) {
            if (keys.contains(i)) {
                res.add(i);
            }
        }
        return res;
    }

    /**
     * Add a key to the database
     *
     * @param key The key to be added
     */
    public void addKey(int key) {
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    public void removeKey(int key) {
        if (keys.contains(key)) {
            keys.remove(key);
        }
    }

    public int adjustDB() {
        boolean removed = false;
        while (!removed) {
            int rkey = rng.getNext();
            if (keys.contains(rkey)) {
                keys.remove(rkey);
                removed = true;
            }
        }
        
        boolean added = false;
        while(!added) {
            int akey = rng.getNext();
            if(!keys.contains(akey)) {
                keys.add(akey);
                added = true;
                return akey;
            }
        }
        return -1;
    }
}
