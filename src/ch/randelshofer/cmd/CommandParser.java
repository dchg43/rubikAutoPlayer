package ch.randelshofer.cmd;

import java.awt.Font;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.DefaultParser.NonOptionAction;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.help.HelpFormatter.Builder;
import org.apache.commons.cli.help.TextHelpAppendable;

public class CommandParser {
    public static final String version = "1.0";

    public static final String copyright = "© 2025-2030 Deng";

    // 格式：长key，短key，类型(null表示没有value，比如h)，中文描述，英文描述
    private static final String[][] parameterInfo = {
            // 帮助
            {"help", "h", null, "显示帮助信息", "show help"},
            // 垂直方向倾斜角度
            {"alpha", "", "int", "垂直方向倾斜角度 -90 ~ 90，默认值-25", "Vertical orientation of the cube, -90..+90. Default: -25"},
            // 水平方向倾斜角度
            {"beta", "", "int", "水平方向倾斜角度 -90 ~ 90，默认值45", "Horizontal orientation of the cube, -90..+90. Default: 45"},
            // 背景色，默认白色
            {"backgroundColor", "", "int", "背景色，默认值0xffffff", "Background color. Default: 0xffffff"},
            // 背景图
            {"backgroundImage", "", "URL", "背景图，例如D:/照片/001.jpg", "Background image. Default: none"},
            // 设置颜色，编号分别为0~5，十六进制RGB格式，下边定义每面颜色时指定编号
            {"colorTable", "", "[name=]int, ...", "每面颜色，顺序为前右下后左上，例如 \n0x8c000f, ... ,0x003373",
                    "RGB color look up table, 6..n entries. Each entry consists of an optional name and a hex value. Default: 0x003373,0xff4600,0xf8f8f8,0x00732f,0x8c000f,0xffd200"},
            // 按块自定义颜色
            {"stickers", "", "name, ...", "每块的颜色，顺序为前右下后左上，共54个数字，\n0~5各需出现9次，例如0,0,0,...5,5,5",
                    "Maps colors from the color table to the stickers of the cube; 54 integer values; front, right, down, back, left, up. Default: 0,0,0,0,0,0,0,0,0, 1,1,1,1,1,1,1,1,1, 2,2,2,2,2,2,2,2,2, 3,3,3,3,3,3,3,3,3, 4,4,4,4,4,4,4,4,4, 5,5,5,5,5,5,5,5,5"},
            // 用标准记号法按块自定义颜色
            {"facelets", "", "name, ...", "标准记号法定义颜色：上U右R前F下D左L后B，例如 \nUUUUUUUUURRRRRRRRRFFFFFFFFF \nDDDDDDDDDLLLLLLLLLBBBBBBBBB",
                    "up, right, front, down, left, back, example: UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB"},
            //        // 按面自定义颜色，顺序为正面 右面 底面 背面 左面 顶面
            //      {"faces", "", "name, ...",
            //          "Maps colors from the color table to the faces of the cube; 6 integer values; front, right, down, back, left, up. Default: 0,1,2,3,4,5"},
            //        // 自定义正面每块颜色
            //        {"stickersFront", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 0,0,0,0,0,0,0,0,0"},
            //        // 自定义右面每块颜色
            //        {"stickersRight", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 1,1,1,1,1,1,1,1,1"},
            //        // 自定义下面每块颜色
            //        {"stickersDown", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 2,2,2,2,2,2,2,2,2"},
            //        // 自定义后面每块颜色
            //        {"stickersBack", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 3,3,3,3,3,3,3,3,3"},
            //        // 自定义左面每块颜色
            //        {"stickersLeft", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 4,4,4,4,4,4,4,4,4"},
            //        // 自定义上面每块颜色
            //        {"stickersUp", "", "(name|int), ...",
            //            "Maps colors from the color table to the stickers on the side of the cube; 9 integer values. Default: 5,5,5,5,5,5,5,5,5"},
            // 是否自动播放
            {"autoPlay", "a", "boolean", "是否自动播放，true/false", "Set this value to true, to start playing the script automatically. Default: false"},
            // 设置自动播放脚本
            {"script", "", "string", "设置自动播放脚本，例如\"R F' D2\"", "Script. Default: no script."},
            // 初始化脚本
            {"initScript", "", "string", "设置初始化的脚本，例如\"L B' U2\"",
                    "This script is used to initialize the cube, and when the reset button is pressed. Default: no script."},
            // 语言
            {"scriptLanguage", "", "string", "设置脚本的语法",
                    "Language of the Script: 'ScriptFRA','BandelowENG','RandelshoferGER','SupersetENG','TouchardDeledicqFRA','Castella'. Default: BandelowENG"},
            // 类型
            {"scriptType", "", "string", "设置脚本的类型", "The type of the script: 'Solver' or 'Generator'. Default: 'Solver'."},
            // 步数
            {"scriptProgress", "", "int", "设置初始处于的步骤",
                    "Position of the progress bar. Default: end of script if scriptType is 'Generator', 0 if script type is 'Solver'."},
            //            // 是否显示步骤脚本
            //            {"displayLines", "", "int", "Number of lines of the Script display: set to 0 to switch the display off. Default: 1"},
            //            // 模拟光线强度
            //            {"ambientLightIntensity", "", "double", "Intensity of ambient light. Default: 0.6"},
            //            // 模拟光线光源
            //            {"lightSourceIntensity", "", "double", "Intensity of the light source: set to 0 to switch the light source off. Default: 1.0"},
            //            // 模拟光线位置
            //            {"lightSourcePosition", "", "int,int,int", "X, Y and Z coordinate of the light source. Default: -500, 500, 1000"},
            // 是否展示后视图
            {"rearView", "", "boolean", "是否展示后视图，true/false", "Set this value to true, to turn the rear view on. Default: false"},
            // 后视图背景色
            {"rearViewBackgroundColor", "", "int", "后视图背景色，默认值0xffffff", "Background color. Default: use value of parameter 'backgroundColor'"},
            // 后视图背景图
            {"rearViewBackgroundImage", "", "URL", "后视图背景图，默认正视图背景图", "Background image. Default: use value of parameter 'backgroundImage'"},
            // 后视图缩放
            {"rearViewScaleFactor", "", "double", "后视图缩放比 0.1 ~ 1.0，默认0.75", "Scale factor of the rear view. Value between 0.1 and 1.0. Default: 0.75"},
            //            // 后视图旋转角度
            //            {"rearViewRotation", "", "int,int,int", "Rotation of the rear view on the X, Y and Z axis in degrees. Default: 180,0,0"}
    };

