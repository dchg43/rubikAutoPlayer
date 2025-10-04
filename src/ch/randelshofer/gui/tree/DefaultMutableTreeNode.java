package ch.randelshofer.gui.tree;


import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import ch.randelshofer.util.EmptyEnumeration;


public class DefaultMutableTreeNode implements Cloneable, Serializable
{
    private static final long serialVersionUID = 8314678496550392251L;

    private DefaultMutableTreeNode parent;

    protected Vector<DefaultMutableTreeNode> children;

    private transient Object userObject;

    private boolean allowsChildren;

    final class BreadthFirstEnumeration implements Enumeration<DefaultMutableTreeNode>
    {
        private Queue queue;

        @SuppressWarnings("unused")
        private final DefaultMutableTreeNode headInstance;

        final class Queue
        {
            QNode head;

            QNode tail;

            @SuppressWarnings("unused")
            private final BreadthFirstEnumeration enuInstance;

            final class QNode
            {
                public Enumeration<DefaultMutableTreeNode> object;

                public QNode next;

                @SuppressWarnings("unused")
                private final Queue queueInstance;

                public QNode(Queue queue, Enumeration<DefaultMutableTreeNode> obj, QNode qNode)
                {
                    this.queueInstance = queue;
                    this.object = obj;
                    this.next = qNode;
                }
            }

            Queue(BreadthFirstEnumeration breadthFirstEnumeration)
            {
                this.enuInstance = breadthFirstEnumeration;
            }

            public void enqueue(Enumeration<DefaultMutableTreeNode> obj)
            {
                if (this.head == null)
                {
                    this.head = this.tail = new QNode(this, obj, null);
                }
                else
                {
                    this.tail.next = new QNode(this, obj, null);
                    this.tail = this.tail.next;
                }
            }

            public Enumeration<?> dequeue()
            {
                if (this.head == null)
                {
                    throw new NoSuchElementException("No more elements");
                }
                Enumeration<?> obj = this.head.object;
                QNode qNode = this.head;
                this.head = this.head.next;
                if (this.head == null)
                {
                    this.tail = null;
                }
                else
                {
                    qNode.next = null;
                }
                return obj;
            }

            public Enumeration<DefaultMutableTreeNode> firstObject()
            {
                if (this.head == null)
                {
                    throw new NoSuchElementException("No more elements");
                }
                return this.head.object;
            }

            public boolean isEmpty()
            {
                return this.head == null;
            }
        }

        public BreadthFirstEnumeration(DefaultMutableTreeNode defaultMutableTreeNode,
                                       DefaultMutableTreeNode defaultMutableTreeNode2)
        {
            this.headInstance = defaultMutableTreeNode;
            Vector<DefaultMutableTreeNode> vector = new Vector<>(1);
            vector.addElement(defaultMutableTreeNode2);
            this.queue = new Queue(this);
            this.queue.enqueue(vector.elements());
        }

        @Override
        public boolean hasMoreElements()
        {
            return !this.queue.isEmpty() && (this.queue.firstObject()).hasMoreElements();
        }

        @Override
        public DefaultMutableTreeNode nextElement()
        {
            Enumeration<DefaultMutableTreeNode> enumeration = this.queue.firstObject();
            DefaultMutableTreeNode defaultMutableTreeNode = enumeration.nextElement();
            Enumeration<DefaultMutableTreeNode> enumerationChildren = defaultMutableTreeNode.children();
            if (!enumeration.hasMoreElements())
            {
                this.queue.dequeue();
            }
            if (enumerationChildren.hasMoreElements())
            {
                this.queue.enqueue(enumerationChildren);
            }
            return defaultMutableTreeNode;
        }
    }

    final class PreorderEnumeration implements Enumeration<DefaultMutableTreeNode>
    {
        private Stack<Enumeration<DefaultMutableTreeNode>> stack;

        @SuppressWarnings("unused")
        private final DefaultMutableTreeNode headInstance;

        public PreorderEnumeration(DefaultMutableTreeNode defaultMutableTreeNode,
                                   DefaultMutableTreeNode defaultMutableTreeNode2)
        {
            this.headInstance = defaultMutableTreeNode;
            Vector<DefaultMutableTreeNode> vector = new Vector<>(1);
            vector.addElement(defaultMutableTreeNode2);
            this.stack = new Stack<>();
            this.stack.push(vector.elements());
        }

        @Override
        public boolean hasMoreElements()
        {
            return !this.stack.empty() && this.stack.peek().hasMoreElements();
        }

        @Override
        public DefaultMutableTreeNode nextElement()
        {
            Enumeration<DefaultMutableTreeNode> enumeration = this.stack.peek();
            DefaultMutableTreeNode defaultMutableTreeNode = enumeration.nextElement();
            Enumeration<DefaultMutableTreeNode> enumerationChildren = defaultMutableTreeNode.children();
            if (!enumeration.hasMoreElements())
            {
                this.stack.pop();
            }
            if (enumerationChildren.hasMoreElements())
            {
                this.stack.push(enumerationChildren);
            }
            return defaultMutableTreeNode;
        }
    }

    public DefaultMutableTreeNode()
    {
        this(null);
    }

    public DefaultMutableTreeNode(Object obj)
    {
        this(obj, true);
    }

    public DefaultMutableTreeNode(Object obj, boolean z)
    {
        this.parent = null;
        this.allowsChildren = z;
        this.userObject = obj;
    }

