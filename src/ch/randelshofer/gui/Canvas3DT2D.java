package ch.randelshofer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.Vector;

import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Transform3D;

public class Canvas3DT2D extends Canvas3DAWT {
    private static final long serialVersionUID = 8714836531087531311L;

    private Canvas3DT2D() {
    }

    public static Canvas3DAWT createCanvas3D() {
        return new Canvas3DT2D();
    }

    private static void setGraphicHints(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
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
        Graphics2D g2d = (Graphics2D) graphics;
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
            GeneralPath generalPath = new GeneralPath();
            double d1 = coords[(vertices[0] * 3) + 2] - z;
            if (d1 != 0.0d) {
                int j = vertices[0] * 3;
                double pointx = width + ((x - ((z * coords[j] - x) / d1)) * scale);
                double pointy = height - ((y - ((z * coords[j + 1] - y) / d1)) * scale);
                generalPath.moveTo(pointx, pointy);
            } else {
                double pointx = width + (x * scale);
                double pointy = height - (y * scale);
                generalPath.moveTo(pointx, pointy);
            }
            for (int i = 1; i < vertices.length; i++) {
                double d = coords[(vertices[i] * 3) + 2] - z;
                if (d != 0.0d) {
                    int j = vertices[i] * 3;
                    double pointx = width + ((x - ((z * coords[j] - x) / d)) * scale);
                    double pointy = height - ((y - ((z * coords[j + 1] - y) / d)) * scale);
                    generalPath.lineTo(pointx, pointy);
                } else {
                    double pointx = width + (x * scale);
                    double pointy = height - (y * scale);
                    generalPath.lineTo(pointx, pointy);
                }
            }
            generalPath.closePath();
            Color color = face3D.getFillColor();
            if (color != null) {
                double brightness = face3D.getBrightness(this.lightSource, this.lightSourceIntensity, this.ambientLightIntensity);
                if (brightness < 1.0d) {
                    color = new Color((int) (brightness * color.getRed()), (int) (brightness * color.getGreen()), (int) (brightness * color.getBlue()));
                }
                g2d.setColor(color);
                g2d.fill(generalPath);
            }
            Color borderColor = face3D.getBorderColor();
            if (borderColor != null) {
                g2d.setColor(borderColor);
                g2d.draw(generalPath);
            }
            if (!this.isAdjusting && face3D.getAction() != null) {
                this.activeFaces.addElement(new FaceElement(generalPath, face3D));
            }
        }
    }
}
