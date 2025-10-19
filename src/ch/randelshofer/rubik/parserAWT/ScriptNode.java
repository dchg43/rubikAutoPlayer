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
            } else {
                if (!this.children.hasMoreElements()) {
                    throw new NoSuchElementException();
                }
                this.subtree = ((ScriptNode) this.children.nextElement()).resolvedEnumeration(this.inverse);
                nextElement = this.subtree.nextElement();
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
        Enumeration<DefaultMutableTreeNode> enumerationResolvedEnumeration = resolvedEnumeration(inverse);
        while (enumerationResolvedEnumeration.hasMoreElements()) {
            ((ScriptNode) enumerationResolvedEnumeration.nextElement()).applyTo(rubiksCubeCore);
        }
    }

    public void applyInverseTo(RubiksCubeCore rubiksCubeCore) {
    }

    public int getSymbol() {
        return ScriptParser.SCRIPT_EXPRESSION;
    }

    public void transform(int axis) {
        Enumeration<DefaultMutableTreeNode> enumerationChildren = children();
        while (enumerationChildren.hasMoreElements()) {
            ((ScriptNode) enumerationChildren.nextElement()).transform(axis);
        }
    }

    public void transformOrientation(int i) {
        if (i >= 1) {
            if (orientationToSymbolMap[i].length == 2) {
                SequenceNode sequenceNode = new SequenceNode();
                sequenceNode.add(new TwistNode(orientationToSymbolMap[i][0]));
                sequenceNode.add(new TwistNode(orientationToSymbolMap[i][1]));
                insert(sequenceNode, 0);
            } else {
                insert(new TwistNode(orientationToSymbolMap[i][0]), 0);
            }
            insert(new TwistNode(84), 1);
        }
    }

    public void inverse() {
        if (this.children != null) {
            Enumeration<DefaultMutableTreeNode> enumerationEnumerateChildrenReversed = enumerateChildrenReversed();
            this.children = new Vector<>();
            while (enumerationEnumerateChildrenReversed.hasMoreElements()) {
                ScriptNode scriptNode = (ScriptNode) enumerationEnumerateChildrenReversed.nextElement();
                scriptNode.inverse();
                this.children.addElement(scriptNode);
            }
        }
    }

    public void reflect() {
        if (this.children != null) {
            Enumeration<DefaultMutableTreeNode> enumerationChildren = children();
            while (enumerationChildren.hasMoreElements()) {
                ((ScriptNode) enumerationChildren.nextElement()).reflect();
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
        Enumeration<DefaultMutableTreeNode> enumerationChildren = children();
        while (enumerationChildren.hasMoreElements()) {
            fullTurnCount += ((ScriptNode) enumerationChildren.nextElement()).getFullTurnCount();
        }
        return fullTurnCount;
    }

    public int getQuarterTurnCount() {
        int quarterTurnCount = 0;
        Enumeration<DefaultMutableTreeNode> enumerationChildren = children();
        while (enumerationChildren.hasMoreElements()) {
            quarterTurnCount += ((ScriptNode) enumerationChildren.nextElement()).getQuarterTurnCount();
        }
        return quarterTurnCount;
    }

    public ScriptNode cloneSubtree() {
        ScriptNode scriptNode = (ScriptNode) clone();
        Enumeration<DefaultMutableTreeNode> enumerationChildren = children();
        while (enumerationChildren.hasMoreElements()) {
            scriptNode.add(((ScriptNode) enumerationChildren.nextElement()).cloneSubtree());
        }
        return scriptNode;
    }
}
