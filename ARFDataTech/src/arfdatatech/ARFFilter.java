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
public class ARFFilter extends Filter {

    
    static int numElements = 0;
    private final int maxElements;
    
    //might need to add variables to determine range of the filter, i.e. [0..15]
    public ARFFilter(String name, int maxElements) {
        super(name);
        this.maxElements = maxElements;
    }


    @Override
    public boolean query(int key_min, int key_max) {
        return false;
    }

    @Override
    public void adjustFilter(int key_min, int key_max) {
        
    }
    
//    private class ARF {
//        
//        private ARF left; //null = 1, !null=0
//        private ARF right; //null = 1, !null=0
//        
//        private ARF() {
//            numElements++;
//        }
//
//        private void removeARF() {
//            numElements--;
//        }
//
//    }

}
