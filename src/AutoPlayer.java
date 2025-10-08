
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import ch.min2phase.Search;
import ch.min2phase.Tools;
import ch.randelshofer.cmd.CommandParser;
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

public class AutoPlayer extends Panel implements Runnable {
    private static final long serialVersionUID = -698774308591767978L;

    private static final char sevenChar = '0';

    private ScriptPlayer player;

    private MultilineLabel scriptTextArea;

    private Panel controlsPanel;

    private ArrayList<Color> colors;

    private Panel rearComponent = null;

    private RubiksCubeCore initCube;

    private CommandParser cmd;

    private Hashtable<URL, Image> imageCache = new Hashtable<>();

    private Map<String, Integer> keyMap = new HashMap<>();

    private ScriptParser scriptParser;

    private boolean initialized = false;

    private boolean isSolver;

    private boolean autoPlay = true;

    private int selectColor = -1;

    private Search search = new Search();

    public static void main(String[] args) throws IOException {
        AutoPlayer scriptPlayer = new AutoPlayer();

        // 解析命令行参数
        scriptPlayer.getCmd().parse(args);
        // 启动
        scriptPlayer.init();

        // 等待自动执行完成
        while (scriptPlayer.getPlayer().isActive()) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
        }
    }

    public AutoPlayer() {
        this.cmd = new CommandParser();

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

    public void init() {
        initComponents();
        initCube();
        initGUI();
        PooledSequentialDispatcherAWT.dispatchConcurrently(this);
        while (!this.initialized) // 等待启动完成
        {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
    }

    private void initCube() {
        this.player = new ScriptPlayer() {
            @Override
            public void reset() {
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
        this.scriptTextArea.setFont(new Font("Dialog", Font.BOLD, 16));
        this.scriptTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                AutoPlayer.this.player.moveToCaret(AutoPlayer.this.scriptTextArea.viewToModel(mouseEvent.getX(), mouseEvent.getY()));
            }
        });
        this.scriptTextArea.setSize(getSize());

        try {
            readParameters();
            ChangeListener changeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent changeEvent) {
                    // 刷新步骤序列
                    AutoPlayer.this.selectCurrentSymbol();
                }
            };
            this.player.getBoundedRangeModel().addChangeListener(changeListener);
            this.player.addChangeListener(changeListener);
            synchronized (getTreeLock()) {
                add("South", this.controlsPanel);
                validate();
                this.controlsPanel.invalidate();
                validate();
            }
        } catch (Throwable e) {
            removeAll();
            setLayout(new BorderLayout());
            TextArea textArea = new TextArea(30, 40);
            add("South", textArea);

            String errString = AutoPlayer.getString(e);
            System.err.println(errString);
            textArea.setText(CommandParser.getAppInfo() + "\n\n" + errString);

            invalidate();
            validate();
        }
    }

    private static String getString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    // 默认绘图函数，魔方未加载、加载中或失败时会显示的内容
    @Override
    public void paint(Graphics graphics) {
        graphics.setFont(new Font("Dialog", 0, 10));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.drawString("Loading " + CommandParser.getAppInfo(), 12, fontMetrics.getHeight());
        // graphics.drawString(CommandParser.copyright, 12, fontMetrics.getHeight() * 2);
    }

    @Override
    public void run() {
        try {
            doParameter("autoPlay");
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialized = true;
    }

    public void stop() {
        if (this.player != null) {
            this.player.stop();
        }
    }

    /** 更新选择的步骤状态 */
    protected void selectCurrentSymbol() {
        int startPosition;
        int endPosition;
        ScriptNode currentSymbol = this.player.getCurrentSymbol();
        if (currentSymbol == null) {
            startPosition = endPosition = this.scriptTextArea.getText().length();
        } else {
            startPosition = currentSymbol.getStartPosition();
            endPosition = currentSymbol.getEndPosition() + 1;
        }
        Color backColor = this.player.isProcessingCurrentSymbol() ? MultilineLabel.activeSelectionBackground : MultilineLabel.inactiveSelectionBackground;
        this.scriptTextArea.select(startPosition, endPosition);
        this.scriptTextArea.setSelectionBackground(backColor);
    }

    private void readParameters() throws IllegalArgumentException {
        AbstractCube3DAWT cube = this.player.getCube3D();
        Color colorBack = new Color(this.cmd.getParameter("backgroundColor", 0xeeeeee));
        this.player.getControlPanelComponent().setBackground(colorBack);
        this.controlsPanel.setBackground(colorBack);
        Transform3D transform3D = new Transform3D();
        transform3D.rotateY((this.cmd.getParameter("beta", 45) / 180.0d) * Math.PI);
        transform3D.rotateX((this.cmd.getParameter("alpha", -25) / 180.0d) * Math.PI);
        this.player.setTransform(transform3D);

        // 设置各面颜色, 顺序：正面, 右面, 底面, 背面, 左面, 顶面
        String[] dflt = {"0x8c000f", "0xffd200", "0x00732f", "0xff4600", "0xf8f8f8", "0x003373", "0x707070"};
        String[] colors_str = this.cmd.getParameters("colorTable", dflt);
        if (colors_str.length < 6) {
            showError("Invalid parameter 'colorTable', must have 6 entries");
        }
        this.colors = new ArrayList<>(dflt.length);
        int colorIndex = 0;
        for (; colorIndex < colors_str.length; colorIndex++) {
            try {
                Color c = new Color(CommandParser.decode(colors_str[colorIndex]));
                if (!this.colors.contains(c)) {
                    this.colors.add(colorIndex, c);
                    continue;
                }
            } catch (NumberFormatException e) {
                showError(new StringBuffer().append("Invalid parameter 'colorTable', value ").append(Arrays.toString(colors_str)).append(
                        " is illegal.\n").append(AutoPlayer.getString(e)).toString());
            }
            // 设置参数异常时使用默认值
            Color c = new Color(CommandParser.decode(dflt[colorIndex]));
            if (this.colors.contains(c)) {
                throw new IllegalArgumentException(new StringBuffer().append("Invalid parameter 'colorTable' value ").append(Arrays.toString(
                        colors_str)).append(" is illegal.").toString());
            }
            this.colors.add(colorIndex, c);
        }
        // 补充未设置的颜色
        for (; colorIndex < dflt.length; colorIndex++) {
            Color c = new Color(CommandParser.decode(dflt[colorIndex]));
            if (this.colors.contains(c)) {
                throw new IllegalArgumentException(new StringBuffer().append("Invalid parameter 'colorTable' value ").append(Arrays.toString(
                        colors_str)).append(" is illegal.").toString());
            }
            this.colors.add(colorIndex, c);
        }
        // 初始化颜色
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube.setStickerColor(i, j, this.colors.get(i));
            }
        }

        // 设置用户自定义颜色
        setCustomColors();

        String language = this.cmd.getParameter("scriptLanguage");
        if (language == null || language.equalsIgnoreCase("BandelowENG")) {
            this.scriptParser = new BandelowENGParser();
        } else if (language.equalsIgnoreCase("RandelshoferGER")) {
            this.scriptParser = new RandelshoferGERParser();
        } else if (language.equalsIgnoreCase("ScriptFRA")) {
            this.scriptParser = new ScriptFRAParser();
        } else if (language.equalsIgnoreCase("SupersetENG")) {
            this.scriptParser = new SupersetENGParser();
        } else if (language.equalsIgnoreCase("HarrisENG")) {
            this.scriptParser = new HarrisENGParser();
        } else if (language.equalsIgnoreCase("TouchardDeledicqFRA")) {
            this.scriptParser = new TouchardDeledicqFRAParser();
        } else if (language.equalsIgnoreCase("Castella")) {
            this.scriptParser = new CastellaParser();
        } else {
            showError("Invalid parameter 'scriptLanguage': Unsupported language " + language);
            this.scriptParser = new BandelowENGParser();
        }

        String scriptType = this.cmd.getParameter("scriptType", "Generator");
        if (scriptType.equalsIgnoreCase("Solver")) {
            this.isSolver = true;
        } else if (scriptType.equalsIgnoreCase("Generator")) {
            this.isSolver = false;
        } else {
            showError("Invalid parameter 'scriptType': Unsupported type " + scriptType);
            this.isSolver = false;
        }

        String script = this.cmd.getParameter("script", "");
        script = script.replace("\\n", "\n");
        try {
            ScriptNode scriptNode = scriptParser.parse(new StringReader(script));
            this.scriptTextArea.setText(script);
            this.player.setScript(scriptNode);
        } catch (Exception e) {
            this.scriptTextArea.setText(null);
            this.player.setScript(null);
            showError("Invalid parameter 'script'\n" + AutoPlayer.getString(e));
        }

        this.initCube.reset();
        String initScript = this.cmd.getParameter("initScript");
        if (initScript != null) {
            initScript = initScript.replace("\\n", "\n");
            try {
                scriptParser.parse(new StringReader(initScript)).applySubtreeTo(this.initCube, false);
            } catch (Exception e) {
                showError("Invalid parameter 'initScript'\n" + AutoPlayer.getString(e));
            }
        }

        if (this.isSolver && this.player.getScript() != null) {
            this.player.getScript().applySubtreeTo(this.initCube, true);
        }
        this.player.reset();
        try {
            int scriptProgress = this.cmd.getParameter("scriptProgress", (this.isSolver || this.cmd.getParameter("autoPlay", true)) ? 0 : -1);
            if (scriptProgress < 0) {
                scriptProgress += this.player.getBoundedRangeModel().getMaximum();
            }
            this.player.getBoundedRangeModel().setValue(scriptProgress);
        } catch (IndexOutOfBoundsException e) {
            showError("Invalid parameter 'scriptProgress'\n" + AutoPlayer.getString(e));
        }

        String displayLines = this.cmd.getParameter("displayLines", "1");
        int iCountTokens = script == null ? 1 : new StringTokenizer(script, "\n").countTokens();
        try {
            iCountTokens = Math.max(Integer.parseInt(displayLines), iCountTokens);
        } catch (NumberFormatException e) {
            showError("Invalid parameter 'displayLines'\n" + AutoPlayer.getString(e));
        }
        if (iCountTokens <= 0) {
            this.scriptTextArea.setVisible(false);
        } else {
            try {
                this.scriptTextArea.setMinRows(iCountTokens);
            } catch (NoSuchMethodError e5) {
            }
        }

        // 配置魔方视图
        Canvas3DAWT visualComponent = (Canvas3DAWT) this.player.getVisualComponent();
        visualComponent.setBackground(colorBack);
        visualComponent.setLightSourceIntensity(this.cmd.getParameter("lightSourceIntensity", 1.0d));
        visualComponent.setAmbientLightIntensity(this.cmd.getParameter("ambientLightIntensity", 0.6d));
        visualComponent.setPreferredSize(new Dimension(1, 1));
        int[] lightSource = this.cmd.getParameters("lightSourcePosition", new int[]{-500, 500, 1000});
        if (lightSource.length != 3) {
            showError("Invalid parameter 'lightSourcePosition' provides " + lightSource.length + " instead of 3 entries.");
        }
        visualComponent.setLightSource(new Point3D(lightSource[0], lightSource[1], lightSource[2]));
        String backgroundImage = this.cmd.getParameter("backgroundImage");
        if (backgroundImage != null) {
            try {
                File imageFile = new File(backgroundImage);
                if (!imageFile.exists()) {
                    imageFile = new File(System.getProperty("user.dir"), backgroundImage);
                }
                if (imageFile.exists()) {
                    URL url = imageFile.toURI().toURL();
                    visualComponent.setBackgroundImage(getImage(url));
                }
            } catch (MalformedURLException e) {
                showError("Invalid parameter 'backgroundImage' malformed URL: " + backgroundImage + "\n" + AutoPlayer.getString(e));
            }
        }

        // 配置魔方后视图
        Component panelComponent;
        if ("false".equalsIgnoreCase(this.cmd.getParameter("rearView", "true"))) {
            panelComponent = visualComponent; // 不包含后视图
        } else {
            initRearComponent();
            panelComponent = this.rearComponent;
        }
        add("Center", panelComponent);
    }

    // 设置用户自定义颜色
    private void setCustomColors() {
        AbstractCube3DAWT cube = this.player.getCube3D();

        // 按面自定义颜色，顺序为正面 右面 底面 背面 左面 顶面，形如0,1,2,3,4,5
        String[] faceColors = this.cmd.getParameters("faces", (String[]) null);
        if (faceColors != null) {
            if (faceColors.length == 6) {
                for (int i = 0; i < 6; i++) {
                    int entry = Integer.parseInt(faceColors[i]);
                    if (this.colors.size() <= entry) {
                        showError("Invalid parameter 'faces', entry " + faceColors[i] + " > " + (this.colors.size() - 1));
                    } else {
                        Color color = this.colors.get(entry);
                        for (int j = 0; j < 9; j++) {
                            cube.setStickerColor(i, j, color);
                        }
                    }
                }
            } else {
                showError("Invalid parameter 'faces' provides " + faceColors.length + " instead of 6 entries.");
            }
        }

        // 按块自定义颜色，顺序为前右下后左上，共54个数字，0~5各需出现9次，例如
        // 0,0,0,0,0,0,0,0,0, 1,1,1,1,1,1,1,1,1, 2,2,2,2,2,2,2,2,2, 3,3,3,3,3,3,3,3,3, 4,4,4,4,4,4,4,4,4, 5,5,5,5,5,5,5,5,5
        String[] stickerColors = this.cmd.getParameters("stickers", (String[]) null);
        if (stickerColors != null) {
            if (stickerColors.length == 54) {
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 9; j++) {
                        try {
                            int entry = Integer.parseInt(stickerColors[i * 9 + j]);
                            if (this.colors.size() <= entry) {
                                showError("'stickers', entry " + entry + " > " + (this.colors.size() - 1));
                            } else {
                                cube.setStickerColor(i, j, this.colors.get(entry));
                            }
                        } catch (NumberFormatException e) {
                            showError("'stickers', entry " + stickerColors[i * 9 + j] + " not digit");
                        }
                    }
                }
            } else {
                showError("Invalid parameter 'stickers' provides " + stickerColors.length + " instead of 54 entries.");
            }
        }

        // 标准记号法定义颜色：顺序是：上面U 右面R 前面F 下面D 左面L 后面B
        // 例如 UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
        String facelets = this.cmd.getParameter("facelets");
        if (facelets != null) {
            if (facelets.trim().length() == 54) {
                setCubeByString(facelets, this.colors);
            } else {
                showError("Invalid parameter 'facelets' provides " + facelets.trim().length() + " instead of 54.");
            }
        }

        // 按单个面自定义每块颜色，类似stickers，每个参数只需要9个数字，形如0,0,0,0,0,0,0,0,0
        String[] strArr = {"stickersFront", "stickersRight", "stickersDown", "stickersBack", "stickersLeft", "stickersUp"};
        for (int i = 0; i < strArr.length; i++) {
            String[] colorLists = this.cmd.getParameters(strArr[i], (String[]) null);
            if (colorLists != null) {
                if (colorLists.length == 9) {
                    for (int j = 0; j < 9; j++) {
                        int entry = Integer.parseInt(colorLists[j]);
                        if (this.colors.size() <= entry) {
                            showError(new StringBuffer().append("Invalid parameter '").append(strArr[i]).append("', unknown entry '").append(
                                    colorLists[j]).append("'.").toString());
                            break;
                        } else {
                            cube.setStickerColor(i, j, this.colors.get(entry));
                        }
                    }
                } else {
                    showError(new StringBuffer().append("Invalid parameter '").append(strArr[i]).append("' provides ").append(colorLists.length).append(
                            " instead of 9 entries.").toString());
                }
            }
        }
    }

    private void initRearComponent() {
        if (this.rearComponent != null) {
            double fMax = Math.max(0.1d, Math.min(1.0d, this.cmd.getParameter("rearViewScaleFactor", 0.75d)));
            this.rearComponent.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
            return;
        }

        Canvas3DAWT visualComponent = (Canvas3DAWT) this.player.getVisualComponent();
        Canvas3DAWT rearCanvas3D = Canvas3DJ2D.createCanvas3D(); // 创建后视图
        rearCanvas3D.setScene(this.player.getCube3D().getScene());
        rearCanvas3D.setSyncObject(this.player.getCube3D().getModel());
        int[] rearViewRotation = this.cmd.getParameters("rearViewRotation", new int[]{180, 0, 0});
        if (rearViewRotation.length != 3) {
            showError("Invalid parameter 'rearViewRotation' provides " + rearViewRotation.length + " instead of 3 values.");
        }
        rearCanvas3D.setTransformModel(new RotatedTransform3DModel((rearViewRotation[0] / 180.0d) * Math.PI, (rearViewRotation[1] / 180.0d) * Math.PI,
                (rearViewRotation[2] / 180.0d) * Math.PI, visualComponent.getTransformModel()));
        double fMax = Math.max(0.1d, Math.min(1.0d, this.cmd.getParameter("rearViewScaleFactor", 0.75d)));
        rearCanvas3D.setScaleFactor(visualComponent.getScaleFactor() * fMax);
        rearCanvas3D.setPreferredSize(visualComponent.getPreferredSize());

        rearCanvas3D.setBackground(new Color(this.cmd.getParameter("rearViewBackgroundColor", this.cmd.getParameter("backgroundColor", 0xeeeeee))));
        String rearImage = this.cmd.getParameter("rearViewBackgroundImage", this.cmd.getParameter("backgroundImage"));
        if (rearImage != null) {
            try {
                File imageFile = new File(rearImage);
                if (!imageFile.exists()) {
                    imageFile = new File(System.getProperty("user.dir"), rearImage);
                }
                if (imageFile.exists()) {
                    URL url = imageFile.toURI().toURL();
                    rearCanvas3D.setBackgroundImage(getImage(url));
                }
            } catch (MalformedURLException e) {
                showError("Invalid parameter 'backgroundImage' malformed URL: " + rearImage + "\n" + AutoPlayer.getString(e));
            }
        }
        rearCanvas3D.setLightSourceIntensity(this.cmd.getParameter("lightSourceIntensity", 1.0d));
        rearCanvas3D.setAmbientLightIntensity(this.cmd.getParameter("ambientLightIntensity", 0.6d));
        int[] lightSource = this.cmd.getParameters("lightSourcePosition", new int[]{-500, 500, 1000});
        if (lightSource.length != 3) {
            showError("Invalid parameter 'lightSourcePosition' provides " + lightSource.length + " instead of 3 entries.");
        }
        rearCanvas3D.setLightSource(new Point3D(lightSource[0], lightSource[1], lightSource[2]));

        this.player.getCube3D().addChangeListener(rearCanvas3D);
        Panel panel = new Panel();
        panel.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
        panel.add(visualComponent);
        panel.add(rearCanvas3D);
        this.rearComponent = panel;
    }

    public Image getImage(URL url) {
        Image img = imageCache.get(url);
        if (img != null) {
            return img;
        }
        try {
            Object o = url.getContent();
            if (o == null) {
                return null;
            }
            if (o instanceof Image) {
                img = (Image) o;
                imageCache.put(url, img);
                return img;
            }
            // Otherwise it must be an ImageProducer.
            img = this.createImage((ImageProducer) o);
            imageCache.put(url, img);
            return img;

        } catch (Exception ex) {
            return null;
        }

    }

    private void showError(String message) {
        System.out.println(message);
    }

    public static String getErrMessage(String result) {
        switch (result.charAt(result.length() - 1)) {
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
        return result;
    }

    private void initGUI() {
        final JFrame frame = new JFrame("AutoPlayer"); // 初始化画布
        frame.setTitle("三阶魔方求解器 by Deng");
        frame.setSize(600, 600); // 设置画布大小
        frame.setPreferredSize(new java.awt.Dimension(600, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() // 添加退出事件
        {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        final JButton[] colorSel = new JButton[6];
        // 顺序：正面红色, 右面黄色, 底面绿色, 背面橙色, 左面白色, 顶面蓝色
        final Color[] initColors = {new Color(230, 0, 0), new Color(240, 220, 0), // 红 黄
                new Color(0, 170, 0), new Color(255, 118, 0), Color.white, Color.blue}; // 绿 橙
        final Border defaultBorder = new LineBorder(new Color(240, 240, 240), 4);
        final Border selectBorder = new LineBorder(Color.black, 4);
        Font defaultFont = new Font("Dialog", Font.BOLD, 14);
        for (int i = 0; i < 6; i++) {
            colorSel[i] = new JButton();
            frame.add(colorSel[i]);
            colorSel[i].setBackground(initColors[i]);
            colorSel[i].setOpaque(true);
            colorSel[i].setBounds(24 + 36 * i, 24, 32, 32);
            colorSel[i].setBorderPainted(true);
            colorSel[i].setBorder(defaultBorder);
            colorSel[i].setName(String.valueOf(i));
            final int value = i;
            colorSel[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (AutoPlayer.this.selectColor != value) {
                        if (AutoPlayer.this.selectColor != -1) {
                            colorSel[AutoPlayer.this.selectColor].setBorder(defaultBorder);
                        }
                        colorSel[value].setBorder(selectBorder);
                        AutoPlayer.this.selectColor = value;
                        AutoPlayer.this.player.getCube3D().setSelectColor(AutoPlayer.this.colors.get(value));
                    }
                }
            });
        }

        // 编辑按钮
        final JButton buttonEdit = new JButton("edit");
        frame.add(buttonEdit);
        buttonEdit.setBounds(250, 20, 65, 40);
        buttonEdit.setFont(defaultFont);
        buttonEdit.setText("编辑");
        buttonEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    return;
                }

                // 判断魔方是否有旋转，因为编辑功能是基于魔方未旋转状态，如果有旋转，设置方块颜色时会错位
                if (!AutoPlayer.this.player.getCube3D().getModel().isSolved()) {
                    // 重置魔方状态，保留块的颜色和顺序
                    String facelets = getCubeString();
                    AutoPlayer.this.cleanAndResetCube(facelets);
                }

                if (AutoPlayer.this.scriptTextArea.getText().length() > 0) {
                    // 重置步骤为空
                    AutoPlayer.this.scriptTextArea.setText(null);
                    AutoPlayer.this.player.setScript(null);
                }

                if (AutoPlayer.this.player.getCube3D().isEditMode()) {
                    ((JButton) evt.getSource()).setBackground(new ColorUIResource(238, 238, 238));
                    AutoPlayer.this.player.getCube3D().setEditMode(false);
                } else {
                    ((JButton) evt.getSource()).setBackground(new Color(184, 207, 229));
                    AutoPlayer.this.player.getCube3D().setEditMode(true);
                    if (AutoPlayer.this.selectColor == -1) {
                        AutoPlayer.this.selectColor = 0;
                        AutoPlayer.this.player.getCube3D().setSelectColor(AutoPlayer.this.colors.get(AutoPlayer.this.selectColor));
                        colorSel[AutoPlayer.this.selectColor].setBorder(selectBorder);
                    }
                }
            }
        });

        // 清空按钮
        final JButton buttonClean = new JButton("clean");
        frame.add(buttonClean);
        buttonClean.setBounds(325, 20, 65, 40);
        buttonClean.setFont(defaultFont);
        buttonClean.setText("清空");
        buttonClean.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    return;
                }
                AutoPlayer.this.player.getCube3D().getModel().reset();

                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 9; j++) {
                        AutoPlayer.this.player.getCube3D().setStickerColor(i, j, AutoPlayer.this.colors.get(6));
                    }
                }
            }
        });

        // 校验按钮
        final JButton buttonCheck = new JButton("check");
        frame.add(buttonCheck);
        buttonCheck.setBounds(420, 20, 65, 40);
        buttonCheck.setFont(defaultFont);
        buttonCheck.setText("校验");
        buttonCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    return;
                }

                String cubeString = getCubeString();
                String result = searchSolution(cubeString);
                if (result.contains("Error")) {
                    String message = "校验不通过：" + AutoPlayer.getErrMessage(result);
                    JOptionPane.showMessageDialog(AutoPlayer.this, message, "失败", JOptionPane.ERROR_MESSAGE);

                } else {
                    String message = "校验通过。";
                    JOptionPane.showMessageDialog(AutoPlayer.this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // 打乱按钮
        final JButton buttonRandom = new JButton("random");
        frame.add(buttonRandom);
        buttonRandom.setBounds(495, 20, 65, 40);
        buttonRandom.setFont(defaultFont);
        buttonRandom.setText("打乱");
        buttonRandom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    return;
                }
                AutoPlayer.this.player.getCube3D().getModel().reset();

                // Random stick by Call Random function
                String facelets = Tools.randomCube();
                setCubeByString(facelets, AutoPlayer.this.colors);
            }
        });

        // 反序按钮
        final JButton buttonSolver = new JButton("Solver");
        frame.add(buttonSolver);
        buttonSolver.setBounds(420, 70, 65, 40);
        buttonSolver.setFont(defaultFont);
        buttonSolver.setText("反序");
        buttonSolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    return;
                }
                String script = AutoPlayer.this.scriptTextArea.getText();
                if (script == null || script.length() == 0) {
                    return;
                }

                int index = script.indexOf('(');
                if (index > 0) {
                    script = script.substring(0, script.indexOf('(') - 1);
                }

                StringTokenizer stringTokenizer = new StringTokenizer(script, " \n");
                String[] tokens = new String[stringTokenizer.countTokens()];
                for (int i = tokens.length - 1; i >= 0; i--) {
                    String tmp = stringTokenizer.nextToken();
                    if (tmp.length() <= 1) {
                        tokens[i] = tmp + '\'';
                    } else if (tmp.charAt(1) == '\'') {
                        tokens[i] = Character.toString(tmp.charAt(0));
                    } else {
                        tokens[i] = tmp;
                    }
                }

                String newScript = String.join(" ", tokens);
                try {
                    ScriptNode scriptNode = AutoPlayer.this.scriptParser.parse(new StringReader(newScript));

                    AutoPlayer.this.player.getCube3D().getModel().reset();
                    String facelets = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB";
                    setCubeByString(facelets, AutoPlayer.this.colors);

                    AutoPlayer.this.scriptTextArea.setText(newScript);
                    AutoPlayer.this.player.setScript(scriptNode);

                    if (AutoPlayer.this.autoPlay) {
                        AutoPlayer.this.player.start();
                    }
                } catch (IOException e) {
                    return;
                }
            }
        });

        // 求解按钮
        final JButton buttonSolution = new JButton("solution");
        frame.add(buttonSolution);
        buttonSolution.setBounds(495, 70, 65, 40);
        buttonSolution.setFont(defaultFont);
        buttonSolution.setText("求解");
        buttonSolution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive()) {
                    // 正在执行中
                    return;
                }

                String facelets = getCubeString();
                // 有旋转，重置为旋转前状态
                if (!AutoPlayer.this.player.getCube3D().getModel().isSolved()) {
                    AutoPlayer.this.cleanAndResetCube(facelets);
                }

                String result = searchSolution(facelets);
                if (result.contains("Error")) {
                    String message = "校验不通过：" + AutoPlayer.getErrMessage(result);
                    JOptionPane.showMessageDialog(AutoPlayer.this, message, "失败", JOptionPane.ERROR_MESSAGE);
                } else {
                    // 取消编辑
                    if (AutoPlayer.this.player.getCube3D().isEditMode()) {
                        buttonEdit.setBackground(new ColorUIResource(238, 238, 238));
                        AutoPlayer.this.player.getCube3D().setEditMode(false);
                    }

                    // 自动计算复位方法
                    try {
                        AutoPlayer.this.cmd.setParameter("script", result);
                        doParameter("script", result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 调整窗口大小
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = frame.getWidth();
                if (width < 580) {
                    width = 580;
                }
                buttonCheck.setLocation(width - 180, 20); // 校验
                buttonRandom.setLocation(width - 105, 20); // 打乱
                buttonSolver.setLocation(width - 180, 70); // 反序
                buttonSolution.setLocation(width - 105, 70); // 求解
                frame.setVisible(true); // 刷新
            }
        });

        Panel panelback = new Panel();
        frame.add(panelback);
        panelback.setBackground(Color.lightGray);
        panelback.setBounds(20, 20, 220, 40);

        // 添加魔方
        frame.add(this, "Center");

        // 显示
        frame.setVisible(true);
    }

    // 清除魔方旋转记录并重置魔方状态
    // 用于编辑和自动复原功能，这两个是基于魔方未旋转状态，如果有旋转，设置和获取方块颜色时会错位
    private void cleanAndResetCube(String facelets) {
        ArrayList<Color> colorCurrent = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            // 以中心块的颜色为基准
            Color c = this.player.getCube3D().getStickerColor(i, 4);
            if (colorCurrent.contains(c)) {
                break;
            }
            colorCurrent.add(i, c);
        }

        // 只有6个面都有颜色时才执行重置
        if (colorCurrent.size() == 6) {
            colorCurrent.add(6, this.colors.get(6));
            // 重置魔方状态，保留块的颜色和顺序
            this.player.getCube3D().getModel().reset();
            setCubeByString(facelets, colorCurrent);
        }
    }

    public String searchSolution(String cubeString) {
        if (cubeString.contains("Error")) {
            return cubeString;
        }
        System.out.println("input: " + cubeString);

        if (!Search.isInited()) {
            Search.init();
        }

        int mask = 0;
        int depth = 18; // 建议 Step: 15 ~ 18
        int maxDepth = 25;
        int maxTries = 200; // 建议 200 ~ 1000
        String result = "Error 8";
        char errkey = '8';
        while ((errkey == '8' && depth <= maxDepth) || errkey == '7') {
            result = this.search.solution(cubeString, depth, 100, 0, mask);
            errkey = result.length() > 0 ? result.charAt(result.length() - 1) : '0';
            int tries = maxTries;
            while (errkey == '8' && tries > 0) {
                result = this.search.next(100, 0, mask);
                errkey = result.charAt(result.length() - 1);
                tries--;
            }
            System.out.println("depth:" + depth + ", result: " + result);
            depth++;
        }
        return result;
    }

    // 初始状态应该获取到的序列为：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
    public String getCubeString() {
        AbstractCube3DAWT cube = this.player.getCube3D();
        RubiksCubeCore model = cube.getModel();
        // 初始化颜色对应表，顺序：front, right, down, back, left, up
        final char[] chars = {'F', 'R', 'D', 'B', 'L', 'U'};
        Map<Color, Character> colorMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            // 以中心块的颜色为基准
            colorMap.put(cube.getStickerColor(i, 4), chars[i]);
        }
        if (colorMap.size() < 6) {
            return "Error 9";
        }
        if (!colorMap.containsKey(this.colors.get(6))) {
            colorMap.put(this.colors.get(6), sevenChar);
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
        final int[][] cornerFacelet = {{6, 18, 38}, {27, 44, 24}, {8, 9, 20}, {29, 26, 15}, {2, 45, 11}, {35, 17, 51}, {0, 36, 47}, {33, 53, 42}};
        final int[][] edgeFacelet = {{19, 7}, {41, 21}, {25, 28}, {5, 10}, {12, 23}, {32, 16}, {46, 1}, {14, 48}, {52, 34}, {3, 37}, {39, 50}, {30, 43}};
        final int[] sideFacelet = {22, 13, 31, 49, 40, 4};
        RubiksCubeCore initModel = new RubiksCubeCore();

        // 从RubiksCubeCore中根据旋转情况计算每个块的实际位置
        char[] searchInput = new char[54];
        int[] cornerLoc = model.getCornerLocations();
        int[] cornerOrient = model.getCornerOrientations();
        for (int i = 0; i < cornerLoc.length; i++) {
            for (int j = 0; j < 3; j++) {
                int cornerSide = initModel.getCornerSide(cornerLoc[i], (cornerOrient[i] + j) % 3);
                int mapindex = (cornerSide == 2 || cornerSide == 5) ? (cornerLoc[i] / 2) : (cornerLoc[i] % 4);
                int cornerIndex = CORNER_MAP[cornerSide][mapindex];
                searchInput[cornerFacelet[i][j]] = colorMap.get(cube.getStickerColor(cornerSide, cornerIndex));
            }
        }

        int[] edgeLoc = model.getEdgeLocations();
        int[] edgeOrient = model.getEdgeOrientations();
        for (int i = 0; i < edgeLoc.length; i++) {
            for (int j = 0; j < 2; j++) {
                int edgeSide = initModel.getEdgeSide(edgeLoc[i], (edgeOrient[i] + j) % 2);
                int edgeIndex = EDGE_MAP[edgeSide][edgeLoc[i]];
                searchInput[edgeFacelet[i][j]] = colorMap.get(cube.getStickerColor(edgeSide, edgeIndex));
            }
        }

        int[] sideLoc = model.getSideLocations();
        for (int i = 0; i < sideLoc.length; i++) {
            int sideLocation = initModel.getSideLocation(sideLoc[i]);
            searchInput[sideFacelet[i]] = colorMap.get(cube.getStickerColor(sideLocation, 4));
        }

        return new String(searchInput);
    }

    // cubeString形如：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
    public void setCubeByString(String cubeString, ArrayList<Color> curColors) {
        // 初始化颜色对应表，顺序：front, right, down, back, left, up
        final char[] chars = {'F', 'R', 'D', 'B', 'L', 'U', sevenChar};
        Map<Character, Color> colorMap = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            colorMap.put(chars[i], curColors.get(i));
        }

        char[] randomChars = cubeString.toCharArray();
        AbstractCube3DAWT cube = this.player.getCube3D();
        final int[] sideMap = {5, 1, 0, 2, 4, 3}; // 对应Tools.randomCube()得到的 U R F D L B
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                char index = randomChars[i * 9 + j];
                if (colorMap.containsKey(index)) {
                    cube.setStickerColor(sideMap[i], j, colorMap.get(index));
                } else {
                    showError("'setCubeByString' error, key: " + index);
                }
            }
        }
    }

    public void doParameter(String key) throws IOException {
        doParameter(key, this.cmd.getParameter(key, ""));
    }

    private void doParameter(String key, String value) throws IOException {
        // 运行中修改配置
        int index = this.keyMap.getOrDefault(key, -1);
        switch (index) {
        case 0: // "autoPlay"
            // 默认true
            if ("false".equalsIgnoreCase(value)) {
                this.autoPlay = false;
                this.player.stop();
            } else {
                this.autoPlay = true;
                this.player.start();
            }
            break;
        case 1: // "script"
            if (this.player.isActive()) {
                this.player.reset();
            }
            value = value.replace("\\n", "\n");
            if (value.length() == 0) {
                this.scriptTextArea.setText(value);
            } else {
                this.scriptTextArea.setText(value + " (Step: " + (value.length() + 2) / 3 + ")");
            }
            ScriptNode scriptNode = this.scriptParser.parse(new StringReader(value));
            this.player.setScript(scriptNode);
            if (this.autoPlay) {
                this.player.start();
            }
            break;
        case 5: // "initScript"
            if (value != null) {
                value = value.replace("\\n", "\n");
                this.scriptParser.parse(new StringReader(value)).applySubtreeTo(this.initCube, false);
                this.player.reset();
            }
            break;
        case 8: // "stickers"
            AbstractCube3DAWT cube = this.player.getCube3D();
            String[] parameters2 = this.cmd.getParameters(key, (String[]) null);
            if (parameters2 != null) {
                if (parameters2.length != 54) {
                    throw new IllegalArgumentException(new StringBuffer().append("Invalid parameter 'stickers' provides ").append(parameters2.length).append(
                            " instead of 54 entries.").toString());
                }
                int i = 0;
                for (int i5 = 0; i5 < 6; i5++) {
                    for (int i6 = 0; i6 < 9; i6++) {
                        int param = Integer.parseInt(parameters2[i++]);
                        if (this.colors.size() <= param) {
                            throw new IllegalArgumentException(new StringBuffer().append("Invalid parameter 'stickers', unknown entry '").append(param).append(
                                    "'.").toString());
                        }
                        cube.setStickerColor(i5, i6, this.colors.get(param));
                    }
                }
                this.player.reset();
            }
            break;
        case 15: // "rearView"
            // 默认true
            if ("false".equalsIgnoreCase(value)) {
                Canvas3DAWT component = (Canvas3DAWT) this.player.getVisualComponent();
                component.setScaleFactor(component.getScaleFactor());
                add("Center", component);
                if (this.rearComponent != null) {
                    remove(this.rearComponent);
                }
                validate();
            } else {
                initRearComponent();
                add("Center", this.rearComponent);
                remove(this.player.getVisualComponent());
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
            throw new IllegalArgumentException(new StringBuffer().append("Invalid parameter ").append(key).append(", value ").append(value).append(
                    " is illegal.").toString());
        //                break;
        }
    }

    public ScriptPlayer getPlayer() {
        return player;
    }

    public CommandParser getCmd() {
        return cmd;
    }

}
