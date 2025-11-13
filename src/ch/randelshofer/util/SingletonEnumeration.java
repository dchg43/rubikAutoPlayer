package ch.randelshofer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class SingletonEnumeration implements Iterator<DefaultMutableTreeNode> {
    private DefaultMutableTreeNode aloneNode;

    public SingletonEnumeration(DefaultMutableTreeNode defaultMutableTreeNode) {
        this.aloneNode = defaultMutableTreeNode;
    }

    @Override
    public boolean hasNext() {
        return this.aloneNode != null;
    }

    @Override
    public DefaultMutableTreeNode next() {
        if (this.aloneNode == null) {
            throw new NoSuchElementException();
        }
        DefaultMutableTreeNode node = this.aloneNode;
        this.aloneNode = null;
        return node;
    }
}
