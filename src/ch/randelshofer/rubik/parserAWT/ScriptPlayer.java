package ch.randelshofer.rubik.parserAWT;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageProducer;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.gui.AbstractButton;
import ch.randelshofer.gui.BoundedRangeModel;
import ch.randelshofer.gui.Canvas3DAWT;
import ch.randelshofer.gui.Canvas3DJ2D;
import ch.randelshofer.gui.DefaultBoundedRangeModel;
import ch.randelshofer.gui.MovieControlAWT;
import ch.randelshofer.gui.PolygonIcon;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;
import ch.randelshofer.gui.tree.DefaultMutableTreeNode;
import ch.randelshofer.media.Player;
import ch.randelshofer.rubik.AbstractCube3DAWT;
import ch.randelshofer.rubik.MiniCube3DAWT;
import ch.randelshofer.rubik.RubiksCubeCore;
import ch.randelshofer.util.ConcurrentDispatcherAWT;


public class ScriptPlayer implements Player, Runnable, ChangeListener, ActionListener
{
    private Panel controlPanel;

    private MovieControlAWT controls;

    private ScriptNode script;

    private volatile int scriptIndex;

    private ChangeEvent changeEvent;

    private static final int STOPPED = 0;

    private static final int STARTING = 1;

    private static final int RUNNING = 2;

    private static final int STOPPING = 3;

    private static final int scaling = 1;

    private AbstractButton resetButton;

    private volatile boolean isProcessingCurrentSymbol;

    private static ConcurrentDispatcherAWT threadPool = new ConcurrentDispatcherAWT();

    private DefaultBoundedRangeModel progress = new DefaultBoundedRangeModel(0, 0, 0, 0);

    private EventListenerList listenerList = new EventListenerList();

    private Vector<ScriptNode> scriptVector = new Vector<>();

    private int state = STOPPED;

    private Transform3D transform = new Transform3D();

    private RubiksCubeCore model = new RubiksCubeCore();

    private AbstractCube3DAWT cube3D = new MiniCube3DAWT();

    private Canvas3DAWT canvas = Canvas3DJ2D.createCanvas3D();

    public ScriptPlayer()
    {
        this.cube3D.setAnimated(true);
        this.cube3D.setModel(this.model);
        this.cube3D.addChangeListener(this.canvas);
        this.canvas.setScene(this.cube3D.getScene());
        this.canvas.setBackground(Color.white);
        this.canvas.setSyncObject(this.model);
        this.canvas.setScaleFactor(0.02d);
        this.canvas.setToIdentity();
        this.progress.addChangeListener(this);
        this.controlPanel = new Panel();
        this.controlPanel.setLayout(new BorderLayout());
        this.controls = new MovieControlAWT();
        this.controls.setVisible(false);
        this.controls.setPlayer(this);
        this.controlPanel.add("Center", this.controls);
        this.resetButton = new AbstractButton();
        this.resetButton.setIcon(new PolygonIcon( // 设置重置按钮大小
            new Polygon[] {
                new Polygon(new int[] {2 * scaling, 3 * scaling, 3 * scaling, 2 * scaling},
                    new int[] {2 * scaling, 2 * scaling, 10 * scaling, 10 * scaling}, 4),
                new Polygon(new int[] {9 * scaling, 9 * scaling, 4 * scaling},
                    new int[] {2 * scaling, 10 * scaling, 6 * scaling}, 3)},
            new Dimension(12 * scaling, 12 * scaling)));
        this.resetButton.setPreferredSize(new Dimension(15 * scaling, 15 * scaling));
        this.resetButton.addActionListener(this);
        this.controlPanel.add("West", this.resetButton);
    }

    public RubiksCubeCore getCubeModel()
    {
        return this.model;
    }

    public void setTransform(Transform3D transform3D)
    {
        this.transform = transform3D;
        this.canvas.setTransform(transform3D);
    }

    public void setCubeModel(RubiksCubeCore rubiksCubeCore)
    {
        this.model = rubiksCubeCore;
        this.canvas.setSyncObject(rubiksCubeCore);
        this.cube3D.setModel(rubiksCubeCore);
    }

    public AbstractCube3DAWT getCube3D()
    {
        return this.cube3D;
    }

