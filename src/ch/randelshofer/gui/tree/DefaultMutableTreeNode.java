package ch.randelshofer.gui.tree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import ch.randelshofer.util.EmptyEnumeration;

public class DefaultMutableTreeNode implements Cloneable, Serializable {
    private static final long serialVersionUID = 8314678496550392251L;

    private DefaultMutableTreeNode parent;

    protected Vector<DefaultMutableTreeNode> children;

    private transient Object userObject;

    private boolean allowsChildren;

    public static final class BreadthFirstEnumeration implements Enumeration<DefaultMutableTreeNode> {
        private Queue queue;

        @SuppressWarnings("unused")
        private final DefaultMutableTreeNode headInstance;

        final class Queue {
            QNode head;

            QNode tail;

            @SuppressWarnings("unused")
            private final BreadthFirstEnumeration enuInstance;

            final class QNode {
                public Enumeration<DefaultMutableTreeNode> object;

                public QNode next;

                @SuppressWarnings("unused")
                private final Queue queueInstance;

                public QNode(Queue queue, Enumeration<DefaultMutableTreeNode> obj, QNode next) {
                    this.queueInstance = queue;
                    this.object = obj;
                    this.next = next;
                }
            }

            Queue(BreadthFirstEnumeration enuInstance) {
                this.enuInstance = enuInstance;
            }

            public void enqueue(Enumeration<DefaultMutableTreeNode> obj) {
                if (this.head == null) {
                    this.head = this.tail = new QNode(this, obj, null);
                } else {
                    this.tail.next = new QNode(this, obj, null);
                    this.tail = this.tail.next;
                }
            }

            public Enumeration<?> dequeue() {
                if (this.head == null) {
                    throw new NoSuchElementException("No more elements");
                }
                Enumeration<?> obj = this.head.object;
                QNode qNode = this.head;
                this.head = this.head.next;
                if (this.head == null) {
                    this.tail = null;
                } else {
                    qNode.next = null;
                }
                return obj;
            }

            public Enumeration<DefaultMutableTreeNode> firstObject() {
                if (this.head == null) {
                    throw new NoSuchElementException("No more elements");
                }
                return this.head.object;
            }

            public boolean isEmpty() {
                return this.head == null;
            }
        }

        public BreadthFirstEnumeration(DefaultMutableTreeNode head, DefaultMutableTreeNode child) {
            this.headInstance = head;
            Vector<DefaultMutableTreeNode> vector = new Vector<>(1);
            vector.addElement(child);
            this.queue = new Queue(this);
            this.queue.enqueue(vector.elements());
        }

        @Override
        public boolean hasMoreElements() {
            return !this.queue.isEmpty() && this.queue.firstObject().hasMoreElements();
        }

        @Override
        public DefaultMutableTreeNode nextElement() {
            Enumeration<DefaultMutableTreeNode> first = this.queue.firstObject();
            DefaultMutableTreeNode next = first.nextElement();
            Enumeration<DefaultMutableTreeNode> children = next.children();
            if (!first.hasMoreElements()) {
                this.queue.dequeue();
            }
            if (children.hasMoreElements()) {
                this.queue.enqueue(children);
            }
            return next;
        }
    }

    public static final class PreorderEnumeration implements Enumeration<DefaultMutableTreeNode> {
        private Stack<Enumeration<DefaultMutableTreeNode>> stack;

        @SuppressWarnings("unused")
        private final DefaultMutableTreeNode headInstance;

        public PreorderEnumeration(DefaultMutableTreeNode head, DefaultMutableTreeNode child) {
            this.headInstance = head;
            Vector<DefaultMutableTreeNode> vector = new Vector<>(1);
            vector.addElement(child);
            this.stack = new Stack<>();
            this.stack.push(vector.elements());
        }

        @Override
        public boolean hasMoreElements() {
            return !this.stack.empty() && this.stack.peek().hasMoreElements();
        }

        @Override
        public DefaultMutableTreeNode nextElement() {
            Enumeration<DefaultMutableTreeNode> enumeration = this.stack.peek();
            DefaultMutableTreeNode next = enumeration.nextElement();
            Enumeration<DefaultMutableTreeNode> children = next.children();
            if (!enumeration.hasMoreElements()) {
                this.stack.pop();
            }
            if (children.hasMoreElements()) {
                this.stack.push(children);
            }
            return next;
        }
    }

    public DefaultMutableTreeNode() {
        this(null);
    }

    public DefaultMutableTreeNode(Object userObject) {
        this(userObject, true);
    }

