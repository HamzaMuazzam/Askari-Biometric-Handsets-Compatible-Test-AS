package com.fgtit.data;

import android.util.Base64;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class ConversionsEx {

    public static int STD_TEMPLATE = 0;//default
    public static int ANSI_378_2004 = 1;
    public static int ISO_19794_2005 = 2;//iso1
    public static int ISO_19794_2009 = 3;//iso2
    public static int ISO_19794_2011 = 4;

    public static int COORD_NOTCHANGE = 0;
    public static int COORD_MIRRORV = 1;
    public static int COORD_MIRRORH = 2;
    public static int COORD_ROTAING = 3;

    private static ConversionsEx mCom = null;

    public static ConversionsEx getInstance() {
        if (mCom == null) {
            mCom = new ConversionsEx();
        }
        return mCom;
    }

    public native int StdToAnsiIso(byte[] input, byte[] output, int size, int imgw, int imgh, int resx, int resy, int type);

    public native int AnsiIsoToStd(byte[] input, byte[] output, int type);

    public native int StdChangeCoord(byte[] input, int size, byte[] output, int dk);

    public native int GetDataType(byte[] input);

    /**
     * Method of converting the private standard template data into ANSI or ISO data
     * @param input private standard template.
     * @param type type of the format that need to convert into.
     * @param dk direction of the fingerprint image.
     * @return
     */
    public String ToAnsiIso(byte[] input, int type, int dk) {
        int dt = GetDataType(input);
        if (dt == STD_TEMPLATE) {
            byte output[] = new byte[512];
            byte tmpdat[] = new byte[512];
            StdChangeCoord(input, 256, tmpdat, dk);
            //if(StdToAnsiIso(tmpdat,output,378,256,288,199,199,type)>0)
            if (StdToAnsiIso(tmpdat, output, 378, 260, 300, 199, 199, type) > 0) {
                return Base64.encodeToString(output, 0, 378, Base64.DEFAULT);
            }
            return "";
        }
        return "";
    }

    public String To_Ansi378_2004(byte[] input) {
        return ToAnsiIso(input, ANSI_378_2004, COORD_MIRRORV);
    }

    public String To_Iso19794_2005(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2005, COORD_MIRRORV);
    }

    public String To_Iso19794_2009(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2009, COORD_MIRRORV);
    }

    public String To_Iso19794_2011(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2011, COORD_MIRRORV);
    }

    static {
        System.loadLibrary("conversionsex");
    }
}
