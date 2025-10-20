package ch.randelshofer.util;

import java.util.Enumeration;
import java.util.Vector;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class ReverseVectorEnumeration implements Enumeration<DefaultMutableTreeNode> {
    private Vector<DefaultMutableTreeNode> vector;

    private int index;

    public ReverseVectorEnumeration(Vector<DefaultMutableTreeNode> vector) {
        this.vector = vector;
        this.index = vector.size() - 1;
    }

    @Override
    public boolean hasMoreElements() {
        return this.index >= 0;
    }

    @Override
    public DefaultMutableTreeNode nextElement() {
        return this.vector.elementAt(this.index--);
    }
}
