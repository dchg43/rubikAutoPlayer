package ch.randelshofer.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Comparator;
import java.util.Vector;

import ch.randelshofer.geom3d.DefaultTransform3DModel;
import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Point3D;
import ch.randelshofer.geom3d.SceneNode;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.geom3d.Transform3DModel;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;

/** 控制魔方3D展示 */
public class Canvas3DAWT extends Canvas implements ChangeListener, MouseListener, MouseMotionListener {
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

    // 用户控制魔方旋转时刷新频率
    private int unpaintedStates = 0;

    protected double scaleFactor = 1.0d;

    protected Vector<FaceElement> activeFaces = new Vector<>();

    private boolean isRotateOnMouseDrag = false;

    protected Insets paintInsets = new Insets(0, 0, 0, 0);

    public Canvas3DAWT() {
        addMouseListener(this);
        setBackground(Color.white);
        setRotateOnMouseDrag(true);
        setTransformModel(new DefaultTransform3DModel());
    }

    public void setTransformModel(Transform3DModel newModel) {
        Transform3DModel oldModel = this.transformModel;
        if (oldModel != null) {
            oldModel.removeChangeListener(this);
        }
        this.transformModel = newModel;
        newModel.addChangeListener(this);
        stateChanged(null);
        firePropertyChange("transformModel", oldModel, newModel);
    }

    public Transform3DModel getTransformModel() {
        return this.transformModel;
    }

    public void setRotateOnMouseDrag(boolean isRotateOnMouseDrag) {
        if (isRotateOnMouseDrag != this.isRotateOnMouseDrag) {
            this.isRotateOnMouseDrag = isRotateOnMouseDrag;
            if (isRotateOnMouseDrag) {
                addMouseMotionListener(this);
            } else {
                removeMouseMotionListener(this);
            }
        }
    }

    public void setPaintInsets(int top, int left, int bottom, int right) {
        if (this.paintInsets == null) {
            this.paintInsets = new Insets(top, left, bottom, right);
            return;
        }
        this.paintInsets.top = top;
        this.paintInsets.left = left;
        this.paintInsets.bottom = bottom;
        this.paintInsets.right = right;
    }

    public void setSyncObject(Object obj) {
        this.lock = obj;
    }

    @Override
    public void update(Graphics graphics) {
        paint(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        Dimension size = getSize();
        if (this.backGfx == null || this.backSize.width != size.width || this.backSize.height != size.height) {
            if (size.width <= 0 || size.height <= 0) {
                return;
            }
            createBackGraphics(size);
            this.backSize = size;
            this.unpaintedStates = 1;
        }
        synchronized (this.lock) {
            if (this.unpaintedStates > 0) {
                this.unpaintedStates = 0;
                paintBackground(this.backGfx);
                paint3D(this.backGfx);
            }
        }
        graphics.drawImage(this.backImg, 0, 0, this);
    }

    protected void createBackGraphics(Dimension size) {
        this.backImg = createImage(size.width, size.height);
        this.backGfx = this.backImg.getGraphics();
    }

    public void setToIdentity() {
        this.transformModel.setToIdentity();
    }

    public void setObserver(double z) {
        this.observer = new Point3D(0.0d, 0.0d, z);
    }

    public void setAmbientLightIntensity(double ambientLightIntensity) {
        this.ambientLightIntensity = ambientLightIntensity;
    }

    public void setLightSourceIntensity(double lightSourceIntensity) {
        this.lightSourceIntensity = lightSourceIntensity;
    }

    public void setLightSource(Point3D lightSource) {
        this.lightSource = lightSource;
    }

    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
        MediaTracker mediaTracker = new MediaTracker(this);
        mediaTracker.addImage(image, 0);
        mediaTracker.checkID(0, true);
    }

    public void setTransform(Transform3D transform3D) {
        this.transformModel.setTransform(transform3D);
    }

    public Transform3D getTransform() {
        return this.transformModel.getTransform();
    }

    @Override
    public boolean imageUpdate(Image image, int infoflags, int x, int y, int w, int h) {
        this.unpaintedStates++;
        if ((infoflags & 64) != 0 && image == this.backgroundImage) {
            this.backgroundImage = null;
        }
        return super.imageUpdate(image, infoflags, x, y, w, h);
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        stateChanged(null);
    }

    public double getScaleFactor() {
        return this.scaleFactor;
    }

    public void setScene(SceneNode scene) {
        this.scene = scene;
        stateChanged(null);
    }

