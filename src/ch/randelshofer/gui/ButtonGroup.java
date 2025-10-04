package ch.randelshofer.gui;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;


public class ButtonGroup implements Serializable, ItemListener
{
    private static final long serialVersionUID = -2734696143977806127L;

    private Vector<AbstractButton> buttons = new Vector<>();

    private AbstractButton selection = null;

    public void add(AbstractButton abstractButton)
    {
        if (abstractButton == null)
        {
            return;
        }
        this.buttons.addElement(abstractButton);
        abstractButton.setGroup(this);
        if (this.selection == null && abstractButton.isSelected())
        {
            this.selection = abstractButton;
        }
        abstractButton.addItemListener(this);
    }

    public void remove(AbstractButton abstractButton)
    {
        if (abstractButton == null)
        {
            return;
        }
        this.buttons.removeElement(abstractButton);
        abstractButton.setGroup(null);
        if (abstractButton == this.selection)
        {
            this.selection = null;
        }
        abstractButton.removeItemListener(this);
    }

    public Enumeration<AbstractButton> getElements()
    {
        return this.buttons.elements();
    }

    public AbstractButton getSelection()
    {
        return this.selection;
    }

    public void setSelected(AbstractButton abstractButton, boolean isSelected)
    {
        if (!isSelected || abstractButton == this.selection)
        {
            return;
        }
        AbstractButton abstractButton2 = this.selection;
        this.selection = abstractButton;
        if (abstractButton2 != null)
        {
            abstractButton2.setSelected(false);
        }
    }

    public boolean isSelected(AbstractButton abstractButton)
    {
        return abstractButton == this.selection;
    }

    public int getButtonCount()
    {
        if (this.buttons == null)
        {
            return 0;
        }
        return this.buttons.size();
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent)
    {
        if (itemEvent.getStateChange() == 1)
        {
            setSelected((AbstractButton)itemEvent.getSource(), true);
        }
    }
}
