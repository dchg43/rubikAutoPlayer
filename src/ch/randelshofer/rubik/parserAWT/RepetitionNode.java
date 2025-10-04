package ch.randelshofer.rubik.parserAWT;


import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.util.ReverseVectorEnumeration;
import ch.randelshofer.util.SingletonEnumeration;


public class RepetitionNode extends ScriptNode
{
    private static final long serialVersionUID = 1767020157765390066L;

    int repeatCount = 1;

    private static class ResolvedEnumeration implements Enumeration<DefaultMutableTreeNode>
    {
        private RepetitionNode root;

        private Enumeration<DefaultMutableTreeNode> children;

        private Enumeration<DefaultMutableTreeNode> subtree;

        private Vector<DefaultMutableTreeNode> cachedChildren = new Vector<>();

        boolean inverse;

        int repeatCount;

        public ResolvedEnumeration(RepetitionNode root, boolean inverse, int repeatCount)
        {
            this.root = root;
            this.inverse = inverse;
            this.repeatCount = repeatCount;
            this.children = inverse ? this.root.enumerateChildrenReversed() : this.root.children();
            while (this.children.hasMoreElements())
            {
                this.cachedChildren.addElement(this.children.nextElement());
            }
            this.children = this.cachedChildren.elements();
            this.subtree = new SingletonEnumeration((DefaultMutableTreeNode)this.root.clone());
        }

        @Override
        public boolean hasMoreElements()
        {
            return this.subtree.hasMoreElements() || this.children.hasMoreElements() || this.repeatCount > 1;
        }

        @Override
        public DefaultMutableTreeNode nextElement()
        {
            DefaultMutableTreeNode objNextElement;
            if (this.subtree.hasMoreElements())
            {
                objNextElement = this.subtree.nextElement();
            }
            else if (!this.children.hasMoreElements())
            {
                this.subtree = ((ScriptNode)this.children.nextElement()).resolvedEnumeration(this.inverse);
                objNextElement = this.subtree.nextElement();
                if (!this.children.hasMoreElements() && this.repeatCount > 1)
                {
                    this.repeatCount--;
                    this.children = this.cachedChildren.elements();
                }
            }
            else
            {
                throw new NoSuchElementException();
            }
            return objNextElement;
        }
    }

    public RepetitionNode()
    {}

    public RepetitionNode(int startpos, int endpos)
    {
        super(startpos, endpos);
    }

    public void setRepeatCount(int repeatCount)
    {
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount()
    {
        return this.repeatCount;
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.REPETITION_EXPRESSION;
    }

    @Override
    public int getFullTurnCount()
    {
        return super.getFullTurnCount() * this.repeatCount;
    }

    @Override
    public int getQuarterTurnCount()
    {
        return super.getQuarterTurnCount() * this.repeatCount;
    }

    @Override
    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean inverse)
    {
        return new ResolvedEnumeration(this, inverse, this.repeatCount);
    }

    @Override
    public Enumeration<DefaultMutableTreeNode> enumerateChildrenReversed()
    {
        return new ReverseVectorEnumeration(this.children);
    }
}
