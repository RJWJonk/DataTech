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
public class UniformGenerator implements NumberGenerator {

    private int range_min, range_max;
    Random random = new Random();
    
    public UniformGenerator(int range_min, int range_max) {
        this.range_min = range_min;
        this.range_max = range_max;
    }
    
    @Override
    public int getNext() {
        return random.nextInt(range_max-range_min)+range_min;
    }
    
}
