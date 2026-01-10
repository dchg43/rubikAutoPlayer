package ch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
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
import ch.randelshofer.gui.BoundedRangeModel;
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

public final class AutoPlayer extends Panel implements Runnable {
    private static final long serialVersionUID = -698774308591767978L;

    private static final String completeCube = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB";

    /** 初始化颜色对应表，顺序：front, right, down, back, left, up */
    private static final char[] chars = {'F', 'R', 'D', 'B', 'L', 'U', '0'};

    private ScriptPlayer player;

    private MultilineLabel scriptTextArea;

    private Panel controlsPanel;

    private ArrayList<Color> colors;

    private Canvas3DAWT rearComponent = null;

    private Panel panelComponent = null;

    private CommandParser cmd;

    private Map<URL, Image> imageCache = new HashMap<>();

    private Map<String, Integer> keyMap = new HashMap<>();

    private List<JButton> testDisableList = new ArrayList<>(5);

    private List<JButton> displayDisableList = new ArrayList<>(3);

    private List<JButton> allDisableList = new ArrayList<>(8);

    private JButton buttonTest;

    private JButton buttonDisplay;

    private Image appIcon;

    private ImageIcon infoIcon;

    private ImageIcon errorIcon;

    private ImageIcon helpIcon;

    private ScriptParser scriptParser;

    private String defaultFont = null;

    private boolean initialized = false;

    private boolean isSolver;

    private boolean autoPlay = true;

    private static final int STOPPED = 0;

    private static final int STARTING = 1;

    private static final int RUNNING = 2;

    private static final int STOPPING = 3;

    private int displayMode = STOPPED;

    private int selectColorButtonIndex = -1;

    private static final Color selectColor = new Color(184, 207, 229);

    private static final Color deselectColor = new ColorUIResource(238, 238, 238);

    private Search search = new Search();

    private boolean DEBUG = false;

    public static void main(String[] args) {
        // GraalVM-Native-Image 编译成的exe文件执行时需要这个配置
        if (System.getProperty("java.home") == null) {
            System.setProperty("java.home", ".");
        }

        AutoPlayer scriptPlayer = new AutoPlayer();

        // 解析命令行参数
        scriptPlayer.getCmd().parse(args);
        // 启动
        scriptPlayer.start();

        // 等待自动执行完成
        try {
            while (scriptPlayer.getPlayer().isActive()) {
                Thread.sleep(500L);
            }
        } catch (InterruptedException e) {
        }

        if (scriptPlayer.getCmd().getParameter("autoTest", 0) > 0) {
            scriptPlayer.setDisplayMode(STARTING);
            scriptPlayer.autoTest(scriptPlayer.getCmd().getParameter("autoTest", 0));
        }
        if (scriptPlayer.getCmd().getParameter("display", false)) {
            scriptPlayer.setDisplayMode(STARTING);
            scriptPlayer.displayDemo();
        }
    }

