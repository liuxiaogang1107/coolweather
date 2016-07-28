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
	 * 设置选择状态
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
	 * 适配数据源
	 * */
	private List<String> dataList = new ArrayList<String>();
	/**
	 * 省列表
	 * */
	private List<Province> provinceList;
	/**
	 * 市列表
	 * */
	private List<City> cityList;
	/**
	 * 城列表
	 * */
	private List<County> countyList;
	/**
	 * 选中的省份
	 * */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 * */
	private City selectedCity;
	/**
	 * 选中的级别
	 * */
	private int currentLevel;
	/**
	 * 是否从WeatherActivity跳转过来
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
					System.out.println(selectedCity.getCityName()+" 我点击了"+" "+selectedCity.getCityCode());
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
		queryProvince(); //加载省级数据
	}
	
	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到在去服务器上查询
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
			titleText.setText("中国");
			
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null, "province");
		}
	}
	
	/**
	 * 查询选中省内所有的市，优先从数据库中查询，如果没有查询到再去服务器上查询
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
	 * 查询选中市内所有的县，优先从数据库中查询，如果没有查询到在去服务器上查询
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
	 * 根据传入的代号和类型从服务器上查询省市县数据
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
					//通过runOnUiThread()方法回到主线程处理逻辑
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
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaACtivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/**
	 * 显示对话框
	 * */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭对话框
	 * */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		
	}
	
	/**
	 * 捕获Back按钮，根据当前的级别判断，此时应该返回市列表、省列表、还是直接退出
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