    private void paintBackground(Graphics graphics) {
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, this.backSize.width, this.backSize.height);
        if (this.backgroundImage != null) {
            // 填充方式：保持图片比例，且居中
            int imgWidth = this.backgroundImage.getWidth(this);
            int imgHeight = this.backgroundImage.getHeight(this);
            int widthScale = imgWidth * this.backSize.height;
            int heightScale = imgHeight * this.backSize.width;
            if (widthScale > heightScale) {
                int showWidth = widthScale / imgHeight;
                graphics.drawImage(this.backgroundImage, -(showWidth - this.backSize.width) / 2, 0, showWidth, this.backSize.height, this);
            } else {
                int showHeight = heightScale / imgWidth;
                graphics.drawImage(this.backgroundImage, 0, -(showHeight - this.backSize.height) / 2, this.backSize.width, showHeight, this);
            }
        }
    }

    // 绘制魔方(略粗略)
    protected void paint3D(Graphics graphics) {
        if (this.scene == null) {
            return;
        }
        Transform3D transform = this.transformModel.getTransform();
        Dimension size = getSize();
        int width = size.width / 2;
        int height = size.height / 2;
        double scale = this.scaleFactor * Math.min(width, height);
        Vector<Face3D> visibleFaces = new Vector<>();
        this.activeFaces.removeAllElements();
        this.scene.addVisibleFaces(visibleFaces, transform, this.observer);
        visibleFaces.sort(Face3DComparator.getInstance());
        int[] xpoints = new int[5];
        int[] ypoints = new int[5];
        double x = this.observer.x;
        double y = this.observer.y;
        double z = this.observer.z;
        for (Face3D face3D : visibleFaces) {
            double[] coords = face3D.getCoords();
            int[] vertices = face3D.getVertices();
            if (xpoints.length < vertices.length + 1) {
                xpoints = new int[vertices.length + 1];
                ypoints = new int[vertices.length + 1];
            }
            for (int i = 0; i < vertices.length; i++) {
                double d = coords[(vertices[i] * 3) + 2] - z;
                if (d != 0.0d) {
                    int j = vertices[i] * 3;
                    xpoints[i] = width + (int) ((x - ((z * coords[j] - x) / d)) * scale);
                    ypoints[i] = height - (int) ((y - ((z * coords[j + 1] - y) / d)) * scale);
                } else {
                    xpoints[i] = width + (int) (x * scale);
                    ypoints[i] = height - (int) (y * scale);
                }
            }
            Color color = face3D.getFillColor();
            if (color != null) {
                double brightness = face3D.getBrightness(this.lightSource, this.lightSourceIntensity, this.ambientLightIntensity);
                if (brightness < 1.0d) {
                    color = new Color((int) (brightness * color.getRed()), (int) (brightness * color.getGreen()), (int) (brightness * color.getBlue()));
                }
                graphics.setColor(color);
                graphics.fillPolygon(xpoints, ypoints, vertices.length);
            }
            Color borderColor = face3D.getBorderColor();
            if (borderColor != null) {
                graphics.setColor(borderColor);
                xpoints[vertices.length] = xpoints[0];
                ypoints[vertices.length] = ypoints[0];
                graphics.drawPolygon(xpoints, ypoints, vertices.length + 1);
            }
            if (face3D.getAction() != null) {
                this.activeFaces.addElement(new FaceElement(new Polygon(xpoints, ypoints, vertices.length), face3D));
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (!isEnabled() || this.isPopupTrigger) {
            return;
        }
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        this.prevx = x;
        this.prevy = y;
        for (int size = this.activeFaces.size() - 1; size >= 0; size--) {
            FaceElement face = this.activeFaces.elementAt(size);
            if (face.getShape().contains(x, y)) {
                face.getFace3D().handleEvent(mouseEvent);
                return;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        this.isArmed = true;
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        this.isArmed = false;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        this.isPopupTrigger = mouseEvent.isPopupTrigger();
        if (!isEnabled() || this.isPopupTrigger) {
            return;
        }
        this.isAdjusting = true;
        this.prevx = mouseEvent.getX();
        this.prevy = mouseEvent.getY();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (this.isAdjusting) {
            this.isAdjusting = false;
            stateChanged(null);
        }
        this.isPopupTrigger |= mouseEvent.isPopupTrigger();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        this.isPopupTrigger = false;
        if (this.isAdjusting && this.isArmed && isEnabled()) {
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();
            Dimension size = getSize();
            this.transformModel.rotate((this.prevy - y) * (Math.PI * 2 / size.width), (this.prevx - x) * (Math.PI * 2 / size.height), 0.0d);
            this.prevx = x;
            this.prevy = y;
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        this.isPopupTrigger = false;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        this.unpaintedStates++;
        // 用户控制魔方旋转时刷新频率
        if (this.unpaintedStates == 1) {
            repaint();
        } else if (this.unpaintedStates > 10) {
            this.unpaintedStates = 1;
            repaint();
        }
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        this.preferredSize = preferredSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize != null ? this.preferredSize : super.getPreferredSize();
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (propertyChangeListener == null) {
            return;
        }
        if (this.changeSupport == null) {
            this.changeSupport = new PropertyChangeSupport(this);
        }
        this.changeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (propertyChangeListener == null || this.changeSupport == null) {
            return;
        }
        this.changeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeSupport propertyChangeSupport = this.changeSupport;
        if (propertyChangeSupport == null) {
            return;
        }
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public static final class FaceElement {
        private Shape shape;

        private Face3D face3D;

        public FaceElement(Shape shape, Face3D face3D) {
            this.shape = shape;
            this.face3D = face3D;
        }

        public Shape getShape() {
            return shape;
        }

        public Face3D getFace3D() {
            return face3D;
        }
    }

    public static final class Face3DComparator implements Comparator<Face3D> {
        private static final Face3DComparator instance = new Face3DComparator();

        private Face3DComparator() {
        }

        @Override
        public int compare(Face3D a, Face3D b) {
            return a.compareTo(b);
        }

        public static Face3DComparator getInstance() {
            return instance;
        }
    }

}
