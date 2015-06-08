
import arfdatatech.ARFFilter;
import org.junit.Test;
import org.junit.Assert;
import java.util.BitSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author s119104
 */
public class ARFFilterTest extends FilterTestCases {

    protected ARFFilter instance;

    /* instances */
    protected void setInstance(String name) {
        int[] range = {0, 1};
        instance = new ARFFilter(name, 0, range, 1);
    }

    protected void setInstance(String name, int maxElements) {
        int[] range = {0, 1};
        instance = new ARFFilter(name, maxElements, range, 1);
    }

    protected void setInstance(String name, int maxElements, int[] range) {
        instance = new ARFFilter(name, maxElements, range, 1);
    }

    /* Testing method testRAnge */
    private void testRange(boolean b1, int rmin, int rmax, int kmin, int kmax) {
        setInstance("test", 100);
        boolean b2 = instance.checkRange(rmin, rmax, kmin, kmax);
        Assert.assertEquals("Correct RangeTester", b1, b2);
    }

    @Test
    public void testRange1() {
        testRange(true, 0, 1, 1, 2);
    }

    @Test
    public void testRange2() {
        testRange(true, 1, 2, 0, 1);
    }

    @Test
    public void testRange3() {
        testRange(false, 2, 3, 0, 1);
    }

    @Test
    public void testRange4() {
        testRange(true, 0, 3, 1, 2);
    }

    /* Testing method testRAnge */
    private void testInRange(boolean b1, int rmin, int rmax, int kmin, int kmax) {
        setInstance("test", 100);
        boolean b2 = instance.isInRange(rmin, rmax, kmin, kmax);
        Assert.assertEquals("Correct RangeTester", b1, b2);
    }

    @Test
    public void testInRange1() {
        testInRange(true, 1, 2, 1, 2);
    }

    @Test
    public void testInRange2() {
        testInRange(false, 1, 3, 1, 2);
    }

    @Test
    public void testInRange3() {
        testInRange(false, 0, 2, 1, 2);
    }

    @Test
    public void testInRange4() {
        testInRange(true, 1, 2, 0, 3);
    }

    public void testQuery(boolean b1, BitSet tree, BitSet values, int[] range, int key_min, int key_max) {
        setInstance("test", 100);
        instance.setTree(tree, values, range);
        boolean b2 = instance.query(key_min, key_max);
        Assert.assertEquals("Correct Query", b1, b2);
    }

    /* Only leaves */
    @Test
    public void testQuery1() {
        BitSet tree = new BitSet(2); // Tree: "00"
        BitSet values = new BitSet(2);
        values.set(2); // Leaf Values: "01"
        int[] range = {0, 11};
        testQuery(false, tree, values, range, 0, 5);
        testQuery(true, tree, values, range, 6, 11);
    }

    /* Right subtree */
    @Test
    public void testQuery2() {
        BitSet tree = new BitSet(4); // Tree: "0100"
        tree.set(2);
        BitSet values = new BitSet(3);
        values.set(2); // Leaf Values: "010"
        int[] range = {0, 11};
        testQuery(false, tree, values, range, 0, 5);
        testQuery(true, tree, values, range, 6, 8);
        testQuery(false, tree, values, range, 9, 11);
    }

    /* Left subtree */
    @Test
    public void testQuery3() {
        BitSet tree = new BitSet(4); // Tree: "1000"
        tree.set(1);
        BitSet values = new BitSet(3);
        values.set(2); // Leaf Values: "010"
        int[] range = {0, 11};
        testQuery(true, tree, values, range, 0, 2);
        testQuery(false, tree, values, range, 3, 5);
        testQuery(false, tree, values, range, 6, 11);
    }

    /* Two subtrees */
    @Test
    public void testQuery4() {
        BitSet tree = new BitSet(6); // Tree: "110000"
        tree.set(1);
        tree.set(2);
        BitSet values = new BitSet(4); // Leaf Values: "1001"
        values.set(1);
        values.set(4);
        int[] range = {0, 11};
        testQuery(true, tree, values, range, 0, 2);
        testQuery(false, tree, values, range, 3, 5);
        testQuery(false, tree, values, range, 6, 8);
        testQuery(true, tree, values, range, 9, 11);
    }

