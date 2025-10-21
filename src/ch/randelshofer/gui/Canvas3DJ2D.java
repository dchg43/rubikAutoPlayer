package ch.randelshofer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Transform3D;

public class Canvas3DJ2D extends Canvas3DAWT {
    private static final long serialVersionUID = -1110753364015391478L;

    private static Boolean isGraphics2DAvailable = null;

    private static Class<?> graphics2DClass;

    private static Method setRenderingHintMethod;

    private static Method setStrokeMethod;

    private static Method fillMethod;

    private static Method drawMethod;

    private static Class<?> renderingHintsClass;

    private static Class<?> renderingHintsKeyClass;

    private static Object keyAntialiasing;

    private static Object valueAntialiasOn;

    private static Object keyFractionalmetrics;

    private static Object valueFractionalmetricsOn;

    private static Class<?> strokeClass;

    private static Class<?> basicStrokeClass;

    private static Object capButt;

    private static Object joinBevel;

    private static Constructor<?> basicStrokeConstructor;

    private static Class<?> generalPathClass;

    private static Method moveToMethod;

    private static Method lineToMethod;

    private static Method closePathMethod;

    private static void createGraphics2DMethods() throws Exception {
        graphics2DClass = Class.forName("java.awt.Graphics2D");
        generalPathClass = Class.forName("java.awt.geom.GeneralPath");
        renderingHintsClass = Class.forName("java.awt.RenderingHints");
        renderingHintsKeyClass = Class.forName("java.awt.RenderingHints$Key");
        strokeClass = Class.forName("java.awt.Stroke");
        basicStrokeClass = Class.forName("java.awt.BasicStroke");
        keyAntialiasing = renderingHintsClass.getField("KEY_ANTIALIASING").get(null);
        valueAntialiasOn = renderingHintsClass.getField("VALUE_ANTIALIAS_ON").get(null);
        keyFractionalmetrics = renderingHintsClass.getField("KEY_FRACTIONALMETRICS").get(null);
        valueFractionalmetricsOn = renderingHintsClass.getField("VALUE_FRACTIONALMETRICS_ON").get(null);
        capButt = basicStrokeClass.getField("CAP_BUTT").get(null);
        joinBevel = basicStrokeClass.getField("JOIN_BEVEL").get(null);
        setRenderingHintMethod = graphics2DClass.getMethod("setRenderingHint", renderingHintsKeyClass, Object.class);
        setStrokeMethod = graphics2DClass.getMethod("setStroke", strokeClass);
        fillMethod = graphics2DClass.getMethod("fill", Shape.class);
        drawMethod = graphics2DClass.getMethod("draw", Shape.class);
        basicStrokeConstructor = basicStrokeClass.getConstructor(float.class, int.class, int.class);
        moveToMethod = generalPathClass.getMethod("moveTo", double.class, double.class);
        lineToMethod = generalPathClass.getMethod("lineTo", double.class, double.class);
        closePathMethod = generalPathClass.getMethod("closePath", new Class[0]);
    }

    private Canvas3DJ2D() {
    }

    public static Canvas3DAWT createCanvas3D() {
        if (isGraphics2DAvailable == null) {
            try {
                createGraphics2DMethods();
                isGraphics2DAvailable = Boolean.TRUE;
                return new Canvas3DJ2D();
            } catch (Exception e) {
                isGraphics2DAvailable = Boolean.FALSE;
                return new Canvas3DAWT();
            }
        }
        return isGraphics2DAvailable == Boolean.TRUE ? new Canvas3DJ2D() : new Canvas3DAWT();
    }

    private static void setGraphicHints(Graphics graphics) {
        try {
            setRenderingHintMethod.invoke(graphics, keyAntialiasing, valueAntialiasOn);
            setRenderingHintMethod.invoke(graphics, keyFractionalmetrics, valueFractionalmetricsOn);
            setStrokeMethod.invoke(graphics, basicStrokeConstructor.newInstance(1.0f, capButt, joinBevel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void createBackGraphics(Dimension size) {
        this.backImg = createImage(size.width, size.height);
        this.backGfx = this.backImg.getGraphics();
        setGraphicHints(this.backGfx);
    }

    // 绘制魔方(更精致)
    @Override
    protected void paint3D(Graphics graphics) {
        try {
            Insets insets = this.paintInsets;
            Dimension size = getSize();
            Transform3D transform = this.transformModel.getTransform();
            double width = (size.width - insets.left - insets.right) / 2;
            double height = (size.height - insets.top - insets.bottom) / 2;
            double scale = this.scaleFactor * Math.min(width, height);
            width += insets.left;
            height += insets.top;
            Vector<Face3D> visibleFaces = new Vector<>();
            this.activeFaces.removeAllElements();
            this.scene.addVisibleFaces(visibleFaces, transform, this.observer);
            visibleFaces.sort(Face3DComparator.getInstance());
            double x = this.observer.x;
            double y = this.observer.y;
            double z = this.observer.z;
            for (Face3D face3D : visibleFaces) {
                // face3D will never be null
                double[] coords = face3D.getCoords();
                int[] vertices = face3D.getVertices();
                Shape generalPath = (Shape) generalPathClass.getDeclaredConstructor().newInstance();
                double d1 = coords[(vertices[0] * 3) + 2] - z;
                if (d1 != 0.0d) {
                    int j = vertices[0] * 3;
                    double pointx = width + ((x - ((z * coords[j] - x) / d1)) * scale);
                    double pointy = height - ((y - ((z * coords[j + 1] - y) / d1)) * scale);
                    moveToMethod.invoke(generalPath, pointx, pointy);
                } else {
                    double pointx = width + (x * scale);
                    double pointy = height - (y * scale);
                    moveToMethod.invoke(generalPath, pointx, pointy);
                }
                for (int i = 1; i < vertices.length; i++) {
                    double d = coords[(vertices[i] * 3) + 2] - z;
                    if (d != 0.0d) {
                        int j = vertices[i] * 3;
                        double pointx = width + ((x - ((z * coords[j] - x) / d)) * scale);
                        double pointy = height - ((y - ((z * coords[j + 1] - y) / d)) * scale);
                        lineToMethod.invoke(generalPath, pointx, pointy);
                    } else {
                        double pointx = width + (x * scale);
                        double pointy = height - (y * scale);
                        lineToMethod.invoke(generalPath, pointx, pointy);
                    }
                }
                closePathMethod.invoke(generalPath);
                Color color = face3D.getFillColor();
                if (color != null) {
                    double brightness = face3D.getBrightness(this.lightSource, this.lightSourceIntensity, this.ambientLightIntensity);
                    color = new Color(Math.min(255, (int) (brightness * color.getRed())), Math.min(255, (int) (brightness * color.getGreen())),
                            Math.min(255, (int) (brightness * color.getBlue())));
                    graphics.setColor(color);
                    fillMethod.invoke(graphics, generalPath);
                }
                Color borderColor = face3D.getBorderColor();
                if (borderColor != null) {
                    graphics.setColor(borderColor);
                    drawMethod.invoke(graphics, generalPath);
                }
                if (!this.isAdjusting && face3D.getAction() != null) {
                    this.activeFaces.addElement(new FaceElement(generalPath, face3D));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
