package ch.randelshofer.rubik;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import ch.randelshofer.geom3d.SceneNode;
import ch.randelshofer.geom3d.Shape3D;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.geom3d.TransformNode;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.gui.event.EventListenerList;
import ch.randelshofer.gui.event.EventListenerList.ListenerNode;
import ch.randelshofer.util.PooledSequentialDispatcherAWT;

public abstract class AbstractCube3DAWT implements RubikListener {
    protected Shape3D centerShape;

    private TransformNode sceneTransform;

    private TransformNode centerTransform;

    private ChangeEvent changeEvent;

    private boolean isAnimated;

    private PooledSequentialDispatcherAWT dispatcher;

    // private boolean isLazy;

    public static final int TWIST_MODE = 0;

    public static final int PARTS_MODE = 1;

    public static final Color PART_FILL_COLOR = new Color(20, 20, 20);

    public static final Color CENTER_FILL_COLOR = Color.white;

    public static final Color PART_BORDER_COLOR = Color.black;

    public static final double[][] CORNER_EXPLODE_TRANSLATION = {{-1.0d, 1.0d, 1.0d}, {-1.0d, -1.0d, 1.0d}, {1.0d, 1.0d, 1.0d}, {1.0d, -1.0d, 1.0d},
            {1.0d, 1.0d, -1.0d}, {1.0d, -1.0d, -1.0d}, {-1.0d, 1.0d, -1.0d}, {-1.0d, -1.0d, -1.0d}};

    public static final double[][] EDGE_EXPLODE_TRANSLATION = {{0.0d, 1.0d, 1.0d}, {-1.0d, 0.0d, 1.0d}, {0.0d, -1.0d, 1.0d}, {1.0d, 1.0d, 0.0d},
            {1.0d, 0.0d, 1.0d}, {1.0d, -1.0d, 0.0d}, {0.0d, 1.0d, -1.0d}, {1.0d, 0.0d, -1.0d}, {0.0d, -1.0d, -1.0d}, {-1.0d, 1.0d, 0.0d}, {-1.0d, 0.0d, -1.0d},
            {-1.0d, -1.0d, 0.0d}};

    public static final double[][] SIDE_EXPLODE_TRANSLATION = {{0.0d, 0.0d, 1.0d}, {1.0d, 0.0d, 0.0d}, {0.0d, -1.0d, 0.0d}, {0.0d, 0.0d, -1.0d},
            {-1.0d, 0.0d, 0.0d}, {0.0d, 1.0d, 0.0d}};

    protected Shape3D[] cornerShapes = new Shape3D[8];

    private Transform3D[] cornerIdentityTransforms = new Transform3D[8];

    protected Shape3D[] edgeShapes = new Shape3D[12];

    private Transform3D[] edgeIdentityTransforms = new Transform3D[12];

    protected Shape3D[] sideShapes = new Shape3D[6];

    private Transform3D[] sideIdentityTransforms = new Transform3D[6];

    protected TransformNode[] cornerTransforms = new TransformNode[8];

    protected TransformNode[] edgeTransforms = new TransformNode[12];

    protected TransformNode[] sideTransforms = new TransformNode[6];

    private RubiksCubeCore model = new RubiksCubeCore();

    private EventListenerList listenerList = new EventListenerList();

    private double explosion = 0.0d;

    private boolean editMode = false;

    private Color selectColor;

    /**
     * 点击四角方块的旋转，顺时针
     */
    class CornerAction implements ActionListener {
        private int corner;

        private int orientation;

        private final AbstractCube3DAWT awtInstance;

