package ch.randelshofer.util;


import java.applet.Applet;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class Applets
{
    private Applets()
    {}

    public static String getParameter(Applet applet, String key, String default_value)
    {
        String value = applet.getParameter(key);
        return value != null ? value : default_value;
    }

    public static boolean getParameter(Applet applet, String key, boolean z)
    {
        String value = applet.getParameter(key);
        return value != null ? value.equals("true") : z;
    }

    public static String[] getParameters(Applet applet, String key, String[] default_value)
    {
        String value = applet.getParameter(key);
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

    public static int getParameter(Applet applet, String key, int default_value)
    {
        String value = applet.getParameter(key);
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

    public static int[] getParameters(Applet applet, String key, int[] default_value)
    {
        String value = applet.getParameter(key);
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

    public static double getParameter(Applet applet, String key, double default_value)
    {
        String value = applet.getParameter(key);
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

    public static Hashtable<String, Object> getIndexedKeyValueParameters(Applet applet, String key,
                                                                         Hashtable<String, Object> default_value)
    {
        String value = applet.getParameter(key);
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
}
