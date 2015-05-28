package com.hunter.camservice.common;

import android.content.Context;
import android.widget.Toast;

public class TipsToast {
	
	public static void show(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
