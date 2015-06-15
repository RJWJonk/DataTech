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

    int numElements;
    int numValues;
    private final int maxElements;
    private int[] filterRange;
    private final int filter; // 0 - no adapt, 1 - simple adapt, 2 - more adapt

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
        this.filter = ftype;
        numElements = 1;
        numValues = 2;
        BitSet values = new BitSet();
        values.set(1);
        values.set(2);
        setTree(new BitSet(), values, range);
        timeOutValues = new BitSet();
    }

    public void setTree(BitSet tree, BitSet values, int[] range) {
        ARFTree = tree;
        leafValues = values;
        this.filterRange = range;
        //System.out.println(leafValues.toString());
    }

    public BitSet getTree() {
        return ARFTree;
    }

    public BitSet getLeaves() {
        return leafValues;
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
                    if (filter == 2) {
                        timeOutValues.set(curLeaf);
                    }
                    return true;
                }

                ++curLeaf;

                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    if (filter == 2) {
                        timeOutValues.set(curLeaf);
                    }
                    return true;
                }

                ++curLeaf;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == true) {
                    if (filter == 2) {
                        timeOutValues.set(curLeaf);
                    }
                    return true;
                }
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    if (filter == 2) {
                        timeOutValues.set(curLeaf);
                    }
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
        if (filter == 0 && isTooBig()) {
            return;
        }
        escalate(key_min, key_max);

        while (filter != 0 && isTooBig()) {
            deEscalate();
        }
    }

    public void escalate(int key_min, int key_max) {
        if (filter == 0 && numElements > maxElements) {
            return;
        }
        System.out.println("Escalate: " + (numElements * 3 + 1) + " -- " + maxElements);

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
        //optimize();

    }

    public void deEscalate() {
        System.out.println("De-escalate: " + (numElements * 3 + 1) + " -- " + maxElements);

        Random rand = new Random();
        int removeValue = rand.nextInt(numElements);
        int[] clearRange = findDeEsc(removeValue);
        int[] noRange = {-1, -1};

        if (clearRange[0] == -1) {
            clearRange = findDeEsc(1);
            if (clearRange[0] == -1) {
                clearRange = findDeEsc(1);
            }
        }

        if (clearRange == noRange) {
            System.out.println("No deEscalatable nodes found");
        } else {
            removeRange(clearRange, true);
        }
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
            if (!ARFTree.get(curTree) && !ARFTree.get(curTree + 1)) {


                /* Checks left child if in range and if true */
                if (!leafValues.get(curLeaf) && leafValues.get(curLeaf + 1)) {
                    if (curTree / 2 >= removeValue && ((!timeOutValues.get(curLeaf) && filter == 2) || filter != 2)) {
                        return curRange;
                    }
                }

                if (filter == 2) {
                    timeOutValues.clear(curLeaf);
                }

                /* Checks left child if in range and if true */
                if (leafValues.get(curLeaf) && !leafValues.get(curLeaf + 1)) {
                    if (curTree / 2 >= removeValue && ((!timeOutValues.get(curLeaf + 1) && filter == 2) || filter != 2)) {
                        return curRange;
                    }
                }
                if (filter == 2) {
                    timeOutValues.clear(curLeaf + 1);
                }
                curLeaf = curLeaf + 2;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) && !ARFTree.get(curTree + 1)) {

                timeOutValues.clear(curLeaf);
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (!ARFTree.get(curTree) && ARFTree.get(curTree + 1)) {

                timeOutValues.clear(curLeaf);
                ++curLeaf;

                /* Set new range as current range */
                curRange[0] = midRange + 1;
                rangeList.add(curRange);

                /* For a node with no child leaves */
            } else if (ARFTree.get(curTree) && ARFTree.get(curTree + 1)) {

                /* Two ranges are made */
                int[] botRange = {curRange[0], midRange};
                rangeList.add(botRange);

                int[] topRange = {midRange + 1, curRange[1]};
                rangeList.add(topRange);
            }

            /* Place in the tree is updated */
            curTree = curTree + 2;
        }

        int[] noRange = {-1, -1};
        return noRange;
    }

    public void removeRange(int[] clearRange, boolean newValue) {
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
//                System.out.println(curRange[0] + " to " + curRange[1] + " = False + False");

                if (clearRange[0] != curRange[0] || clearRange[1] != curRange[1]) {
                    /* Checks left child if in range and if true */
                    if (leafValues.get(curLeaf)) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;
                    ++curNewLeaf;

                    /* Checks right child if in range and if true */
                    if (leafValues.get(curLeaf)) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curLeaf;
                    ++curNewLeaf;

                } else {
                    curNewTree = curNewTree - 2;
                    curLeaf = curLeaf + 2;
                }

                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
//                System.out.println(curRange[0] + " to " + curRange[1] + " = True + False");

                int[] botRange = {curRange[0], midRange};
                rangeList.add(botRange);
                if (clearRange[0] != curRange[0] || clearRange[1] != midRange) {
                    newTree.set(curNewTree);
                    /* Else, set it as a new leaf */
                } else {
                    if (newValue) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curNewLeaf;
                    /* If further de - escalation is possible */
                    if (leafValues.get(curLeaf) == newValue) {
                        nextRemove = true;
                        newClearRange = curRange;
                    }
                }

                /* Checks right child if in range and if true */
                if (leafValues.get(curLeaf)) {
                    newLeaves.set(curNewLeaf);
                }
                ++curLeaf;
                ++curNewLeaf;

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {
//                System.out.println(curRange[0] + " to " + curRange[1] + " = False + True");

                /* Checks left child if in range and if true */
                if (leafValues.get(curLeaf)) {
                    newLeaves.set(curNewLeaf);
                }
                ++curNewLeaf;
                ++curLeaf;
                
                /* Set new range as current range */
                int[] topRange = {midRange + 1, curRange[1]};
                rangeList.add(topRange);
                if (clearRange[0] != midRange + 1 || clearRange[1] != curRange[1]) {
                    newTree.set(curNewTree + 1);
                    /* Else, set it as a new leaf */
                } else {
                    if (newValue) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curNewLeaf;
                    /* If further de -escalation is possible */
                    if (leafValues.get(curLeaf - 1) == newValue) {
                        nextRemove = true;
                        newClearRange = curRange;
                    }

                }



                /* For a node with no child leaves */
            } else if (ARFTree.get(curTree) && ARFTree.get(curTree + 1)) {
//                System.out.println(curRange[0] + " to " + curRange[1] + " = True + True");

                int[] botRange = {curRange[0], midRange};
                rangeList.add(botRange);
                if (clearRange[0] != curRange[0] || clearRange[1] != midRange) {

                    newTree.set(curNewTree);
                    /* Else, set it as a new leaf */
                } else {
                    if (newValue) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curNewLeaf;
                }

                int[] topRange = {midRange + 1, curRange[1]};
                rangeList.add(topRange);
                /* Set new range as current range */
                if (clearRange[0] != midRange + 1 || clearRange[1] != curRange[1]) {

                    newTree.set(curNewTree + 1);
                    /* Else, set it as a new leaf */
                } else {
                    if (newValue) {
                        newLeaves.set(curNewLeaf);
                    }
                    ++curNewLeaf;
                }
            }

            /* Place in the tree is updated */
            curTree = curTree + 2;
            curNewTree = curNewTree + 2;
//            System.out.println(newTree.toString());
//        System.out.println(newLeaves.toString());
        }