    public DefaultMutableTreeNode(Object userObject, boolean allowsChildren) {
        this.parent = null;
        this.allowsChildren = allowsChildren;
        this.userObject = userObject;
    }

    public void insert(DefaultMutableTreeNode child, int index) {
        if (!this.allowsChildren) {
            throw new IllegalStateException("node does not allow children");
        }
        if (child == null) {
            throw new IllegalArgumentException("new child is null");
        }
        if (isNodeAncestor(child)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }
        DefaultMutableTreeNode parent = child.getParent();
        if (parent != null) {
            parent.remove(child);
        }
        child.setParent(this);
        if (this.children == null) {
            this.children = new Vector<>();
        }
        this.children.insertElementAt(child, index);
    }

    public void remove(int index) {
        DefaultMutableTreeNode child = this.children.remove(index);
        child.setParent(null);
    }

    public void setParent(DefaultMutableTreeNode parent) {
        this.parent = parent;
    }

    public DefaultMutableTreeNode getParent() {
        return this.parent;
    }

    public DefaultMutableTreeNode getChildAt(int index) {
        if (this.children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return this.children.elementAt(index);
    }

    public int getChildCount() {
        if (this.children == null) {
            return 0;
        }
        return this.children.size();
    }

    public int getIndex(DefaultMutableTreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("argument is null");
        }
        if (isNodeChild(child)) {
            return this.children.indexOf(child);
        }
        return -1;
    }

    public Enumeration<DefaultMutableTreeNode> children() {
        return this.children == null ? EmptyEnumeration.EMPTY_ENUMERATION : this.children.elements();
    }

    public void setAllowsChildren(boolean allowsChildren) {
        if (allowsChildren != this.allowsChildren) {
            this.allowsChildren = allowsChildren;
            if (this.allowsChildren) {
                return;
            }
            removeAllChildren();
        }
    }

    public boolean getAllowsChildren() {
        return this.allowsChildren;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    public Object getUserObject() {
        return this.userObject;
    }

    public void removeFromParent() {
        DefaultMutableTreeNode parent = getParent();
        if (parent != null) {
            parent.remove(this);
        }
    }

    public void remove(DefaultMutableTreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("argument is null");
        }
        if (!isNodeChild(child)) {
            throw new IllegalArgumentException("argument is not a child");
        }
        remove(getIndex(child));
    }

    public void removeAllChildren() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            remove(i);
        }
    }

    public void add(DefaultMutableTreeNode child) {
        if (child == null || child.getParent() != this) {
            insert(child, getChildCount());
        } else {
            insert(child, getChildCount() - 1);
        }
    }

    public boolean isNodeAncestor(DefaultMutableTreeNode ancestor) {
        if (ancestor == null) {
            return false;
        }
        DefaultMutableTreeNode parent = this;
        while (parent != ancestor) {
            parent = parent.getParent();
            if (parent == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isNodeDescendant(DefaultMutableTreeNode descendant) {
        if (descendant == null) {
            return false;
        }
        return descendant.isNodeAncestor(this);
    }

    public int getDepth() {
        DefaultMutableTreeNode leaf = null;
        BreadthFirstEnumeration elems = breadthFirstEnumeration();
        while (elems.hasMoreElements()) {
            leaf = elems.nextElement();
        }
        if (leaf == null) {
            throw new Error("nodes should be null");
        }
        return leaf.getLevel() - getLevel();
    }

    public int getLevel() {
        int i = 0;
        DefaultMutableTreeNode parent = this.getParent();
        while (parent != null) {
            i++;
            parent = parent.getParent();
        }
        return i;
    }

    public PreorderEnumeration preorderEnumeration() {
        return new PreorderEnumeration(this, this);
    }

    public BreadthFirstEnumeration breadthFirstEnumeration() {
        return new BreadthFirstEnumeration(this, this);
    }

    public boolean isNodeChild(DefaultMutableTreeNode child) {
        if (child == null || getChildCount() == 0) {
            return false;
        } else {
            return child.getParent() == this;
        }
    }

    @Override
    public String toString() {
        if (this.userObject == null) {
            return "null";
        }
        return this.userObject.toString();
    }

    @Override
    public Object clone() {
        try {
            DefaultMutableTreeNode tree = (DefaultMutableTreeNode) super.clone();
            tree.children = null;
            tree.parent = null;
            return tree;
        } catch (CloneNotSupportedException e) {
            throw new Error(e.toString());
        }
    }
}
