package ch.randelshofer.rubik.parserAWT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.rubik.RubiksCubeCore;
import ch.randelshofer.util.ReverseVectorEnumeration;
import ch.randelshofer.util.SingletonEnumeration;

public class ScriptNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 5279804692331477131L;

    private int startpos;

    private int endpos;

    private static final int[][] orientationToSymbolMap = {new int[0], {72}, {78}, {75}, {73}, {79}, {76}, {74}, {80}, {77}, {72, 73}, {72, 79}, {72, 76},
            {78, 73}, {78, 76}, {75, 73}, {75, 79}, {75, 76}, {72, 74}, {72, 77}, {78, 74}, {78, 77}, {75, 74}, {75, 77}};

    private static class ResolvedEnumeration implements Iterator<DefaultMutableTreeNode> {
        private ScriptNode root;

        private Iterator<DefaultMutableTreeNode> children;

        private Iterator<DefaultMutableTreeNode> subtree;

        boolean inverse;

        public ResolvedEnumeration(ScriptNode root, boolean inverse) {
            this.root = root;
            this.inverse = inverse;
            this.children = inverse ? this.root.enumerateChildrenReversed() : this.root.children();
            this.subtree = new SingletonEnumeration((DefaultMutableTreeNode) this.root.clone());
        }

        @Override
        public boolean hasNext() {
            return this.subtree.hasNext() || this.children.hasNext();
        }

        @Override
        public DefaultMutableTreeNode next() {
            DefaultMutableTreeNode nextElement;
            if (this.subtree.hasNext()) {
                nextElement = this.subtree.next();
            } else if (this.children.hasNext()) {
                this.subtree = ((ScriptNode) this.children.next()).resolvedEnumeration(this.inverse);
                nextElement = this.subtree.next();
            } else {
                throw new NoSuchElementException();
            }
            return nextElement;
        }
    }

    public ScriptNode() {
        setAllowsChildren(true);
    }

    public ScriptNode(int startpos, int endpos) {
        this.startpos = startpos;
        this.endpos = endpos;
        setAllowsChildren(true);
    }

    public int getStartPosition() {
        return this.startpos;
    }

    public void setStartPosition(int startpos) {
        this.startpos = startpos;
    }

    public int getEndPosition() {
        return this.endpos;
    }

    public void setEndPosition(int endpos) {
        this.endpos = endpos;
    }

    public void applyTo(RubiksCubeCore rubiksCubeCore) {
    }

    public void applySubtreeTo(RubiksCubeCore rubiksCubeCore, boolean inverse) {
        Iterator<DefaultMutableTreeNode> resolvedNode = resolvedEnumeration(inverse);
        while (resolvedNode.hasNext()) {
            ((ScriptNode) resolvedNode.next()).applyTo(rubiksCubeCore);
        }
    }

    public void applyInverseTo(RubiksCubeCore rubiksCubeCore) {
    }

    public int getSymbol() {
        return ScriptParser.SCRIPT_EXPRESSION;
    }

    public void transform(int symbol) {
        Iterator<DefaultMutableTreeNode> children = children();
        while (children.hasNext()) {
            ((ScriptNode) children.next()).transform(symbol);
        }
    }

    public void transformOrientation(int index) {
        if (index >= 1) {
            if (orientationToSymbolMap[index].length == 2) {
                SequenceNode sequenceNode = new SequenceNode();
                sequenceNode.add(new TwistNode(orientationToSymbolMap[index][0]));
                sequenceNode.add(new TwistNode(orientationToSymbolMap[index][1]));
                insert(sequenceNode, 0);
            } else {
                insert(new TwistNode(orientationToSymbolMap[index][0]), 0);
            }
            insert(new TwistNode(84), 1);
        }
    }

    public void inverse() {
        if (this.children != null) {
            Iterator<DefaultMutableTreeNode> enumerateNode = enumerateChildrenReversed();
            this.children = new ArrayList<>();
            while (enumerateNode.hasNext()) {
                ScriptNode scriptNode = (ScriptNode) enumerateNode.next();
                scriptNode.inverse();
                this.children.add(scriptNode);
            }
        }
    }

    public void reflect() {
        if (this.children != null) {
            Iterator<DefaultMutableTreeNode> children = children();
            while (children.hasNext()) {
                ((ScriptNode) children.next()).reflect();
            }
        }
    }

    public Iterator<DefaultMutableTreeNode> resolvedEnumeration(boolean inverse) {
        return new ResolvedEnumeration(this, inverse);
    }

    public Iterator<DefaultMutableTreeNode> enumerateChildrenReversed() {
        return new ReverseVectorEnumeration(this.children);
    }

    public int getFullTurnCount() {
        int fullTurnCount = 0;
        Iterator<DefaultMutableTreeNode> children = children();
        while (children.hasNext()) {
            fullTurnCount += ((ScriptNode) children.next()).getFullTurnCount();
        }
        return fullTurnCount;
    }

    public int getQuarterTurnCount() {
        int quarterTurnCount = 0;
        Iterator<DefaultMutableTreeNode> children = children();
        while (children.hasNext()) {
            quarterTurnCount += ((ScriptNode) children.next()).getQuarterTurnCount();
        }
        return quarterTurnCount;
    }

    public ScriptNode cloneSubtree() {
        ScriptNode scriptNode = (ScriptNode) clone();
        Iterator<DefaultMutableTreeNode> children = children();
        while (children.hasNext()) {
            scriptNode.add(((ScriptNode) children.next()).cloneSubtree());
        }
        return scriptNode;
    }
}