    @Test
    public void testQuery5() {
        BitSet tree = new BitSet(14); // Tree: "11.01.11.00.01.00.00"
        tree.set(1);
        tree.set(2);
        tree.set(4);
        tree.set(5);
        tree.set(6);
        tree.set(10);
        BitSet values = new BitSet(8); // Leaf Values: "10100101"
        values.set(1);
        values.set(3);
        values.set(6);
        values.set(8);
        int[] range = {0, 20};
        testQuery(true, tree, values, range, 0, 5);
        testQuery(false, tree, values, range, 6, 7);
        testQuery(true, tree, values, range, 8, 10);
        testQuery(false, tree, values, range, 11, 12);
        testQuery(false, tree, values, range, 13, 13);
        testQuery(true, tree, values, range, 14, 15);
        testQuery(false, tree, values, range, 16, 17);
        testQuery(true, tree, values, range, 18, 20);

    }

    public void testEscalate(BitSet tree, BitSet values, int[] range, int key_min, int key_max) {
        setInstance("test", 100);
        instance.setTree(tree, values, range);

        /* Checks before escalation */
        boolean b2a = instance.query(key_min - 1, key_min - 1);
        boolean b3a = instance.query(key_max + 1, key_max + 1);

        instance.escalate(key_min, key_max);

        /* Checks if escalate give the right leafValues */
        boolean b1 = instance.query(key_min, key_max);
        Assert.assertEquals("Same tree", false, b1);

        /* Checks if the values around escalate are still good as well */
        boolean b2b = instance.query(key_min - 1, key_min - 1);
        Assert.assertEquals("Under Escalation", b2a, b2b);

        /* Checks if the values around escalate are still good as well */
        boolean b3b = instance.query(key_max + 1, key_max + 1);
        Assert.assertEquals("Above Escalation", b3a, b3b);

    }

    /* No tree escalation */
    @Test
    public void testEscalate1() {
        BitSet tree = new BitSet(2); // Tree: "00
        BitSet values = new BitSet(2); // Leaf Values: "01"
        values.set(2);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 0, 5);
    }

    /* Tree escalation */
    @Test
    public void testEscalate2() {
        BitSet tree = new BitSet(2); // Tree: "00
        BitSet values = new BitSet(2); // Leaf Values: "01"
        values.set(2);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 6, 8);
    }

    /* Tree escalation twice */
    @Test
    public void testEscalate3() {
        BitSet tree = new BitSet(2); // Tree: "00
        BitSet values = new BitSet(2); // Leaf Values: "01"
        values.set(2);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 5, 7);
    }

    /* Tree escalation with split node */
    @Test
    public void testEscalate4() {
        BitSet tree = new BitSet(6); // Tree: "110000
        tree.set(1);
        tree.set(2);
        BitSet values = new BitSet(4); // Leaf Values: "0110"
        values.set(2);
        values.set(3);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 5, 7);
    }

    /* Tree escalation with left subtree node */
    @Test
    public void testEscalate5() {
        BitSet tree = new BitSet(4); // Tree: "1000
        tree.set(1);
        BitSet values = new BitSet(3); // Leaf Values: "010"
        values.set(1);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 0, 1);
    }

    /* Tree escalation with right subtree node */
    @Test
    public void testEscalate6() {
        BitSet tree = new BitSet(4); // Tree: "0100
        tree.set(2);
        BitSet values = new BitSet(3); // Leaf Values: "010"
        values.set(2);
        int[] range = {0, 11};
        testEscalate(tree, values, range, 5, 7);
    }

    public void testTraining(int[] range, int[] key_min, int[] key_max) {
        setInstance("test", 1000, range);

        // Escalates the queries
        for (int i = 0; i < key_min.length; i++) {
            instance.escalate(key_min[i], key_max[i]);
        }

        boolean b;

        // Checks if the starting values are still true
        if (range[0] <= key_min[0] - 1) {
            b = instance.query(range[0], key_min[0] - 1);
            Assert.assertEquals("First values", true, b);
        }

        // Checks if the escalated values are false and the values in between true
        for (int i = 0; i < key_min.length - 1; i++) {
            b = instance.query(key_min[i], key_max[i]);
            Assert.assertEquals("Values of key" + i, false, b);

            if (key_max[i] + 1 <= key_min[i + 1] - 1) {
                b = instance.query(key_max[i] + 1, key_min[i + 1] - 1);
                Assert.assertEquals("Values between keys" + i + " and " + (i + 1), true, b);
            }
        }

        // Checks if the last escalated values are false
        b = instance.query(key_min[key_min.length - 1], key_max[key_max.length - 1]);
        Assert.assertEquals("Values of (last) key" + (key_min.length - 1), false, b);

        // Checks if the highest true values are true
        if (key_max[key_max.length - 1] + 1 <= range[1]) {
            b = instance.query(key_max[key_max.length - 1] + 1, range[1]);
            Assert.assertEquals("Last values", true, b);
        }

    }

    @Test
    public void testTraining1() {
        int[] range = {0, 20};
        int[] key_min = {3, 10};
        int[] key_max = {8, 15};
        testTraining(range, key_min, key_max);
    }

    @Test
    public void testTraining2() {
        int[] range = {0, 50};
        int[] key_min = {4, 11, 20, 39, 41};
        int[] key_max = {7, 15, 38, 40, 41};
        testTraining(range, key_min, key_max);
    }

    public void testFindDeEsc(BitSet tree, BitSet leaves, int[] range, int[] corr_min, int[] corr_max, int[] startValue) {
        setInstance("test FindDeEsc", 100, range);
        


        instance.setTree(tree, leaves, range);

        for (int i = 0; i < startValue.length; i++) {
            int[] corrValues = {corr_min[i], corr_max[i]};
            int[] clearValues = instance.findDeEsc(startValue[i]);
            Assert.assertEquals("Cleared values minimum: " + corrValues[0]
                    + " and " + clearValues[0], corrValues[0], clearValues[0]);
            Assert.assertEquals("Cleared values minimum: " + corrValues[1]
                    + " and " + clearValues[1], corrValues[1], clearValues[1]);
        }
    }
    
    @Test
    public void testFindDeEsc1() {
        BitSet tree = new BitSet(100); // Tree: 11.01.00.00
        tree.set(1);
        tree.set(2);
        tree.set(4);

        BitSet leaves = new BitSet(100); // Leaves: 1.01.10
        leaves.set(1);
        leaves.set(3);
        leaves.set(4);
        
        int[] range = {0, 20};
        int[] corr_min = {11, 6};
        int[] corr_max = {20, 10};
        int[] startValue = {1, 3};
        
        testFindDeEsc(tree, leaves, range, corr_min, corr_max, startValue);
        
    }
    
        @Test
    public void testFindDeEsc2() {
        BitSet tree = new BitSet(100); // Tree: 11.01.00.11.00.00
        tree.set(1);
        tree.set(2);
        tree.set(4);
        tree.set(7);
        tree.set(8);

        BitSet leaves = new BitSet(100); // Leaves: 1.01.10.10.01
        leaves.set(1);
        leaves.set(3);
        leaves.set(4);
        leaves.set(6);
        leaves.set(9);
        
        int[] range = {0, 20};
        int[] corr_min = {11, 6, 9};
        int[] corr_max = {20, 8, 10};
        int[] startValue = {6, 3, 5};
        
        testFindDeEsc(tree, leaves, range, corr_min, corr_max, startValue);
        
    }

    public void testRemoveRange(BitSet tree, BitSet leaves, int[] range, int[] clearRange, BitSet newTree, BitSet newLeaves) {
        setInstance("test range removal", 100, range);
        instance.setTree(tree, leaves, range);

        instance.removeRange(clearRange);
        
        Assert.assertEquals("Same tree ", instance.getTree(), newTree); 
    }
    
