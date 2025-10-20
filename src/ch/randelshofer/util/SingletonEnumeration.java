package ch.randelshofer.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;

public class SingletonEnumeration implements Enumeration<DefaultMutableTreeNode> {
    private DefaultMutableTreeNode aloneNode;

    public SingletonEnumeration(DefaultMutableTreeNode defaultMutableTreeNode) {
        this.aloneNode = defaultMutableTreeNode;
    }

    @Override
    public boolean hasMoreElements() {
        return this.aloneNode != null;
    }

    @Override
    public synchronized DefaultMutableTreeNode nextElement() {
        if (this.aloneNode == null) {
            throw new NoSuchElementException();
        }
        DefaultMutableTreeNode node = this.aloneNode;
        this.aloneNode = null;
        return node;
    }
}
