package ch.randelshofer.rubik.parserAWT;

public class ExpressionNode extends ScriptNode
{
    private static final long serialVersionUID = -123494831068447950L;

    public ExpressionNode()
    {}

    public ExpressionNode(int startpos, int endpos)
    {
        super(startpos, endpos);
    }

    @Override
    public int getSymbol()
    {
        return ScriptParser.STATEMENT_EXPRESSION;
    }
}
