package ch.randelshofer.rubik;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import ch.randelshofer.geom3d.Point3D;
import ch.randelshofer.geom3d.RotatedTransform3DModel;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.gui.Canvas3DAWT;
import ch.randelshofer.gui.Canvas3DJ2D;
import ch.randelshofer.gui.MultilineLabel;
import ch.randelshofer.gui.RatioLayout;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.rubik.parserAWT.BandelowENGParser;
import ch.randelshofer.rubik.parserAWT.CastellaParser;
import ch.randelshofer.rubik.parserAWT.HarrisENGParser;
import ch.randelshofer.rubik.parserAWT.RandelshoferGERParser;
import ch.randelshofer.rubik.parserAWT.ScriptFRAParser;
import ch.randelshofer.rubik.parserAWT.ScriptNode;
import ch.randelshofer.rubik.parserAWT.ScriptParser;
import ch.randelshofer.rubik.parserAWT.ScriptPlayer;
import ch.randelshofer.rubik.parserAWT.SupersetENGParser;
import ch.randelshofer.rubik.parserAWT.TouchardDeledicqFRAParser;
import ch.randelshofer.util.Applets;
import ch.randelshofer.util.PooledSequentialDispatcherAWT;


public class RubikPlayerApp extends Applet implements Runnable
{
    private static final long serialVersionUID = -698774308591767979L;

    private ScriptPlayer player;

    // private boolean isInitialized = false;

    private MultilineLabel scriptTextArea;

    private Panel controlsPanel;

    private static final String version = "5.2.1";

    private static final String copyright = "© 2000-2005 W. Randelshofer";

    private static final Color inactiveSelectionBackground = new Color(0xD5, 0xD5, 0xD5);

    private static final Color activeSelectionBackground = new Color(0xFF, 0xFF, 0x40);

    private Canvas3DAWT rearCanvas3D;

    private RubiksCubeCore initCube;

    private boolean isSolver;

    @Override
    public void init()
    {
        initComponents();
        PooledSequentialDispatcherAWT.dispatchConcurrently(this);
    }

    @Override
    public void stop()
    {
        if (this.player != null)
        {
            this.player.stop();
        }
    }

    @Override
    public void paint(Graphics graphics)
    {
        graphics.setFont(new Font("Dialog", 0, 10));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.drawString("Loading Rubik Player " + version, 12, fontMetrics.getHeight());
        graphics.drawString(copyright, 12, fontMetrics.getHeight() * 2);
    }

