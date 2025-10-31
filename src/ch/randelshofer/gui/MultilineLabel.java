package ch.randelshofer.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MultilineLabel extends Canvas {
    private static final long serialVersionUID = -1943567795692517989L;

    public static final Color inactiveSelectionBackground = new Color(0xD5, 0xD5, 0xD5);

    // private static final Color activeSelectionBackground = new Color(0xFF, 0xFF, 0x40);
    public static final Color activeSelectionBackground = new Color(0x00, 0xFF, 0x40);

    private List<String> lines = new ArrayList<>();

    private int selectionStart = -1;

    private int selectionEnd = -1;

    private int minRows;

    private String text = "";

    // 文本框边距：上 左 下 右
    private Insets insets = new Insets(2, 6, 6, 3);

    // 上一次绘制覆盖图层位置
    private Insets lastFill = new Insets(0, 0, 0, 0);

    // 选中的文本背景色。分为正在执行和未执行两种，分别由active和inactive设置
    private Color selectionBackground;

    private Graphics graphics = null;

    public MultilineLabel() {
        setBackground(Color.white);
        setForeground(Color.black);
        initComponents();
    }

    public int viewToModel(int x, int y) {
        FontMetrics fontMetrics = this.graphics.getFontMetrics(this.graphics.getFont());
        int row = (y - this.insets.top) / fontMetrics.getHeight();
        if (row < 0) {
            return 0;
        }
        if (row >= this.lines.size()) {
            return this.text.length();
        }

        int length = 0;
        for (int i = 0; i < row; i++) {
            length += this.lines.get(i).length();
        }

        String lineHeight = this.lines.get(row);
        int pos = x - this.insets.left;
        for (int i = pos / fontMetrics.charWidth('l') + 1; i <= lineHeight.length(); i++) {
            if (fontMetrics.stringWidth(lineHeight.substring(0, i)) > pos) {
                return (length + i) - 1;
            }
        }
        return length + lineHeight.length();
    }

    public void setText(String text) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = text;
        }

        this.graphics = getGraphics();
        wrapText();
        if (this.graphics != null) {
            this.graphics.clearRect(0, 0, getWidth(), getHeight());
            revalidate();
        }
    }

    private void wrapText() {
        List<String> lines = new ArrayList<>();
        int width = (getWidth() - this.insets.left) - this.insets.right;
        if (width <= 0) {
            lines.add(this.text);
            this.lines = lines;
            return;
        }

        FontMetrics fontMetrics = this.graphics.getFontMetrics(this.graphics.getFont());
        StringTokenizer stringTokenizer = new StringTokenizer(this.text, " \n", true);
        StringBuilder sb = new StringBuilder();
        while (stringTokenizer.hasMoreTokens()) {
            String strNextToken = stringTokenizer.nextToken();
            if (strNextToken.equals("\n")) {
                sb.append(strNextToken);
                lines.add(sb.toString());
                sb.setLength(0);
            } else if (fontMetrics.stringWidth(sb + strNextToken) <= width) {
                sb.append(strNextToken);
            } else if (strNextToken.equals(" ")) {
                sb.append(strNextToken);
                lines.add(sb.toString());
                sb.setLength(0);
            } else {
                lines.add(sb.toString());
                sb.setLength(0);
                sb.append(strNextToken);
            }
        }
        if (sb.length() > 0) {
            lines.add(sb.toString());
        }
        this.lines = lines;
    }

    public String getText() {
        return this.text;
    }

    public synchronized void select(int startPosition, int endPosition, Color background) {
        boolean repaint = false;
        if (endPosition <= this.text.length() && endPosition >= startPosition && startPosition >= 0
            && (this.selectionStart != startPosition || this.selectionEnd != endPosition)) {
            this.selectionStart = startPosition;
            this.selectionEnd = endPosition;
            repaint = true;
        }
        if (!background.equals(this.selectionBackground)) {
            this.selectionBackground = background;
            repaint = true;
        }
        if (repaint) {
            repaint();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        initComponents();
    }

    public void setInsets(Insets insets) {
        this.insets = insets;
        invalidate();
    }

    public Insets getInsets() {
        return this.insets;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = new Dimension();
        Insets insets = getInsets();
        if (this.graphics == null) {
            this.graphics = getGraphics();
        }
        FontMetrics fontMetrics = this.graphics.getFontMetrics(this.graphics.getFont());
        for (String line : this.lines) {
            size.width = Math.max(size.width, fontMetrics.stringWidth(line));
        }
        size.height = fontMetrics.getHeight() * Math.max(this.minRows, this.lines.size());
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }

    public void setMinRows(int minRows) {
        this.minRows = minRows;
        invalidate();
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        this.graphics = g;
        g.clearRect(lastFill.top, lastFill.left, lastFill.bottom, lastFill.right);
        // 绘制选择图层
        Insets insets = getInsets();
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
        if (this.selectionEnd > this.selectionStart) {
            g.setColor(this.selectionBackground);
            int cur = 0;
            int y = insets.top;
            int height = fontMetrics.getHeight();
            for (String line : this.lines) {
                int length = cur + line.length();
                if (this.selectionEnd <= length) {
                    int iMax = Math.max(0, this.selectionStart - cur);
                    int x = insets.left + fontMetrics.stringWidth(line.substring(0, iMax));
                    int weight = fontMetrics.stringWidth(line.substring(iMax, Math.max(0, Math.min(line.length(), this.selectionEnd - cur))));
                    lastFill.set(x, y, weight, height);
                    g.fillRect(lastFill.top, lastFill.left, lastFill.bottom, lastFill.right); // 绘制选择覆盖图层
                    break;
                }
                cur = length;
                y += height;
            }
        }

        // 绘制文字
        g.setColor(getForeground());
        int ascent = insets.top + fontMetrics.getAscent();
        for (String line : this.lines) {
            if (line.length() > 0 && line.charAt(line.length() - 1) == '\n') {
                line = line.substring(0, line.length() - 1);
            }
            g.drawString(line, insets.left, ascent);
            ascent += fontMetrics.getHeight();
        }

        // 绘制边框 (-1,-1,2,2)刚好不显示；(2,2,-4,-4)显示黑色边框
        g.setColor(Color.black);
        g.drawRect(-1, -1, getWidth() + 2, getHeight() + 2);
    }

    private void initComponents() {
        wrapText();
        Component container = this;
        Container parent = this.getParent();
        while (parent != null && parent.isValid()) {
            container = parent;
            parent = parent.getParent();
        }
        container.validate();
    }
}
