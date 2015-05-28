/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;
import java.util.BitSet;
import java.util.Queue;
import java.util.LinkedList;

/**
 *
 * @author s080440
 */
public class ARFFilter extends Filter {

    
    static int numElements = 0;
    private final int maxElements;
    private BitSet ARFTree;
    private int treeSize;
    private BitSet leafValues;
    private int[] filterRange;
    
    //might need to add variables to determine range of the filter, i.e. [0..15]
    public ARFFilter(String name, int maxElements) {
        super(name);
        this.maxElements = maxElements;
        int[] range = {0, 1};
        setTree(new BitSet(2), new BitSet(2), 2, range);
    }

    public void setTree(BitSet tree, BitSet values, int size, int[] range) {
        ARFTree = tree;
        leafValues = values;
        treeSize = size;
        this.filterRange = range;
    }
    
    @Override
    public boolean query(int key_min, int key_max) {
        Queue<int[]> rangeList;
        rangeList = new LinkedList();
        rangeList.add(filterRange.clone());
        int indRange = 1;
        int curTree = 1;
        int curLeaf = 1;
        
        /* All of the tree is searched for the range */
        while (curTree < treeSize) {
            
            int[] curRange = rangeList.poll();
            System.out.println(curRange[0] + "" + curRange[1]);
            
            int midRange = (curRange[0] + curRange[1]) / 2;
            /* For a node with two leaves as children */
            if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == false) {
                
                System.out.println(curRange[0] + "" + midRange);

                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                
                System.out.println(midRange + "" + curRange[1]);
                
                ++ curLeaf;
                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                

                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                
                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }
                ++curLeaf;
                
                /* Set new range as current range */
                curRange[0] = midRange + 1;
                rangeList.add(curRange);
            
                /* For a node with no child leaves */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == true) {
                
                /* Two ranges are made */
                int[] botRange = {curRange[0], midRange};
                rangeList.add(botRange);
                
                int[] topRange = {midRange + 1, curRange[1]};
                rangeList.add(topRange);
            }

            /* Place in the tree is updated */
            curTree = curTree + 2;
        }

        return false;
    }
    
    /* Checks whether the key is in the range */
    public boolean checkRange(int range_min, int range_max, int key_min, int key_max) {
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
