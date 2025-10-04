package ch.randelshofer.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.util.Arrays;


public class Canvas3DJ2D extends Canvas3DAWT
{
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

    private static Method containsMethod;

    private static Class<?> strokeClass;

    private static Class<?> basicStrokeClass;

    private static Object capButt;

    private static Object joinBevel;

    private static Constructor<?> basicStrokeConstructor;

    private static Class<?> generalPathClass;

    private static Method moveToMethod;

    private static Method lineToMethod;

    private static Method closePathMethod;

    private static void createGraphics2DMethods()
    {
        if (isGraphics2DAvailable == null)
        {
            try
            {
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
                setRenderingHintMethod = graphics2DClass.getMethod("setRenderingHint", renderingHintsKeyClass,
                    Object.class);
                setStrokeMethod = graphics2DClass.getMethod("setStroke", strokeClass);
                fillMethod = graphics2DClass.getMethod("fill", Shape.class);
                drawMethod = graphics2DClass.getMethod("draw", Shape.class);
                basicStrokeConstructor = basicStrokeClass.getConstructor(float.class, int.class, int.class);
                containsMethod = Shape.class.getMethod("contains", double.class, double.class);
                moveToMethod = generalPathClass.getMethod("moveTo", double.class, double.class);
                lineToMethod = generalPathClass.getMethod("lineTo", double.class, double.class);
                closePathMethod = generalPathClass.getMethod("closePath", new Class[0]);
                isGraphics2DAvailable = Boolean.TRUE;
            }
            catch (Exception e)
            {
                isGraphics2DAvailable = Boolean.FALSE;
                e.printStackTrace();
            }
        }
    }

    private Canvas3DJ2D()
    {}

    public static Canvas3DAWT createCanvas3D()
    {
        createGraphics2DMethods();
        return isGraphics2DAvailable == Boolean.TRUE ? new Canvas3DJ2D() : new Canvas3DAWT();
    }

    private static void setGraphicHints(Graphics graphics)
    {
        try
        {
            setRenderingHintMethod.invoke(graphics, keyAntialiasing, valueAntialiasOn);
            setRenderingHintMethod.invoke(graphics, keyFractionalmetrics, valueFractionalmetricsOn);
            setStrokeMethod.invoke(graphics, basicStrokeConstructor.newInstance(1.0f, capButt, joinBevel));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createBackGraphics(Dimension dimension)
    {
        this.backImg = createImage(dimension.width, dimension.height);
        this.backGfx = this.backImg.getGraphics();
        setGraphicHints(this.backGfx);
    }

    @Override
    protected void paint3D(Graphics graphics)
    {
        try
        {
            Insets insets = this.paintInsets;
            Object[] objArr = new Double[2];
            Dimension size = getSize();
            Transform3D transform = this.transformModel.getTransform();
            double f = insets.left + (((size.width - insets.left) - insets.right) / 2);
            double f2 = insets.top + (((size.height - insets.top) - insets.bottom) / 2);
            double fMin = Math.min(((size.width - insets.left) - insets.right) / 2,
                ((size.height - insets.top) - insets.bottom) / 2) * this.scaleFactor;
            double f3 = -fMin;
            Vector<Face3D> vector = new Vector<>();
            this.activeFaces.removeAllElements();
            this.scene.addVisibleFaces(vector, transform, this.observer);
            Face3D[] face3DArr = new Face3D[vector.size()];
            vector.copyInto(face3DArr);
            Arrays.sort(face3DArr);
            double f4 = this.observer.x;
            double f5 = this.observer.y;
            double f6 = this.observer.z;
            for (Face3D face3D : face3DArr)
            {
                if (face3D != null)
                {
                    double[] coords = face3D.getCoords();
                    int[] vertices = face3D.getVertices();
                    Object objNewInstance = generalPathClass.getDeclaredConstructor().newInstance();
                    int i = vertices[0] * 3;
                    double f7 = coords[(vertices[0] * 3) + 2] - f6;
                    if (f7 != 0.0d)
                    {
                        objArr[0] = f + ((f4 - (((f6 * coords[i]) - f4) / f7)) * fMin);
                        objArr[1] = f2 + ((f5 - (((f6 * coords[i + 1]) - f5) / f7)) * f3);
                        moveToMethod.invoke(objNewInstance, objArr);
                    }
                    else
                    {
                        objArr[0] = f + (f4 * fMin);
                        objArr[1] = f2 + (f5 * f3);
                        moveToMethod.invoke(objNewInstance, objArr);
                    }
                    for (int i2 = 1; i2 < vertices.length; i2++)
                    {
                        int i3 = vertices[i2] * 3;
                        double f8 = coords[(vertices[i2] * 3) + 2] - f6;
                        if (f8 != 0.0d)
                        {
                            objArr[0] = f + ((f4 - (((f6 * coords[i3]) - f4) / f8)) * fMin);
                            objArr[1] = f2 + ((f5 - (((f6 * coords[i3 + 1]) - f5) / f8)) * f3);
                            lineToMethod.invoke(objNewInstance, objArr);
                        }
                        else
                        {
                            objArr[0] = f + (f4 * fMin);
                            objArr[1] = f2 + (f5 * f3);
                            lineToMethod.invoke(objNewInstance, objArr);
                        }
                    }
                    closePathMethod.invoke(objNewInstance);
                    Color color = face3D.getFillColor();
                    if (color != null)
                    {
                        double brightness = this.lightSource == null ? 1.0d : face3D.getBrightness(this.lightSource,
                            this.lightSourceIntensity, this.ambientLightIntensity);
                        graphics.setColor(new Color(Math.min(255, (int)(color.getRed() * brightness)),
                            Math.min(255, (int)(color.getGreen() * brightness)),
                            Math.min(255, (int)(color.getBlue() * brightness))));
                        fillMethod.invoke(graphics, objNewInstance);
                    }
                    Color borderColor = face3D.getBorderColor();
                    if (borderColor != null)
                    {
                        graphics.setColor(borderColor);
                        drawMethod.invoke(graphics, objNewInstance);
                    }
                    if (!this.isAdjusting && face3D.getAction() != null)
                    {
                        this.activeFaces.addElement(objNewInstance);
                        this.activeFaces.addElement(face3D);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent paramMouseEvent)
    {
        try
        {
            if (isEnabled() && !this.isPopupTrigger)
            {
                int i = paramMouseEvent.getX();
                int j = paramMouseEvent.getY();
                this.prevx = i;
                this.prevy = j;
                Object[] arrayOfObject = {i, j};
                for (int k = this.activeFaces.size() - 2; k >= 0; k -= 2)
                {
                    Shape shape = (Shape)this.activeFaces.elementAt(k);
                    Face3D face3D = (Face3D)this.activeFaces.elementAt(k + 1);
                    if (containsMethod.invoke(shape, arrayOfObject).equals(Boolean.TRUE))
                    {
                        face3D.handleEvent(paramMouseEvent);
                        break;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

}