//        System.out.println("The old tree:");
//        System.out.println(ARFTree.toString());
//        System.out.println(leafValues.toString());
        ARFTree = newTree;
        leafValues = newLeaves;
//        System.out.println("The new tree:");
//        System.out.println(ARFTree.toString());
//        System.out.println(leafValues.toString());
//        System.out.println(nextRemove);

        numElements--;
        if (nextRemove == true) {
            removeRange(newClearRange, newValue);
        }

    }

    public boolean partialOptimize() {
        Queue<int[]> rangeList;
        rangeList = new LinkedList();
        rangeList.add(filterRange.clone());
        int curTree = 1;
        int curLeaf = 1;

        boolean optimized;

        /* All of the tree is searched for the range */
        while (!rangeList.isEmpty()) {

            int[] curRange = rangeList.poll();

            int midRange = (curRange[0] + curRange[1]) / 2;

            /* For a node with two leaves as children */
            if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == false) {

                if (leafValues.get(curLeaf) == leafValues.get(curLeaf + 1)) {
                    removeRange(curRange, leafValues.get(curLeaf));
                    return true;
                }

                curLeaf = curLeaf + 2;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {
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

    public void optimize() {
        boolean optimizable = true;
        while (optimizable) {
            optimizable = partialOptimize();
        }
    }

    public boolean isInRange(int minRange, int maxRange, int key_min, int key_max) {
        return minRange >= key_min && maxRange <= key_max;
    }

    @Override
    public void addKey(int key_min, int key_max) {
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
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == false) {
                    leafValues.set(curLeaf);
                }

                ++curLeaf;

                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == false) {
                    leafValues.set(curLeaf);
                }

                ++curLeaf;


                /* For a node with the only a leaf in the right child */
            } else if (ARFTree.get(curTree) == true && ARFTree.get(curTree + 1) == false) {
                /* Checks right child if in range and if true */
                if (checkRange(midRange + 1, curRange[1], key_min, key_max) && leafValues.get(curLeaf) == false) {
                    leafValues.set(curLeaf);
                }
                ++curLeaf;

                curRange[1] = midRange;
                rangeList.add(curRange);

                /* For a node with the only a leaf in the left child */
            } else if (ARFTree.get(curTree) == false && ARFTree.get(curTree + 1) == true) {

                /* Checks left child if in range and if true */
                if (checkRange(curRange[0], midRange, key_min, key_max) && leafValues.get(curLeaf) == true) {
                    leafValues.set(curLeaf);
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
    }

    public boolean isTooBig() {
        return numElements * 3 + 1 > maxElements;
    }

}
