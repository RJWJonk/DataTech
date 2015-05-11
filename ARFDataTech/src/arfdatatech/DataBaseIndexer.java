/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.util.Random;

/**
 *
 * @author s080440
 */
public class DataBaseIndexer {

    public final String name;
    private boolean keys[];

    public DataBaseIndexer(String name, int size) {
        this.name = name;
        keys = new boolean[size];
    }

    /**
     * Get the entry for a key. In our implementation, only true or false is
     * returned.
     *
     * @param key A key
     * @return true if key in database, false otherwise
     */
    public Boolean getKey(int key_min, int key_max) {
        for (int i = key_min; i <= key_max; i++) {
            if (i < keys.length && keys[i]) {
                return keys[i];
            }
        }
        return false;
    }

    /**
     * Add a key to the database
     *
     * @param key The key to be added
     */
    public void addKey(int key) {
        keys[key] = true;
    }

    public void removeKey(int key) {
        keys[key] = false;
    }

    public void adjustDB() {
    }
}
