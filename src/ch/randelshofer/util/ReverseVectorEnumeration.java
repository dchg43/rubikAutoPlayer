package ch.randelshofer.util;

import java.util.Iterator;
import java.util.List;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class ReverseVectorEnumeration implements Iterator<DefaultMutableTreeNode> {
    private List<DefaultMutableTreeNode> nodes;

    private int index;

    public ReverseVectorEnumeration(List<DefaultMutableTreeNode> nodes) {
        this.nodes = nodes;
        this.index = nodes.size() - 1;
    }

    @Override
    public boolean hasNext() {
        return this.index >= 0;
    }

    @Override
    public DefaultMutableTreeNode next() {
        return this.nodes.get(this.index--);
    }
}
