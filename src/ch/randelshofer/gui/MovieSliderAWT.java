package ch.randelshofer.gui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;


public class MovieSliderAWT extends Canvas implements ChangeListener
{
    private static final long serialVersionUID = -8332266206293355378L;

    // private static final int THUMB_WIDTH = 8;

    // private static final int THUMB_HEIGHT = 13;

    // private static final int HALF_THUMB_WIDTH = 4;

    private int progressPos_;

    private BoundedRangeModel model_ = new DefaultBoundedRangeModel();

    private int thumbPos_ = 0;

    private BoundedRangeModel progressModel_ = new DefaultBoundedRangeModel(1, 0, 0, 1);

    public MovieSliderAWT()
    {
        this.model_.addChangeListener(this);
        setBackground(Color.lightGray);
    }

    public synchronized void setModel(BoundedRangeModel boundedRangeModel)
    {
        if (this.model_ != null)
        {
            this.model_.removeChangeListener(this);
        }
        this.model_ = boundedRangeModel == null ? new DefaultBoundedRangeModel() : boundedRangeModel;
        if (this.model_ != null)
        {
            this.model_.addChangeListener(this);
        }
        repaint();
    }

    public synchronized BoundedRangeModel getModel()
    {
        return this.model_;
    }

    @Override
    public Dimension preferredSize()
    {
        return new Dimension(50, 15);
    }

    @Override
    public Dimension minimumSize()
    {
        return new Dimension(18, 15);
    }

    @Override
    public void processMouseEvent(MouseEvent e)
    {
        int id = e.getID();
        switch (id)
        {
            case MouseEvent.MOUSE_PRESSED: // 按下
            case MouseEvent.MOUSE_RELEASED: // 弹起
                if (isEnabled())
                {
                    moveThumb(e.getX());
                }
                break;
        }
    }

    @Override
    public void processMouseMotionEvent(MouseEvent e)
    {
        int id = e.getID();
        switch (id)
        {
            case MouseEvent.MOUSE_DRAGGED:
                if (isEnabled())
                {
                    moveThumb(e.getX());
                }
                break;
        }
    }

    @Override
    public boolean mouseDown(Event event, int i, int i2)
    {
        if (isEnabled())
        {
            moveThumb(i);
        }
        return true;
    }

    @Override
    public boolean mouseDrag(Event event, int i, int i2)
    {
        if (isEnabled())
        {
            moveThumb(i);
        }
        return true;
    }

    @Override
    public boolean mouseUp(Event event, int i, int i2)
    {
        if (isEnabled())
        {
            moveThumb(i);
        }
        return true;
    }

    protected void moveThumb(int i)
    {
        int p = computeProgressPos();
        if (i <= 4)
        {
            i = 5;
        }
        if (i >= p - 4)
        {
            i = (p - 4) - 1;
        }
        int h = this.model_.getMaximum() - this.model_.getMinimum();
        int j = Math.max(1, 2 * h);
        int f = ((i - 4 - 1) * h + p * h / j) / (p - 10);
        this.model_.setValue(f);
    }

    protected int computeThumbPos()
    {
        if (this.model_ == null)
        {
            return 4;
        }
        int i = (computeProgressPos() - 4) + 2;
        int f = 0;
        if (this.model_.getMaximum() != this.model_.getMinimum())
        {
            f = i * this.model_.getValue() / (this.model_.getMaximum() - this.model_.getMinimum());
        }
        return Math.max(0, f) + 4;
    }

    public synchronized void setProgressModel(BoundedRangeModel boundedRangeModel)
    {
        if (this.progressModel_ != null)
        {
            this.progressModel_.removeChangeListener(this);
        }
        this.progressModel_ = boundedRangeModel;
        if (this.progressModel_ != null)
        {
            this.progressModel_.addChangeListener(this);
        }
    }

    public synchronized BoundedRangeModel getProgressModel()
    {
        return this.progressModel_;
    }

    @Override
    public void paint(Graphics graphics)
    {
        this.thumbPos_ = computeThumbPos();
        this.progressPos_ = computeProgressPos();
        paint(graphics, this.thumbPos_, this.progressPos_);
    }

    public void paint(Graphics graphics, int i, int i2)
    {
        Dimension size = getSize();
        int i3 = size.width;
        int i4 = size.height;
        int iMin = Math.min(Math.max(i, 0), i3);
        if (!isEnabled())
        {
            graphics.setColor(Color.gray);
        }
        graphics.drawRect(0, 0, i3 - 1, i4 - 1);
        graphics.drawRect(4, 4, (i3 - 8) - 1, i4 - 9);
        if (isEnabled())
        {
            graphics.setColor(Color.white);
            graphics.drawLine(1, 1, i3 - 2, 1);
            graphics.drawLine(1, 2, 1, i4 - 2);
            graphics.drawLine(5, 5, (i3 - 4) - 3, 5);
            graphics.drawLine(5, 6, 5, i4 - 6);
            if (i2 > 0)
            {
                graphics.setColor(Color.gray);
                graphics.fillRect(6, 6, i2 - 4, i4 - 11);
            }
            graphics.setColor(Color.white);
            graphics.drawRect((iMin - 4) + 1, 1, 6, i4 - 3);
            graphics.setColor(getForeground());
        }
        graphics.drawRect(iMin - 4, 0, 8, i4 - 1);
        graphics.drawRect((iMin - 4) + 2, 2, 4, i4 - 5);
    }

    protected int computeProgressPos()
    {
        BoundedRangeModel boundedRangeModel = this.progressModel_;
        int i = (getSize().width - 8) - 3;
        if (boundedRangeModel == null)
        {
            return 6;
        }
        int f = 0;
        if (boundedRangeModel.getMaximum() != boundedRangeModel.getMinimum())
        {
            f = i * boundedRangeModel.getValue() / (boundedRangeModel.getMaximum() - boundedRangeModel.getMinimum());
        }
        return f + 4;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        if (computeProgressPos() != this.progressPos_ || computeThumbPos() != this.thumbPos_)
        {
            repaint();
        }
    }

    @Override
    public void setEnabled(boolean z)
    {
        if (z != isEnabled())
        {
            super.setEnabled(z);
            repaint();
        }
    }
}
