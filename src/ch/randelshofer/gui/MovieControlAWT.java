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
public class MovieControlAWT extends Panel implements ActionListener, ItemListener, ChangeListener {
    private static final long serialVersionUID = -1687732120831089973L;

    private Player player;

    private MovieSliderAWT slider;

    private AbstractButton forwardButton;

    private AbstractButton rewindButton;

    private ToggleButton startButton;

    private BoundedRangeModel boundedRangeModel;

    public MovieControlAWT() {
        setForeground(Color.black);
        Dimension preferredSize = new Dimension(15, 15);
        Dimension initSize = new Dimension(13, 13);
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        // 播放按钮
        this.startButton = new ToggleButton();
        this.startButton.setUnselectedIcon(new PolygonIcon(new Polygon(new int[]{4, 7, 7, 4}, new int[]{2, 5, 6, 9}, 4), initSize));
        this.startButton.setSelectedIcon(new PolygonIcon(
                new Polygon[]{new Polygon(new int[]{3, 4, 4, 3}, new int[]{2, 2, 9, 9}, 4), new Polygon(new int[]{7, 8, 8, 7}, new int[]{2, 2, 9, 9}, 4)},
                initSize));
        this.startButton.addItemListener(this);
        this.startButton.setPreferredSize(preferredSize);
        this.startButton.setMinimumSize(preferredSize);
        gridBagLayout.setConstraints(this.startButton, new GridBagConstraints());
        add(this.startButton);

        // 进度条
        this.slider = new MovieSliderAWT();
        GridBagConstraints sliderConstraints = new GridBagConstraints();
        sliderConstraints.gridx = 1;
        sliderConstraints.fill = 2;
        sliderConstraints.weightx = 1.0d;
        gridBagLayout.setConstraints(this.slider, sliderConstraints);
        add(this.slider);

        // 后退按钮
        this.rewindButton = new AbstractButton();
        this.rewindButton.setIcon(new PolygonIcon(
                new Polygon[]{new Polygon(new int[]{4, 4, 1, 1}, new int[]{2, 9, 6, 5}, 4), new Polygon(new int[]{7, 8, 8, 7}, new int[]{2, 2, 9, 9}, 4)},
                initSize));
        this.rewindButton.setPreferredSize(preferredSize);
        this.rewindButton.setMinimumSize(preferredSize);
        this.rewindButton.addActionListener(this);
        GridBagConstraints rewindConstraints = new GridBagConstraints();
        rewindConstraints.gridx = 2;
        gridBagLayout.setConstraints(this.rewindButton, rewindConstraints);
        add(this.rewindButton);

        // 前进按钮
        this.forwardButton = new AbstractButton();
        this.forwardButton.setIcon(new PolygonIcon(
                new Polygon[]{new Polygon(new int[]{2, 3, 3, 2}, new int[]{2, 2, 9, 9}, 4), new Polygon(new int[]{6, 9, 9, 6}, new int[]{2, 5, 6, 9}, 4)},
                initSize));
        this.forwardButton.setPreferredSize(preferredSize);
        this.forwardButton.setMinimumSize(preferredSize);
        this.forwardButton.addActionListener(this);
        GridBagConstraints forwardConstraints = new GridBagConstraints();
        forwardConstraints.gridx = 3;
        gridBagLayout.setConstraints(this.forwardButton, forwardConstraints);
        add(this.forwardButton);
    }

    public synchronized void setPlayer(Player player) {
        if (this.player != null) {
            this.player.removeChangeListener(this);
        }
        if (player != null) {
            this.player = player;
            this.boundedRangeModel = this.player.getBoundedRangeModel();
            this.slider.setModel(this.boundedRangeModel);
            this.startButton.setSelected(this.player.isActive());
            this.player.addChangeListener(this);
        } else {
            this.player = null;
            this.boundedRangeModel = null;
            this.slider.setModel(null);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        Point startLocation = this.startButton.getLocation();
        this.startButton.setLocation(startLocation.x - 1, startLocation.y);
        Point rewindLocation = this.rewindButton.getLocation();
        this.rewindButton.setLocation(rewindLocation.x + 1, rewindLocation.y);
        Rectangle bounds = this.slider.getBounds();
        this.slider.setBounds(bounds.x - 2, bounds.y, bounds.width + 4, bounds.height);
    }

    public void setProgressModel(BoundedRangeModel progressModel) {
        this.slider.setProgressModel(progressModel);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (this.boundedRangeModel != null) {
            int value = this.boundedRangeModel.getValue();
            Object source = actionEvent.getSource();
            if (source == this.forwardButton) {
                this.boundedRangeModel.setValue(value == this.boundedRangeModel.getMaximum() ? this.boundedRangeModel.getMinimum() : value + 1);
            } else if (source == this.rewindButton) {
                this.boundedRangeModel.setValue(value == this.boundedRangeModel.getMinimum() ? this.boundedRangeModel.getMaximum() : value - 1);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        if (this.player == null || this.startButton.isSelected() == this.player.isActive()) {
            return;
        }
        if (this.startButton.isSelected()) {
            this.player.start();
            while (this.player.isInactive()) { // 解决频繁点击启动停止按钮会卡死的问题
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                }
            }
        } else {
            this.player.stop();
        }
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        this.startButton.setSelected(this.player.isActive());
    }
}
