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
        instance = new ARFFilter(name, 0);
    }
    
        protected void setInstance(String name, int maxElements) {
        instance = new ARFFilter(name, maxElements);
    }
     
        
    /* Testing method testRAnge */    
    private void testRange(boolean b1, int rmin, int rmax, int kmin, int kmax) {
        instance = new ARFFilter("test", 100);
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
        instance = new ARFFilter("test", 100);
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
        instance = new ARFFilter("test", 100);
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
        tree.set(1); tree.set(2); tree.set(4); tree.set(5); tree.set(6); tree.set(10);
        BitSet values = new BitSet(8); // Leaf Values: "10100101"
        values.set(1); values.set(3); values.set(6); values.set(8);
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
        instance = new ARFFilter("test", 100);
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
        tree.set(1); tree.set(2);
        BitSet values = new BitSet(4); // Leaf Values: "0110"
        values.set(2); values.set(3);
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
    
    
}
