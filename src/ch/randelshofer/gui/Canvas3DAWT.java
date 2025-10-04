package ch.randelshofer.gui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Vector;

import ch.randelshofer.geom3d.DefaultTransform3DModel;
import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Point3D;
import ch.randelshofer.geom3d.SceneNode;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.geom3d.Transform3DModel;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.util.Arrays;


public class Canvas3DAWT extends Canvas implements ChangeListener, MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = -8917036824539916552L;

    protected SceneNode scene;

    protected Graphics backGfx;

    protected Image backImg;

    protected Transform3DModel transformModel;

    protected int prevx;

    protected int prevy;

    private Image backgroundImage;

    protected boolean isAdjusting;

    protected boolean isPopupTrigger;

    private PropertyChangeSupport changeSupport;

    private Dimension preferredSize = new Dimension(200, 200);

    private Dimension backSize = new Dimension(0, 0);

    private Object lock = new Object();

    protected Point3D observer = new Point3D(0.0d, 0.0d, 260.0d);

    protected Point3D lightSource = new Point3D(-500.0d, 500.0d, 1000.0d);

    protected double ambientLightIntensity = 0.6d;

    protected double lightSourceIntensity = 1.0d;

    private boolean isArmed = true;

    private int unpaintedStates = 1;

    protected double scaleFactor = 1.0d;

    protected Vector<Object> activeFaces = new Vector<>();

    private boolean isRotateOnMouseDrag = false;

    protected Insets paintInsets = new Insets(0, 0, 0, 0);

    public Canvas3DAWT()
    {
        addMouseListener(this);
        setBackground(Color.white);
        setRotateOnMouseDrag(true);
        setTransformModel(new DefaultTransform3DModel());
    }

    public void setTransformModel(Transform3DModel transform3DModel)
    {
        Transform3DModel transform3DModel2 = this.transformModel;
        if (transform3DModel2 != null)
        {
            transform3DModel2.removeChangeListener(this);
        }
        this.transformModel = transform3DModel;
        transform3DModel.addChangeListener(this);
        stateChanged(null);
        firePropertyChange("transformModel", transform3DModel2, transform3DModel);
    }

    public Transform3DModel getTransformModel()
    {
        return this.transformModel;
    }

    public void setRotateOnMouseDrag(boolean z)
    {
        if (z != this.isRotateOnMouseDrag)
        {
            this.isRotateOnMouseDrag = z;
            if (z)
            {
                addMouseMotionListener(this);
            }
            else
            {
                removeMouseMotionListener(this);
            }
        }
    }

    public void setPaintInsets(int i, int i2, int i3, int i4)
    {
        if (this.paintInsets == null)
        {
            this.paintInsets = new Insets(i, i2, i3, i4);
            return;
        }
        this.paintInsets.top = i;
        this.paintInsets.left = i2;
        this.paintInsets.bottom = i3;
        this.paintInsets.right = i4;
    }

    public void setSyncObject(Object obj)
    {
        this.lock = obj;
    }

    @Override
    public void update(Graphics graphics)
    {
        paint(graphics);
    }

    @Override
    public void paint(Graphics graphics)
    {
        Dimension size = getSize();
        if (this.backGfx == null || this.backSize.width != size.width || this.backSize.height != size.height)
        {
            if (size.width <= 0 || size.height <= 0)
            {
                return;
            }
            createBackGraphics(size);
            this.backSize = size;
            this.unpaintedStates = 1;
        }
        synchronized (this.lock)
        {
            if (this.unpaintedStates > 0)
            {
                this.unpaintedStates = 0;
                paintBackground(this.backGfx);
                paint3D(this.backGfx);
            }
        }
        graphics.drawImage(this.backImg, 0, 0, this);
    }

    protected void createBackGraphics(Dimension dimension)
    {
        this.backImg = createImage(dimension.width, dimension.height);
        this.backGfx = this.backImg.getGraphics();
    }

    public void setToIdentity()
    {
        this.transformModel.setToIdentity();
    }

    public void setObserver(double f)
    {
        this.observer = new Point3D(0.0d, 0.0d, f);
    }

    public void setAmbientLightIntensity(double d)
    {
        this.ambientLightIntensity = d;
    }

    public void setLightSourceIntensity(double d)
    {
        this.lightSourceIntensity = d;
    }

    public void setLightSource(Point3D point3D)
    {
        this.lightSource = point3D;
    }

    public void setBackgroundImage(Image image)
    {
        this.backgroundImage = image;
        MediaTracker mediaTracker = new MediaTracker(this);
        mediaTracker.addImage(image, 0);
        mediaTracker.checkID(0, true);
    }

    public void setTransform(Transform3D transform3D)
    {
        this.transformModel.setTransform(transform3D);
    }

    public Transform3D getTransform()
    {
        return this.transformModel.getTransform();
    }

    @Override
    public boolean imageUpdate(Image image, int i, int i2, int i3, int i4, int i5)
    {
        this.unpaintedStates++;
        if ((i & 64) != 0 && image == this.backgroundImage)
        {
            this.backgroundImage = null;
        }
        return super.imageUpdate(image, i, i2, i3, i4, i5);
    }

    public void setScaleFactor(double d)
    {
        this.scaleFactor = d;
        stateChanged(null);
    }

    public double getScaleFactor()
    {
        return this.scaleFactor;
    }

    public void setScene(SceneNode sceneNode)
    {
        this.scene = sceneNode;
        stateChanged(null);
    }

    private void paintBackground(Graphics graphics)
    {
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, this.backSize.width, this.backSize.height);
        if (this.backgroundImage != null)
        {
            graphics.drawImage(this.backgroundImage, 0, 0, this.backSize.width, this.backSize.height, this);
        }
    }

    protected void paint3D(Graphics graphics)
    {
        if (this.scene == null)
        {
            return;
        }
        Transform3D transform = this.transformModel.getTransform();
        Dimension size = getSize();
        int i = size.width / 2;
        int i2 = size.height / 2;
        double dMin = Math.min(i, i2) * this.scaleFactor;
        double d = -dMin;
        Vector<Face3D> vector = new Vector<>();
        this.activeFaces.removeAllElements();
        this.scene.addVisibleFaces(vector, transform, this.observer);
        Face3D[] face3DArr = new Face3D[vector.size()];
        vector.copyInto(face3DArr);
        Arrays.sort(face3DArr);
        int[] iArr = new int[5];
        int[] iArr2 = new int[5];
        double d2 = this.observer.x;
        double d3 = this.observer.y;
        double d4 = this.observer.z;
        for (Face3D face3D : face3DArr)
        {
            double[] coords = face3D.getCoords();
            int[] vertices = face3D.getVertices();
            if (iArr.length < vertices.length + 1)
            {
                iArr = new int[vertices.length + 1];
                iArr2 = new int[vertices.length + 1];
            }
            for (int i3 = 0; i3 < vertices.length; i3++)
            {
                int i4 = vertices[i3] * 3;
                double d5 = coords[(vertices[i3] * 3) + 2] - d4;
                if (d5 != 0.0d)
                {
                    iArr[i3] = i + ((int)((d2 - (((d4 * coords[i4]) - d2) / d5)) * dMin));
                    iArr2[i3] = i2 + ((int)((d3 - (((d4 * coords[i4 + 1]) - d3) / d5)) * d));
                }
                else
                {
                    iArr[i3] = i + ((int)(d2 * dMin));
                    iArr2[i3] = i2 + ((int)(d3 * d));
                }
            }
            Color color = face3D.getFillColor();
            if (color != null)
            {
                double brightness = this.lightSource == null ? 1.0d : face3D.getBrightness(this.lightSource,
                    this.lightSourceIntensity, this.ambientLightIntensity);
                graphics.setColor(new Color(Math.min(255, (int)(color.getRed() * brightness)),
                    Math.min(255, (int)(color.getGreen() * brightness)),
                    Math.min(255, (int)(color.getBlue() * brightness))));
                graphics.fillPolygon(iArr, iArr2, vertices.length);
            }
            Color borderColor = face3D.getBorderColor();
            if (borderColor != null)
            {
                graphics.setColor(borderColor);
                iArr[vertices.length] = iArr[0];
                iArr2[vertices.length] = iArr2[0];
                graphics.drawPolygon(iArr, iArr2, vertices.length + 1);
            }
            if (face3D.getAction() != null)
            {
                this.activeFaces.addElement(new Polygon(iArr, iArr2, vertices.length));
                this.activeFaces.addElement(face3D);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (!isEnabled() || this.isPopupTrigger)
        {
            return;
        }
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        this.prevx = x;
        this.prevy = y;
        for (int size = this.activeFaces.size() - 2; size >= 0; size -= 2)
        {
            Polygon polygon = (Polygon)this.activeFaces.elementAt(size);
            Face3D face3D = (Face3D)this.activeFaces.elementAt(size + 1);
            if (polygon.contains(x, y))
            {
                face3D.handleEvent(mouseEvent);
                return;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent)
    {
        this.isArmed = true;
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent)
    {
        this.isArmed = false;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent)
    {
        this.isPopupTrigger = mouseEvent.isPopupTrigger();
        if (!isEnabled() || this.isPopupTrigger)
        {
            return;
        }
        this.isAdjusting = true;
        this.prevx = mouseEvent.getX();
        this.prevy = mouseEvent.getY();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (this.isAdjusting)
        {
            this.isAdjusting = false;
            stateChanged(null);
        }
        this.isPopupTrigger |= mouseEvent.isPopupTrigger();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent)
    {
        this.isPopupTrigger = false;
        if (this.isAdjusting && this.isArmed && isEnabled())
        {
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();
            Dimension size = getSize();
            this.transformModel.rotate((this.prevy - y) * (Math.PI * 2 / size.width),
                (this.prevx - x) * (Math.PI * 2 / size.height), 0.0d);
            this.prevx = x;
            this.prevy = y;
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent)
    {
        this.isPopupTrigger = false;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        this.unpaintedStates++;
        if (this.unpaintedStates == 1 || this.unpaintedStates > 10)
        {
            repaint();
        }
    }

    @Override
    public void setPreferredSize(Dimension dimension)
    {
        this.preferredSize = dimension;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return this.preferredSize != null ? this.preferredSize : super.getPreferredSize();
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener propertyChangeListener)
    {
        if (propertyChangeListener == null)
        {
            return;
        }
        if (this.changeSupport == null)
        {
            this.changeSupport = new PropertyChangeSupport(this);
        }
        this.changeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener propertyChangeListener)
    {
        if (propertyChangeListener == null || this.changeSupport == null)
        {
            return;
        }
        this.changeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    @Override
    protected void firePropertyChange(String str, Object obj, Object obj2)
    {
        PropertyChangeSupport propertyChangeSupport = this.changeSupport;
        if (propertyChangeSupport == null)
        {
            return;
        }
        propertyChangeSupport.firePropertyChange(str, obj, obj2);
    }
}
