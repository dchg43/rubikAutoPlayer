
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import ch.min2phase.Search;
import ch.min2phase.Tools;
import ch.randelshofer.geom3d.Point3D;
import ch.randelshofer.geom3d.RotatedTransform3DModel;
import ch.randelshofer.geom3d.Transform3D;
import ch.randelshofer.gui.Canvas3DAWT;
import ch.randelshofer.gui.Canvas3DJ2D;
import ch.randelshofer.gui.MultilineLabel;
import ch.randelshofer.gui.RatioLayout;
import ch.randelshofer.gui.event.ChangeEvent;
import ch.randelshofer.gui.event.ChangeListener;
import ch.randelshofer.rubik.AbstractCube3DAWT;
import ch.randelshofer.rubik.RubiksCubeCore;
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
import ch.randelshofer.util.PooledSequentialDispatcherAWT;


public class AutoPlayer extends Panel implements Runnable
{
    private static final long serialVersionUID = -698774308591767978L;

    private ScriptPlayer player;

    private MultilineLabel scriptTextArea;

    private Panel controlsPanel;

    private static final String version = "5.2.1";

    private static final String copyright = "© 2000-2005 W. Randelshofer";

    private static final Color inactiveSelectionBackground = new Color(0xD5, 0xD5, 0xD5);

//  private static final Color activeSelectionBackground = new Color(0xFF, 0xFF, 0x40);
    private static final Color activeSelectionBackground = new Color(0x00, 0xFF, 0x40);

    private Color[] colors;

    private Panel rearComponent = null;

    private RubiksCubeCore initCube;

    private Hashtable<String, String> atts;

    private Hashtable<URL, Image> imageCache = new Hashtable<>();

    private Map<String, Integer> keyMap = new HashMap<>();

    private ScriptParser scriptParser;

    private URL docBase;

    private boolean initialized = false;

    private boolean isSolver;

    private boolean autoPlay = false;

    private Search search = new Search();

    public AutoPlayer()
    {
        this.atts = new Hashtable<>();

        // init keyMap
        keyMap.put("autoPlay", 0);

        keyMap.put("script", 1);
        keyMap.put("scriptLanguage", 2);
        keyMap.put("scriptType", 3);
        keyMap.put("scriptProgress", 4);
        keyMap.put("initScript", 5);
        keyMap.put("displayLines", 6);

        keyMap.put("faces", 7);
        keyMap.put("stickers", 8);
        keyMap.put("stickersFront", 9);
        keyMap.put("stickersRight", 10);
        keyMap.put("stickersDown", 11);
        keyMap.put("stickersBack", 12);
        keyMap.put("stickersLeft", 13);
        keyMap.put("stickersUp", 14);

        keyMap.put("rearView", 15);
        keyMap.put("rearViewBackgroundColor", 16);
        keyMap.put("rearViewBackgroundImage", 17);
        keyMap.put("rearViewScaleFactor", 18);
        keyMap.put("rearViewRotation", 19);

        keyMap.put("alpha", 20);
        keyMap.put("beta", 21);
        keyMap.put("backgroundColor", 22);
        keyMap.put("backgroundImage", 23);
        keyMap.put("colorTable", 24);
        keyMap.put("ambientLightIntensity", 25);
        keyMap.put("lightSourceIntensity", 26);
        keyMap.put("lightSourcePosition", 27);
    }

    public void init()
    {
        initComponents();
        PooledSequentialDispatcherAWT.dispatchConcurrently(this);
        while (!this.initialized) // 等待启动完成
        {
            try
            {
                Thread.sleep(10L);
            }
            catch (InterruptedException e)
            {}
        }
    }

    public void stop()
    {
        if (this.player != null)
        {
            this.player.stop();
        }
    }

