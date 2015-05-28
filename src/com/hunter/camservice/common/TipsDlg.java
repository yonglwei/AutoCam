package com.hunter.camservice.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class TipsDlg extends AlertDialog {
	private Builder dlgBuilder;

	public TipsDlg(final Context context) {
		super(context);
		
		dlgBuilder = new Builder(context);
		
		dlgBuilder.setTitle("��ʾ:"); 
	    dlgBuilder.setMessage("��������ʧ�ܣ���������������"); 
	     
	    dlgBuilder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() { 
	        public void onClick(DialogInterface dialog, int which) { 
	                
	        } 
	    }); 
	    dlgBuilder.setNegativeButton("��������", new DialogInterface.OnClickListener() { 
	        public void onClick(DialogInterface dialog, int which) { 
	                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	        } 
	    }); 
	    dlgBuilder.create();
	}
	
	public void show() {
		this.show();
	}
	
	public static void showNetworkTips(final Context context, String title, String msg) {
		Builder dlgBuilder = new Builder(context);
		
		dlgBuilder.setTitle(title); 
	    dlgBuilder.setMessage(msg); 
	     
	    dlgBuilder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() { 
	        public void onClick(DialogInterface dialog, int which) { 
	                
	        } 
	    }); 
	    
	    dlgBuilder.setNegativeButton("��������", new DialogInterface.OnClickListener() { 
	        public void onClick(DialogInterface dialog, int which) { 
	                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	        } 
	    }); 
	    
	    dlgBuilder.create().show();
	}
}