    public void insert(DefaultMutableTreeNode defaultMutableTreeNode, int i)
    {
        if (!this.allowsChildren)
        {
            throw new IllegalStateException("node does not allow children");
        }
        if (defaultMutableTreeNode == null)
        {
            throw new IllegalArgumentException("new child is null");
        }
        if (isNodeAncestor(defaultMutableTreeNode))
        {
            throw new IllegalArgumentException("new child is an ancestor");
        }
        DefaultMutableTreeNode parent = defaultMutableTreeNode.getParent();
        if (parent != null)
        {
            parent.remove(defaultMutableTreeNode);
        }
        defaultMutableTreeNode.setParent(this);
        if (this.children == null)
        {
            this.children = new Vector<>();
        }
        this.children.insertElementAt(defaultMutableTreeNode, i);
    }

    public void remove(int i)
    {
        DefaultMutableTreeNode childAt = getChildAt(i);
        this.children.removeElementAt(i);
        childAt.setParent(null);
    }

    public void setParent(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        this.parent = defaultMutableTreeNode;
    }

    public DefaultMutableTreeNode getParent()
    {
        return this.parent;
    }

    public DefaultMutableTreeNode getChildAt(int i)
    {
        if (this.children == null)
        {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return this.children.elementAt(i);
    }

    public int getChildCount()
    {
        if (this.children == null)
        {
            return 0;
        }
        return this.children.size();
    }

    public int getIndex(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        if (defaultMutableTreeNode == null)
        {
            throw new IllegalArgumentException("argument is null");
        }
        if (isNodeChild(defaultMutableTreeNode))
        {
            return this.children.indexOf(defaultMutableTreeNode);
        }
        return -1;
    }

    public Enumeration<DefaultMutableTreeNode> children()
    {
        return this.children == null ? EmptyEnumeration.EMPTY_ENUMERATION : this.children.elements();
    }

    public void setAllowsChildren(boolean z)
    {
        if (z != this.allowsChildren)
        {
            this.allowsChildren = z;
            if (this.allowsChildren)
            {
                return;
            }
            removeAllChildren();
        }
    }

    public boolean getAllowsChildren()
    {
        return this.allowsChildren;
    }

    public void setUserObject(Object obj)
    {
        this.userObject = obj;
    }

    public Object getUserObject()
    {
        return this.userObject;
    }

    public void removeFromParent()
    {
        DefaultMutableTreeNode parent = getParent();
        if (parent != null)
        {
            parent.remove(this);
        }
    }

    public void remove(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        if (defaultMutableTreeNode == null)
        {
            throw new IllegalArgumentException("argument is null");
        }
        if (!isNodeChild(defaultMutableTreeNode))
        {
            throw new IllegalArgumentException("argument is not a child");
        }
        remove(getIndex(defaultMutableTreeNode));
    }

    public void removeAllChildren()
    {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--)
        {
            remove(childCount);
        }
    }

    public void add(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        if (defaultMutableTreeNode == null || defaultMutableTreeNode.getParent() != this)
        {
            insert(defaultMutableTreeNode, getChildCount());
        }
        else
        {
            insert(defaultMutableTreeNode, getChildCount() - 1);
        }
    }

    public boolean isNodeAncestor(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        if (defaultMutableTreeNode == null)
        {
            return false;
        }
        DefaultMutableTreeNode defaultMutableTreeNode2 = this;
        while (defaultMutableTreeNode2 != defaultMutableTreeNode)
        {
            DefaultMutableTreeNode parent = defaultMutableTreeNode2.getParent();
            defaultMutableTreeNode2 = parent;
            if (parent == null)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isNodeDescendant(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        if (defaultMutableTreeNode == null)
        {
            return false;
        }
        return defaultMutableTreeNode.isNodeAncestor(this);
    }

    public int getDepth()
    {
        Object objNextElement = null;
        Enumeration<?> enumerationBreadthFirstEnumeration = breadthFirstEnumeration();
        while (enumerationBreadthFirstEnumeration.hasMoreElements())
        {
            objNextElement = enumerationBreadthFirstEnumeration.nextElement();
        }
        if (objNextElement == null)
        {
            throw new Error("nodes should be null");
        }
        return ((DefaultMutableTreeNode)objNextElement).getLevel() - getLevel();
    }

    public int getLevel()
    {
        int i = 0;
        DefaultMutableTreeNode parent = this.getParent();
        while (parent != null)
        {
            parent = parent.getParent();
            i++;
        }
        return i;
    }

    public Enumeration<?> preorderEnumeration()
    {
        return new PreorderEnumeration(this, this);
    }

    public Enumeration<?> breadthFirstEnumeration()
    {
        return new BreadthFirstEnumeration(this, this);
    }

    public boolean isNodeChild(DefaultMutableTreeNode defaultMutableTreeNode)
    {
        boolean z;
        if (defaultMutableTreeNode == null || getChildCount() == 0)
        {
            z = false;
        }
        else
        {
            z = defaultMutableTreeNode.getParent() == this;
        }
        return z;
    }

    @Override
    public String toString()
    {
        if (this.userObject == null)
        {
            return "null";
        }
        return this.userObject.toString();
    }

    @Override
    public Object clone()
    {
        try
        {
            DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode)super.clone();
            defaultMutableTreeNode.children = null;
            defaultMutableTreeNode.parent = null;
            return defaultMutableTreeNode;
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error(e.toString());
        }
    }
}