    // 默认绘图函数，魔方未加载、加载中或失败时会显示的内容
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
        this.player = new ScriptPlayer()
        {
            @Override
            public void reset()
            {
                super.reset();
                getCubeModel().setTo(AutoPlayer.this.initCube);
            }
        };
        this.initCube = new RubiksCubeCore();
        this.scriptTextArea = new MultilineLabel();
        this.controlsPanel = new Panel(); // 底部整个控制框
        this.controlsPanel.setLayout(new BorderLayout());
        this.controlsPanel.add("North", this.player.getControlPanelComponent()); // 进度条和控制按钮
        this.controlsPanel.add("South", this.scriptTextArea); // 执行文本显示
        this.scriptTextArea.setFont(new Font("Dialog", 0, 12));
        this.scriptTextArea.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                AutoPlayer.this.player.moveToCaret(
                    AutoPlayer.this.scriptTextArea.viewToModel(mouseEvent.getX(), mouseEvent.getY()));
            }
        });
        this.scriptTextArea.setSize(getSize());

        try
        {
            readParameters();
            ChangeListener changeListener = new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent changeEvent)
                {
                    AutoPlayer.this.selectCurrentSymbol();
                }
            };
            this.player.getBoundedRangeModel().addChangeListener(changeListener);
            this.player.addChangeListener(changeListener);
            synchronized (getTreeLock())
            {
                add("South", this.controlsPanel);
                validate();
                this.controlsPanel.invalidate();
                validate();
            }
            doParameter("autoPlay");
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
        initialized = true;
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
        AbstractCube3DAWT cube = this.player.getCube3D();
        Color colorBack = new Color(getParameter("backgroundColor", 0xFFFFFF));
        this.player.getControlPanelComponent().setBackground(colorBack);
        this.controlsPanel.setBackground(colorBack);
        Transform3D transform3D = new Transform3D();
        transform3D.rotateY((getParameter("beta", 45) / 180.0d) * Math.PI);
        transform3D.rotateX((getParameter("alpha", -25) / 180.0d) * Math.PI);
        this.player.setTransform(transform3D);
        // 设置各面颜色, 顺序：正面, 右面, 底面, 背面, 左面, 顶面
        String[] dflt = {"0x003373", "0x8c000f", "0xf8f8f8", "0x00732f", "0xff4600", "0xffd200", "0x707070"};
        String[] colors_str = getParameters("colorTable", dflt);
        this.colors = new Color[colors_str.length];
        for (int i = 0; i < colors_str.length; i++)
        {
            try
            {
                this.colors[i] = new Color(decode(colors_str[i]));
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
                if (this.colors.length <= i)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter 'colorTable', entry number ").append(i).append(
                            " missing.").toString());
                }
                for (int j = 0; j < 9; j++)
                {
                    cube.setStickerColor(i, j, this.colors[i]);
                }
            }
        }
        String[] faceColors = getParameters("faces", (String[])null);
        if (faceColors != null)
        {
            if (faceColors.length != 6)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'faces' provides ").append(faceColors.length).append(
                        " instead of 6 entries.").toString());
            }
            for (int i = 0; i < 6; i++)
            {
                int entry = Integer.parseInt(faceColors[i]);
                if (this.colors.length <= entry)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter 'faces', unknown entry '").append(
                            faceColors[i]).append("'.").toString());
                }
                Color color = this.colors[entry];
                for (int j = 0; j < 9; j++)
                {
                    cube.setStickerColor(i, j, color);
                }
            }
        }
        String[] stickerColors = getParameters("stickers", (String[])null);
        if (stickerColors != null)
        {
            if (stickerColors.length != 54)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'stickers' provides ").append(
                        stickerColors.length).append(" instead of 54 entries.").toString());
            }
            int index = 0;
            for (int i = 0; i < 6; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    int entry = Integer.parseInt(stickerColors[index++]);
                    if (this.colors.length <= entry)
                    {
                        throw new IllegalArgumentException(
                            new StringBuffer().append("Invalid parameter 'stickers', unknown entry '").append(
                                entry).append("'.").toString());
                    }
                    cube.setStickerColor(i, j, this.colors[entry]);
                }
            }
        }
        String[] strArr = {"stickersFront", "stickersRight", "stickersDown", "stickersBack", "stickersLeft",
            "stickersUp"};
        for (int i = 0; i < 6; i++)
        {
            String[] colorLists = getParameters(strArr[i], (String[])null);
            if (colorLists != null)
            {
                if (colorLists.length != 9)
                {
                    throw new IllegalArgumentException(
                        new StringBuffer().append("Invalid parameter '").append(strArr[i]).append("' provides ").append(
                            colorLists.length).append(" instead of 9 entries.").toString());
                }
                for (int j = 0; j < 9; j++)
                {
                    int entry = Integer.parseInt(colorLists[j]);
                    if (this.colors.length <= entry)
                    {
                        throw new IllegalArgumentException(
                            new StringBuffer().append("Invalid parameter '").append(strArr[i]).append(
                                "', unknown entry '").append(colorLists[j]).append("'.").toString());
                    }
                    cube.setStickerColor(i, j, this.colors[entry]);
                }
            }
        }

        String language = getParameter("scriptLanguage");
        if (language == null || language.equals("BandelowENG"))
        {
            this.scriptParser = new BandelowENGParser();
        }
        else if (language.equals("RandelshoferGER"))
        {
            this.scriptParser = new RandelshoferGERParser();
        }
        else if (language.equals("ScriptFRA"))
        {
            this.scriptParser = new ScriptFRAParser();
        }
        else if (language.equals("SupersetENG"))
        {
            this.scriptParser = new SupersetENGParser();
        }
        else if (language.equals("HarrisENG"))
        {
            this.scriptParser = new HarrisENGParser();
        }
        else if (language.equals("TouchardDeledicqFRA"))
        {
            this.scriptParser = new TouchardDeledicqFRAParser();
        }
        else if (language.equals("Castella"))
        {
            this.scriptParser = new CastellaParser();
        }
        else
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptLanguage': Unsupported language '").append(
                    language).append("'").toString());
        }
        String scriptType = getParameter("scriptType", "Generator");
        if (scriptType.equals("Solver"))
        {
            this.isSolver = true;
        }
        else if (scriptType.equals("Generator"))
        {
            this.isSolver = false;
        }
        else
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptType': Unsupported type '").append(
                    scriptType).append("'").toString());
        }

        String script = getParameter("script", "");
        script = script.replace("\\n", "\n");
        try
        {
            ScriptNode scriptNode = scriptParser.parse(new StringReader(script));
            this.scriptTextArea.setText(script);
            this.player.setScript(scriptNode);
        }
        catch (Exception e2)
        {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e2.printStackTrace(printWriter);
            printWriter.close();
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'script':\n").append(e2.getMessage()).append("\n").append(
                    stringWriter).toString());
        }

        this.initCube.reset();
        String initScript = getParameter("initScript");
        if (initScript != null)
        {
            initScript = initScript.replace("\\n", "\n");
            try
            {
                scriptParser.parse(new StringReader(initScript)).applySubtreeTo(this.initCube, false);
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
            int scriptProgress = getParameter("scriptProgress",
                (this.isSolver || getParameter("autoPlay", false)) ? 0 : -1);
            if (scriptProgress < 0)
            {
                scriptProgress = (this.player.getBoundedRangeModel().getMaximum() - scriptProgress) + 1;
            }
            this.player.getBoundedRangeModel().setValue(scriptProgress);
        }
        catch (IndexOutOfBoundsException e8)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'scriptProgress':\n").append(e8.getMessage()).toString());
        }

        String displayLines = getParameter("displayLines", "1");
        int iCountTokens = script == null ? 1 : new StringTokenizer(script, "\n").countTokens();
        try
        {
            iCountTokens = Math.max(Integer.parseInt(displayLines), iCountTokens);
        }
        catch (NumberFormatException e4)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'displayLines':\n").append(e4.getMessage()).toString());
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

        // 配置魔方视图
        Canvas3DAWT visualComponent = (Canvas3DAWT)this.player.getVisualComponent();
        visualComponent.setBackground(colorBack);
        visualComponent.setLightSourceIntensity(getParameter("lightSourceIntensity", 1.0d));
        visualComponent.setAmbientLightIntensity(getParameter("ambientLightIntensity", 0.6d));
        visualComponent.setPreferredSize(new Dimension(1, 1));
        int[] lightSource = getParameters("lightSourcePosition", new int[] {-500, 500, 1000});
        if (lightSource.length != 3)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'lightSourcePosition' provides ").append(
                    lightSource.length).append(" instead of 3 entries.").toString());
        }
        visualComponent.setLightSource(new Point3D(lightSource[0], lightSource[1], lightSource[2]));
        String backgroundImage = getParameter("backgroundImage");
        if (backgroundImage != null)
        {
            try
            {
                visualComponent.setBackgroundImage(getImage(new URL(getDocumentBase(), backgroundImage)));
            }
            catch (MalformedURLException e6)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'backgroundImage' malformed URL: ").append(
                        backgroundImage).toString());
            }
        }

        // 配置魔方后视图
        Component panelComponent;
        if (getParameter("rearView", "false").equals("true"))
        {
            initRearComponent();
            panelComponent = this.rearComponent;
        }
        else
        {
            panelComponent = visualComponent; // 不包含后视图
        }
        add("Center", panelComponent);

    }

    private void initRearComponent()
    {
        if (this.rearComponent != null)
        {
            double fMax = Math.max(0.1d, Math.min(1.0d, getParameter("rearViewScaleFactor", 0.75d)));
            this.rearComponent.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
            return;
        }

        Canvas3DAWT visualComponent = (Canvas3DAWT)this.player.getVisualComponent();
        Canvas3DAWT rearCanvas3D = Canvas3DJ2D.createCanvas3D(); // 创建后视图
        rearCanvas3D.setScene(this.player.getCube3D().getScene());
        rearCanvas3D.setSyncObject(this.player.getCube3D().getModel());
        int[] rearViewRotation = getParameters("rearViewRotation", new int[] {180, 0, 0});
        if (rearViewRotation.length != 3)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'rearViewRotation' provides ").append(
                    rearViewRotation.length).append(" instead of 3 values.").toString());
        }
        rearCanvas3D.setTransformModel(new RotatedTransform3DModel((rearViewRotation[0] / 180.0d) * Math.PI,
            (rearViewRotation[1] / 180.0d) * Math.PI, (rearViewRotation[2] / 180.0d) * Math.PI,
            visualComponent.getTransformModel()));
        double fMax = Math.max(0.1d, Math.min(1.0d, getParameter("rearViewScaleFactor", 0.75d)));
        rearCanvas3D.setScaleFactor(visualComponent.getScaleFactor() * fMax);
        rearCanvas3D.setPreferredSize(visualComponent.getPreferredSize());

        rearCanvas3D.setBackground(
            new Color(getParameter("rearViewBackgroundColor", getParameter("backgroundColor", 0xFFFFFF))));
        String rearImage = getParameter("rearViewBackgroundImage", getParameter("backgroundImage"));
        if (rearImage != null)
        {
            try
            {
                rearCanvas3D.setBackgroundImage(getImage(new URL(getDocumentBase(), rearImage)));
            }
            catch (MalformedURLException e7)
            {
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter 'backgroundImage' malformed URL: ").append(
                        rearImage).toString());
            }
        }
        rearCanvas3D.setLightSourceIntensity(getParameter("lightSourceIntensity", 1.0d));
        rearCanvas3D.setAmbientLightIntensity(getParameter("ambientLightIntensity", 0.6d));
        int[] lightSource = getParameters("lightSourcePosition", new int[] {-500, 500, 1000});
        if (lightSource.length != 3)
        {
            throw new IllegalArgumentException(
                new StringBuffer().append("Invalid parameter 'lightSourcePosition' provides ").append(
                    lightSource.length).append(" instead of 3 entries.").toString());
        }
        rearCanvas3D.setLightSource(new Point3D(lightSource[0], lightSource[1], lightSource[2]));

        this.player.getCube3D().addChangeListener(rearCanvas3D);
        Panel panel = new Panel();
        panel.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
        panel.add(visualComponent);
        panel.add(rearCanvas3D);
        this.rearComponent = panel;
    }

    private URL getDocumentBase()
    {
        if (this.docBase == null)
        {
            try
            {
                this.docBase = new URL(System.getProperty("user.dir"));
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
        return this.docBase;
    }

    private Image getImage(URL url)
    {
        Image img = imageCache.get(url);
        if (img != null)
        {
            return img;
        }
        try
        {
            Object o = url.getContent();
            if (o == null)
            {
                return null;
            }
            if (o instanceof Image)
            {
                img = (Image)o;
                imageCache.put(url, img);
                return img;
            }
            // Otherwise it must be an ImageProducer.
            img = this.createImage((ImageProducer)o);
            imageCache.put(url, img);
            return img;

        }
        catch (Exception ex)
        {
            return null;
        }

    }

    public String getAppletInfo()
    {
        return "Rubik Player " + version + ", " + copyright + ". All rights reserved.";
    }

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

    private void initGUI()
    {
        JFrame frame = new JFrame("RubikPlayer"); // 初始化画布
        frame.setTitle("三阶魔方求解器");
        frame.setSize(600, 600); // 设置画布大小
        frame.setPreferredSize(new java.awt.Dimension(600, 600));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() // 添加退出事件
        {
            @Override
            public void windowClosing(WindowEvent windowEvent)
            {
                System.exit(0);
            }
        });

        JButton[] colorSel = new JButton[6];
        // 顺序：正面红色, 右面黄色, 底面绿色, 背面橙色, 左面白色, 顶面蓝色
        final Color[] initColors = {new Color(230, 0, 0), Color.yellow, new Color(0, 170, 0), new Color(255, 108, 0),
            Color.white, Color.blue};
        Border defaultBorder = BorderFactory.createEtchedBorder();
        Border selectBorder = new LineBorder(Color.black, 4);
        for (int i = 0; i < 6; i++)
        {
            colorSel[i] = new JButton();
            frame.add(colorSel[i]);
            colorSel[i].setBackground(initColors[i]);
            colorSel[i].setOpaque(true);
            colorSel[i].setBounds(20 + 40 * i, 20, 40, 40);
            colorSel[i].setBorderPainted(true);
            colorSel[i].setBorder(defaultBorder);
            colorSel[i].setName(String.valueOf(i));
            final int value = i;
            colorSel[i].addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
//                    if (!player.getCube3D().isEditMode())
//                    {
//                        JOptionPane.showMessageDialog(null, "非编辑状态", "错误", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
                    for (int j = 0; j < 6; j++)
                    {
                        if (j == value)
                        {
                            colorSel[j].setBorder(selectBorder);
                        }
                        else
                        {
                            colorSel[j].setBorder(defaultBorder);
                        }
                    }
                    player.getCube3D().setSelectColor(colors[value]);
                }
            });
        }

        // 编辑按钮
        JButton buttonEdit = new JButton("edit");
        frame.add(buttonEdit);
        buttonEdit.setBounds(270, 20, 65, 40);
        buttonEdit.setText("编辑");
        buttonEdit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (player.isActive())
                {
                    // 移到最后，player会自动停止
                    player.getBoundedRangeModel().setValue(player.getBoundedRangeModel().getMaximum());
                }

                // 判断魔方是否有旋转，因为编辑功能是基于魔方未旋转状态，如果有旋转，设置方块颜色时错位
                if (!player.getCube3D().getModel().isSolved())
                {
                    Color[] colorArr = new Color[7];
                    HashSet<Color> colorSet = new HashSet<>();
                    for (int i = 0; i < 6; i++)
                    {
                        // 以中心块的颜色为基准
                        colorArr[i] = player.getCube3D().getStickerColor(i, 4);
                        colorSet.add(colorArr[i]);
                    }
                    if (colorSet.size() == 6)
                    {
                        colorArr[6] = colors[6];
                        // 重置魔方状态，保留块的颜色和顺序
                        String cube = getCubeString();
                        player.getCube3D().getModel().reset();
                        setCubeString(cube, colorArr);
                    }
                }

                if (player.getCube3D().isEditMode())
                {
                    ((JButton)evt.getSource()).setBackground(new ColorUIResource(238, 238, 238));
                    player.getCube3D().setEditMode(false);
                }
                else
                {
                    ((JButton)evt.getSource()).setBackground(new Color(184, 207, 229));
                    player.getCube3D().setEditMode(true);
                    if (player.getCube3D().getSelectColor() == null)
                    {
                        player.getCube3D().setSelectColor(colors[0]);
                        colorSel[0].setBorder(selectBorder);
                    }
                }
            }
        });

        // 清空按钮
        JButton buttonClean = new JButton("clean");
        frame.add(buttonClean);
        buttonClean.setBounds(420, 20, 65, 40);
        buttonClean.setText("清空");
        buttonClean.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                player.getCube3D().getModel().reset();

                for (int i = 0; i < 6; i++)
                {
                    for (int j = 0; j < 9; j++)
                    {
                        player.getCube3D().setStickerColor(i, j, colors[6]);
                    }
                }
            }
        });

        // 打乱按钮
        JButton buttonRandom = new JButton("random");
        frame.add(buttonRandom);
        buttonRandom.setBounds(495, 20, 65, 40);
        buttonRandom.setText("打乱");
        buttonRandom.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                AbstractCube3DAWT cube = player.getCube3D();
                cube.getModel().reset();
                // Random stick by Call Random function
                setCubeString(Tools.randomCube(), colors);
            }
        });

        // 校验按钮
        JButton buttonCheck = new JButton("check");
        frame.add(buttonCheck);
        buttonCheck.setBounds(420, 70, 65, 40);
        buttonCheck.setText("校验");
        buttonCheck.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                String result = searchSolution();
                if (result.contains("Error"))
                {
                    switch (result.charAt(result.length() - 1))
                    {
                        case '1':
                            // "There are not exactly nine facelets of each color!"
                            result = "需要每种颜色都有9个块。";
                            break;
                        case '2':
                            // "Not all 12 edges exist exactly once!"
                            result = "12种棱块需要各存在一个。";
                            break;
                        case '3':
                            // "Flip error: One edge has to be flipped!"
                            result = "至少有一个棱块方向是反的。";
                            break;
                        case '4':
                            // "Not all 8 corners exist exactly once!"
                            result = "8种角块需要各存在一个。";
                            break;
                        case '5':
                            // "Twist error: One corner has to be twisted!"
                            result = "至少有一个角块需要扭一下。";
                            break;
                        case '6':
                            // "Parity error: Two corners or two edges have to be exchanged!"
                            result = "需要交换两个角块或两个棱块的位置。";
                            break;
                        case '7':
                            // "No solution exists for the given maximum move number!"
                            result = "没有低于25次移动的方案。";
                            break;
                        case '8':
                            // "Timeout, no solution found within given maximum time!"
                            result = "计算超时。";
                            break;
                        case '9':
                            result = "6个面的中心块需要各有一个颜色且不相同。";
                            break;
                    }
                    JOptionPane.showMessageDialog(null, "校验不通过：" + result, "失败", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "校验通过", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // 求解按钮
        JButton buttonSolution = new JButton("solution");
        frame.add(buttonSolution);
        buttonSolution.setBounds(495, 70, 65, 40);
        buttonSolution.setText("求解");
        buttonSolution.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                String result = searchSolution();
                if (result.contains("Error"))
                {
                    switch (result.charAt(result.length() - 1))
                    {
                        case '1':
                            // "There are not exactly nine facelets of each color!"
                            result = "需要每种颜色都有9个块。";
                            break;
                        case '2':
                            // "Not all 12 edges exist exactly once!"
                            result = "12种棱块需要各存在一个。";
                            break;
                        case '3':
                            // "Flip error: One edge has to be flipped!"
                            result = "至少有一个棱块方向是反的。";
                            break;
                        case '4':
                            // "Not all 8 corners exist exactly once!"
                            result = "8种角块需要各存在一个。";
                            break;
                        case '5':
                            // "Twist error: One corner has to be twisted!"
                            result = "至少有一个角块需要扭一下。";
                            break;
                        case '6':
                            // "Parity error: Two corners or two edges have to be exchanged!"
                            result = "需要交换两个角块或两个棱块的位置。";
                            break;
                        case '7':
                            // "No solution exists for the given maximum move number!"
                            result = "没有低于25次移动的方案。";
                            break;
                        case '8':
                            // "Timeout, no solution found within given maximum time!"
                            result = "计算超时。";
                            break;
                        case '9':
                            result = "6个面的中心块不能有相同颜色。";
                            break;
                    }
                    JOptionPane.showMessageDialog(null, "校验不通过：" + result, "失败", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    if (player.getCube3D().isEditMode())
                    {
                        buttonEdit.setBackground(new ColorUIResource(238, 238, 238));
                        player.getCube3D().setEditMode(false);
                    }

                    // 自动计算复位方法
                    try
                    {
                        setParameter("script", result);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                int width = frame.getWidth();
                if (width < 525)
                {
                    width = 525;
                }
                buttonClean.setLocation(width - 180, 20);
                buttonRandom.setLocation(width - 105, 20);
                buttonCheck.setLocation(width - 180, 70);
                buttonSolution.setLocation(width - 105, 70);
                frame.setVisible(true); // 刷新
            }
        });

        // 添加魔方
        frame.add(this, "Center");

        frame.setVisible(true); // 显示
    }

    public static void main(String[] strArr)
        throws IOException
    {
        AutoPlayer scriptPlayer = new AutoPlayer();

        scriptPlayer.setParameter("scriptLanguage", "SupersetENG");
        scriptPlayer.setParameter("scriptProgress", "0");
        scriptPlayer.setParameter("colorTable", "0x8c000f,0xffd200,0x00732f,0xff4600,0xf8f8f8,0x003373,0x707070");
        scriptPlayer.setParameter("autoPlay", "true");
        scriptPlayer.setParameter("rearView", "true");
        // 红0 黄1 绿2 橙3 白4 蓝5
//        scriptPlayer.setParameter("stickers",
//            "0,0,0,0,0,0,0,0,0, 1,1,1,1,1,1,1,1,1, 2,2,2,2,2,2,2,2,2, 3,3,3,3,3,3,3,3,3, 4,4,4,4,4,4,4,4,4, 5,5,5,5,5,5,5,5,5");

//        scriptPlayer.setParameter("initScript", "F R2 D F R B2 L2 U F B R L' B' U2 R'");
//        scriptPlayer.setParameter("initScript", "R' L' B  L' D' F  R  L  B2 U  F2 R2 L2 D' R2 U  R2 D2 L2 D' B' ");

        scriptPlayer.init(); // 启动
        scriptPlayer.initGUI();

        // 等待自动执行完成
        while (scriptPlayer.player.isActive())
        {
            try
            {
                Thread.sleep(500L);
            }
            catch (InterruptedException e)
            {}
        }

    }

    public String searchSolution()
    {
        String cubeString = this.getCubeString();
        if (cubeString.contains("Error"))
        {
            return cubeString;
        }
        System.out.println("input: " + cubeString);

        if (!Search.isInited())
        {
            Search.init();
        }

        int mask = 0;
        int depth = 18; // 15 ~ 18
        int maxDepth = 22;
        int maxTries = 200; // 200 ~ 1000
        String result = "Error";
        while (result.contains("Error") && depth < maxDepth)
        {
            result = this.search.solution(cubeString, depth, 100, 0, mask);
            int tries = maxTries;
            while (result.startsWith("Error 8") && tries > 0)
            {
                result = this.search.next(100, 0, mask);
                tries--;
            }
            System.out.println("maxDepth:" + depth + ", result: " + result);
            depth++;
        }
        return result;
    }

    // 初始状态应该获取到的序列为：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
    private String getCubeString()
    {
        AbstractCube3DAWT cube = this.player.getCube3D();
        RubiksCubeCore model = cube.getModel();
        // 初始化颜色对应表，顺序：front, right, down, back, left, up
        final char[] chars = {'F', 'R', 'D', 'B', 'L', 'U'};
        Map<Color, Character> colorMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++)
        {
            // 以中心块的颜色为基准
            colorMap.put(cube.getStickerColor(i, 4), chars[i]);
        }
        if (colorMap.size() < 6)
        {
            return "Error 9";
        }
        if (!colorMap.containsKey(colors[6]))
        {
            colorMap.put(colors[6], '0');
        }

        // CORNER_MAP[CornerSide][cornerLoc % 4] （详见图片<块的命名>）
        final int[][] CORNER_MAP = {{0, 6, 2, 8}, {2, 8, 0, 6}, {0, 2, 8, 6}, {0, 6, 2, 8}, {2, 8, 0, 6}, {6, 8, 2, 0}};
        // EDGE_MAP[edgeSide][edgeLoc]
        final int[][] EDGE_MAP = { //
            {1, 3, 7, 0, 5, 0, 0, 0, 0, 0, 0, 0}, // 0
            {0, 0, 0, 1, 3, 7, 0, 5, 0, 0, 0, 0}, // 1
            {0, 0, 1, 0, 0, 5, 0, 0, 7, 0, 0, 3}, // 2
            {0, 0, 0, 0, 0, 0, 1, 3, 7, 0, 5, 0}, // 3
            {0, 5, 0, 0, 0, 0, 0, 0, 0, 1, 3, 7}, // 4
            {7, 0, 0, 5, 0, 0, 1, 0, 0, 3, 0, 0}}; // 5

        // input产生顺序：up 0, right 9, front 18, down 27, left 36, back 45 （详见图片<求解映射表>）
        final int[][] CORNER_INDEX = {{6, 18, 38}, {27, 44, 24}, {8, 9, 20}, {29, 26, 15}, {2, 45, 11}, {35, 17, 51},
            {0, 36, 47}, {33, 53, 42}};
        final int[][] EDGE_INDEX = {{19, 7}, {41, 21}, {25, 28}, {5, 10}, {12, 23}, {32, 16}, {46, 1}, {14, 48},
            {52, 34}, {3, 37}, {39, 50}, {30, 43}};
        final int[] SIDE_INDEX = {22, 13, 31, 49, 40, 4};
        RubiksCubeCore initModel = new RubiksCubeCore();

        // 从RubiksCubeCore中根据旋转情况计算每个块的实际位置
        char[] searchInput = new char[54];
        int[] cornerLoc = model.getCornerLocations();
        int[] cornerOrient = model.getCornerOrientations();
        for (int i = 0; i < cornerLoc.length; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                int cornerSide = initModel.getCornerSide(cornerLoc[i], (cornerOrient[i] + j) % 3);
                int mapindex = (cornerSide == 2 || cornerSide == 5) ? (cornerLoc[i] / 2) : (cornerLoc[i] % 4);
                int cornerIndex = CORNER_MAP[cornerSide][mapindex];
                searchInput[CORNER_INDEX[i][j]] = colorMap.get(cube.getStickerColor(cornerSide, cornerIndex));
            }
        }

        int[] edgeLoc = model.getEdgeLocations();
        int[] edgeOrient = model.getEdgeOrientations();
        for (int i = 0; i < edgeLoc.length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                int edgeSide = initModel.getEdgeSide(edgeLoc[i], (edgeOrient[i] + j) % 2);
                int edgeIndex = EDGE_MAP[edgeSide][edgeLoc[i]];
                searchInput[EDGE_INDEX[i][j]] = colorMap.get(cube.getStickerColor(edgeSide, edgeIndex));
            }
        }

        int[] sideLoc = model.getSideLocations();
        for (int i = 0; i < sideLoc.length; i++)
        {
            int sideLocation = initModel.getSideLocation(sideLoc[i]);
            searchInput[SIDE_INDEX[i]] = colorMap.get(cube.getStickerColor(sideLocation, 4));
        }

        return new String(searchInput);
    }

    // cubeString形如：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
    private void setCubeString(String cubeString, Color[] colorSet)
    {
        AbstractCube3DAWT cube = this.player.getCube3D();

        // 初始化颜色对应表，顺序：front, right, down, back, left, up
        final char[] chars = {'F', 'R', 'D', 'B', 'L', 'U', '0'};
        Map<Character, Color> colorMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++)
        {
            colorMap.put(chars[i], colorSet[i]);
        }

        char[] randomChars = cubeString.toCharArray();
        final int[] sideMap = {5, 1, 0, 2, 4, 3}; // 对应Tools.randomCube()的 U R F D L B
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                cube.setStickerColor(sideMap[i], j, colorMap.get(randomChars[i * 9 + j]));
            }
        }
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
    }

    public String getParameter(String key)
    {
        return this.atts.get(key);
    }

    public String getParameter(String key, String default_value)
    {
        String value = getParameter(key);
        return value != null ? value : default_value;
    }

    public boolean getParameter(String key, boolean z)
    {
        String value = getParameter(key);
        return value != null ? value.equals("true") : z;
    }

    public String[] getParameters(String key, String[] default_value)
    {
        String value = getParameter(key);
        if (value == null)
        {
            return default_value;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
        String[] strArr = new String[stringTokenizer.countTokens()];
        for (int i = 0; i < strArr.length; i++)
        {
            strArr[i] = stringTokenizer.nextToken();
        }
        return strArr;
    }

    public int getParameter(String key, int default_value)
    {
        String value = getParameter(key);
        if (value != null)
        {
            try
            {
                return decode(value);
            }
            catch (NumberFormatException e)
            {}
        }
        return default_value;
    }

    public int[] getParameters(String key, int[] default_value)
    {
        String value = getParameter(key);
        if (value != null)
        {
            try
            {
                StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
                int[] iArr = new int[stringTokenizer.countTokens()];
                for (int i = 0; i < iArr.length; i++)
                {
                    iArr[i] = decode(stringTokenizer.nextToken());
                }
                return iArr;
            }
            catch (NumberFormatException e)
            {}
        }
        return default_value;
    }

    public double getParameter(String key, double default_value)
    {
        String value = getParameter(key);
        if (value != null)
        {
            try
            {
                return Double.valueOf(value).doubleValue();
            }
            catch (NumberFormatException e)
            {}
        }
        return default_value;
    }

    public Hashtable<String, Object> getIndexedKeyValueParameters(String key, Hashtable<String, Object> default_value)
    {
        String value = getParameter(key);
        if (value == null)
        {
            return default_value;
        }
        String strKey;
        String strValue;
        Hashtable<String, Object> hashtable = new Hashtable<>();
        StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
        int iCountTokens = stringTokenizer.countTokens();
        for (int i = 0; i < iCountTokens; i++)
        {
            String nextToken = stringTokenizer.nextToken();
            int iIndexOf = nextToken.indexOf('=');
            if (iIndexOf < 1)
            {
                strKey = null;
                strValue = nextToken;
            }
            else
            {
                strKey = nextToken.substring(0, iIndexOf);
                strValue = nextToken.substring(iIndexOf + 1);
            }
            String string = Integer.toString(i);
            if (strKey != null)
            {
                hashtable.put(strKey, strValue);
            }
            if (!hashtable.contains(string))
            {
                hashtable.put(string, strValue);
            }
        }
        return hashtable;
    }

    public static int decode(String str)
        throws NumberFormatException
    {
        int i2 = 0;
        boolean z = false;
        if (str.startsWith("-"))
        {
            z = true;
            i2 = 0 + 1;
        }
        int i = 10;
        if (str.startsWith("0x", i2) || str.startsWith("0X", i2))
        {
            i2 += 2;
            i = 16;
        }
        else if (str.startsWith("#", i2))
        {
            i2++;
            i = 16;
        }
        else if (str.startsWith("0", i2) && str.length() > 1 + i2)
        {
            i2++;
            i = 8;
        }
        if (str.startsWith("-", i2))
        {
            throw new NumberFormatException("Negative sign in wrong position");
        }
        int numValueOf;
        try
        {
            numValueOf = Integer.valueOf(str.substring(i2), i);
            numValueOf = z ? -numValueOf : numValueOf;
        }
        catch (NumberFormatException e)
        {
            numValueOf = Integer.valueOf(
                z ? new String(new StringBuffer().append("-").append(str.substring(i2)).toString()) : str.substring(i2),
                i);
        }
        return numValueOf;
    }

    public void setParameter(String key, String value)
        throws IOException
    {
        this.atts.put(key, value);
        if (this.initialized)
        {
            doParameter(key, value);
        }
    }

    private void doParameter(String key)
        throws IOException
    {
        doParameter(key, getParameter(key, ""));
    }

    private void doParameter(String key, String value)
        throws IOException
    {
        // 运行中修改配置
        int index = this.keyMap.getOrDefault(key, -1);
        switch (index)
        {
            case 0: // "autoPlay"
                if ("true".equals(value))
                {
                    this.autoPlay = true;
                    this.player.start();
                }
                else
                {
                    this.autoPlay = false;
                    this.player.stop();
                }
                break;
            case 1: // "script"
                if (this.player.isActive())
                {
                    this.player.reset();
                }
                value = value.replace("\\n", "\n");
                ScriptNode scriptNode = this.scriptParser.parse(new StringReader(value));
                this.scriptTextArea.setText(value);
                this.player.setScript(scriptNode);
                if (this.autoPlay)
                {
                    this.player.start();
                }
                break;
            case 5: // "initScript"
                if (value != null)
                {
                    value = value.replace("\\n", "\n");
                    this.scriptParser.parse(new StringReader(value)).applySubtreeTo(this.initCube, false);
                    this.player.reset();
                }
                break;
            case 8: // "stickers"
                AbstractCube3DAWT cube = this.player.getCube3D();
                String[] parameters2 = getParameters(key, (String[])null);
                if (parameters2 != null)
                {
                    if (parameters2.length != 54)
                    {
                        throw new IllegalArgumentException(
                            new StringBuffer().append("Invalid parameter 'stickers' provides ").append(
                                parameters2.length).append(" instead of 54 entries.").toString());
                    }
                    int i = 0;
                    for (int i5 = 0; i5 < 6; i5++)
                    {
                        for (int i6 = 0; i6 < 9; i6++)
                        {
                            int param = Integer.parseInt(parameters2[i++]);
                            if (this.colors.length <= param)
                            {
                                throw new IllegalArgumentException(
                                    new StringBuffer().append("Invalid parameter 'stickers', unknown entry '").append(
                                        param).append("'.").toString());
                            }
                            cube.setStickerColor(i5, i6, this.colors[param]);
                        }
                    }
                    this.player.reset();
                }
                break;
            case 15: // "rearView"
                if ("true".equals(value))
                {
                    initRearComponent();
                    add("Center", this.rearComponent);
                    remove(this.player.getVisualComponent());
                    validate();
                }
                else
                {
                    Canvas3DAWT component = (Canvas3DAWT)this.player.getVisualComponent();
                    component.setScaleFactor(component.getScaleFactor());
                    add("Center", component);
                    if (this.rearComponent != null)
                    {
                        remove(this.rearComponent);
                    }
                    validate();
                }
                break;

            /** 以下不支持运行过程中修改 */
            case 2: // "scriptLanguage"
            case 3: // "scriptType"
            case 4: // "scriptProgress"
            case 6: // "displayLines"
            case 7: // "faces"
            case 9: // "stickersFront"
            case 10: // "stickersRight"
            case 11: // "stickersDown"
            case 12: // "stickersBack"
            case 13: // "stickersLeft"
            case 14: // "stickersUp"
            case 16: // "rearViewBackgroundColor"
            case 17: // "rearViewBackgroundImage"
            case 18: // "rearViewScaleFactor"
            case 19: // "rearViewRotation"
            case 20: // "alpha"
            case 21: // "beta"
            case 22: // "backgroundColor"
            case 23: // "backgroundImage"
            case 24: // "colorTable"
            case 25: // "ambientLightIntensity"
            case 26: // "lightSourceIntensity"
            case 27: // "lightSourcePosition"
            default:
                throw new IllegalArgumentException(
                    new StringBuffer().append("Invalid parameter ").append(key).append(", value ").append(value).append(
                        " is illegal.").toString());
//                break;
        }

    }
}
