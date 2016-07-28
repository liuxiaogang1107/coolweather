package com.example.coolweather;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class ChooseAreaACtivity extends Activity {
	/**
	 * ����ѡ��״̬
	 * */
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private Context mContext;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	/**
	 * ��������Դ
	 * */
	private List<String> dataList = new ArrayList<String>();
	/**
	 * ʡ�б�
	 * */
	private List<Province> provinceList;
	/**
	 * ���б�
	 * */
	private List<City> cityList;
	/**
	 * ���б�
	 * */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 * */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 * */
	private City selectedCity;
	/**
	 * ѡ�еļ���
	 * */
	private int currentLevel;
	/**
	 * �Ƿ��WeatherActivity��ת����
	 * */
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		mContext = ChooseAreaACtivity.this;
		
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		
		
		
		adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(mContext);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
					
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					System.out.println(selectedCity.getCityName()+" �ҵ����"+" "+selectedCity.getCityCode());
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTY) {
					String countyName = countyList.get(position).getCountyNanme();
					System.out.println(countyList.get(position).getCountyNanme());
					Intent intent = new Intent(mContext,WeatherActivity.class);
					intent.putExtra("county_name", countyName);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvince(); //����ʡ������
	}
	
	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryProvince(){
		provinceList = coolWeatherDB.loadProvices();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
				
			}
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleText.setText("�й�");
			
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null, "province");
		}
	}
	
	/**
	 * ��ѯѡ��ʡ�����е��У����ȴ����ݿ��в�ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryCities(){
		cityList = coolWeatherDB.loadCitys(selectedProvince.getId());
		
		if (cityList.size() > 0) {
			dataList.clear();
			
			for (City city : cityList) {
				dataList.add(city.getCityName());
				System.out.println(city.getCityCode());
			}
			
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
			
		}
	}
	
	/**
	 * ��ѯѡ���������е��أ����ȴ����ݿ��в�ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryCounties(){
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		
		
		if (countyList.size() > 0) {
			dataList.clear();
			
			for (County county : countyList) {
				dataList.add(county.getCountyNanme());
			}
			
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
	 * */
	private void queryFromServer(final String code, final String type){
		String address;
		
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
					
				}else if ("city".equals(type)) {
					
					result = Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
					
					
				}else if ("county".equals(type)) {
					
					result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
					
				
				if (result) {
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							
							if ("province".equals(type)) {
								queryProvince();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaACtivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/**
	 * ��ʾ�Ի���
	 * */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * �رնԻ���
	 * */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		
	}
	
	/**
	 * ����Back��ť�����ݵ�ǰ�ļ����жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 * */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		}else if (currentLevel == LEVEL_CITY) {
			queryProvince();
		}else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}
