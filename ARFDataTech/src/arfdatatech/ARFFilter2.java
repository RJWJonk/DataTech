/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arfdatatech;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author s080440
 */
public class ARFFilter2 extends Filter {

    public static final int NO_ADAPT = 0;
    public static final int BIT_0 = 1;
    public static final int BIT_1 = 2;

    private final ARFTree root;
    private int curNodes;
    private ARFTree clock;

    private final int maxElements;
    private int[] domain;
    private final int filter; // 0 - no adapt, 1 - simple adapt, 2 - more adapt

    public ARFFilter2(String name, int maxElements, int[] domain, int ftype) {
        super(name);
        Range r = new Range(domain[0], domain[1]);
        root = new ARFNode(r, null);
        curNodes = 1;
        root.setLeft(new TrueNode(splitRange(r, 'l'), root));
        root.setRight(new TrueNode(splitRange(r, 'r'), root));
        clock = null;

        this.domain = domain;
        this.maxElements = maxElements;
        this.filter = ftype;
    }

    @Override
    public boolean query(int key_min, int key_max) {

        Queue<ARFTree> todo = new LinkedList<>();
        todo.add(root);

        Range keys = new Range(key_min, key_max);

        while (!todo.isEmpty()) {

            ARFTree curNode = todo.poll();
            Range leftRange = splitRange(curNode.range, 'l');
            Range rightRange = splitRange(curNode.range, 'r');

            if (overlaps(leftRange, keys)) {
                if (curNode.left instanceof ARFNode) {
                    todo.add(curNode.left);
                } else if (curNode.left instanceof TrueNode) {
                    TrueNode n = (TrueNode) curNode.left;
                    n.used = true;
                    return true;
                } else if (curNode.left instanceof FalseNode) {
                    FalseNode n = (FalseNode) curNode.left;
                    n.used = true;
                }
            }

            if (overlaps(rightRange, keys)) {
                if (curNode.right instanceof ARFNode) {
                    todo.add(curNode.right);
                } else if (curNode.right instanceof TrueNode) {
                    TrueNode n = (TrueNode) curNode.right;
                    n.used = true;
                    return true;
                } else if (curNode.right instanceof FalseNode) {
                    FalseNode n = (FalseNode) curNode.right;
                    n.used = true;
                }
            }

        }

        return false;
    }

    @Override
    public void adjustFilter(int key_min, int key_max) {
        if (filter == 0) {
            return;
        }

        escalate(key_min, key_max);
        deEscalate();
    }

    public void escalate(int key_min, int key_max) {

        Range keys = new Range(key_min, key_max);

        ARFTree node = navigate(key_min);
        while (node.range.left < key_min && node instanceof TrueNode) {
            char target = contains(splitRange(node.parent.range, 'l'), key_min) ? 'l' : 'r';
            split(node, target);
            node = navigate(key_min);
        }
        node = navigate(key_max);
        while (node.range.right > key_max && node instanceof TrueNode) {
            char target = contains(splitRange(node.parent.range, 'l'), key_max) ? 'l' : 'r';
            split(node, target);
            node = navigate(key_max);
        }
        markEmpty(keys);

    }

    private ARFTree navigate(int key) {

        ARFTree curNode = root;

        while (curNode instanceof ARFNode) {
            Range leftRange = splitRange(curNode.range, 'l');
            if (contains(leftRange, key)) {
                curNode = curNode.left;
            } else {
                curNode = curNode.right;
            }
        }

        return curNode;
    }

    private void split(ARFTree n, char target) {

        Range leftRange = splitRange(n.range, 'l');
        Range rightRange = splitRange(n.range, 'r');
        ARFNode newNode;
        if (n instanceof TrueNode) {
            newNode = new ARFNode(n.range, n.parent);
            newNode.left = new TrueNode(leftRange, newNode);
            newNode.right = new TrueNode(rightRange, newNode);
        } else {
            newNode = new ARFNode(n.range, n.parent);
            newNode.left = new FalseNode(leftRange, newNode);
            newNode.right = new FalseNode(rightRange, newNode);
        }

        if (target == 'l') {
            newNode.parent.setLeft(newNode);
        } else if (target == 'r') {
            newNode.parent.setRight(newNode);
        }

        curNodes++;
    }

    private void markEmpty(Range keys) {
        Queue<ARFTree> todo = new LinkedList<>();
        todo.add(root);

        while (!todo.isEmpty()) {
            ARFTree n = todo.poll();

            if (n instanceof ARFNode) {
                todo.add(n.left);
                todo.add(n.right);
            } else {

                if (overlaps(n.range, keys)) {
                    ARFTree f = new FalseNode(n.range, n.parent);

                    //find which side of parent is has to be attached to..
                    if (overlaps(splitRange(n.parent.range, 'l'), f.range)) {
                        //found left
                        f.parent.left = f;
                    } else {
                        //found right
                        f.parent.right = f;
                    }

                }
            }

        }
    }

    private void markOccupied(Range keys) {
        Queue<ARFTree> todo = new LinkedList<>();
        todo.add(root);

        while (!todo.isEmpty()) {
            ARFTree n = todo.poll();

            if (n instanceof ARFNode) {
                todo.add(n.left);
                todo.add(n.right);
            } else {

                if (overlaps(n.range, keys)) {
                    ARFTree f = new TrueNode(n.range, n.parent);

                    //find which side of parent is has to be attached to..
                    if (overlaps(splitRange(n.parent.range, 'l'), f.range)) {
                        //found left
                        f.parent.left = f;
                    } else {
                        //found right
                        f.parent.right = f;
                    }

                }
            }

        }
    }

