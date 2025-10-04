package ch.randelshofer.rubik.parserAWT;


import java.util.Enumeration;

import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.rubik.RubiksCubeCore;
import ch.randelshofer.util.SingletonEnumeration;


public class TwistNode extends ScriptNode
{
    private static final long serialVersionUID = 159366833446647834L;

    private int symbol;

    public TwistNode()
    {
        this.symbol = 84;
        setAllowsChildren(false);
    }

    public TwistNode(int i)
    {
        this.symbol = i;
        setAllowsChildren(false);
    }

    public TwistNode(int i, int i2, int i3)
    {
        super(i2, i3);
        this.symbol = i;
        setAllowsChildren(false);
    }

    @Override
    public int getSymbol()
    {
        return this.symbol;
    }

    public void setSymbol(int i)
    {
        this.symbol = i;
    }

    @Override
    public void applyTo(RubiksCubeCore rubiksCubeCore)
    {
        applyTo(rubiksCubeCore, false);
    }

    public void applyTo(RubiksCubeCore rubiksCubeCore, boolean z)
    {
        ScriptParser.applyTo(rubiksCubeCore, this.symbol, z);
    }

    @Override
    public void applyInverseTo(RubiksCubeCore rubiksCubeCore)
    {
        applyTo(rubiksCubeCore, true);
    }

    @Override
    public int getFullTurnCount()
    {
        int layerMask = ScriptParser.getLayerMask(this.symbol);
        return (layerMask == 0 || layerMask == 7) ? 0 : 1;
    }

    @Override
    public int getQuarterTurnCount()
    {
        int layerMask = ScriptParser.getLayerMask(this.symbol);
        int iAbs = Math.abs(ScriptParser.getAngle(this.symbol));
        if (layerMask == 0 || layerMask == 7)
        {
            return 0;
        }
        return layerMask == 2 ? iAbs * 2 : iAbs;
    }

    public void append(ScriptParser scriptParser, StringBuffer stringBuffer)
    {
        stringBuffer.append(scriptParser.getFirstToken(this.symbol));
    }

    @Override
    public Enumeration<DefaultMutableTreeNode> resolvedEnumeration(boolean z)
    {
        if (z)
        {
            TwistNode twistNode = (TwistNode)clone();
            twistNode.inverse();
            return new SingletonEnumeration(twistNode);
        }
        return new SingletonEnumeration(this);
    }

    @Override
    public void transform(int i)
    {
        this.symbol = ScriptParser.transformSymbol(i, this.symbol);
    }

    @Override
    public void inverse()
    {
        this.symbol = ScriptParser.inverseSymbol(this.symbol);
    }

    @Override
    public void reflect()
    {
        this.symbol = ScriptParser.reflectSymbol(this.symbol);
    }

    public boolean isRotationSymbol()
    {
        return ScriptParser.isRotationSymbol(this.symbol);
    }
}
