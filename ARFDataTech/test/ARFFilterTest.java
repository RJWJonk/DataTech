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
    
    public void testQuery(boolean b1, BitSet tree, BitSet values, int treeSize, int[] range, int key_min, int key_max) {
        instance = new ARFFilter("test", 100);
        instance.setTree(tree, values, treeSize, range);
        boolean b2 = instance.query(key_min, key_max);
        Assert.assertEquals("Correct Query", b1, b2);
    }
    
    /* Only leaves */
//    @Test
    public void testQuery1() {
        BitSet tree = new BitSet(2); // Tree: "00"
        int treeSize = 2;
        BitSet values = new BitSet(2); 
        values.set(2); // Leaf Values: "01"
        int[] range = {0, 11};
        testQuery(false, tree, values, treeSize, range, 0, 5);
        testQuery(true, tree, values, treeSize, range, 6, 11);
    }
    
        /* Right subtree */
//    @Test
    public void testQuery2() {
        BitSet tree = new BitSet(4); // Tree: "0100"
        tree.set(2);
        int treeSize = 4;
        BitSet values = new BitSet(3); 
        values.set(2); // Leaf Values: "010"
        int[] range = {0, 11};
        testQuery(false, tree, values, treeSize, range, 0, 5);
        testQuery(true, tree, values, treeSize, range, 6, 8);
        testQuery(false, tree, values, treeSize, range, 9, 11);        
    }
    
            /* Left subtree */
//    @Test
    public void testQuery3() {
        BitSet tree = new BitSet(4); // Tree: "1000"
        tree.set(1);
        int treeSize = 4;
        BitSet values = new BitSet(3); 
        values.set(2); // Leaf Values: "010"
        int[] range = {0, 11};
        testQuery(true, tree, values, treeSize, range, 0, 2);
        testQuery(false, tree, values, treeSize, range, 3, 5);
        testQuery(false, tree, values, treeSize, range, 6, 11);   
    }
    
                /* Two subtrees */
        @Test
    public void testQuery4() {
        BitSet tree = new BitSet(6); // Tree: "110000"
        tree.set(1);
        tree.set(2);
        int treeSize = 6;
        BitSet values = new BitSet(4); // Leaf Values: "1001"
        values.set(1); 
        values.set(4);
        int[] range = {0, 11};
        testQuery(true, tree, values, treeSize, range, 0, 2);
        testQuery(false, tree, values, treeSize, range, 3, 5);
        testQuery(false, tree, values, treeSize, range, 6, 8);
        testQuery(true, tree, values, treeSize, range, 9, 11);
    }
    
        @Test
    public void testQuery5() {
        BitSet tree = new BitSet(14); // Tree: "11.01.11.00.01.00.00"
        tree.set(1); tree.set(2); tree.set(4); tree.set(5); tree.set(6); tree.set(10);
        int treeSize = 14;
        BitSet values = new BitSet(8); // Leaf Values: "10100101"
        values.set(1); values.set(3); values.set(6); values.set(8);
        int[] range = {0, 20};
        testQuery(true, tree, values, treeSize, range, 0, 5);
        testQuery(false, tree, values, treeSize, range, 6, 7);
        testQuery(true, tree, values, treeSize, range, 8, 10);
        testQuery(false, tree, values, treeSize, range, 11, 12);
        testQuery(false, tree, values, treeSize, range, 13, 13);
        testQuery(true, tree, values, treeSize, range, 14, 15);
        testQuery(false, tree, values, treeSize, range, 16, 17);
        testQuery(true, tree, values, treeSize, range, 18, 20);
        
    }
}
