package ch.randelshofer.rubik.parserAWT;


import java.util.Enumeration;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;


public class InversionNode extends ScriptNode
{
    private static final long serialVersionUID = 2579430851956145652L;

    public InversionNode()
    {}

    public InversionNode(int i, int i2)
    {
        super(i, i2);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.INVERSION_EXPRESSION;
    }

    @Override
    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean z)
    {
        return super.resolvedEnumeration(!z);
    }
}
