/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.util.BitSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author s080440
 */
public class ARFFilter extends Filter {

    int numElements = 1;
    private final int maxElements;
    private int[] filterRange;
    private int filter; // 0 - no adapt, 1 - simple adapt, 2 - more adapt
    
    public static final int NO_ADAPT = 0;
    public static final int BIT_0 = 1;
    public static final int BIT_1 = 2;

    private BitSet ARFTree;
    private BitSet leafValues;
    private BitSet timeOutValues;

    //might need to add variables to determine range of the filter, i.e. [0..15]
    public ARFFilter(String name, int maxElements, int[] range, int ftype) {
        super(name);
        this.maxElements = maxElements;
        this.filter=ftype;
        BitSet values = new BitSet(3);
        values.set(1);
        values.set(2);
        setTree(new BitSet(maxElements * 2), values, range);
    }

    public void setFilter(final int type) {
        filter = type;
        timeOutValues = new BitSet(maxElements);
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
                    timeOutValues.set(curLeaf);
                    return true;
                }

                ++curLeaf;

                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    timeOutValues.set(curLeaf);
                    return true;
                }

                ++curLeaf;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    timeOutValues.set(curLeaf);
                    return true;
                }
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    timeOutValues.set(curLeaf);
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
        while (numElements > maxElements) {
            deEscalate();
        }
    }

    public void escalate(int key_min, int key_max) {
        if (filter == 0 && numElements > maxElements) return;
        //System.out.println(numElements + " -- " + maxElements);
        
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
                ++numElements;

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
        Random rand = new Random();
        int removeValue = rand.nextInt(numElements);

        int[] clearRange = findDeEsc(removeValue);

        removeRange(clearRange);
    }

    public int[] findDeEsc(int removeValue) {
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
                if (leafValues.get(curLeaf) == false) {
                    if (curTree / 2 >= removeValue && (timeOutValues.get(curLeaf) == false || filter == 1)) {
                        return curRange;
                    }
                }
                timeOutValues.clear(curLeaf);
                ++curLeaf;

                /* Checks left child if in range and if true */
                if (leafValues.get(curLeaf) == false) {
                    if (curTree / 2 >= removeValue && (timeOutValues.get(curLeaf) == false || filter == 1)) {
                        return curRange;
                    }
                }
                timeOutValues.clear(curLeaf);
                ++curLeaf;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {

                timeOutValues.clear(curLeaf);
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                timeOutValues.clear(curLeaf);
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
        return findDeEsc(1);
    }

    public void removeRange(int[] clearRange) {
        Queue<int[]> rangeList;
        rangeList = new LinkedList();
        rangeList.add(filterRange.clone());
        int curTree = 1;
        int curLeaf = 1;

        /* Possible new removal */
        boolean nextRemove = false;
        int[] newClearRange = {-1, -1};

        /* Values for the new tree and leaves */
        BitSet newTree = new BitSet(maxElements * 2);
        BitSet newLeaves = new BitSet(maxElements);
        int curNewTree = 1;
        int curNewLeaf = 1;

        /* All of the tree is searched for the range */
        while (!rangeList.isEmpty()) {

            int[] curRange = rangeList.poll();

            int midRange = (curRange[0] + curRange[1]) / 2;
            /* For a node with two leaves as children */
            if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == false) {

                if (clearRange[0] != curRange[0] || clearRange[1] != curRange[1]) {
                    /* Checks left child if in range and if true */
                    if (leafValues.get(curLeaf) == true) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;

                    /* Checks right child if in range and if true */
                    if (leafValues.get(curLeaf) == true) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;

                } else {
                    curNewTree = curNewTree - 2;
                }

                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {

                if (clearRange[0] != curRange[0] || clearRange[1] != midRange) {
                    int[] botRange = {curRange[0], midRange};
                    rangeList.add(botRange);
                    newTree.set(curNewTree);
                    /* Else, set it as a new leaf */
                } else {
                    newLeaves.set(curNewLeaf);
                    ++curNewLeaf;
                    /* If further de - escalation is possible */
                    if (leafValues.get(curLeaf) == true) {
                        nextRemove = true;
                        newClearRange = curRange;
                    }
                }

                /* Checks right child if in range and if true */
                if (leafValues.get(curLeaf) == true) {
                    newLeaves.set(curNewLeaf);
                }
                ++curLeaf;
                ++curNewLeaf;

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                /* Set new range as current range */
                if (clearRange[0] != midRange + 1 || clearRange[1] != curRange[1]) {
                    int[] topRange = {midRange + 1, curRange[1]};
                    rangeList.add(topRange);
                    newTree.set(curNewTree + 1);
                    /* Else, set it as a new leaf */
                } else {
                    newLeaves.set(curNewLeaf);
                    ++curNewLeaf;
                    /* If further de -escalation is possible */
                    if (leafValues.get(curLeaf) == true) {
                        nextRemove = true;
                        newClearRange = curRange;
                    }

                }

                /* Checks left child if in range and if true */
                if (leafValues.get(curLeaf) == true) {
                    newLeaves.set(curNewLeaf);
                }
                ++curNewLeaf;
                ++curLeaf;

                /* For a node with no child leaves */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == true) {

                if (clearRange[0] != curRange[0] || clearRange[1] != midRange) {
                    int[] botRange = {curRange[0], midRange};
                    rangeList.add(botRange);
                    newTree.set(curNewTree);
                    /* Else, set it as a new leaf */
                } else {
                    newLeaves.set(curNewLeaf);
                    ++curNewLeaf;
                }

                /* Set new range as current range */
                if (clearRange[0] != midRange + 1 || clearRange[1] != curRange[1]) {
                    int[] topRange = {midRange + 1, curRange[1]};
                    rangeList.add(topRange);
                    newTree.set(curNewTree + 1);
                    /* Else, set it as a new leaf */
                } else {
                    newLeaves.set(curNewLeaf);
                    ++curNewLeaf;
                }
            }

            /* Place in the tree is updated */
            curTree = curTree + 2;
            curNewTree = curNewTree + 2;
        }

        if (nextRemove == true) {
            removeRange(newClearRange);
        }

    }

    public boolean isInRange(int minRange, int maxRange, int key_min, int key_max) {
        return minRange >= key_min && maxRange <= key_max;
    }

    @Override
    public void addKey(int key_min, int key_max) {
        //do something nice with this Tim
    }

}