    /** 演示：生成随机序列并执行 */
    public void displayDemo() {
        if (!BandelowENGParser.class.isInstance(this.scriptParser)) {
            this.displayMode = STOPPED;
            return;
        }
        synchronized (this) {
            if (this.displayMode == STARTING) {
                this.displayMode = RUNNING;
            } else {
                return;
            }
        }
        this.scriptTextArea.setText(null);
        this.player.setDisableButtonWhenRun(null);
        if (this.buttonDisplay != null) {
            this.buttonDisplay.setBackground(selectColor);
        }
        for (JButton disButton : this.displayDisableList) {
            disButton.setEnabled(false);
        }

        final String supportTokens = "R;U;F;L;D;B;R';U';F';L';D';B';R2;U2;F2;L2;D2;B2;R2';U2';F2';L2';D2';B2';MR;MU;MF;ML;MD;MB;MR';MU';MF';ML';MD';MB';MR2;MU2;MF2;ML2;MD2;MB2;MR2';MU2';MF2';ML2';MD2';MB2';CR;CU;CF;CL;CD;CB;CR';CU';CF';CL';CD';CB';CR2;CU2;CF2;CL2;CD2;CB2;CR2';CU2';CF2';CL2';CD2';CB2'";
        String[] tokens = supportTokens.split(";");
        final Random gen = new Random();
        StringBuilder buffer = new StringBuilder();
        while (this.displayMode == RUNNING) {
            for (int i = 0; i < 20; i++) {
                buffer.append(tokens[gen.nextInt(tokens.length)]).append(' ');
            }
            String result = buffer.toString();
            buffer.setLength(0);

            ScriptNode script = null;
            try {
                script = this.scriptParser.parse(new StringReader(result));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            this.player.setScript(script);
            // this.scriptTextArea.setText(result);
            this.player.start();
            try {
                while (this.displayMode == RUNNING && this.player.isActive()) {
                    Thread.sleep(50L);
                }
            } catch (InterruptedException e) {
            }
            this.player.stop();
        }
        this.player.setScript(null);
        this.player.setDisableButtonWhenRun(this.allDisableList);
        for (JButton disButton : this.displayDisableList) {
            disButton.setEnabled(true);
        }
        if (this.buttonDisplay != null) {
            this.buttonDisplay.setBackground(deselectColor);
        }
        this.displayMode = STOPPED;
        this.player.makesureFinished();

        try {
            // 不增加sleep界面会闪一下，原因未知
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String facelets = getCubeString(false);
        cleanAndResetCube(facelets);
    }

    /** 反复打乱并自动复原 */
    public void autoTest(long testTimes) {
        // 测试自动求解算法
        synchronized (this) {
            if (this.displayMode == STARTING) {
                this.displayMode = RUNNING;
            } else {
                return;
            }
        }

        if (this.buttonTest != null) {
            this.buttonTest.setBackground(selectColor);
        }
        for (JButton disButton : this.testDisableList) {
            disButton.setEnabled(false);
        }
        RubiksCubeCore model = this.player.getCubeModel();
        model.setQuiet(true);
        this.player.setEnabled(false);
        this.scriptTextArea.setEnabled(false);
        this.player.makesureFinished();

        long times = 0;
        long start = System.nanoTime();
        try {
            String facelets;
            ScriptNode scriptNode;
            BoundedRangeModel progress = this.player.getBoundedRangeModel();
            for (; times < testTimes && this.displayMode == RUNNING; times++) {
                model.reset();
                facelets = Tools.randomCube();
                setCubeByString(facelets, this.colors);
                facelets = searchSolution(facelets);
                scriptNode = this.scriptParser.parse(facelets);
                this.player.setScript(scriptNode);
                this.scriptTextArea.setText(facelets);
                progress.setValue(progress.getMaximum());
                this.player.makesureFinished();
                facelets = getCubeString(false);
                if (!completeCube.equals(facelets)) {
                    if (this.displayMode == RUNNING) {
                        model.setQuiet(false);
                        String message = "Auto test failed.\n script: " + this.scriptTextArea.getText() + "\n result: " + facelets;
                        JOptionPane.showOptionDialog(this, message, "失败", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, this.errorIcon,
                                CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
                        if (this.buttonTest != null) {
                            this.buttonTest.setBackground(deselectColor);
                        }
                        for (JButton disButton : this.testDisableList) {
                            disButton.setEnabled(true);
                        }
                        this.player.setEnabled(true);
                        this.scriptTextArea.setEnabled(true);
                        this.displayMode = STOPPED;
                        return;
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            model.setQuiet(false);
            String message = "Auto test failed.";
            JOptionPane.showOptionDialog(this, message, "失败", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, this.errorIcon,
                    CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
            if (this.buttonTest != null) {
                this.buttonTest.setBackground(deselectColor);
            }
            for (JButton disButton : this.testDisableList) {
                disButton.setEnabled(true);
            }
            this.player.setEnabled(true);
            this.scriptTextArea.setEnabled(true);
            this.displayMode = STOPPED;
            return;
        }
        double timeInSecond = (System.nanoTime() - start) / 1000000000.0d;
        this.player.setScript(null);
        this.scriptTextArea.setText(null);
        AbstractCube3DAWT cube = this.player.getCube3D();
        cube.getModel().reset();
        for (int i = 0; i < 6; i++) {
            Color c = this.colors.get(i);
            for (int j = 0; j < 9; j++) {
                cube.setStickerColor(i, j, c);
            }
        }
        // 刷新魔方
        this.player.makesureFinished();
        cube.fireStateChanged();
        model.setQuiet(false);
        String message = String.format("完成%d次测试，用时%.2f秒。", times, timeInSecond);
        JOptionPane.showOptionDialog(this, message, "成功", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, this.infoIcon,
                CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
        if (this.buttonTest != null) {
            this.buttonTest.setBackground(deselectColor);
        }
        for (JButton disButton : this.testDisableList) {
            disButton.setEnabled(true);
        }
        this.player.setEnabled(true);
        this.scriptTextArea.setEnabled(true);
        this.displayMode = STOPPED;
    }

    public AutoPlayer() {
        ClassLoader loader = AutoPlayer.class.getClassLoader();
        this.appIcon = new ImageIcon(loader.getResource("ico.png")).getImage();
        this.infoIcon = new ImageIcon(loader.getResource("info40.png"));
        this.errorIcon = new ImageIcon(loader.getResource("error40.png"));
        this.helpIcon = new ImageIcon(loader.getResource("help40.png"));

        initDefaultFont();
        this.cmd = new CommandParser();
        cmd.setDefaultFont(this.defaultFont);
        cmd.setHelpIcon(this.helpIcon);

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

    public void start() {
        initComponents();
        PooledSequentialDispatcherAWT.dispatchConcurrently(this);
        this.search.init();
        try {
            while (!this.initialized) // 等待启动完成
            {
                Thread.sleep(10L);
            }
        } catch (InterruptedException e) {
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        initControlsPanel();
        initGUI();
    }

    private void initDefaultFont() {
        FontRenderContext frc = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, 15);
            // 判断是否支持中文
            if (font.canDisplayUpTo("编辑©") == -1) {
                // 判断是否等宽字体
                if (font.getStringBounds("il ", frc).getWidth() == font.getStringBounds("WMG", frc).getWidth()) {
                    this.defaultFont = fontName;
                    if ("DialogInput".equals(fontName)) { // 优先
                        return;
                    }
                }
                if (this.defaultFont == null) {
                    this.defaultFont = fontName;
                }
            }
        }
        if (this.defaultFont == null && fontNames.length > 0) {
            this.defaultFont = fontNames[0];
        }
    }

    private void initControlsPanel() {
        this.player = new ScriptPlayer() {
            @Override
            public void reset() {
                super.reset();
                // getCubeModel().setTo(AutoPlayer.this.initCube);
            }
        };
        this.player.setDisableButtonWhenRun(this.allDisableList);
        this.scriptTextArea = new MultilineLabel();
        this.controlsPanel = new Panel(); // 底部整个控制框
        this.controlsPanel.setLayout(new BorderLayout());
        this.controlsPanel.add("North", this.player.getControlPanelComponent()); // 进度条和控制按钮
        this.controlsPanel.add("South", this.scriptTextArea); // 执行文本显示
        this.scriptTextArea.setFont(new Font(this.defaultFont, Font.BOLD, 16));
        this.scriptTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int cursor = AutoPlayer.this.scriptTextArea.viewToModel(mouseEvent.getX(), mouseEvent.getY());
                if (cursor < AutoPlayer.this.scriptTextArea.getText().length()) {
                    AutoPlayer.this.player.moveToCaret(cursor);
                }
            }
        });
        this.scriptTextArea.setSize(getWidth(), getHeight());

        try {
            initCube();
            ChangeListener changeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent changeEvent) {
                    // 刷新步骤序列
                    AutoPlayer.this.selectCurrentSymbol();
                }
            };
            this.player.getBoundedRangeModel().addChangeListener(changeListener);
            this.player.addChangeListener(changeListener);
            this.player.getjToggle().addActionListener(new ActionListener() {
                private boolean openRear = AutoPlayer.this.cmd.getParameter("rearView", true);

                @Override
                public void actionPerformed(ActionEvent e) {
                    openRear = !openRear;
                    initPanelComponent(openRear);
                }
            });
            synchronized (getTreeLock()) {
                add("South", this.controlsPanel);
            }
        } catch (Throwable e) {
            removeAll();
            setLayout(new BorderLayout());
            TextArea textArea = new TextArea(30, 40);
            add("South", textArea);

            String errString = getString(e);
            System.err.println(errString);
            textArea.setText(CommandParser.getAppInfo() + "\n\n" + errString);
        }
    }

    private void initCube() throws IllegalArgumentException {
        AbstractCube3DAWT cube = this.player.getCube3D();
        Color colorBack = new Color(this.cmd.getParameter("backgroundColor", 0xf7f7f7));
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
                showError(new StringBuilder().append("Invalid parameter 'colorTable', value ").append(Arrays.toString(colors_str)).append(" is illegal.\n")
                        .append(getString(e)).toString());
            }
            // 设置参数异常时使用默认值
            Color c = new Color(CommandParser.decode(dflt[colorIndex]));
            if (this.colors.contains(c)) {
                throw new IllegalArgumentException(new StringBuilder().append("Invalid parameter 'colorTable' value ").append(Arrays.toString(colors_str))
                        .append(" is illegal.").toString());
            }
            this.colors.add(colorIndex, c);
        }
        // 补充未设置的颜色
        for (; colorIndex < dflt.length; colorIndex++) {
            Color c = new Color(CommandParser.decode(dflt[colorIndex]));
            if (this.colors.contains(c)) {
                throw new IllegalArgumentException(new StringBuilder().append("Invalid parameter 'colorTable' value ").append(Arrays.toString(colors_str))
                        .append(" is illegal.").toString());
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
        initColors();

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
            this.player.setScript(scriptNode);
            this.scriptTextArea.setText(script);
        } catch (Exception e) {
            this.player.setScript(null);
            this.scriptTextArea.setText(null);
            showError("Invalid parameter 'script'\n" + getString(e));
        }

        RubiksCubeCore initCube = new RubiksCubeCore();
        String initScript = this.cmd.getParameter("initScript");
        if (initScript != null) {
            initScript = initScript.replace("\\n", "\n");
            try {
                scriptParser.parse(new StringReader(initScript)).applySubtreeTo(initCube, false);
            } catch (Exception e) {
                showError("Invalid parameter 'initScript'\n" + getString(e));
            }
        }

        if (this.isSolver && this.player.getScript() != null) {
            this.player.getScript().applySubtreeTo(initCube, true);
        }
        this.player.reset();
        this.player.getCubeModel().setTo(initCube);
        try {
            int scriptProgress = this.cmd.getParameter("scriptProgress", (this.isSolver || this.cmd.getParameter("autoPlay", true)) ? 0 : -1);
            if (scriptProgress < 0) {
                scriptProgress += this.player.getBoundedRangeModel().getMaximum();
            }
            this.player.getBoundedRangeModel().setValue(scriptProgress);
        } catch (IndexOutOfBoundsException e) {
            showError("Invalid parameter 'scriptProgress'\n" + getString(e));
        }

        String displayLines = this.cmd.getParameter("displayLines", "1");
        int iCountTokens = script == null ? 1 : new StringTokenizer(script, "\n").countTokens();
        try {
            iCountTokens = Math.max(Integer.parseInt(displayLines), iCountTokens);
        } catch (NumberFormatException e) {
            showError("Invalid parameter 'displayLines'\n" + getString(e));
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
                showError("Invalid parameter 'backgroundImage' malformed URL: " + backgroundImage + "\n" + getString(e));
            }
        }

        // 初始化魔方后视图
        initRearComponent();
        initPanelComponent(this.cmd.getParameter("rearView", true));
    }

    private void initPanelComponent(boolean openRear) {
        if (openRear) {
            this.panelComponent.remove(this.player.getVisualComponent());
            this.panelComponent.remove(this.rearComponent);
            this.panelComponent.add(this.player.getVisualComponent());
            this.panelComponent.add(this.rearComponent);
            remove(this.player.getVisualComponent());
            add("Center", this.panelComponent);
        } else {
            remove(this.panelComponent);
            add("Center", this.player.getVisualComponent());
        }
        revalidate();
    }

    // 设置用户自定义颜色
    private void initColors() {
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
                            showError(new StringBuilder().append("Invalid parameter '").append(strArr[i]).append("', unknown entry '").append(colorLists[j])
                                    .append("'.").toString());
                            break;
                        } else {
                            cube.setStickerColor(i, j, this.colors.get(entry));
                        }
                    }
                } else {
                    showError(new StringBuilder().append("Invalid parameter '").append(strArr[i]).append("' provides ").append(colorLists.length)
                            .append(" instead of 9 entries.").toString());
                }
            }
        }
    }

    private void initRearComponent() {
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

        rearCanvas3D.setBackground(new Color(this.cmd.getParameter("rearViewBackgroundColor", this.cmd.getParameter("backgroundColor", 0xf7f7f7))));
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
                showError("Invalid parameter 'backgroundImage' malformed URL: " + rearImage + "\n" + getString(e));
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
        this.rearComponent = rearCanvas3D;

        Panel panel = new Panel();
        panel.setLayout(new RatioLayout(1.0d - (0.5d * fMax)));
        panel.add(this.player.getVisualComponent());
        panel.add(this.rearComponent);
        this.panelComponent = panel;
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

    public ImageIcon reSizeIcon(URL imagePath, int newWidth, int newHeight) {
        Image image = new ImageIcon(imagePath).getImage();
        Image reSizeImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(reSizeImage);
    }

    private void initGUI() {
        final int width = 650; // 窗口宽度
        final int height = 600; // 窗口高度
        final int buttonWidth = 66; // 按钮宽度
        final int buttonHeight = 40; // 按钮高度
        final int windowBorder = 20; // 按钮距窗口边框距离
        final int colorBorder = 4; // 颜色选择框边框宽度
        final int buttonSpace = 10; // 按钮间距
        final int buttonStep = buttonWidth + buttonSpace;
        final int buttonLeft = windowBorder + (buttonHeight - colorBorder) * 6 + colorBorder;
        final int buttonRight = buttonLeft + (buttonStep) * 5 - buttonWidth;
        final int minWidth = buttonRight + windowBorder + buttonWidth;
        final int buttonLine2 = windowBorder + buttonSpace + buttonHeight;

        final JFrame frame = new JFrame("AutoPlayer"); // 初始化画布
        frame.setTitle("三阶魔方求解器 by Deng");
        frame.setSize(width, height); // 设置画布大小
        frame.setPreferredSize(new java.awt.Dimension(width, height));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setIconImage(this.appIcon); // 设置窗口图标

        // 添加退出事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        // 添加键盘事件
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 回车键
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ((JButton) e.getComponent()).doClick();
                }
            }
        };

        final JButton[] colorSel = new JButton[6];
        // 顺序：正面红色, 右面黄色, 底面绿色, 背面橙色, 左面白色, 顶面蓝色
        final Color[] initColors = {new Color(230, 0, 0), new Color(240, 220, 0), // 红 黄
                new Color(0, 170, 0), new Color(255, 118, 0), Color.white, Color.blue}; // 绿 橙
        final Border defaultBorder = new LineBorder(new Color(240, 240, 240), colorBorder);
        final Border selectBorder = new LineBorder(new Color(118, 188, 245), colorBorder);
        Font defaultFont = new Font(this.defaultFont, Font.BOLD, 14);
        int colory = windowBorder + colorBorder;
        int colorStep = buttonHeight - colorBorder;
        int colorWidth = colorStep - colorBorder;
        for (int i = 0; i < 6; i++) {
            colorSel[i] = new JButton();
            frame.add(colorSel[i]);
            colorSel[i].setBackground(initColors[i]);
            colorSel[i].setOpaque(true);
            colorSel[i].setBounds(colory + colorStep * i, colory, colorWidth, colorWidth);
            colorSel[i].setBorderPainted(true);
            colorSel[i].setBorder(defaultBorder);
            colorSel[i].setName(String.valueOf(i));
            colorSel[i].addKeyListener(keyListener);
            final int value = i;
            colorSel[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (AutoPlayer.this.selectColorButtonIndex != value) {
                        if (AutoPlayer.this.selectColorButtonIndex != -1) {
                            colorSel[AutoPlayer.this.selectColorButtonIndex].setBorder(defaultBorder);
                        }
                        AutoPlayer.this.selectColorButtonIndex = value;
                        colorSel[value].setBorder(selectBorder);
                        AutoPlayer.this.player.getCube3D().setSelectColor(AutoPlayer.this.colors.get(value));
                    }
                }
            });
        }
        Panel panelback = new Panel();
        frame.add(panelback);
        panelback.setBackground(Color.lightGray);
        panelback.setBounds(windowBorder, windowBorder, colorStep * 6 + colorBorder, buttonHeight);

        // 编辑按钮
        final JButton buttonEdit = new JButton("edit");
        frame.add(buttonEdit);
        this.testDisableList.add(buttonEdit);
        this.displayDisableList.add(buttonEdit);
        this.allDisableList.add(buttonEdit);
        buttonEdit.setBounds(buttonLeft + buttonSpace, windowBorder, buttonWidth, buttonHeight);
        buttonEdit.setFont(defaultFont);
        buttonEdit.setText("编辑");
        buttonEdit.addKeyListener(keyListener);
        buttonEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive() || AutoPlayer.this.displayMode != STOPPED) {
                    return;
                }

                AutoPlayer.this.player.makesureFinished();
                AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                // 判断魔方是否有旋转，因为编辑功能是基于魔方未旋转状态，如果有旋转，设置方块颜色时会错位
                if (!cube.getModel().isSolved()) {
                    // 重置魔方状态，保留块的颜色和顺序
                    String facelets = getCubeString(false);
                    cleanAndResetCube(facelets);
                    AutoPlayer.this.player.makesureFinished();
                }

                if (cube.isEditMode()) {
                    cube.setEditMode(false);
                    ((JButton) evt.getSource()).setBackground(deselectColor);
                } else {
                    cube.setEditMode(true);
                    ((JButton) evt.getSource()).setBackground(selectColor);
                    if (AutoPlayer.this.selectColorButtonIndex == -1) {
                        // colorSel[0].requestFocus();
                        colorSel[0].doClick();
                    }
                }
            }
        });

        // 校验按钮
        // 有一种错误的魔方序列校验应该失败，但是却校验成功并给出解法，但是实际无法复原
        // 错误序列如一个对向中心块互换。
        // 所有这些错误序列给的复原解法执行后最终都会变成这个序列 DUDUUUDUDRRRRRRRRRFFFFFFFFFUDUDDDUDULLLLLLLLLBBBBBBBBB
        final JButton buttonCheck = new JButton("check");
        frame.add(buttonCheck);
        this.testDisableList.add(buttonCheck);
        this.displayDisableList.add(buttonCheck);
        this.allDisableList.add(buttonCheck);
        buttonCheck.setBounds(buttonLeft + buttonSpace * 2 + buttonWidth, windowBorder, buttonWidth, buttonHeight);
        buttonCheck.setFont(defaultFont);
        buttonCheck.setText("校验");
        buttonCheck.addKeyListener(keyListener);
        buttonCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive() || AutoPlayer.this.displayMode != STOPPED) {
                    return;
                }

                AutoPlayer.this.player.makesureFinished();
                String cubeString = getCubeString(true);
                String result = searchSolution(cubeString);
                if (result.contains("Error")) {
                    String message = "校验不通过：" + getErrMessage(result);
                    JOptionPane.showOptionDialog(AutoPlayer.this, message, "失败", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            AutoPlayer.this.errorIcon, CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
                } else {
                    String message = "校验通过，可求解。";
                    JOptionPane.showOptionDialog(AutoPlayer.this, message, "成功", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            AutoPlayer.this.infoIcon, CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
                }
            }
        });

        // 清空按钮
        final JButton buttonClean = new JButton("clean");
        frame.add(buttonClean);
        this.testDisableList.add(buttonClean);
        this.allDisableList.add(buttonClean);
        buttonClean.setBounds(buttonRight - buttonStep * 2, windowBorder, buttonWidth, buttonHeight);
        buttonClean.setFont(defaultFont);
        buttonClean.setText("清空");
        buttonClean.addKeyListener(keyListener);
        buttonClean.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive() && AutoPlayer.this.displayMode != RUNNING) {
                    return;
                }

                Color c = AutoPlayer.this.colors.get(6);
                AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 9; j++) {
                        cube.setStickerColor(i, j, c);
                    }
                }
                // 刷新魔方
                cube.fireStateChanged();
            }
        });

        // 打乱按钮
        final JButton buttonRandom = new JButton("random");
        frame.add(buttonRandom);
        this.testDisableList.add(buttonRandom);
        this.allDisableList.add(buttonRandom);
        buttonRandom.setBounds(buttonRight - buttonStep, windowBorder, buttonWidth, buttonHeight);
        buttonRandom.setFont(defaultFont);
        buttonRandom.setText("打乱");
        buttonRandom.addKeyListener(keyListener);
        buttonRandom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive() && AutoPlayer.this.displayMode != RUNNING) {
                    return;
                }

                // Random stick by Call Random function
                String facelets = Tools.randomCube();
                setCubeByString(facelets, AutoPlayer.this.colors);
            }
        });

        // 反序按钮
        final JButton buttonSolver = new JButton("Solver");
        frame.add(buttonSolver);
        this.testDisableList.add(buttonSolver);
        this.displayDisableList.add(buttonSolver);
        this.allDisableList.add(buttonSolver);
        buttonSolver.setBounds(buttonRight, windowBorder, buttonWidth, buttonHeight);
        buttonSolver.setFont(defaultFont);
        buttonSolver.setText("反序");
        buttonSolver.addKeyListener(keyListener);
        buttonSolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (AutoPlayer.this.player.isActive() || AutoPlayer.this.displayMode != STOPPED) {
                    return;
                }
                String script = AutoPlayer.this.scriptTextArea.getText();
                if (script == null || script.length() == 0) {
                    return;
                }

                // 取消编辑
                AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                if (cube.isEditMode()) {
                    buttonEdit.setBackground(deselectColor);
                    cube.setEditMode(false);
                }

                AutoPlayer.this.player.makesureFinished();
                BoundedRangeModel progress = AutoPlayer.this.player.getBoundedRangeModel();
                if (progress.getValue() != progress.getMaximum()) {
                    // 进度条移到最后
                    progress.setValue(progress.getMaximum());
                    // 刷新魔方
                    cube.fireStateChanged();
                    AutoPlayer.this.player.makesureFinished();
                }

                // 判断魔方是否有旋转，因为编辑时仍然能执行反序，如果有旋转，设置方块颜色时会错位
                if (!cube.getModel().isSolved()) {
                    // 有旋转，重置为旋转前状态
                    String facelets = getCubeString(false);
                    cleanAndResetCube(facelets);
                    AutoPlayer.this.player.makesureFinished();
                }

                // 获取旋转序列
                int index = script.indexOf('(');
                if (index > 0) {
                    script = script.substring(0, script.indexOf('(') - 1);
                }

                // 将旋转序列反序
                String[] splits = script.split(" +|\n");
                StringBuilder result = new StringBuilder();
                for (int i = splits.length - 1; i >= 0; i--) {
                    String tmp = splits[i];
                    if (tmp.length() <= 1) {
                        result.append(tmp).append('\'');
                    } else if (tmp.charAt(tmp.length() - 1) == '\'') {
                        result.append(tmp.substring(0, tmp.length() - 1)).append(' ');
                    } else {
                        result.append(tmp); // .append('\'');
                    }
                    result.append(' ');
                }
                String newScript = result.toString();

                // 写回反序序列并执行
                try {
                    doParameter("script", newScript);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 测试按钮
        final JButton buttonTest = new JButton("test");
        frame.add(buttonTest);
        this.displayDisableList.add(buttonTest);
        this.allDisableList.add(buttonTest);
        this.buttonTest = buttonTest;
        buttonTest.setBounds(buttonRight - buttonStep * 2, buttonLine2, buttonWidth, buttonHeight);
        buttonTest.setFont(defaultFont);
        buttonTest.setText("测试");
        buttonTest.addKeyListener(keyListener);
        buttonTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                synchronized (AutoPlayer.this) {
                    if (AutoPlayer.this.player.isActive() && AutoPlayer.this.displayMode != RUNNING) {
                        return;
                    }
                    // 取消编辑
                    AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                    if (cube.isEditMode()) {
                        buttonEdit.setBackground(deselectColor);
                        cube.setEditMode(false);
                    }

                    if (AutoPlayer.this.displayMode == RUNNING) {
                        AutoPlayer.this.displayMode = STOPPING;
                        AutoPlayer.this.player.stop();
                    } else if (AutoPlayer.this.displayMode == STOPPED) {
                        AutoPlayer.this.displayMode = STARTING;
                        new Thread() {
                            @Override
                            public void run() {
                                autoTest(Long.MAX_VALUE);
                            }
                        }.start();
                    }
                }
            }
        });

        // 演示按钮
        final JButton buttonDisplay = new JButton("display");
        frame.add(buttonDisplay);
        this.testDisableList.add(buttonDisplay);
        this.allDisableList.add(buttonDisplay);
        this.buttonDisplay = buttonDisplay;
        buttonDisplay.setBounds(buttonRight - buttonStep, buttonLine2, buttonWidth, buttonHeight);
        buttonDisplay.setFont(defaultFont);
        buttonDisplay.setText("演示");
        buttonDisplay.addKeyListener(keyListener);
        buttonDisplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                synchronized (AutoPlayer.this) {
                    if (AutoPlayer.this.player.isActive() && AutoPlayer.this.displayMode != RUNNING) {
                        return;
                    }
                    // 取消编辑
                    AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                    if (cube.isEditMode()) {
                        buttonEdit.setBackground(deselectColor);
                        cube.setEditMode(false);
                    }

                    if (AutoPlayer.this.displayMode == RUNNING) {
                        AutoPlayer.this.displayMode = STOPPING;
                        AutoPlayer.this.player.stop();
                    } else if (AutoPlayer.this.displayMode == STOPPED) {
                        AutoPlayer.this.displayMode = STARTING;
                        new Thread() {
                            @Override
                            public void run() {
                                displayDemo();
                            }
                        }.start();
                    }
                }
            }
        });

        // 求解按钮
        final JButton buttonSolution = new JButton("solution");
        frame.add(buttonSolution);
        this.testDisableList.add(buttonSolution);
        this.displayDisableList.add(buttonSolution);
        this.allDisableList.add(buttonSolution);
        buttonSolution.setBounds(buttonRight, buttonLine2, buttonWidth, buttonHeight);
        buttonSolution.setFont(defaultFont);
        buttonSolution.setText("求解");
        buttonSolution.addKeyListener(keyListener);
        buttonSolution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                synchronized (AutoPlayer.this) {
                    if (AutoPlayer.this.displayMode != STOPPED) {
                        AutoPlayer.this.displayMode = STOPPING;
                        AutoPlayer.this.player.stop();
                        return;
                    }
                }
                if (AutoPlayer.this.player.isActive()) {
                    // 正在执行中
                    return;
                }

                // 求解并校验
                AutoPlayer.this.player.makesureFinished();
                String facelets = getCubeString(true);
                String result = searchSolution(facelets);
                if (result.contains("Error")) {
                    String message = "校验不通过：" + getErrMessage(result);
                    JOptionPane.showOptionDialog(AutoPlayer.this, message, "失败", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            AutoPlayer.this.errorIcon, CommandParser.DEFAULTOPTION, CommandParser.DEFAULTOPTION[0]);
                    return;
                }

                // 取消编辑
                AbstractCube3DAWT cube = AutoPlayer.this.player.getCube3D();
                if (cube.isEditMode()) {
                    buttonEdit.setBackground(deselectColor);
                    cube.setEditMode(false);
                }

                // 有旋转，重置为旋转前状态
                if (!cube.getModel().isSolved()) {
                    cleanAndResetCube(facelets);
                    AutoPlayer.this.player.makesureFinished();
                }

                // 自动执行复位方法
                try {
                    doParameter("script", result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 调整窗口大小
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int column3 = buttonRight;
                int frameWidth = getWidth() & (~1); // 将最后一位改为0, 防止按钮左边框显示不完整
                if (frameWidth > minWidth) {
                    column3 = frameWidth - windowBorder - buttonWidth;
                }
                int column2 = column3 - buttonStep;
                int column1 = column2 - buttonStep;
                buttonClean.setBounds(column1, windowBorder, buttonWidth, buttonHeight); // 清空
                buttonRandom.setBounds(column2, windowBorder, buttonWidth, buttonHeight); // 打乱
                buttonSolver.setBounds(column3, windowBorder, buttonWidth, buttonHeight); // 反序
                buttonTest.setBounds(column1, buttonLine2, buttonWidth, buttonHeight); // 测试
                buttonDisplay.setBounds(column2, buttonLine2, buttonWidth, buttonHeight); // 演示
                buttonSolution.setBounds(column3, buttonLine2, buttonWidth, buttonHeight); // 求解
                AutoPlayer.this.scriptTextArea.revalidate(); // 刷新
            }
        });

        // 添加魔方
        frame.add(this, "Center");

        // 显示
        frame.setVisible(true);
        // buttonSolution.requestFocusInWindow(); // 设置默认焦点
        // frame.getRootPane().setDefaultButton(buttonSolution); // 设置按下回车键默认操作(会跟keyListener重复执行)
        revalidate();
    }

    // 默认绘图函数，魔方未加载、加载中或失败时会显示的内容
    @Override
    public void paint(Graphics g) {
        g.setFont(new Font(this.defaultFont, Font.PLAIN, 10));
        FontMetrics fontMetrics = g.getFontMetrics();
        g.drawString("Loading " + CommandParser.getAppInfo(), 12, fontMetrics.getHeight());
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
        this.scriptTextArea.select(startPosition, endPosition, backColor);
    }

    private void showError(String message) {
        System.out.println(message);
    }

    private static String getString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
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
            result = "没有低于21次移动的方案。";
            break;
        case '8':
            // "Timeout, no solution found within given maximum time!"
            result = "计算超时。";
            break;
        case '9':
            result = "6个面的中心块需要各有一个不同的颜色。";
            break;
        }
        return result;
    }

    /**
     * 清除魔方旋转记录并重置魔方状态
     * 用于编辑和自动复原功能，这两个是基于魔方未旋转状态，如果有旋转，设置和获取方块颜色时会错位
     * @param facelets 类似 UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
     */
    private void cleanAndResetCube(String facelets) {
        // 复制一份颜色表，已经添加到colorCurrent的从该表去除，用于去重
        ArrayList<Color> colorList = new ArrayList<>();
        for (Color c : this.colors) {
            colorList.add(c);
        }

        AbstractCube3DAWT cube = this.player.getCube3D();
        ArrayList<Color> colorCurrent = new ArrayList<>();
        int currentIndex = 0;
        for (int i = 0; i < 6; i++) {
            // 以中心块的颜色为基准
            Color c = cube.getStickerColor(i, 4);
            if (!colorCurrent.contains(c) && !this.colors.get(6).equals(c)) {
                colorCurrent.add(currentIndex++, c);
                colorList.remove(c);
            }
        }

        // colorCurrent长度需要为7，不足的补齐。第7个灰色必然会补
        while (currentIndex < 7) {
            colorCurrent.add(currentIndex++, colorList.remove(0));
        }
        // 重置魔方状态，保留块的颜色和顺序
        cube.getModel().setQuiet(true);
        cube.getModel().reset();
        setCubeByString(facelets, colorCurrent);
        cube.getModel().setQuiet(false);
    }

    /**
     * 搜索自动复原方案
     * @param cubeString 类似 UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
     * @return 输出类似 R2 F' L
     */
    public String searchSolution(String cubeString) {
        if (cubeString.length() < 54) {
            return cubeString;
        }
        if (this.DEBUG) {
            System.out.println("input: " + cubeString);
        }

        int depth = 15; // 建议 Step: 15 ~ 18
        // 结果长度跟这个值相关，0, 0, 0, 0, 5, 300, 3000, 300000得到的几率大概是0.1,0.4,1,3,20,70,6,0(%)
        final int[] maxTries = {0, 0, 0, 0, 5, 300, 3000, 300000}; // 对应depth的15 16 17 18 19 20 21 22
        final int maxDepth = depth + maxTries.length - 1;
        String result = "Error 8";
        int tries = 0;
        String verify = this.search.verify(cubeString);
        if (verify != null) {
            return verify;
        }

        int mask = 0;
        int maxProbe = 1;
        char errkey = '8';
        while (errkey == '8' || errkey == '7') {
            result = this.search.solution(depth, maxProbe, 1, mask);
            errkey = result.length() > 0 ? result.charAt(result.length() - 1) : '0';
            tries = maxTries[depth - 15];
            while (errkey == '8' && tries > 0) {
                result = this.search.next(maxProbe, 1, mask);
                errkey = result.charAt(result.length() - 1);
                tries--;
            }
            depth++;
            if (depth >= maxDepth) {
                if (depth == maxDepth) {
                    maxProbe = 10000;
                } else {
                    break;
                }
            }
        }

        if (this.DEBUG) {
            System.out.println("depth:" + (--depth) + ", tries: " + (maxTries[depth - 15] - tries) + ", result: " + result);
        }
        return result;
    }

    /**
     * 初始状态应该获取到的序列为：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
     * check: 是否校验魔方是否完整
    */
    public String getCubeString(boolean check) {
        AbstractCube3DAWT cube = this.player.getCube3D();
        // 复制一份字符表，已经添加到colorMap的从该表去除，用于去重
        ArrayList<Character> charList = new ArrayList<>();
        ArrayList<Color> tmp = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            charList.add(chars[i]);
            tmp.add(this.colors.get(i));
        }

        Map<Color, Character> colorMap = new HashMap<>();
        colorMap.put(this.colors.get(6), chars[6]);
        for (int i = 0; i < 6; i++) {
            // 以中心块的颜色为基准
            Color centerColor = cube.getStickerColor(i, 4);
            if (!colorMap.containsKey(centerColor)) {
                colorMap.put(centerColor, charList.remove(0));
            }
        }
        if (check && colorMap.size() < 6) {
            return "Error 9";
        }

        tmp.removeAll(colorMap.keySet());
        for (int i = 0; i < tmp.size(); i++) {
            colorMap.put(tmp.get(i), charList.remove(0));
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
        RubiksCubeCore model = cube.getModel();
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

    /**
     *  用标准记号法按块设置颜色
     * @param cubeString 形如：UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB
     * @param curColors 对应颜色序列，长度需要7，顺序为U R F D L B
     */
    public void setCubeByString(String cubeString, ArrayList<Color> curColors) {
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
        cube.fireStateChanged();
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
            ScriptNode scriptNode = this.scriptParser.parse(new StringReader(value));
            this.player.setScript(scriptNode);
            this.scriptTextArea.setText(value);
            if (this.autoPlay) {
                this.player.start();
            }
            break;
        case 5: // "initScript"
            if (value != null) {
                value = value.replace("\\n", "\n");
                RubiksCubeCore initCube = new RubiksCubeCore();
                this.scriptParser.parse(new StringReader(value)).applySubtreeTo(initCube, false);
                this.player.reset();
                this.player.getCubeModel().setTo(initCube);
            }
            break;
        case 8: // "stickers"
            AbstractCube3DAWT cube = this.player.getCube3D();
            String[] parameters2 = this.cmd.getParameters(key, (String[]) null);
            if (parameters2 == null) {
                break;
            }
            if (parameters2.length != 54) {
                throw new IllegalArgumentException(new StringBuilder().append("Invalid parameter 'stickers' provides ").append(parameters2.length)
                        .append(" instead of 54 entries.").toString());
            }
            int i = 0;
            for (int i5 = 0; i5 < 6; i5++) {
                for (int i6 = 0; i6 < 9; i6++) {
                    int param = Integer.parseInt(parameters2[i++]);
                    if (this.colors.size() <= param) {
                        throw new IllegalArgumentException(
                                new StringBuilder().append("Invalid parameter 'stickers', unknown entry '").append(param).append("'.").toString());
                    }
                    cube.setStickerColor(i5, i6, this.colors.get(param));
                }
            }
            cube.fireStateChanged();
            this.player.makesureFinished();
            break;
        case 15: // "rearView"
            initPanelComponent("true".equalsIgnoreCase(value));
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
                    new StringBuilder().append("Invalid parameter ").append(key).append(", value ").append(value).append(" is illegal.").toString());
        }
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    public ScriptPlayer getPlayer() {
        return player;
    }

    public CommandParser getCmd() {
        return cmd;
    }

}
