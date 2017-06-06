package com.example.lemon.faceandvoiceidentfiy.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * 字体工具类
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 * */
public class FontsUtil {
	public static Typeface font_yuehei;
	
	public static void createFonts(Context context) {
		if (null == font_yuehei) {
			font_yuehei = Typeface.createFromAsset(context.getAssets(), "fonts/zzgf_yuehei.otf");
		}
	}
	
	public static String ToDBC(String input) {
	   char[] c = input.toCharArray();
	   for (int i = 0; i< c.length; i++) {
	       if (c[i] == 12288) {
	         c[i] = (char) 32;
	         continue;
	       }if (c[i]> 65280&& c[i]< 65375)
	          c[i] = (char) (c[i] - 65248);
	       }
	   return new String(c);
	}
}
