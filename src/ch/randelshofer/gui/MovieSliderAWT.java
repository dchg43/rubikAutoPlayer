package ch.randelshofer.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;

/** 控制进度条的展示 */
public class MovieSliderAWT extends Canvas implements ChangeListener {
    private static final long serialVersionUID = -8332266206293355378L;

    // private static final int THUMB_WIDTH = 8;

    // private static final int THUMB_HEIGHT = 13;

    // private static final int HALF_THUMB_WIDTH = 4;

    private int progressPos_;

    private BoundedRangeModel model_ = new DefaultBoundedRangeModel();

    private int thumbPos_ = 0;

    private BoundedRangeModel progressModel_ = new DefaultBoundedRangeModel(1, 0, 0, 1);

    public MovieSliderAWT() {
        this.model_.addChangeListener(this);
        setBackground(Color.lightGray);
    }

    public synchronized void setModel(BoundedRangeModel boundedRangeModel) {
        if (this.model_ != null) {
            this.model_.removeChangeListener(this);
        }
        this.model_ = boundedRangeModel == null ? new DefaultBoundedRangeModel() : boundedRangeModel;
        if (this.model_ != null) {
            this.model_.addChangeListener(this);
        }
        repaint();
    }

    public synchronized BoundedRangeModel getModel() {
        return this.model_;
    }

    @Override
    public Dimension preferredSize() {
        return new Dimension(50, 15);
    }

    @Override
    public Dimension minimumSize() {
        return new Dimension(18, 15);
    }

    @Override
    public void processMouseEvent(MouseEvent e) {
        int id = e.getID();
        switch (id) {
        case MouseEvent.MOUSE_PRESSED: // 按下
        case MouseEvent.MOUSE_RELEASED: // 弹起
            if (isEnabled()) {
                moveThumb(e.getX());
            }
            break;
        }
    }

    @Override
    public void processMouseMotionEvent(MouseEvent e) {
        int id = e.getID();
        switch (id) {
        case MouseEvent.MOUSE_DRAGGED:
            if (isEnabled()) {
                moveThumb(e.getX());
            }
            break;
        }
    }

    @Override
    public boolean mouseDown(Event event, int x, int y) {
        if (isEnabled()) {
            moveThumb(x);
        }
        return true;
    }

    @Override
    public boolean mouseDrag(Event event, int x, int y) {
        if (isEnabled()) {
            moveThumb(x);
        }
        return true;
    }

    @Override
    public boolean mouseUp(Event event, int x, int y) {
        if (isEnabled()) {
            moveThumb(x);
        }
        return true;
    }

    protected void moveThumb(int x) {
        int p = computeProgressPos();
        if (x <= 4) {
            x = 5;
        }
        if (x >= p - 4) {
            x = (p - 4) - 1;
        }
        int h = this.model_.getMaximum() - this.model_.getMinimum();
        int j = Math.max(1, 2 * h);
        int f = ((x - 4 - 1) * h + p * h / j) / (p - 10);
        this.model_.setValue(f);
    }

    protected int computeThumbPos() {
        if (this.model_ == null) {
            return 4;
        }
        int i = (computeProgressPos() - 4) + 2;
        int f = 0;
        if (this.model_.getMaximum() != this.model_.getMinimum()) {
            f = i * this.model_.getValue() / (this.model_.getMaximum() - this.model_.getMinimum());
        }
        return Math.max(0, f) + 4;
    }

    public synchronized void setProgressModel(BoundedRangeModel boundedRangeModel) {
        if (this.progressModel_ != null) {
            this.progressModel_.removeChangeListener(this);
        }
        this.progressModel_ = boundedRangeModel;
        if (this.progressModel_ != null) {
            this.progressModel_.addChangeListener(this);
        }
    }

    public synchronized BoundedRangeModel getProgressModel() {
        return this.progressModel_;
    }

    @Override
    public void paint(Graphics graphics) {
        this.thumbPos_ = computeThumbPos();
        this.progressPos_ = computeProgressPos();
        paint(graphics, this.thumbPos_, this.progressPos_);
    }

    public void paint(Graphics graphics, int x, int y) {
        Dimension size = getSize();
        int width = size.width;
        int height = size.height;
        int xMin = Math.min(Math.max(x, 0), width);
        if (!isEnabled()) {
            graphics.setColor(Color.gray);
        }
        graphics.drawRect(0, 0, width - 1, height - 1);
        graphics.drawRect(4, 4, (width - 8) - 1, height - 9);
        if (isEnabled()) {
            graphics.setColor(Color.white);
            graphics.drawLine(1, 1, width - 2, 1);
            graphics.drawLine(1, 2, 1, height - 2);
            graphics.drawLine(5, 5, (width - 4) - 3, 5);
            graphics.drawLine(5, 6, 5, height - 6);
            if (y > 0) {
                graphics.setColor(Color.gray);
                graphics.fillRect(6, 6, y - 4, height - 11);
            }
            graphics.setColor(Color.white);
            graphics.drawRect((xMin - 4) + 1, 1, 6, height - 3);
            graphics.setColor(getForeground());
        }
        graphics.drawRect(xMin - 4, 0, 8, height - 1);
        graphics.drawRect((xMin - 4) + 2, 2, 4, height - 5);
    }

    protected int computeProgressPos() {
        BoundedRangeModel boundedRangeModel = this.progressModel_;
        int i = (getSize().width - 8) - 3;
        if (boundedRangeModel == null) {
            return 6;
        }
        int f = 0;
        if (boundedRangeModel.getMaximum() != boundedRangeModel.getMinimum()) {
            f = i * boundedRangeModel.getValue() / (boundedRangeModel.getMaximum() - boundedRangeModel.getMinimum());
        }
        return f + 4;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        if (computeProgressPos() != this.progressPos_ || computeThumbPos() != this.thumbPos_) {
            repaint();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            repaint();
        }
    }
}
