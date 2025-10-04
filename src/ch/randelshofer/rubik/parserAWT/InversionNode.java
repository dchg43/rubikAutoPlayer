package ch.randelshofer.rubik.parserAWT;


import java.util.Enumeration;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;


public class InversionNode extends ScriptNode
{
    private static final long serialVersionUID = 2579430851956145652L;

    public InversionNode()
    {}

    public InversionNode(int startpos, int endpos)
    {
        super(startpos, endpos);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.INVERSION_EXPRESSION;
    }

    @Override
    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean inverse)
    {
        return super.resolvedEnumeration(!inverse);
    }
}