    @Override
    public void run()
    {
        Component visualComponent;
        this.player = new ScriptPlayer()
        {
            @Override
            public void reset()
            {
                super.reset();
                getCubeModel().setTo(RubikPlayerApp.this.initCube);
            }
        };
        this.initCube = new RubiksCubeCore();
        this.scriptTextArea = new MultilineLabel();
        this.controlsPanel = new Panel();
        this.controlsPanel.setLayout(new BorderLayout());
        this.controlsPanel.add("North", this.player.getControlPanelComponent());
        this.controlsPanel.add("South", this.scriptTextArea);
        this.scriptTextArea.setFont(new Font("Dialog", 0, 12));
        this.scriptTextArea.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                RubikPlayerApp.this.player.moveToCaret(
                    RubikPlayerApp.this.scriptTextArea.viewToModel(mouseEvent.getX(), mouseEvent.getY()));
            }
        });
        this.scriptTextArea.setSize(getSize());
        if (Applets.getParameter(this, "rearView", "false").equals("true"))
        {
            Canvas3DAWT visualComponent2 = (Canvas3DAWT)this.player.getVisualComponent();
            this.rearCanvas3D = Canvas3DJ2D.createCanvas3D();
            this.rearCanvas3D.setScene(this.player.getCube3D().getScene());
            this.rearCanvas3D.setSyncObject(this.player.getCube3D().getModel());
            this.player.getCube3D().addChangeListener(this.rearCanvas3D);
            int[] parameters = Applets.getParameters(this, "rearViewRotation", new int[] {180, 0, 0});
            if (parameters.length != 3)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'rearViewRotation' provides ").append(
                        parameters.length).append(" instead of 3 values.").toString());
            }
            this.rearCanvas3D.setTransformModel(
                new RotatedTransform3DModel((parameters[0] / 180.0d) * Math.PI, (parameters[1] / 180.0d) * Math.PI,
                    (parameters[2] / 180.0d) * Math.PI, visualComponent2.getTransformModel()));
            this.rearCanvas3D.setScaleFactor(visualComponent2.getScaleFactor());
            visualComponent2.setPreferredSize(new Dimension(1, 1));
            this.rearCanvas3D.setPreferredSize(new Dimension(1, 1));
            double fMax = Math.max(0.1d, Math.min(1.0d, Applets.getParameter(this, "rearViewScaleFactor", 0.75d)));
            Panel panel2 = new Panel();
            panel2.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
            panel2.add(visualComponent2);
            panel2.add(this.rearCanvas3D);
            this.rearCanvas3D.setScaleFactor(this.rearCanvas3D.getScaleFactor() * fMax);
            visualComponent = panel2;
        }
        else
        {
            visualComponent = this.player.getVisualComponent();
        }
        try
        {
            readParameters();
            ChangeListener changeListener = new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent changeEvent)
                {
                    RubikPlayerApp.this.selectCurrentSymbol();
                }
            };
            this.player.getBoundedRangeModel().addChangeListener(changeListener);
            this.player.addChangeListener(changeListener);
            synchronized (getTreeLock())
            {
                add("Center", visualComponent);
                add("South", this.controlsPanel);
                validate();
                this.controlsPanel.invalidate();
                validate();
            }
            if (Applets.getParameter(this, "autoPlay", false))
            {
                try
                {
                    Thread.sleep(100L);
                }
                catch (InterruptedException e)
                {}
                this.player.start();
            }
        }
        catch (Throwable th)
        {
            removeAll();
            setLayout(new BorderLayout());
            TextArea textArea = new TextArea(10, 40);
            add("Center", textArea);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            th.printStackTrace(printWriter);
            printWriter.close();
            textArea.setText(new StringBuffer().append(getAppletInfo()).append("\n\n").append(th).append("\n").append(
                stringWriter.toString()).toString());
            invalidate();
            validate();
        }
    }

    protected void selectCurrentSymbol()
    {
        int startPosition;
        int endPosition;
        ScriptNode currentSymbol = this.player.getCurrentSymbol();
        if (currentSymbol == null)
        {
            startPosition = endPosition = this.scriptTextArea.getText().length();
        }
        else
        {
            startPosition = currentSymbol.getStartPosition();
            endPosition = currentSymbol.getEndPosition() + 1;
        }
        this.scriptTextArea.select(startPosition, endPosition);
        this.scriptTextArea.setSelectionBackground(
            this.player.isProcessingCurrentSymbol() ? activeSelectionBackground : inactiveSelectionBackground);
    }

    private void readParameters()
        throws IllegalArgumentException
    {
        AbstractCube3DAWT cube3D = this.player.getCube3D();
        Color color = new Color(Applets.getParameter(this, "backgroundColor", 0xFFFFFF));
        Canvas3DAWT visualComponent = (Canvas3DAWT)this.player.getVisualComponent();
        visualComponent.setBackground(color);
        this.player.getControlPanelComponent().setBackground(color);
        this.controlsPanel.setBackground(color);
        Transform3D transform3D = new Transform3D();
        transform3D.rotateY((Applets.getParameter(this, "beta", 45) / 180.0d) * Math.PI);
        transform3D.rotateX((Applets.getParameter(this, "alpha", -25) / 180.0d) * Math.PI);
        this.player.setTransform(transform3D);
        // 设置各面颜色, 正面蓝色, 右面橙色, 底面白色, 背面绿色, 左面红色, 顶面黄色
        String[] default_colors = {"0x003373", "0xff4600", "0xf8f8f8", "0x00732f", "0x8c000f", "0xffd200"};
        String[] colors_str = Applets.getParameters(this, "colorTable", default_colors);
        Color[] colors = new Color[colors_str.length];
        for (int i = 0; i < colors_str.length; i++)
        {
            try
            {
                colors[i] = new Color(Applets.decode(colors_str[i]));
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'colorTable', value ").append(colors_str[i]).append(
                        " for entry ").append(i).append(" is illegal.").toString());
            }
        }
        if (getParameter("faces") == null)
        {
            for (int i = 0; i < 6; i++)
            {
                if (colors.length <= i)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter 'colorTable', entry number ").append(i).append(
                            " missing.").toString());
                }
                for (int i2 = 0; i2 < 9; i2++)
                {
                    cube3D.setStickerColor(i, i2, colors[i]);
                }
            }
        }
        String[] parameters = Applets.getParameters(this, "faces", (String[])null);
        if (parameters != null)
        {
            if (parameters.length != 6)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'faces' provides ").append(parameters.length).append(
                        " instead of 6 entries.").toString());
            }
            for (int i3 = 0; i3 < 6; i3++)
            {
                int param = Integer.parseInt(parameters[i3]);
                if (colors.length <= param)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter 'faces', unknown entry '").append(
                            parameters[i3]).append("'.").toString());
                }
                Color color3 = colors[param];
                for (int i4 = 0; i4 < 9; i4++)
                {
                    cube3D.setStickerColor(i3, i4, color3);
                }
            }
        }
        String[] parameters2 = Applets.getParameters(this, "stickers", (String[])null);
        if (parameters2 != null)
        {
            if (parameters2.length != 54)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'stickers' provides ").append(
                        parameters2.length).append(" instead of 54 entries.").toString());
            }
            for (int i5 = 0; i5 < 6; i5++)
            {
                for (int i6 = 0; i6 < 9; i6++)
                {
                    int param = Integer.parseInt(parameters2[(i5 * 9) + i6]);
                    if (colors.length <= param)
                    {
                        throw new IllegalArgumentException(
                            new StringBuffer().append("Invalid parameter 'stickers', unknown entry '").append(
                                param).append("'.").toString());
                    }
                    cube3D.setStickerColor(i5, i6, colors[param]);
                }
            }
        }
        String[] strArr = {"stickersFront", "stickersRight", "stickersDown", "stickersBack", "stickersLeft",
            "stickersUp"};
        for (int i7 = 0; i7 < 6; i7++)
        {
            String[] parameters3 = Applets.getParameters(this, strArr[i7], (String[])null);
            if (parameters3 != null)
            {
                if (parameters3.length != 9)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter '").append(strArr[i7]).append(
                            "' provides ").append(parameters3.length).append(" instead of 9 entries.").toString());
                }
                for (int i8 = 0; i8 < 9; i8++)
                {
                    int param = Integer.parseInt(parameters3[i8]);
                    if (colors.length <= param)
                    {
                        throw new IllegalArgumentException(
                            new StringBuffer().append("Invalid parameter '").append(strArr[i7]).append(
                                "', unknown entry '").append(parameters3[i8]).append("'.").toString());
                    }
                    cube3D.setStickerColor(i7, i8, colors[param]);
                }
            }
        }
        ScriptParser scriptParser;
        String parameter = getParameter("scriptLanguage");
        if (parameter == null || parameter.equals("BandelowENG"))
        {
            scriptParser = new BandelowENGParser();
        }
        else if (parameter.equals("RandelshoferGER"))
        {
            scriptParser = new RandelshoferGERParser();
        }
        else if (parameter.equals("ScriptFRA"))
        {
            scriptParser = new ScriptFRAParser();
        }
        else if (parameter.equals("SupersetENG"))
        {
            scriptParser = new SupersetENGParser();
        }
        else if (parameter.equals("HarrisENG"))
        {
            scriptParser = new HarrisENGParser();
        }
        else if (parameter.equals("TouchardDeledicqFRA"))
        {
            scriptParser = new TouchardDeledicqFRAParser();
        }
        else if (parameter.equals("Castella"))
        {
            scriptParser = new CastellaParser();
        }
        else
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptLanguage': Unsupported language '").append(
                    parameter).append("'").toString());
        }
        String parameter2 = Applets.getParameter(this, "scriptType", "Generator");
        if (parameter2.equals("Solver"))
        {
            this.isSolver = true;
        }
        else if (parameter2.equals("Generator"))
        {
            this.isSolver = false;
        }
        else
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptType': Unsupported type '").append(
                    parameter2).append("'").toString());
        }
        String parameter3 = getParameter("script");
        if (parameter3 == null)
        {
            this.scriptTextArea.setVisible(false);
        }
        else
        {
            parameter3 = parameter3.replace("\\n", "\n");
            try
            {
                ScriptNode scriptNode = scriptParser.parse(new StringReader(parameter3));
                this.scriptTextArea.setText(parameter3);
                this.player.setScript(scriptNode);
            }
            catch (Exception e2)
            {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e2.printStackTrace(printWriter);
                printWriter.close();
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'script':\n").append(e2.getMessage()).append(
                        "\n").append(stringWriter).toString());
            }
        }

        this.initCube.reset();
        String parameter4 = getParameter("initScript");
        if (parameter4 != null)
        {
            parameter4 = parameter4.replace("\\n", "\n");
            try
            {
                scriptParser.parse(new StringReader(parameter4)).applySubtreeTo(this.initCube, false);
            }
            catch (Exception e3)
            {
                StringWriter stringWriter2 = new StringWriter();
                PrintWriter printWriter2 = new PrintWriter(stringWriter2);
                e3.printStackTrace(printWriter2);
                printWriter2.close();
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'initScript':\n").append(e3.getMessage()).append(
                        "\n").append(stringWriter2).toString());
            }
        }

        if (this.isSolver && this.player.getScript() != null)
        {
            this.player.getScript().applySubtreeTo(this.initCube, true);
        }
        this.player.reset();
        try
        {
            int parameter5 = Applets.getParameter(this, "scriptProgress",
                (this.isSolver || Applets.getParameter(this, "autoPlay", false)) ? 0 : -1);
            if (parameter5 < 0)
            {
                parameter5 = (this.player.getBoundedRangeModel().getMaximum() - parameter5) + 1;
            }
            this.player.getBoundedRangeModel().setValue(parameter5);
        }
        catch (IndexOutOfBoundsException e8)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptProgress':\n").append(e8.getMessage()).toString());
        }
        String parameter6 = getParameter("displayLines");
        int iCountTokens;
        if (parameter6 == null)
        {
            iCountTokens = parameter3 == null ? 0 : new StringTokenizer(parameter3, "\n").countTokens();
        }
        else
        {
            try
            {
                iCountTokens = Integer.parseInt(parameter6);
            }
            catch (NumberFormatException e4)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'displayLines':\n").append(
                        e4.getMessage()).toString());
            }
        }
        if (iCountTokens <= 0)
        {
            this.scriptTextArea.setVisible(false);
        }
        else
        {
            try
            {
                this.scriptTextArea.setMinRows(iCountTokens);
            }
            catch (NoSuchMethodError e5)
            {}
        }
        visualComponent.setLightSourceIntensity(Applets.getParameter(this, "lightSourceIntensity", 1.0d));
        visualComponent.setAmbientLightIntensity(Applets.getParameter(this, "ambientLightIntensity", 0.6d));
        int[] parameters4 = Applets.getParameters(this, "lightSourcePosition", new int[] {-500, 500, 1000});
        if (parameters4.length != 3)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'lightSourcePosition' provides ").append(
                    parameters4.length).append(" instead of 3 entries.").toString());
        }
        visualComponent.setLightSource(new Point3D(parameters4[0], parameters4[1], parameters4[2]));
        String parameter7 = getParameter("backgroundImage");
        if (parameter7 != null)
        {
            try
            {
                visualComponent.setBackgroundImage(getImage(new URL(getDocumentBase(), parameter7)));
            }
            catch (MalformedURLException e6)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'backgroundImage' malformed URL: ").append(
                        parameter7).toString());
            }
        }
        if (Applets.getParameter(this, "rearView", "false").equals("true"))
        {
            this.rearCanvas3D.setBackground(new Color(Applets.getParameter(this, "rearViewBackgroundColor",
                Applets.getParameter(this, "backgroundColor", 0xFFFFFF))));
            String parameter8 = Applets.getParameter(this, "rearViewBackgroundImage", getParameter("backgroundImage"));
            if (parameter8 != null)
            {
                try
                {
                    this.rearCanvas3D.setBackgroundImage(getImage(new URL(getDocumentBase(), parameter8)));
                }
                catch (MalformedURLException e7)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter 'backgroundImage' malformed URL: ").append(
                            parameter8).toString());
                }
            }
            this.rearCanvas3D.setLightSourceIntensity(Applets.getParameter(this, "lightSourceIntensity", 1.0d));
            this.rearCanvas3D.setAmbientLightIntensity(Applets.getParameter(this, "ambientLightIntensity", 0.6d));
            int[] parameters5 = Applets.getParameters(this, "lightSourcePosition", new int[] {-500, 500, 1000});
            if (parameters5.length != 3)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'lightSourcePosition' provides ").append(
                        parameters5.length).append(" instead of 3 entries.").toString());
            }
            this.rearCanvas3D.setLightSource(new Point3D(parameters5[0], parameters5[1], parameters5[2]));
        }
    }

    @Override
    public String getAppletInfo()
    {
        return "Rubik Player " + version + ", " + copyright + ". All rights reserved.";
    }

    @Override
    public String[][] getParameterInfo()
    {
        return new String[][] {
            // 垂直方向倾斜角度
            {"alpha", "int", "Vertical orientation of the cube, -90..+90. Default: -25"},
            // 水平方向倾斜角度
            {"beta", "int", "Horizontal orientation of the cube, -90..+90. Default: 45"},
            // 背景色，默认白色
            {"backgroundColor", "int", "Background color. Default: 0xffffff"},
            // 背景图
            {"backgroundImage", "URL", "Background image. Default: none"},
            // 设置颜色，编号分别为0~5，十六进制RGB格式，下边定义每面颜色时指定编号
            {"colorTable", "[name=]int, ...",
                "RGB color look up table, 6..n entries. Each entry consists of an optional name and a hex value. Default: 0x003373,0xff4600,0xf8f8f8,0x00732f,0x8c000f,0xffd200"},
            // 按面自定义颜色，顺序为正面 右面 底面 背面 左面 顶面
            {"faces", "name, ...",
                "Maps colors from the color table to the faces of the cube; 6 integer values; front, right, down, back, left, up. Default: 0,1,2,3,4,5"},
            // 按块自定义颜色
            {"stickers", "name, ...",
                "Maps colors from the color table to the stickers of the cube; 54 integer values; front, right, down, back, left, up. Default: 0,0,0,0,0,0,0,0,0, 1,1,1,1,1,1,1,1,1, 2,2,2,2,2,2,2,2,2, 3,3,3,3,3,3,3,3,3, 4,4,4,4,4,4,4,4,4, 5,5,5,5,5,5,5,5,5"},
            // 自定义正面每块颜色
            {"stickersFront", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 0,0,0,0,0,0,0,0,0"},
            // 自定义右面每块颜色
            {"stickersRight", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 1,1,1,1,1,1,1,1,1"},
            // 自定义下面每块颜色
            {"stickersDown", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 2,2,2,2,2,2,2,2,2"},
            // 自定义后面每块颜色
            {"stickersBack", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 3,3,3,3,3,3,3,3,3"},
            // 自定义左面每块颜色
            {"stickersLeft", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 4,4,4,4,4,4,4,4,4"},
            // 自定义上面每块颜色
            {"stickersUp", "(name|int), ...",
                "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 5,5,5,5,5,5,5,5,5"},
            // 设置自动播放脚本
            {"script", "string", "Script. Default: no script."},
            {"scriptLanguage", "string",
                "Language of the Script: 'ScriptFRA','BandelowENG','RandelshoferGER','SupersetENG','TouchardDeledicqFRA','Castella'. Default: BandelowENG"},
            {"scriptType", "string", "The type of the script: 'Solver' or 'Generator'. Default: 'Solver'."},
            {"scriptProgress", "int",
                "Position of the progress bar. Default: end of script if scriptType is 'Generator', 0 if script type is 'Solver'."},
            {"initScript", "string",
                "This script is used to initialize the cube, and when the reset button is pressed. Default: no script."},
            {"displayLines", "int",
                "Number of lines of the Script display: set to 0 to switch the display off. Default: 1"},
            // 模拟光线强度、光源、位置
            {"ambientLightIntensity", "double", "Intensity of ambient light. Default: 0.6"},
            {"lightSourceIntensity", "double",
                "Intensity of the light source: set to 0 to switch the light source off. Default: 1.0"},
            {"lightSourcePosition", "int,int,int",
                "X, Y and Z coordinate of the light source. Default: -500, 500, 1000"},
            // 是否展示后视图
            {"rearView", "boolean", "Set this value to true, to turn the rear view on. Default: false"},
            {"rearViewBackgroundColor", "int", "Background color. Default: use value of parameter 'backgroundColor'"},
            {"rearViewBackgroundImage", "URL", "Background image. Default: use value of parameter 'backgroundImage'"},
            {"rearViewScaleFactor", "double",
                "Scale factor of the rear view. Value between 0.1 and 1.0. Default: 0.75"},
            {"rearViewRotation", "int,int,int",
                "Rotation of the rear view on the X, Y and Z axis in degrees. Default: 180,0,0"},
            // 是否自动播放
            {"autoPlay", "boolean",
                "Set this value to true, to start playing the script automatically. Default: false"}};
    }

    public static void main(String[] strArr)
    {
        Frame frame = new Frame("RubikPlayer"); // 初始化画布
        frame.setSize(400, 400); // 设置画布大小
        ScriptPlayer scriptPlayer = new ScriptPlayer();
        frame.add(scriptPlayer.getVisualComponent(), "Center"); // 添加魔方
        Transform3D transform3D = new Transform3D();
        transform3D.rotateY(Math.PI / 4);
        transform3D.rotateX(-Math.PI / 7.2);
        scriptPlayer.setTransform(transform3D); // 修改魔方视角
        frame.add(scriptPlayer.getControlPanelComponent(), "South"); // 添加重置按钮
        frame.addWindowListener(new WindowAdapter() // 添加退出事件
        {
            @Override
            public void windowClosing(WindowEvent windowEvent)
            {
                System.exit(0);
            }
        });
        frame.setVisible(true); // 显示
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
    }
}
