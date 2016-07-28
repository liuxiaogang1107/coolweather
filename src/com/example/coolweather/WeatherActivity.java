package com.example.coolweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {
	
	private LinearLayout weatherInfoLayout;
	/**
	 *
	 * 用于显示城市名
	 * */
	private TextView cityNameText;
	/**
	 * 用于显示发布的时间
	 * */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 * */
	private TextView weatherDespText;
	/**
	 * 用于显示温度1
	 * */
	private TextView temp1Text;
	/**
	 * 用于显示当前的日期
	 * */
	private TextView currentDataText;
	/**
	 * 切换城市按钮
	 * */
	private Button switchCity;
	/**
	 * 更新天气的按钮
	 * */
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化控件
		
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		currentDataText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		//获取县的名字
		String countyName = getIntent().getStringExtra("county_name");

		if (!TextUtils.isEmpty(countyName)) {
			//有县级代号就去查询天气
			publishText.setText("同步中....");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyName);

		}else{
			//没有县级代号就直接显示本地天气
			showWeather();
		}
	}
	
	

	
	/**
	 * 查询天气代号所对应的天气
	 * */
	private void queryWeatherInfo(String weatherNmane){
		
		
		String address = "http://op.juhe.cn/onebox/weather/query?cityname="+weatherNmane+"&key=d9540ca8af9dc7237810c7c7cbd20fed";
		
		queryFromServer(address);
	}
	
	/**
	 * 根据传入的地址和类型去想服务器查询天气代号或者天气信息
	 * */
	private void queryFromServer(final String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				
					if (!TextUtils.isEmpty(response)) {
						//处理服务器返回的天气信息
						Utility.handWeatherResponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								showWeather();
							}
						});
						
					}
				
				
			}
			
			@Override
			public void onError(Exception e) {
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
				
			}
		});
		
	}
	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
	 * */
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		cityNameText.setText(preferences.getString("city_name", ""));
		//温度
		temp1Text.setText(preferences.getString("temp1", "")+"℃");
		//天气
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		//发布时间
		publishText.setText(preferences.getString("publish_time","")+"发布");
		//显示当前的日期
		currentDataText.setText(preferences.getString("current_data", ""));
		//显示布局
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}




	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaACtivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String cityName = prefs.getString("city_name", "");
			if (!TextUtils.isEmpty(cityName)) {
				queryWeatherInfo(cityName);
			}
			
			break;

		default:
			break;
		}
		
	}
}
