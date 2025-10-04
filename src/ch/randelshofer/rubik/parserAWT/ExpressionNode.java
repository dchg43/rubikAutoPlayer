package ch.randelshofer.rubik.parserAWT;

public class ExpressionNode extends ScriptNode
{
    private static final long serialVersionUID = -123494831068447950L;

    public ExpressionNode()
    {}

    public ExpressionNode(int i, int i2)
    {
        super(i, i2);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.STATEMENT_EXPRESSION;
    }
}
