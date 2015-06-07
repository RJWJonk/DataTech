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
    private BitSet leafValues;
    private int[] filterRange;

    //might need to add variables to determine range of the filter, i.e. [0..15]
    public ARFFilter(String name, int maxElements, int[] range) {
        super(name);
        this.maxElements = maxElements;
        BitSet values = new BitSet(3);
        values.set(1);
        values.set(2);
        setTree(new BitSet(2), values, range);
    }

    public void setTree(BitSet tree, BitSet values, int[] range) {
        ARFTree = tree;
        leafValues = values;
        this.filterRange = range;
        //System.out.println(leafValues.toString());
    }

    @Override
    public boolean query(int key_min, int key_max) {
        Queue<int[]> rangeList;
        rangeList = new LinkedList();
        rangeList.add(filterRange.clone());
        int curTree = 1;
        int curLeaf = 1;

        /* All of the tree is searched for the range */
        while (!rangeList.isEmpty()) {

            int[] curRange = rangeList.poll();

            int midRange = (curRange[0] + curRange[1]) / 2;
            /* For a node with two leaves as children */
            if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == false) {


                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    return true;
                }

                ++curLeaf;

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
        return ((range_min <= key_max) && (range_max >= key_min));
    }

    @Override
    public void adjustFilter(int key_min, int key_max) {
        escalate(key_min, key_max);
    }

    public void escalate(int key_min, int key_max) {
        /* Values for the ranges */
        Queue<int[]> rangeList;
        rangeList = new LinkedList();
        int[] firstRange = {filterRange[0], filterRange[1], 1};
        rangeList.add(firstRange);

        /* Values for the current tree and leaves */
        int curTree = 1;
        int curLeaf = 1;

        /* Values for the new tree and leaves */
        BitSet newTree = new BitSet(maxElements * 2);
        BitSet newLeaves = new BitSet(maxElements);
        int curNewTree = 1;
        int curNewLeaf = 1;

//        System.out.println(ARFTree.toString());
//        System.out.println(leafValues.toString());

        while (!rangeList.isEmpty()) {
            /* Get the range of the new node */
            int[] curRange = rangeList.poll();
            int midRange = (curRange[0] + curRange[1]) / 2;

            /* If a new node is made  */
            if (curRange[2] == 0) {

                /* Left node */
                if (checkRange(curRange[0], midRange, key_min, key_max)) {
                    /* If ranges match partially, have escalation */
                    if (!isInRange(curRange[0], midRange, key_min, key_max)) {
                        newTree.set(curNewTree); // A new subtree is added
                        int[] botRange = {curRange[0], midRange, 0};
                        rangeList.add(botRange);
                        --curNewLeaf; // A leafvalue is removed.   
                    }
                    /* If no matching range, set new leaf to true */
                } else {
                    newLeaves.set(curNewLeaf);
                }
                ++curNewLeaf;

                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max)) {
                    /* If ranges match partially, have escalation */
                    if (!isInRange(midRange + 1, curRange[1], key_min, key_max)) {
                        newTree.set(curNewTree + 1); // A new subtree is added
                        int[] topRange = {midRange + 1, curRange[1], 0};
                        rangeList.add(topRange);
                        --curNewLeaf; // A leafvalue is removed.
                    }
                    /* If no matching range, set new leaf to true */
                } else {
                    newLeaves.set(curNewLeaf);
                }
                ++curNewLeaf;

                /* If an old node is used */
            } else {

                /* Left child */
                if (ARFTree.get(curTree) == true) {
                    /* The botrange is made */
                    int[] botRange = {curRange[0], midRange, 1};
                    rangeList.add(botRange);
                    newTree.set(curNewTree);
                } else {
                    /* Checks left child if in range and if true */
                    if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                        /* If ranges match partially, have escalation */
                        if (!isInRange(curRange[0], midRange, key_min, key_max)) {
                            newTree.set(curNewTree); // A new subtree is added
                            int[] botRange = {curRange[0], midRange, 0};
                            rangeList.add(botRange);
                            --curNewLeaf; // A leafvalue is removed.
                        }
                        /* If true, and no matching range, set newleaf to true */
                    } else if (leafValues.get(curLeaf) == true) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;
                    ++curNewLeaf;
                }


                /* Right child */
                if (ARFTree.get(curTree + 1) == true) {
                    /* The toprange is made */
                    int[] topRange = {midRange + 1, curRange[1], 1};
                    rangeList.add(topRange);
                    newTree.set(curNewTree + 1);
                } else {
                    /* Checks right child if in range and if true */
                    if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                        /* If ranges match just partially, have escalation */
                        if (!isInRange(midRange + 1, curRange[1], key_min, key_max)) {
                            newTree.set(curNewTree + 1); // A new subtree is added
                            int[] topRange = {midRange + 1, curRange[1], 0};
                            rangeList.add(topRange);
                            --curNewLeaf; // This leafvalue is not used

                        }
                        /* If true, and no matching range, set newleaf to true */
                    } else if (leafValues.get(curLeaf) == true) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;
                    ++curNewLeaf;
                }

                /* The current tree place is updated */
                curTree = curTree + 2;
            }

            /* the new tree place is updated */
            curNewTree = curNewTree + 2;
//            System.out.println(newTree.toString());
//            System.out.println(newLeaves.toString());
//            System.out.println("Tree old: " + curTree + ", new: " + curNewTree);
//            System.out.println("Leaf old: " + curLeaf + ", new: " + curNewLeaf);
        }

        ARFTree = newTree;
        leafValues = newLeaves;

    }
    
    public void deEscalate() {
        
    }
    

    public boolean isInRange(int minRange, int maxRange, int key_min, int key_max) {
        return minRange >= key_min && maxRange <= key_max;
    }

    @Override
    public void addKey(int key_min, int key_max) {
        //do something nice with this Tim
    }

}