    public void setCube3D(AbstractCube3DAWT abstractCube3DAWT)
    {
        if (this.cube3D != null)
        {
            this.cube3D.removeChangeListener(this.canvas);
            this.cube3D.setModel(new RubiksCubeCore());
        }
        this.cube3D = abstractCube3DAWT;
        if (this.cube3D != null)
        {
            this.cube3D.setAnimated(true);
            this.cube3D.addChangeListener(this.canvas);
            this.cube3D.setModel(getCubeModel());
            this.canvas.setScene(this.cube3D.getScene());
        }
    }

    public synchronized ScriptNode getScript()
    {
        return this.script;
    }

    public synchronized void setScript(ScriptNode scriptNode)
    {
        stop();
        this.script = scriptNode;
        this.scriptVector.removeAllElements();
        this.scriptIndex = 0;
        this.controls.setVisible(scriptNode != null);
        if (scriptNode == null)
        {
            this.progress.setRangeProperties(0, 0, 0, 0, false);
        }
        else
        {
            Enumeration<DefaultMutableTreeNode> enumerationResolvedEnumeration = scriptNode.resolvedEnumeration(false);
            while (enumerationResolvedEnumeration.hasMoreElements())
            {
                ScriptNode scriptNode2 = (ScriptNode)enumerationResolvedEnumeration.nextElement();
                if (((scriptNode2 instanceof TwistNode) && ((TwistNode)scriptNode2).getSymbol() != 84)
                    || (scriptNode2 instanceof PermutationNode))
                {
                    this.scriptVector.addElement(scriptNode2);
                }
            }
            this.progress.setRangeProperties(0, 0, 0, this.scriptVector.size(), false);
        }
        updateEnabled();
    }

    public void moveToCaret(int i)
    {
        for (int i2 = 0; i2 < this.scriptVector.size(); i2++)
        {
            ScriptNode scriptNode = this.scriptVector.elementAt(i2);
            if (scriptNode.getStartPosition() <= i && scriptNode.getEndPosition() >= i)
            {
                this.progress.setValue(i2);
                return;
            }
        }
    }

    private void updateEnabled()
    {
        if (this.script != null)
        {
            this.controls.setEnabled(true);
        }
        else
        {
            this.controls.setEnabled(false);
        }
    }

    @Override
    public ImageProducer getImageProducer()
    {
        return null;
    }

    @Override
    public BoundedRangeModel getBoundedRangeModel()
    {
        return this.progress;
    }

    @Override
    public void start()
    {
        boolean z = false;
        synchronized (this)
        {
            if (this.state == STOPPED)
            {
                this.state = STARTING;
                z = true;
            }
        }
        if (z)
        {
            this.cube3D.getDispatcher().dispatch(this, threadPool);
            fireStateChanged();
        }
    }

    // 循环执行自动脚本
    @Override
    public void run()
    {
        synchronized (this)
        {
            if (this.state != STARTING)
            {
                if (this.state != STOPPED)
                {
                    this.state = STOPPED;
                }
                return;
            }

            this.state = RUNNING;
        }
        fireStateChanged();
        if (this.progress.getMaximum() > 0)
        {
            if (this.progress.getValue() == this.progress.getMaximum())
            {
                this.progress.setValue(0);
                this.model.setQuiet(true);
                while (this.scriptIndex > 0)
                {
                    ScriptNode scriptNode = this.scriptVector.elementAt(--this.scriptIndex);
                    scriptNode.applyInverseTo(this.model);
                }
                this.model.setQuiet(false);
            }
            while ((this.state == RUNNING && this.progress.getValue() != this.progress.getMaximum())
                   || this.scriptIndex != this.progress.getValue())
            {
                this.isProcessingCurrentSymbol = true;
                fireStateChanged();
                int iMin = Math.min(this.progress.getValue() + 1, this.progress.getMaximum());
                if (this.scriptIndex == iMin - 1)
                {
                    ScriptNode scriptNode = this.scriptVector.elementAt(this.scriptIndex++);
                    scriptNode.applyTo(this.model);
                }
                else if (this.scriptIndex == iMin + 1)
                {
                    ScriptNode scriptNode = this.scriptVector.elementAt(--this.scriptIndex);
                    scriptNode.applyInverseTo(this.model);
                }
                else
                {
                    this.model.setQuiet(true);
                    while (this.scriptIndex < iMin)
                    {
                        ScriptNode scriptNode = this.scriptVector.elementAt(this.scriptIndex++);
                        scriptNode.applyTo(this.model);
                    }
                    while (this.scriptIndex > iMin)
                    {
                        ScriptNode scriptNode = this.scriptVector.elementAt(--this.scriptIndex);
                        scriptNode.applyInverseTo(this.model);
                    }
                    this.model.setQuiet(false);
                }
                this.isProcessingCurrentSymbol = false;
                this.progress.setValue(this.progress.getValue() + 1);
                fireStateChanged();
            }
        }
        synchronized (this)
        {
            this.state = STOPPED;
            notifyAll();
        }
        fireStateChanged();
    }

