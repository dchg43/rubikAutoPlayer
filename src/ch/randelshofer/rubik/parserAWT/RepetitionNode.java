package ch.randelshofer.rubik.parserAWT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.util.ReverseVectorEnumeration;
import ch.randelshofer.util.SingletonEnumeration;

public class RepetitionNode extends ScriptNode {
    private static final long serialVersionUID = 1767020157765390066L;

    private int repeatCount = 1;

    private static class ResolvedEnumeration implements Iterator<DefaultMutableTreeNode> {
        private RepetitionNode root;

        private Iterator<DefaultMutableTreeNode> children;

        private Iterator<DefaultMutableTreeNode> subtree;

        private List<DefaultMutableTreeNode> cachedChildren = new ArrayList<>();

        private boolean inverse;

        private int repeatCount;

        public ResolvedEnumeration(RepetitionNode root, boolean inverse, int repeatCount) {
            this.root = root;
            this.inverse = inverse;
            this.repeatCount = repeatCount;
            this.children = inverse ? this.root.enumerateChildrenReversed() : this.root.children();
            while (this.children.hasNext()) {
                this.cachedChildren.add(this.children.next());
            }
            this.children = this.cachedChildren.iterator();
            this.subtree = new SingletonEnumeration((DefaultMutableTreeNode) this.root.clone());
        }

        @Override
        public boolean hasNext() {
            return this.subtree.hasNext() || this.children.hasNext() || this.repeatCount > 1;
        }

        @Override
        public DefaultMutableTreeNode next() {
            DefaultMutableTreeNode nextElement;
            if (this.subtree.hasNext()) {
                nextElement = this.subtree.next();
            } else if (this.children.hasNext()) {
                this.subtree = ((ScriptNode) this.children.next()).resolvedEnumeration(this.inverse);
                nextElement = this.subtree.next();
                if (!this.children.hasNext() && this.repeatCount > 1) {
                    this.repeatCount--;
                    this.children = this.cachedChildren.iterator();
                }
            } else {
                throw new NoSuchElementException();
            }
            return nextElement;
        }
    }

    public RepetitionNode() {
    }

    public RepetitionNode(int startpos, int endpos) {
        super(startpos, endpos);
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return this.repeatCount;
    }

    @Override
    public int getSymbol() {
        return ScriptParser.REPETITION_EXPRESSION;
    }

    @Override
    public int getFullTurnCount() {
        return super.getFullTurnCount() * this.repeatCount;
    }

    @Override
    public int getQuarterTurnCount() {
        return super.getQuarterTurnCount() * this.repeatCount;
    }

    @Override
    public Iterator<DefaultMutableTreeNode> resolvedEnumeration(boolean inverse) {
        return new ResolvedEnumeration(this, inverse, this.repeatCount);
    }

    @Override
    public Iterator<DefaultMutableTreeNode> enumerateChildrenReversed() {
        return new ReverseVectorEnumeration(this.children);
    }
}