//    @Test
    /* Single removal */
    public void testRemoveRange1() {
        BitSet tree = new BitSet(100); // Tree: 11.01.00.00
        tree.set(1);
        tree.set(2);
        tree.set(4);

        BitSet leaves = new BitSet(100); // Leaves: 1.01.10
        leaves.set(1);
        leaves.set(3);
        leaves.set(4);
        
        int[] range = {0, 20};
        int[] clearRange = {11, 20};
        
        BitSet newTree = new BitSet(100); // Tree: 10.01.00
        newTree.set(1);
        newTree.set(4);

        BitSet newLeaves = new BitSet(100); // Leaves: 1.1.10
        newLeaves.set(1);
        newLeaves.set(2);
        newLeaves.set(3);
        
        testRemoveRange(tree, leaves, range, clearRange, newTree, newLeaves);
    }
    
    @Test
    /* Double removal */
    public void testRemoveRange2() {
        BitSet tree = new BitSet(100); // Tree: 11.01.00.00
        tree.set(1);
        tree.set(2);
        tree.set(4);

        BitSet leaves = new BitSet(100); // Leaves: 1.01.10
        leaves.set(1);
        leaves.set(3);
        leaves.set(4);
        
        int[] range = {0, 20};        
        int[] clearRange = {6, 10};
        
        BitSet newTree = new BitSet(100); // Tree: 01.00
        newTree.set(2);

        BitSet newLeaves = new BitSet(100); // Leaves: 1.01
        newLeaves.set(1);
        newLeaves.set(3);
        
        testRemoveRange(tree, leaves, range, clearRange, newTree, newLeaves);
    }
    
}
