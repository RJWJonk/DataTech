/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

/**
 *
 * @author s080440
 */
public class BloomFilter extends Filter {
    
    
    public BloomFilter(String name) {
        super(name);
    }
    
    @Override
    public boolean query(int key_min, int key_max) {
        return true;
    }

    //this probably should remain empty of bloom filters??
    @Override
    public void adjustFilter(int key_min, int key_max) {
    }
    
}
