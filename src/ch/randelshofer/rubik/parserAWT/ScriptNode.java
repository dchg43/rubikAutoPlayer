package ch.randelshofer.rubik.parserAWT;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

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

    private static class ResolvedEnumeration implements Enumeration<DefaultMutableTreeNode> {
        private ScriptNode root;

        private Enumeration<DefaultMutableTreeNode> children;

        private Enumeration<DefaultMutableTreeNode> subtree;

        boolean inverse;

        public ResolvedEnumeration(ScriptNode root, boolean inverse) {
            this.root = root;
            this.inverse = inverse;
            this.children = inverse ? this.root.enumerateChildrenReversed() : this.root.children();
            this.subtree = new SingletonEnumeration((DefaultMutableTreeNode) this.root.clone());
        }

        @Override
        public boolean hasMoreElements() {
            return this.subtree.hasMoreElements() || this.children.hasMoreElements();
        }

        @Override
        public DefaultMutableTreeNode nextElement() {
            DefaultMutableTreeNode nextElement;
            if (this.subtree.hasMoreElements()) {
                nextElement = this.subtree.nextElement();
            } else if (this.children.hasMoreElements()) {
                this.subtree = ((ScriptNode) this.children.nextElement()).resolvedEnumeration(this.inverse);
                nextElement = this.subtree.nextElement();
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
        Enumeration<DefaultMutableTreeNode> resolvedNode = resolvedEnumeration(inverse);
        while (resolvedNode.hasMoreElements()) {
            ((ScriptNode) resolvedNode.nextElement()).applyTo(rubiksCubeCore);
        }
    }

    public void applyInverseTo(RubiksCubeCore rubiksCubeCore) {
    }

    public int getSymbol() {
        return ScriptParser.SCRIPT_EXPRESSION;
    }

    public void transform(int symbol) {
        Enumeration<DefaultMutableTreeNode> children = children();
        while (children.hasMoreElements()) {
            ((ScriptNode) children.nextElement()).transform(symbol);
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
            Enumeration<DefaultMutableTreeNode> enumerateNode = enumerateChildrenReversed();
            this.children = new Vector<>();
            while (enumerateNode.hasMoreElements()) {
                ScriptNode scriptNode = (ScriptNode) enumerateNode.nextElement();
                scriptNode.inverse();
                this.children.addElement(scriptNode);
            }
        }
    }

    public void reflect() {
        if (this.children != null) {
            Enumeration<DefaultMutableTreeNode> children = children();
            while (children.hasMoreElements()) {
                ((ScriptNode) children.nextElement()).reflect();
            }
        }
    }

    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean inverse) {
        return new ResolvedEnumeration(this, inverse);
    }

    public Enumeration<DefaultMutableTreeNode> enumerateChildrenReversed() {
        return new ReverseVectorEnumeration(this.children);
    }

    public int getFullTurnCount() {
        int fullTurnCount = 0;
        Enumeration<DefaultMutableTreeNode> children = children();
        while (children.hasMoreElements()) {
            fullTurnCount += ((ScriptNode) children.nextElement()).getFullTurnCount();
        }
        return fullTurnCount;
    }

    public int getQuarterTurnCount() {
        int quarterTurnCount = 0;
        Enumeration<DefaultMutableTreeNode> children = children();
        while (children.hasMoreElements()) {
            quarterTurnCount += ((ScriptNode) children.nextElement()).getQuarterTurnCount();
        }
        return quarterTurnCount;
    }

    public ScriptNode cloneSubtree() {
        ScriptNode scriptNode = (ScriptNode) clone();
        Enumeration<DefaultMutableTreeNode> children = children();
        while (children.hasMoreElements()) {
            scriptNode.add(((ScriptNode) children.nextElement()).cloneSubtree());
        }
        return scriptNode;
    }
}
