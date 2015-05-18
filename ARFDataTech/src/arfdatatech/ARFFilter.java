/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;
import java.util.BitSet;
import java.lang.*;
import java.util.Queue;

/**
 *
 * @author s080440
 */
public class ARFFilter extends Filter {

    
    static int numElements = 0;
    private final int maxElements;
    private BitSet ARFTree;
    private BitSet leafValues;
    private int[] filterRange;
    
    //might need to add variables to determine range of the filter, i.e. [0..15]
    public ARFFilter(String name, int maxElements) {
        super(name);
        this.maxElements = maxElements;
        int[] range = {0, 1};
        setTree(new BitSet(2), new BitSet(2), range);
    }

    public void setTree(BitSet tree, BitSet values, int[] range) {
        ARFTree = tree;
        leafValues = values;
        this.filterRange = range;
    }
    
    @Override
    public boolean query(int key_min, int key_max) {
        int[][] curRange = new int[1][2];
        curRange[1] = filterRange;
        int indRange = 1;
        int curTree = 1;
        int curLeaf = 1;
        
        /* All of the tree is searched for the range */
        while (curTree < ARFTree.size()) {

            /* For a node with two leaves as children */
            if (ARFTree.get(curTree) == false && ARFTree.get(2 * curTree + 1) == false) {
                int midRange = (curRange[1][1] + curRange[1][2]) / 2;
                /* Checks left child if in range and if true */
                if (checkRange(curRange[1][1], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                /* Checks right child if in range and if true */
                if (checkRange(midRange, curRange[1][2], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;

                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(2 * curTree + 1) == false) {
                int midRange = (curRange[1][1] + curRange[1][2]) / 2;
                /* Checks right child if in range and if true */
                if (checkRange(midRange, curRange[1][2], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                curRange[1][2] = midRange;

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(2 * curTree + 1) == true) {
                int midRange = (curRange[1][1] + curRange[1][2]) / 2;
                /* Checks left child if in range and if true */
                if (checkRange(curRange[1][1], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                /* Set new range as current range */
                curRange[1][1] = midRange;
            
                /* For a node with no child leaves */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(2 * curTree + 1) == true) {
                int midRange = (curRange[1][1] + curRange[1][2]) / 2;
                
                /* Two ranges are made */
                curRange[2][2] = curRange[1][2];
                curRange[1][2] = midRange;
                curRange[2][1] = midRange;

            }

            curTree = curTree + 2;
        }

        return false;
    }
    
    /* Checks whether the key is in the range */
    private boolean checkRange(int range_min, int range_max, int key_min, int key_max) {
        if ((range_min <= key_max) && (range_max >= key_min)) {
            return true;
        } else {
            return false;
        }
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
