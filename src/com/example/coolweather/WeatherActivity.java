package com.example.coolweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends Activity {
	
	private LinearLayout weatherInfoLayout;
	/**
	 *
	 * ������ʾ������
	 * */
	private TextView cityNameText;
	/**
	 * ������ʾ������ʱ��
	 * */
	private TextView publishText;
	/**
	 * ������ʾ����������Ϣ
	 * */
	private TextView weatherDespText;
	/**
	 * ������ʾ�¶�1
	 * */
	private TextView temp1Text;
	/**
	 * ������ʾ��ǰ������
	 * */
	private TextView currentDataText;
	/**
	 * �л����а�ť
	 * */
	private Button switchCity;
	/**
	 * ���������İ�ť
	 * */
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ���ؼ�
		
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		
		currentDataText = (TextView) findViewById(R.id.current_date);
		//��ȡ�ص�����
		String countyName = getIntent().getStringExtra("county_name");
		Log.d("county_code", countyName);
		if (!TextUtils.isEmpty(countyName)) {
			//���ؼ����ž�ȥ��ѯ����
			publishText.setText("ͬ����....");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyName);

		}else{
			//û���ؼ����ž�ֱ����ʾ��������
			showWeather();
		}
	}
	
	

	
	/**
	 * ��ѯ������������Ӧ������
	 * */
	private void queryWeatherInfo(String weatherNmane){
		
		
		String address = "http://op.juhe.cn/onebox/weather/query?cityname="+weatherNmane+"&key=d9540ca8af9dc7237810c7c7cbd20fed";
		
		queryFromServer(address);
	}
	
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ
	 * */
	private void queryFromServer(final String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				
					if (!TextUtils.isEmpty(response)) {
						//������������ص�������Ϣ
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
						publishText.setText("ͬ��ʧ��");
					}
				});
				
			}
		});
		
	}
	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��������
	 * */
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		cityNameText.setText(preferences.getString("city_name", ""));
		//�¶�
		temp1Text.setText(preferences.getString("temp1", "")+"��");
		//����
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		//����ʱ��
		publishText.setText(preferences.getString("publish_time","")+"����");
		//��ʾ��ǰ������
		currentDataText.setText(preferences.getString("current_data", ""));
		//��ʾ����
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
}
