package ch.randelshofer.gui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.List;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;


/** 底部进度条两边小按钮 */
public class AbstractButton extends Canvas implements ItemSelectable
{
    private static final long serialVersionUID = 5800391645017871439L;

    private ChangeEvent changeEvent;

    private Dimension preferredSize;

    private Dimension minimumSize;

    private boolean isPressed;

    private boolean isSelected;

    private boolean isArmed;

    private EventListenerList listenerList = new EventListenerList();

    private String actionCommand;

    private Icon selectedIcon;

    private Icon unselectedIcon;

    protected ButtonGroup group;

    public void setPressed(boolean isPressed)
    {
        if (isPressed != this.isPressed)
        {
            this.isPressed = isPressed;
            if (!isPressed)
            {
                fireActionPerformed(new ActionEvent(this, 1001, getActionCommand()));
            }
            fireStateChanged();
            repaint();
        }
    }

    public void setGroup(ButtonGroup buttonGroup)
    {
        this.group = buttonGroup;
    }

    public boolean isPressed()
    {
        return this.isPressed;
    }

    public void setSelected(boolean isSelected)
    {
        if (isSelected != this.isSelected)
        {
            this.isSelected = isSelected;
            fireItemStateChanged(new ItemEvent(this, 701, this, isSelected ? 1 : 2));
            fireStateChanged();
            repaint();
        }
    }

    public boolean isSelected()
    {
        return this.isSelected;
    }

    public void setArmed(boolean isArmed)
    {
        if (isArmed != this.isArmed)
        {
            this.isArmed = isArmed;
            fireStateChanged();
            repaint();
        }
    }

    public boolean isArmed()
    {
        return this.isArmed;
    }

    public void setActionCommand(String actionCommand)
    {
        this.actionCommand = actionCommand;
    }

    public String getActionCommand()
    {
        return this.actionCommand;
    }

    public void setSelectedIcon(Icon icon)
    {
        this.selectedIcon = icon;
    }

    public void setUnselectedIcon(Icon icon)
    {
        this.unselectedIcon = icon;
    }

    public void setIcon(Icon icon)
    {
        setUnselectedIcon(icon);
    }

    public Icon getIcon()
    {
        return getUnselectedIcon();
    }

    public Icon getUnselectedIcon()
    {
        return this.unselectedIcon;
    }

    public Icon getSelectedIcon()
    {
        return this.selectedIcon;
    }