    private Hashtable<String, String> atts;

    public CommandParser() {
        this.atts = new Hashtable<>();
    }

    public void parse(String[] args) {
        Options options = new Options();
        for (String[] param : parameterInfo) {
            options.addOption(param[1] == null || param[1].length() == 0 ? null : param[1], param[0], param[2] == null ? false : true, param[3]);
        }
        CommandLine cmds = null;
        try {
            cmds = new DefaultParser().parse(options, null, NonOptionAction.IGNORE, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        Iterator<Option> iterator = cmds.iterator();
        while (iterator.hasNext()) {
            Option option = iterator.next();
            String value = option.getValue();
            if (value != null) {
                this.atts.put(option.getLongOpt(), value);
            }
        }
        if (cmds.hasOption("h")) {
            Thread runnable = new Thread() {
                @Override
                public void run() {
                    String header = null;
                    String footer = null;
                    Builder builder = HelpFormatter.builder();
                    builder.setShowSince(false);
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    TextHelpAppendable appendable = new TextHelpAppendable(printWriter);
                    appendable.setMaxWidth(66);
                    builder.setHelpAppendable(appendable);
                    HelpFormatter formatter = builder.get();
                    try {
                        formatter.printHelp(getAppInfo(), header, options, footer, false);
                        printWriter.close();
                        String message = stringWriter.toString();
                        System.out.println(message);
                        // 修改字体，改成等宽字体
                        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("宋体", Font.BOLD, 13)));
                        JOptionPane.showMessageDialog(null, message, "帮助", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            runnable.start();
        }
    }

    public String getParameter(String key) {
        return this.atts.get(key);
    }

    public String getParameter(String key, String default_value) {
        String value = getParameter(key);
        return value != null ? value : default_value;
    }

    public boolean getParameter(String key, boolean z) {
        String value = getParameter(key);
        return value != null ? value.equals("true") : z;
    }

    public String[] getParameters(String key, String[] default_value) {
        String value = getParameter(key);
        if (value == null) {
            return default_value;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
        String[] strArr = new String[stringTokenizer.countTokens()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = stringTokenizer.nextToken();
        }
        return strArr;
    }

    public int getParameter(String key, int default_value) {
        String value = getParameter(key);
        if (value != null) {
            try {
                return decode(value);
            } catch (NumberFormatException e) {
            }
        }
        return default_value;
    }

    public int[] getParameters(String key, int[] default_value) {
        String value = getParameter(key);
        if (value != null) {
            try {
                StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
                int[] iArr = new int[stringTokenizer.countTokens()];
                for (int i = 0; i < iArr.length; i++) {
                    iArr[i] = decode(stringTokenizer.nextToken());
                }
                return iArr;
            } catch (NumberFormatException e) {
            }
        }
        return default_value;
    }

    public double getParameter(String key, double default_value) {
        String value = getParameter(key);
        if (value != null) {
            try {
                return Double.valueOf(value).doubleValue();
            } catch (NumberFormatException e) {
            }
        }
        return default_value;
    }

    public Hashtable<String, Object> getIndexedKeyValueParameters(String key, Hashtable<String, Object> default_value) {
        String value = getParameter(key);
        if (value == null) {
            return default_value;
        }
        String strKey;
        String strValue;
        Hashtable<String, Object> hashtable = new Hashtable<>();
        StringTokenizer stringTokenizer = new StringTokenizer(value, ", ");
        int iCountTokens = stringTokenizer.countTokens();
        for (int i = 0; i < iCountTokens; i++) {
            String nextToken = stringTokenizer.nextToken();
            int iIndexOf = nextToken.indexOf('=');
            if (iIndexOf < 1) {
                strKey = null;
                strValue = nextToken;
            } else {
                strKey = nextToken.substring(0, iIndexOf);
                strValue = nextToken.substring(iIndexOf + 1);
            }
            String string = Integer.toString(i);
            if (strKey != null) {
                hashtable.put(strKey, strValue);
            }
            if (!hashtable.contains(string)) {
                hashtable.put(string, strValue);
            }
        }
        return hashtable;
    }

    public static int decode(String str) throws NumberFormatException {
        int i2 = 0;
        boolean z = false;
        if (str.startsWith("-")) {
            z = true;
            i2 = 0 + 1;
        }
        int i = 10;
        if (str.startsWith("0x", i2) || str.startsWith("0X", i2)) {
            i2 += 2;
            i = 16;
        } else if (str.startsWith("#", i2)) {
            i2++;
            i = 16;
        } else if (str.startsWith("0", i2) && str.length() > 1 + i2) {
            i2++;
            i = 8;
        }
        if (str.startsWith("-", i2)) {
            throw new NumberFormatException("Negative sign in wrong position");
        }
        int numValueOf;
        try {
            numValueOf = Integer.valueOf(str.substring(i2), i);
            numValueOf = z ? -numValueOf : numValueOf;
        } catch (NumberFormatException e) {
            numValueOf = Integer.valueOf(z ? new String(new StringBuffer().append("-").append(str.substring(i2)).toString()) : str.substring(i2), i);
        }
        return numValueOf;
    }

    public void setParameter(String key, String value) throws IOException {
        this.atts.put(key, value);
    }

    public String getAppInfo() {
        return "Rubik Player " + version + ", " + copyright;
    }

}
