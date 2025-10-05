package ch.randelshofer.gui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.StringTokenizer;
import java.util.Vector;


public class MultilineLabel extends Canvas
{
    private static final long serialVersionUID = -1943567795692517989L;

    private String[] lines;

    private int selectionStart;

    private int selectionEnd;

    private int minRows;

    private String text = "";

    private Insets insets = new Insets(2, 3, 3, 3);

    private Color selectionBackground = new Color(181, 213, 255);

    public MultilineLabel()
    {
        initComponents();
        setBackground(Color.white);
        setForeground(Color.black);
    }

    public void setSelectionBackground(Color selectionBackground)
    {
        this.selectionBackground = selectionBackground;
        repaint();
    }

    public int viewToModel(int x, int y)
    {
        FontMetrics fontMetrics = getFontMetrics(getFont());
        int height = (y - this.insets.top) / fontMetrics.getHeight();
        if (height < 0 || this.lines == null)
        {
            return 0;
        }
        if (height >= this.lines.length)
        {
            return this.text.length();
        }
        int length = 0;
        for (int i = 0; i < height; i++)
        {
            length += this.lines[i].length();
        }
        for (int i = 1; i <= this.lines[height].length(); i++)
        {
            if (fontMetrics.stringWidth(this.lines[height].substring(0, i)) + this.insets.left > x)
            {
                return (length + i) - 1;
            }
        }
        return length + this.lines[height].length();
    }

    public void setText(String text)
    {
        this.text = text;
        repaint();
        revalidate();
    }

    private void wrapText()
    {
        String text = this.text;
        if (text == null)
        {
            return;
        }
        int width = (getSize().width - this.insets.left) - this.insets.right;
        FontMetrics fontMetrics = getFontMetrics(getFont());
        Vector<String> vector = new Vector<>();
        StringTokenizer stringTokenizer = new StringTokenizer(text, " \n", true);
        StringBuffer stringBuffer = new StringBuffer();
        while (stringTokenizer.hasMoreTokens())
        {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals("\n"))
            {
                stringBuffer.append(strNextToken);
                vector.addElement(stringBuffer.toString());
                stringBuffer.setLength(0);
            }
            else if (fontMetrics.stringWidth(stringBuffer + strNextToken) <= width)
            {
                stringBuffer.append(strNextToken);
            }
            else if (strNextToken.equals(" "))
            {
                stringBuffer.append(strNextToken);
                vector.addElement(stringBuffer.toString());
                stringBuffer.setLength(0);
            }
            else
            {
                vector.addElement(stringBuffer.toString());
                stringBuffer.setLength(0);
                stringBuffer.append(strNextToken);
            }
        }
        if (stringBuffer.length() > 0)
        {
            vector.addElement(stringBuffer.toString());
        }
        String[] strArr = new String[vector.size()];
        vector.copyInto(strArr);
        this.lines = strArr;
    }

    public String getText()
    {
        return this.text;
    }

    public synchronized void select(int startPosition, int endPosition)
    {
        this.selectionStart = Math.min(this.text.length(), Math.max(0, startPosition));
        this.selectionEnd = Math.min(this.text.length(), Math.max(startPosition, endPosition));
        repaint();
    }

    @Override
    public void invalidate()
    {
        this.lines = null;
        super.invalidate();
    }

    public void setInsets(Insets insets)
    {
        this.insets = insets;
        invalidate();
    }

    public Insets getInsets()
    {
        return this.insets;
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension size = new Dimension();
        Insets insets = getInsets();
        if (this.lines == null)
        {
            wrapText();
        }
        FontMetrics fontMetrics = getFontMetrics(getFont());
        for (String line : this.lines)
        {
            size.width = Math.max(size.width, fontMetrics.stringWidth(line));
        }
        size.height = fontMetrics.getHeight() * Math.max(this.minRows, this.lines.length);
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }

    public void setMinRows(int minRows)
    {
        this.minRows = minRows;
        invalidate();
    }

    @Override
    public void paint(Graphics graphics)
    {
        Dimension size = getSize();
        graphics.setColor(Color.black);
        graphics.drawRect(0, -1, size.width - 1, size.height);
        if (this.text == null)
        {
            return;
        }
        if (this.lines == null)
        {
            invalidate();
            wrapText();
            Component container = this;
            Container parent = this.getParent();
            while (parent != null && parent.isValid())
            {
                container = parent;
                parent = parent.getParent();
            }
            container.validate();
            return;
        }
        String[] strArr = this.lines;
        if (strArr == null)
        {
            return;
        }
        Insets insets = getInsets();
        FontMetrics fontMetrics = getFontMetrics(getFont());
        if (this.selectionEnd > this.selectionStart)
        {
            graphics.setColor(this.selectionBackground);
            int l = 0;
            int y = insets.top;
            int height = fontMetrics.getHeight();
            for (String element : strArr)
            {
                int length = l + element.length();
                if (length >= this.selectionStart && l <= this.selectionEnd)
                {
                    int iMax = Math.max(0, this.selectionStart - l);
                    int x = insets.left + fontMetrics.stringWidth(element.substring(0, iMax));
                    int weight = fontMetrics.stringWidth(
                        element.substring(iMax, Math.max(0, Math.min(element.length(), this.selectionEnd - l))));
                    graphics.fillRect(x, y, weight, height);
                }
                l = length;
                y += height;
            }
        }
        graphics.setColor(getForeground());
        int ascent = insets.top + fontMetrics.getAscent();
        for (String sub : strArr)
        {
            if (sub.length() > 0 && sub.charAt(sub.length() - 1) == '\n')
            {
                sub = sub.substring(0, sub.length() - 1);
            }
            graphics.drawString(sub, insets.left, ascent);
            ascent += fontMetrics.getHeight();
        }
    }

    private void initComponents()
    {}
}