    @Override
    public void paint(Graphics graphics)
    {
        Dimension size = getSize();
        int width = size.width;
        int height = size.height;
        if (!isEnabled())
        {
            graphics.setColor(Color.gray);
        }
        graphics.drawRect(0, 0, width - 1, height - 1);
        if (this.isPressed && this.isArmed)
        {
            graphics.setColor(Color.gray.darker());
            graphics.fillRect(1, 1, width - 3, height - 3);
            graphics.setColor(Color.darkGray);
            graphics.drawLine(1, 1, width - 2, 1);
            graphics.drawLine(1, 1, 1, height - 2);
            graphics.setColor(Color.gray);
            graphics.drawLine(2, height - 2, width - 2, height - 2);
            graphics.drawLine(width - 2, height - 2, width - 2, 2);
        }
        else
        {
            graphics.setColor((!this.isSelected || this.group == null) ? Color.lightGray : new Color(160, 160, 160));
            graphics.fillRect(1, 1, width - 2, height - 2);
            if (isEnabled())
            {
                graphics.setColor(this.isSelected ? Color.gray : Color.white);
                graphics.drawLine(1, 1, width - 3, 1);
                graphics.drawLine(1, 1, 1, height - 3);
                if (!this.isSelected && this.group != null)
                {
                    graphics.setColor(Color.gray);
                    graphics.drawLine(1, height - 2, width - 2, height - 2);
                    graphics.drawLine(width - 2, height - 2, width - 2, 2);
                }
            }
        }
        Icon icon = (!this.isSelected || this.selectedIcon == null) ? this.unselectedIcon : this.selectedIcon;
        if (icon != null)
        {
            graphics.setColor(getForeground());
            icon.paintIcon(this, graphics, 2, 1);
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        return preferredSize();
    }

    @Override
    public Dimension preferredSize()
    {
        if (this.preferredSize != null)
        {
            return this.preferredSize;
        }
        Icon icon = getIcon();
        return icon != null ? new Dimension(icon.getIconWidth() + 4,
            icon.getIconHeight() + 4) : super.getPreferredSize();
    }

    @Override
    public void setPreferredSize(Dimension preferredSize)
    {
        this.preferredSize = preferredSize;
    }

    @Override
    public Dimension getMinimumSize()
    {
        return minimumSize();
    }

    @Override
    public Dimension minimumSize()
    {
        return this.minimumSize == null ? super.getMinimumSize() : this.minimumSize;
    }

    @Override
    public void setMinimumSize(Dimension minimumSize)
    {
        this.minimumSize = minimumSize;
    }

    @Override
    public void processMouseEvent(MouseEvent e)
    {
        int id = e.getID();
        switch (id)
        {
            case MouseEvent.MOUSE_PRESSED: // 按下
                setArmed(true);
                setPressed(true);
                repaint();
                break;
            case MouseEvent.MOUSE_RELEASED: // 弹起
                setPressed(false);
                repaint();
                break;
            case MouseEvent.MOUSE_CLICKED: // 点击（按下并弹起）
                setArmed(true);
                setPressed(false);
                repaint();
                break;
            case MouseEvent.MOUSE_EXITED: // 移开
                setArmed(false);
                repaint();
                break;
            case MouseEvent.MOUSE_ENTERED: // 移入
                setArmed(true);
                repaint();
                break;
        }
    }

    @Override
    public boolean mouseEnter(Event event, int x, int y)
    {
        setArmed(true);
        repaint();
        return true;
    }

    @Override
    public boolean mouseExit(Event event, int x, int y)
    {
        setArmed(false);
        repaint();
        return true;
    }

    @Override
    public boolean mouseDown(Event event, int x, int y)
    {
        setArmed(true);
        setPressed(true);
        repaint();
        return true;
    }

    @Override
    public boolean mouseUp(Event event, int x, int y)
    {
        setPressed(false);
        repaint();
        return true;
    }

    public void addChangeListener(ChangeListener changeListener)
    {
        this.listenerList.add(ChangeListener.class, changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener)
    {
        this.listenerList.remove(ChangeListener.class, changeListener);
    }

    public void addActionListener(ActionListener actionListener)
    {
        this.listenerList.add(ActionListener.class, actionListener);
    }

    public void removeActionListener(ActionListener actionListener)
    {
        this.listenerList.remove(ActionListener.class, actionListener);
    }

    @Override
    public void addItemListener(ItemListener itemListener)
    {
        this.listenerList.add(ItemListener.class, itemListener);
    }

    @Override
    public void removeItemListener(ItemListener itemListener)
    {
        this.listenerList.remove(ItemListener.class, itemListener);
    }

    protected void fireActionPerformed(ActionEvent actionEvent)
    {
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList)
        {
            if (node.getClazz() == ActionListener.class)
            {
                ((ActionListener)node.getListener()).actionPerformed(actionEvent);
            }
        }
    }

    protected void fireItemStateChanged(ItemEvent itemEvent)
    {
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList)
        {
            if (node.getClazz() == ItemListener.class)
            {
                ((ItemListener)node.getListener()).itemStateChanged(itemEvent);
            }
        }
    }

    protected void fireStateChanged()
    {
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList)
        {
            if (node.getClazz() == ChangeListener.class)
            {
                if (this.changeEvent == null)
                {
                    this.changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)node.getListener()).stateChanged(this.changeEvent);
            }
        }
    }

    public void stateChanged(ChangeEvent changeEvent)
    {
        repaint();
        fireStateChanged();
    }

    @Override
    public Object[] getSelectedObjects()
    {
        return null;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        if (enabled != isEnabled())
        {
            super.setEnabled(enabled);
            repaint();
        }
    }

}