        public CornerAction(AbstractCube3DAWT awtInstance, int corner, int orientation) {
            this.awtInstance = awtInstance;
            this.corner = corner;
            this.orientation = orientation;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() instanceof MouseEvent && ((MouseEvent) actionEvent.getSource()).getClickCount() <= 1) {
                if (isEditMode()) {
                    // CORNER_MAP[CornerSide][cornerLoc % 4] （详见图片<块的命名>）
                    final int[][] CORNER_MAP = {{0, 6, 2, 8}, {2, 8, 0, 6}, {0, 2, 8, 6}, {0, 6, 2, 8}, {2, 8, 0, 6}, {6, 8, 2, 0}};
                    int cornerSide = this.awtInstance.model.getCornerSide(this.corner, this.orientation);
                    int mapindex = (cornerSide == 2 || cornerSide == 5) ? (this.corner / 2) : (this.corner % 4);
                    int cornerIndex = CORNER_MAP[cornerSide][mapindex];
                    this.awtInstance.setStickerColor(cornerSide, cornerIndex, getSelectColor());
                    fireStateChanged();
                } else {
                    this.awtInstance.getDispatcher().dispatch(new SideEvent(this.awtInstance,
                            this.awtInstance.model.getCornerSide(this.corner, this.orientation), (actionEvent.getModifiers() & 0x9) != 0));
                }
            }
        }
    }

    /**
     * 点击棱上方块的旋转，包括顺时针和逆时针
     */
    class EdgeAction implements ActionListener {
        private int edge;

        private int orientation;

        private final AbstractCube3DAWT awtInstance;

        public EdgeAction(AbstractCube3DAWT awtInstance, int edge, int orientation) {
            this.awtInstance = awtInstance;
            this.edge = edge;
            this.orientation = orientation;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() instanceof MouseEvent && ((MouseEvent) actionEvent.getSource()).getClickCount() <= 1) {
                if (isEditMode()) {
                    // EDGE_MAP[edgeSide][edgeLoc] （详见图片<块的命名>）
                    final int[][] EDGE_MAP = { //
                            {1, 3, 7, 0, 5, 0, 0, 0, 0, 0, 0, 0}, // 0
                            {0, 0, 0, 1, 3, 7, 0, 5, 0, 0, 0, 0}, // 1
                            {0, 0, 1, 0, 0, 5, 0, 0, 7, 0, 0, 3}, // 2
                            {0, 0, 0, 0, 0, 0, 1, 3, 7, 0, 5, 0}, // 3
                            {0, 5, 0, 0, 0, 0, 0, 0, 0, 1, 3, 7}, // 4
                            {7, 0, 0, 5, 0, 0, 1, 0, 0, 3, 0, 0}}; // 5
                    int edgeSide = this.awtInstance.model.getEdgeSide(this.edge, this.orientation ^ 1);
                    int edgeIndex = EDGE_MAP[edgeSide][this.edge];
                    this.awtInstance.setStickerColor(edgeSide, edgeIndex, getSelectColor());
                    fireStateChanged();
                } else {
                    this.awtInstance.getDispatcher().dispatch(new EdgeEvent(this.awtInstance,
                            this.awtInstance.model.getEdgeLayerSide(this.edge, this.orientation), (actionEvent.getModifiers() & 0x9) != 0));
                }
            }
        }
    }

    /**
     * 点击中心方块的旋转，顺时针（为了逆时针旋转，改了SideEvent的最后一个字段）
     */
    class SideAction implements ActionListener {
        private int side;

        private final AbstractCube3DAWT awtInstance;

        public SideAction(AbstractCube3DAWT awtInstance, int side) {
            this.awtInstance = awtInstance;
            this.side = side;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() instanceof MouseEvent && ((MouseEvent) actionEvent.getSource()).getClickCount() <= 1) {
                if (isEditMode()) {
                    this.awtInstance.setStickerColor(this.side, 4, getSelectColor());
                    fireStateChanged();
                } else {
                    this.awtInstance.getDispatcher().dispatch(
                            new SideEvent(this.awtInstance, this.awtInstance.model.getSideLocation(this.side), (actionEvent.getModifiers() & 0x9) == 0));
                }
            }
        }
    }

    class EdgeEvent implements Runnable {
        private int side;

        private boolean isClockwise;

        private final AbstractCube3DAWT awtInstance;

        /**
         * @param abstractCube3DAWT
         * @param i 所在面
         * @param z 旋转方向
         */
        public EdgeEvent(AbstractCube3DAWT awtInstance, int side, boolean isClockwise) {
            this.awtInstance = awtInstance;
            this.side = side;
            this.isClockwise = isClockwise;
        }

        @Override
        public void run() {
            this.awtInstance.model.twistEdge(this.side, this.isClockwise);
        }
    }

    class SideEvent implements Runnable {
        private int side;

        private boolean isClockwise;

        private final AbstractCube3DAWT awtInstance;

        /**
         * @param abstractCube3DAWT
         * @param i 所在面
         * @param z 旋转方向
         */
        public SideEvent(AbstractCube3DAWT awtInstance, int side, boolean isClockwise) {
            this.awtInstance = awtInstance;
            this.side = side;
            this.isClockwise = isClockwise;
        }

        @Override
        public void run() {
            this.awtInstance.model.twistSide(this.side, this.isClockwise);
        }
    }

    public AbstractCube3DAWT() {
        init();
    }

    private void computeTransformation() {
        synchronized (this.model) {
            for (int i = 0; i < this.sideTransforms.length; i++) {
                int sideLocation = this.model.getSideLocation(i);
                Transform3D transform3D = (Transform3D) this.sideIdentityTransforms[sideLocation].clone();
                transform3D.translate(SIDE_EXPLODE_TRANSLATION[sideLocation][0] * this.explosion, SIDE_EXPLODE_TRANSLATION[sideLocation][1] * this.explosion,
                        SIDE_EXPLODE_TRANSLATION[sideLocation][2] * this.explosion);
                this.sideTransforms[i].setTransform(transform3D);
            }
            for (int i = 0; i < this.edgeTransforms.length; i++) {
                int edgeLocation = this.model.getEdgeLocation(i);
                Transform3D transform = this.edgeTransforms[i].getTransform();
                transform.setToIdentity();
                if (this.model.getEdgeOrientation(i) == 1) {
                    transform.rotateZ(Math.PI);
                    transform.rotateX(Math.PI / 2);
                }
                Transform3D transform3D2 = (Transform3D) this.edgeIdentityTransforms[edgeLocation].clone();
                transform3D2.translate(EDGE_EXPLODE_TRANSLATION[edgeLocation][0] * this.explosion, EDGE_EXPLODE_TRANSLATION[edgeLocation][1] * this.explosion,
                        EDGE_EXPLODE_TRANSLATION[edgeLocation][2] * this.explosion);
                transform.concatenate(transform3D2);
            }
            for (int i = 0; i < this.cornerTransforms.length; i++) {
                int cornerLocation = this.model.getCornerLocation(i);
                Transform3D transform2 = this.cornerTransforms[i].getTransform();
                transform2.setToIdentity();
                switch (this.model.getCornerOrientation(i)) {
                case 1:
                    transform2.rotateZ(-Math.PI / 2);
                    transform2.rotateX(Math.PI / 2);
                    break;
                case 2:
                    transform2.rotate(-Math.PI / 2, 0.0d, Math.PI / 2);
                    break;
                }
                Transform3D transform3D3 = (Transform3D) this.cornerIdentityTransforms[cornerLocation].clone();
                transform3D3.translate(CORNER_EXPLODE_TRANSLATION[cornerLocation][0] * this.explosion,
                        CORNER_EXPLODE_TRANSLATION[cornerLocation][1] * this.explosion, CORNER_EXPLODE_TRANSLATION[cornerLocation][2] * this.explosion);
                transform2.concatenate(transform3D3);
            }
        }
    }

    public SceneNode getScene() {
        return this.sceneTransform;
    }

    public void update() {
        computeTransformation();
        fireStateChanged();
    }

    public void setModel(RubiksCubeCore model) {
        if (this.model != null) {
            this.model.removeRubikListener(this);
        }
        this.model = model;
        if (this.model != null) {
            this.model.addRubikListener(this);
            update();
        }
    }

    public RubiksCubeCore getModel() {
        return this.model;
    }

    public void setCornerVisible(int index, boolean isVisible) {
        this.cornerShapes[index].setVisible(isVisible);
        fireStateChanged();
    }

    public void setEdgeVisible(int index, boolean isVisible) {
        this.edgeShapes[index].setVisible(isVisible);
        fireStateChanged();
    }

    public void setSideVisible(int index, boolean isVisible) {
        this.sideShapes[index].setVisible(isVisible);
        fireStateChanged();
    }

    public void setCenterVisible(boolean isVisible) {
        this.centerShape.setVisible(isVisible);
        fireStateChanged();
    }

    public void setExplosion(double explosion) {
        this.explosion = 27.0d * explosion;
        computeTransformation();
        fireStateChanged();
    }

    public double getExplosion() {
        return this.explosion / 27.0d;
    }

    public abstract void setStickerColor(int face, int sticker, Color color);

    public abstract Color getStickerColor(int face, int sticker);

    protected void init() {
        initCorners();
        initEdges();
        initSides();
        initCenter();
        initTransforms();
        initActions();
        setModel(new RubiksCubeCore());
    }

    protected abstract void initCorners();

    protected abstract void initEdges();

    protected abstract void initSides();

    protected abstract void initCenter();

    protected void initTransforms() {
        this.sceneTransform = new TransformNode();
        this.cornerIdentityTransforms[0] = new Transform3D();
        this.cornerIdentityTransforms[0].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[1] = new Transform3D();
        this.cornerIdentityTransforms[1].rotateZ(-Math.PI / 2);
        this.cornerIdentityTransforms[1].rotateX(Math.PI / 2);
        this.cornerIdentityTransforms[1].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[1].rotateZ(-Math.PI / 2);
        this.cornerIdentityTransforms[2] = new Transform3D();
        this.cornerIdentityTransforms[2].rotateZ(-Math.PI / 2);
        this.cornerIdentityTransforms[2].rotateX(Math.PI / 2);
        this.cornerIdentityTransforms[2].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[2].rotateZ(Math.PI / 2);
        this.cornerIdentityTransforms[3] = new Transform3D();
        this.cornerIdentityTransforms[3].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[3].rotateZ(Math.PI);
        this.cornerIdentityTransforms[4] = new Transform3D();
        this.cornerIdentityTransforms[4].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[4].rotateY(Math.PI);
        this.cornerIdentityTransforms[5] = new Transform3D();
        this.cornerIdentityTransforms[5].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[5].rotate(Math.PI, Math.PI / 2, 0.0d);
        this.cornerIdentityTransforms[6] = new Transform3D();
        this.cornerIdentityTransforms[6].rotate(-Math.PI / 2, 0.0d, Math.PI / 2);
        this.cornerIdentityTransforms[6].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[6].rotateX(Math.PI / 2);
        this.cornerIdentityTransforms[7] = new Transform3D();
        this.cornerIdentityTransforms[7].translate(-18.0d, 18.0d, 18.0d);
        this.cornerIdentityTransforms[7].rotateX(Math.PI);
        this.edgeIdentityTransforms[0] = new Transform3D();
        this.edgeIdentityTransforms[0].rotateZ(Math.PI);
        this.edgeIdentityTransforms[0].rotateX(Math.PI / 2);
        this.edgeIdentityTransforms[0].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[1] = new Transform3D();
        this.edgeIdentityTransforms[1].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[1].rotateZ(-Math.PI / 2);
        this.edgeIdentityTransforms[2] = new Transform3D();
        this.edgeIdentityTransforms[2].rotateZ(Math.PI);
        this.edgeIdentityTransforms[2].rotateX(Math.PI / 2);
        this.edgeIdentityTransforms[2].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[2].rotateZ(Math.PI);
        this.edgeIdentityTransforms[3] = new Transform3D();
        this.edgeIdentityTransforms[3].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[3].rotate(0.0d, -Math.PI / 2, 0.0d);
        this.edgeIdentityTransforms[4] = new Transform3D();
        this.edgeIdentityTransforms[4].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[4].rotateZ(Math.PI / 2);
        this.edgeIdentityTransforms[5] = new Transform3D();
        this.edgeIdentityTransforms[5].rotateZ(Math.PI);
        this.edgeIdentityTransforms[5].rotateX(Math.PI / 2);
        this.edgeIdentityTransforms[5].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[5].rotate(0.0d, -Math.PI / 2, Math.PI / 2);
        this.edgeIdentityTransforms[6] = new Transform3D();
        this.edgeIdentityTransforms[6].rotateZ(Math.PI);
        this.edgeIdentityTransforms[6].rotateX(Math.PI / 2);
        this.edgeIdentityTransforms[6].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[6].rotate(0.0d, Math.PI, 0.0d);
        this.edgeIdentityTransforms[7] = new Transform3D();
        this.edgeIdentityTransforms[7].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[7].rotate(0.0d, Math.PI, Math.PI / 2);
        this.edgeIdentityTransforms[8] = new Transform3D();
        this.edgeIdentityTransforms[8].rotateZ(Math.PI);
        this.edgeIdentityTransforms[8].rotateX(Math.PI / 2);
        this.edgeIdentityTransforms[8].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[8].rotate(Math.PI, 0.0d, 0.0d);
        this.edgeIdentityTransforms[9] = new Transform3D();
        this.edgeIdentityTransforms[9].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[9].rotate(0.0d, Math.PI / 2, 0.0d);
        this.edgeIdentityTransforms[10] = new Transform3D();
        this.edgeIdentityTransforms[10].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[10].rotate(0.0d, Math.PI, -Math.PI / 2);
        this.edgeIdentityTransforms[11] = new Transform3D();
        this.edgeIdentityTransforms[11].translate(0.0d, 18.0d, 18.0d);
        this.edgeIdentityTransforms[11].rotate(0.0d, -Math.PI / 2, Math.PI);
        this.sideIdentityTransforms[0] = new Transform3D();
        this.sideIdentityTransforms[0].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[1] = new Transform3D();
        this.sideIdentityTransforms[1].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[1].rotateY(-Math.PI / 2);
        this.sideIdentityTransforms[2] = new Transform3D();
        this.sideIdentityTransforms[2].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[2].rotateX(-Math.PI / 2);
        this.sideIdentityTransforms[3] = new Transform3D();
        this.sideIdentityTransforms[3].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[3].rotateY(Math.PI);
        this.sideIdentityTransforms[4] = new Transform3D();
        this.sideIdentityTransforms[4].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[4].rotateY(Math.PI / 2);
        this.sideIdentityTransforms[5] = new Transform3D();
        this.sideIdentityTransforms[5].translate(0.0d, 0.0d, 18.0d);
        this.sideIdentityTransforms[5].rotateX(Math.PI / 2);
        for (int i = 0; i < 8; i++) {
            this.cornerTransforms[i] = new TransformNode();
            this.cornerTransforms[i].addChild(this.cornerShapes[i]);
            this.sceneTransform.addChild(this.cornerTransforms[i]);
        }
        for (int i = 0; i < 12; i++) {
            this.edgeTransforms[i] = new TransformNode();
            this.edgeTransforms[i].addChild(this.edgeShapes[i]);
            this.sceneTransform.addChild(this.edgeTransforms[i]);
        }
        for (int i = 0; i < 6; i++) {
            this.sideTransforms[i] = new TransformNode();
            this.sideTransforms[i].addChild(this.sideShapes[i]);
            this.sceneTransform.addChild(this.sideTransforms[i]);
        }
        this.centerTransform = new TransformNode();
        this.centerTransform.addChild(this.centerShape);
        this.sceneTransform.addChild(this.centerTransform);
    }

    protected abstract void initActions();

    @Override
    public void rubikTwisted(RubikEvent rubikEvent) {
        update();
    }

    @Override
    public void rubikTwisting(RubikEvent rubikEvent) {
        if (this.isAnimated) {
            animateTwist(rubikEvent);
        }
    }

    // 转动一次
    protected void animateTwist(RubikEvent rubikEvent) {
        Vector<TransformNode> vector = new Vector<>();
        Transform3D transform3D = new Transform3D();
        int layerMask = rubikEvent.getLayerMask();
        double angle = rubikEvent.getAngle();
        int i = (angle == 2.0d || angle == -2.0d) ? 20 : 10;
        double d = (Math.PI / 2 / i) * angle;
        switch (rubikEvent.getAxis()) {
        case 0:
            transform3D.rotateX(d);
            if ((layerMask & 0x1) == 1) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(0)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(1)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(6)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(7)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(1)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(9)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(10)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(11)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(4)]);
            }
            if ((layerMask & 0x2) == 2) {
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(0)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(2)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(6)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(8)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(0)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(2)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(3)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(5)]);
                vector.addElement(this.centerTransform);
            }
            if ((layerMask & 0x4) == 4) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(2)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(3)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(4)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(5)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(3)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(4)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(5)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(7)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(1)]);
                break;
            }
            break;
        case 1:
            transform3D.rotateY(d);
            if ((layerMask & 0x1) == 1) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(1)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(3)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(5)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(7)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(2)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(5)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(8)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(11)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(2)]);
            }
            if ((layerMask & 0x2) == 2) {
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(1)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(4)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(7)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(10)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(0)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(1)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(3)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(4)]);
                vector.addElement(this.centerTransform);
            }
            if ((layerMask & 0x4) == 4) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(0)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(2)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(4)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(6)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(0)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(3)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(6)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(9)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(5)]);
                break;
            }
            break;
        case 2:
            transform3D.rotateZ(d);
            if ((layerMask & 0x1) == 1) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(4)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(5)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(6)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(7)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(6)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(7)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(8)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(10)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(3)]);
            }
            if ((layerMask & 0x2) == 2) {
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(3)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(5)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(9)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(11)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(1)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(2)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(4)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(5)]);
                vector.addElement(this.centerTransform);
            }
            if ((layerMask & 0x4) == 4) {
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(0)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(1)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(2)]);
                vector.addElement(this.cornerTransforms[this.model.getCornerAt(3)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(0)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(1)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(2)]);
                vector.addElement(this.edgeTransforms[this.model.getEdgeAt(4)]);
                vector.addElement(this.sideTransforms[this.model.getSideAt(0)]);
            }
            break;
        }
        try {
            // 调整两次之间的转动间隔
            Thread.sleep(50L);
        } catch (InterruptedException e) {
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        for (int j = 1; j < i; j++) {
            synchronized (this.model) {
                Enumeration<TransformNode> enumerationElements = vector.elements();
                while (enumerationElements.hasMoreElements()) {
                    enumerationElements.nextElement().getTransform().concatenate(transform3D);
                }
            }
            fireStateChanged();
            // 影响转动速度
            jCurrentTimeMillis += 50L;
            long jCurrentTimeMillis2 = jCurrentTimeMillis - System.currentTimeMillis();
            if (jCurrentTimeMillis2 > 0L) {
                try {
                    Thread.sleep(jCurrentTimeMillis2);
                } catch (InterruptedException e2) {
                }
            } else {
                jCurrentTimeMillis -= jCurrentTimeMillis2;
                Thread.yield();
            }
        }
        computeTransformation();
        fireStateChanged();
    }

    @Override
    public void rubikChanged(RubikEvent rubikEvent) {
        update();
    }

    @Override
    public void rubikPartRotated(RubikEvent rubikEvent) {
        update();
    }

    public void addChangeListener(ChangeListener changeListener) {
        this.listenerList.add(ChangeListener.class, changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        this.listenerList.remove(ChangeListener.class, changeListener);
    }

    public void setAnimated(boolean isAnimated) {
        this.isAnimated = isAnimated;
    }

    public boolean isAnimated() {
        return this.isAnimated;
    }

    public void fireStateChanged() {
        List<ListenerNode> listenerList = this.listenerList.getListenerList();
        for (ListenerNode node : listenerList) {
            if (node.getClazz() == ChangeListener.class) {
                if (this.changeEvent == null) {
                    this.changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) node.getListener()).stateChanged(this.changeEvent);
            }
        }
    }

    public void setDispatcher(PooledSequentialDispatcherAWT dispatcher) {
        this.dispatcher = dispatcher;
    }

    public PooledSequentialDispatcherAWT getDispatcher() {
        if (this.dispatcher == null) {
            this.dispatcher = new PooledSequentialDispatcherAWT();
        }
        return this.dispatcher;
    }

    public abstract String getName();

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.editMode = isEditMode;
    }

    public Color getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(Color selectColor) {
        this.selectColor = selectColor;
    }
}