    public void deEscalate() {
//        if (filter == 0) {
//            return;
//        }

        while (isTooBig()) {
            //System.out.println(curNodes*3+1 + " --- " + maxElements);
            ARFTree victim = findVictim();
            ARFTree sibling = findSibling(victim);
            merge(victim, sibling);
        }
    }

    private ARFTree findVictim() {
        if (clock == null) {
            clock = navigate(0);
            nextClock();
        }

        if (filter != 2) {
            if (clock instanceof TrueNode) {
                TrueNode n = (TrueNode) clock;
                nextClock();
                return n;
            } else if (clock instanceof FalseNode) {
                FalseNode n = (FalseNode) clock;
                nextClock();
                return n;
            }

        } else if (filter == 2) {
            if (clock instanceof TrueNode) {
                TrueNode n = (TrueNode) clock;
                nextClock();
                boolean used = n.used;
                n.used = false;
                return used ? findVictim() : n;
            } else if (clock instanceof FalseNode) {
                FalseNode n = (FalseNode) clock;
                nextClock();
                boolean used = n.used;
                n.used = false;
                return used ? findVictim() : n;
            }

        }

        return null;
    }

    private void nextClock() {
        int key = clock.range.right;
        clock = navigate((key + 1) % domain[1]);

        while (!hasSibling(clock)) {
            key = clock.range.right;
            clock = navigate((key + 1) % domain[1]);
        }

    }

    private boolean hasSibling(ARFTree victim) {
        ARFTree left = victim.parent.left;
        ARFTree right = victim.parent.right;
        return (left instanceof TrueNode || left instanceof FalseNode)
                && (right instanceof TrueNode || right instanceof FalseNode);
    }

    private ARFTree findSibling(ARFTree victim) {
        ARFTree left = victim.parent.left;
        ARFTree right = victim.parent.right;
        return victim == left ? right : left;
    }

    private void merge(ARFTree node1, ARFTree node2) {
        //decide range
        int leftRange = node1.range.left < node2.range.left ? node1.range.left : node2.range.left;
        int rightRange = node1.range.right > node2.range.right ? node1.range.right : node2.range.right;
        Range r = new Range(leftRange, rightRange);
        //merge nodes
        if (node1 instanceof TrueNode || node2 instanceof TrueNode) {

            ARFTree merged = new TrueNode(r, node1.parent.parent);
            if (overlaps(merged.range, splitRange(merged.parent.range, 'l'))) {
                merged.parent.left = merged;
            } else {
                merged.parent.right = merged;
            }
        } else {
            ARFTree merged = new FalseNode(r, node1.parent.parent);
            if (overlaps(merged.range, splitRange(merged.parent.range, 'l'))) {
                merged.parent.left = merged;
            } else {
                merged.parent.right = merged;
            }
        }
        curNodes--;
    }

    @Override
    public void addKey(int key_min, int key_max) {
        Range keys = new Range(key_min, key_max);
        markOccupied(keys);
    }

    private Range splitRange(Range range, char s) {
        if (s == 'l') {
            int newleft = range.left;
            int newright = (range.right + range.left) / 2;
            Range newRange = new Range(newleft, newright);
            return newRange;
        } else if (s == 'r') {
            int newleft = 1 + (range.right + range.left) / 2;
            int newright = range.right;
            Range newRange = new Range(newleft, newright);
            return newRange;
        }
        return null;
    }

    private boolean overlaps(Range r1, Range r2) {
        if (r1.left < r2.left) {
            return r1.right >= r2.left;
        } else {
            return r2.right >= r1.left;
        }
    }

    private boolean contains(Range r, int k) {
        return r.left <= k && k <= r.right;
    }

    public boolean isTooBig() {
        return 3 * curNodes + 1 > maxElements;
    }

    private class ARFTree {

        protected Range range;
        protected ARFTree parent;
        protected ARFTree left;
        protected ARFTree right;

        private ARFTree(Range range) {
            this.range = range;
        }

        protected void setParent(ARFTree l) {
            this.left = l;
        }

        private void setLeft(ARFTree l) {
            this.left = l;
        }

        private void setRight(ARFTree r) {
            this.right = r;
        }

        @Override
        public String toString() {
            Queue<ARFTree> todo = new LinkedList<>();
            todo.add(root);

            String result = "";
            while (!todo.isEmpty()) {
                ARFTree n = todo.poll();
                result += n.range.toString();
                if (n.left instanceof ARFNode) {
                    todo.add(n.left);
                }
                if (n.right instanceof ARFNode) {
                    todo.add(n.right);
                }
            }

            return result;
        }

    }

    private class ARFNode extends ARFTree {

        private ARFNode(Range range, ARFTree p) {
            super(range);
            parent = p;
        }

    }

    private class TrueNode extends ARFTree {

        private boolean used;

        private TrueNode(Range range, ARFTree p) {
            super(range);
            parent = p;
            used = false;
        }
    }

    private class FalseNode extends ARFTree {

        private boolean used;

        private FalseNode(Range range, ARFTree p) {
            super(range);
            parent = p;
            used = false;
        }
    }

    private class Range {

        private int left;
        private int right;

        private Range(int l, int r) {
            this.left = l;
            this.right = r;
        }

        @Override
        public String toString() {
            return "[" + left + "," + right + "]";
        }
    }

}
