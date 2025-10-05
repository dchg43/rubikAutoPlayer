package ch.randelshofer.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.media.Player;


/** 控制步骤列表的操作 */
public class MovieControlAWT extends Panel implements ActionListener, ItemListener, ChangeListener
{
    private static final long serialVersionUID = -1687732120831089973L;

    private Player player;

    private MovieSliderAWT slider;

    private AbstractButton forwardButton;

    private AbstractButton rewindButton;

    private ToggleButton startButton;

    private BoundedRangeModel boundedRangeModel;

    public MovieControlAWT()
    {
        setForeground(Color.black);
        Dimension dimension = new Dimension(15, 15);
        Dimension dimension2 = new Dimension(13, 13);
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        this.startButton = new ToggleButton();
        this.startButton.setUnselectedIcon(
            new PolygonIcon(new Polygon(new int[] {4, 7, 7, 4}, new int[] {2, 5, 6, 9}, 4), dimension2));
        this.startButton.setSelectedIcon(
            new PolygonIcon(new Polygon[] {new Polygon(new int[] {3, 4, 4, 3}, new int[] {2, 2, 9, 9}, 4),
                new Polygon(new int[] {7, 8, 8, 7}, new int[] {2, 2, 9, 9}, 4)}, dimension2));
        this.startButton.addItemListener(this);
        this.startButton.setPreferredSize(dimension);
        this.startButton.setMinimumSize(dimension);
        gridBagLayout.setConstraints(this.startButton, new GridBagConstraints());
        add(this.startButton);
        this.slider = new MovieSliderAWT();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.weightx = 1.0d;
        gridBagLayout.setConstraints(this.slider, gridBagConstraints);
        add(this.slider);
        this.rewindButton = new AbstractButton();
        this.rewindButton.setIcon(
            new PolygonIcon(new Polygon[] {new Polygon(new int[] {4, 4, 1, 1}, new int[] {2, 9, 6, 5}, 4),
                new Polygon(new int[] {7, 8, 8, 7}, new int[] {2, 2, 9, 9}, 4)}, dimension2));
        this.rewindButton.setPreferredSize(dimension);
        this.rewindButton.setMinimumSize(dimension);
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 2;
        gridBagLayout.setConstraints(this.rewindButton, gridBagConstraints2);
        add(this.rewindButton);
        this.rewindButton.addActionListener(this);
        this.forwardButton = new AbstractButton();
        this.forwardButton.setIcon(
            new PolygonIcon(new Polygon[] {new Polygon(new int[] {2, 3, 3, 2}, new int[] {2, 2, 9, 9}, 4),
                new Polygon(new int[] {6, 9, 9, 6}, new int[] {2, 5, 6, 9}, 4)}, dimension2));
        this.forwardButton.setPreferredSize(dimension);
        this.forwardButton.setMinimumSize(dimension);
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 3;
        gridBagLayout.setConstraints(this.forwardButton, gridBagConstraints3);
        add(this.forwardButton);
        this.forwardButton.addActionListener(this);
    }

    public synchronized void setPlayer(Player player)
    {
        if (this.player != null)
        {
            this.player.removeChangeListener(this);
        }
        this.player = player;
        this.boundedRangeModel = this.player == null ? null : this.player.getBoundedRangeModel();
        this.slider.setModel(this.boundedRangeModel);
        if (this.player != null)
        {
            this.startButton.setSelected(this.player.isActive());
            this.player.addChangeListener(this);
        }
    }

    @Override
    public void doLayout()
    {
        super.doLayout();
        Point startLocation = this.startButton.getLocation();
        this.startButton.setLocation(startLocation.x - 1, startLocation.y);
        Point rewindLocation = this.rewindButton.getLocation();
        this.rewindButton.setLocation(rewindLocation.x + 1, rewindLocation.y);
        Rectangle bounds = this.slider.getBounds();
        this.slider.setBounds(bounds.x - 2, bounds.y, bounds.width + 4, bounds.height);
    }

    public void setProgressModel(BoundedRangeModel boundedRangeModel)
    {
        this.slider.setProgressModel(boundedRangeModel);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        Object source = actionEvent.getSource();
        if (this.boundedRangeModel != null)
        {
            int value = this.boundedRangeModel.getValue();
            if (source == this.forwardButton)
            {
                this.boundedRangeModel.setValue(
                    value == this.boundedRangeModel.getMaximum() ? this.boundedRangeModel.getMinimum() : value + 1);
            }
            else if (source == this.rewindButton)
            {
                this.boundedRangeModel.setValue(
                    value == this.boundedRangeModel.getMinimum() ? this.boundedRangeModel.getMaximum() : value - 1);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent)
    {
        if (this.player == null || this.startButton.isSelected() == this.player.isActive())
        {
            return;
        }
        if (this.startButton.isSelected())
        {
            this.player.start();
        }
        else
        {
            this.player.stop();
        }
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        this.startButton.setSelected(this.player.isActive());
    }
}
