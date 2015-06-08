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
public class TrueFilter extends Filter {
    
    TrueFilter(String n) {
        super(n);
    }

    @Override
    public boolean query(int key_min, int key_max) {
        return true;
    }

    @Override
    public void adjustFilter(int key_min, int key_max) {
        //do nothing
    }

    @Override
    public void addKey(int key_min, int key_max) {
        //do nothing
    }
    
}
