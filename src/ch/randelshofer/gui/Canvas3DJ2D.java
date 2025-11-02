package ch.randelshofer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.PriorityQueue;
import java.util.Queue;

import ch.randelshofer.geom3d.Face3D;
import ch.randelshofer.geom3d.Transform3D;

/** 控制魔方3D展示 */
public class Canvas3DJ2D extends Canvas3DAWT {
    private static final long serialVersionUID = 8714836531087531311L;

    private Canvas3DJ2D() {
    }

    public static Canvas3DAWT createCanvas3D() {
        return new Canvas3DJ2D();
    }

    private static void setGraphicHints(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        // 质量优先
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // 消除绘图锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 启用笔画规范
        // g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        // 消除文字锯齿
        // g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 启用字体规范
        // g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
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
        Transform3D transform = this.transformModel.getTransform();
        double width = (getWidth() - insets.left - insets.right) / 2;
        double height = (getHeight() - insets.top - insets.bottom) / 2;
        double scale = this.scaleFactor * Math.min(width, height);
        width += insets.left;
        height += insets.top;
        // PriorityQueue为有序队列，插入新数据时会自动插入到合适的位置以保证队列有序，不需要重新排序，所以使用该队列
        Queue<Face3D> visibleFaces = new PriorityQueue<>(Canvas3DAWT.maxfaceItemNum, Face3DComparator.getInstance());
        this.scene.addVisibleFaces(visibleFaces, transform, this.observer);
        double pointx;
        double pointy;
        double x = this.observer.x;
        double y = this.observer.y;
        double z = this.observer.z;
        this.activeFaces.clear();
        while (!visibleFaces.isEmpty()) {
            Face3D face3D = visibleFaces.poll();
            // face3D will never be null
            double[] coords = face3D.getCoords();
            int[] vertices = face3D.getVertices();
            GeneralPath generalPath = new GeneralPath();
            double d1 = coords[(vertices[0] * 3) + 2] - z;
            if (d1 != 0.0d) {
                int j = vertices[0] * 3;
                pointx = width + ((x - ((z * coords[j] - x) / d1)) * scale);
                pointy = height - ((y - ((z * coords[j + 1] - y) / d1)) * scale);
            } else {
                pointx = width + (x * scale);
                pointy = height - (y * scale);
            }
            generalPath.moveTo(pointx, pointy);
            for (int i = 1; i < vertices.length; i++) {
                double d = coords[(vertices[i] * 3) + 2] - z;
                if (d != 0.0d) {
                    int j = vertices[i] * 3;
                    pointx = width + ((x - ((z * coords[j] - x) / d)) * scale);
                    pointy = height - ((y - ((z * coords[j + 1] - y) / d)) * scale);
                } else {
                    pointx = width + (x * scale);
                    pointy = height - (y * scale);
                }
                generalPath.lineTo(pointx, pointy);
            }
            generalPath.closePath();
            Color color = face3D.getFillColor();
            if (color != null) {
                double brightness = face3D.getBrightness(this.lightSource, this.lightSourceIntensity, this.ambientLightIntensity);
                color = new Color(Math.min(255, (int) (brightness * color.getRed())), Math.min(255, (int) (brightness * color.getGreen())),
                        Math.min(255, (int) (brightness * color.getBlue())));
                g2d.setColor(color);
                g2d.fill(generalPath);
            }
            Color borderColor = face3D.getBorderColor();
            if (borderColor != null) {
                g2d.setColor(borderColor);
                g2d.draw(generalPath);
            }
            if (!this.isAdjusting && face3D.getAction() != null) {
                this.activeFaces.add(new FaceElement(generalPath, face3D));
            }
        }
    }
}