    @Override
    public void stop()
    {
        synchronized (this)
        {
            if (this.state == RUNNING || this.state == STARTING)
            {
                this.state = STOPPING;
            }
            else
            {
                this.state = STOPPED;
            }
        }
        while (this.state != STOPPED)
        {
            try
            {
                Thread.sleep(10L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        this.cube3D.getDispatcher().reassign();
        update();
    }

    public void reset()
    {
        stop();
        this.scriptIndex = 0;
        this.progress.setValue(0);
        this.model.reset();
        this.canvas.setTransform(this.transform);
        fireStateChanged();
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener)
    {
        this.listenerList.remove(ChangeListener.class, changeListener);
    }

    @Override
    public boolean isActive()
    {
        return this.state != STOPPED;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener)
    {
        this.listenerList.add(ChangeListener.class, changeListener);
    }

    @Override
    public Component getVisualComponent()
    {
        return this.canvas;
    }

    @Override
    public Component getControlPanelComponent()
    {
        return this.controlPanel;
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

    public ScriptNode getCurrentSymbol()
    {
        int i = this.scriptIndex;
        if (i < this.scriptVector.size())
        {
            return this.scriptVector.elementAt(i);
        }
        return null;
    }

    public void setResetButtonVisible(boolean z)
    {
        this.resetButton.setVisible(z);
    }

    public boolean isProcessingCurrentSymbol()
    {
        return this.isProcessingCurrentSymbol;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        if (changeEvent.getSource() == this.progress && !isActive())
        {
            update();
        }
    }

    private void update()
    {
        Runnable runnable = new Runnable()
        {
            private final ScriptPlayer playerInstance = ScriptPlayer.this;

            @Override
            public void run()
            {
                int value = this.playerInstance.progress.getValue();
                this.playerInstance.isProcessingCurrentSymbol = true;
                if (this.playerInstance.scriptIndex == value - 1)
                {
                    this.playerInstance.fireStateChanged();
                    this.playerInstance.scriptVector.elementAt(this.playerInstance.scriptIndex++).applyTo(
                        this.playerInstance.model);
                }
                else if (this.playerInstance.scriptIndex == value + 1)
                {
                    ScriptNode scriptNode = this.playerInstance.scriptVector.elementAt(
                        --this.playerInstance.scriptIndex);
                    this.playerInstance.fireStateChanged();
                    scriptNode.applyInverseTo(this.playerInstance.model);
                }
                else
                {
                    this.playerInstance.model.setQuiet(true);
                    while (this.playerInstance.scriptIndex < value)
                    {
                        this.playerInstance.scriptVector.elementAt(this.playerInstance.scriptIndex++).applyTo(
                            this.playerInstance.model);
                    }
                    while (this.playerInstance.scriptIndex > value)
                    {
                        this.playerInstance.scriptVector.elementAt(--this.playerInstance.scriptIndex).applyInverseTo(
                            this.playerInstance.model);
                    }
                    this.playerInstance.model.setQuiet(false);
                }
                this.playerInstance.isProcessingCurrentSymbol = false;
                this.playerInstance.fireStateChanged();
            }
        };
        if (this.cube3D.isAnimated())
        {
            this.cube3D.getDispatcher().dispatch(runnable);
        }
        else
        {
            runnable.run();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getSource() == this.resetButton)
        {
            reset();
        }
    }

}
