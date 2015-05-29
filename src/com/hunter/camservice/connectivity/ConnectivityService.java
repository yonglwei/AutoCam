package com.hunter.camservice.connectivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hunter.camservice.common.TipsToast;

public class ConnectivityService extends Service {
	private static final String tag = ConnectivityService.class.getSimpleName();
	
	private final Handler networkHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 1:
				Log.d(tag, msg.obj.toString());
				TipsToast.show(ConnectivityService.this, checkStatus().toString());
				break;
			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	private BroadcastReceiver broadcastRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message msg = new Message();
			msg.what = 1;
			msg.obj = intent.getAction();
			networkHandler.sendMessage(msg);
		}
	};
	
	public ConnectivityStatus checkStatus() {
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isActive = false;
		if (connMgr.getActiveNetworkInfo() != null) {
            isActive = connMgr.getActiveNetworkInfo().isAvailable();
		}
		
		if (isActive) {
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if(networkInfo.getTypeName().equalsIgnoreCase("wifi")) {
				return ConnectivityStatus.CONNECT_WIFI;
			}
			
			switch(networkInfo.getSubtype()) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return ConnectivityStatus.CONNECT_2G;
				
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return ConnectivityStatus.CONNECT_3G;

			case TelephonyManager.NETWORK_TYPE_LTE:
				return ConnectivityStatus.CONNECT_4G;
				
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return ConnectivityStatus.CONNECT_OTHER;

				default:
					return ConnectivityStatus.CONNECT_OTHER;
			}
		}
		return ConnectivityStatus.NO_CONNECT;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter mFilter = new IntentFilter();  
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);  
        registerReceiver(broadcastRecv, mFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastRecv);
	}
}
