package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class AutoUpdateService extends Service {
	
	//定时刷新服务

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				updareWeather();
				
			}

			
		}).start();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 8 * 60 * 60 * 1000;
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		
		Intent i = new Intent(this, AutoUpdateService.class);
		
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		
		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void updareWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherName = prefs.getString("city_name", "");
		
		String address = "http://op.juhe.cn/onebox/weather/query?cityname="+weatherName+"&key=d9540ca8af9dc7237810c7c7cbd20fed";
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handWeatherResponse(AutoUpdateService.this, response);
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
		
	}
}
