package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text. TextUtils;
import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;


public class Utility {
	
	
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response){
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			
			if (allProvinces != null && allProvinces.length > 0) {
				
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					
					
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			
			if (allCities != null && allCities.length > 0) {
				
				for (String c : allCities) {
					String[] array = c.split("\\|");
					
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					
					
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			
			if (allCounties != null && allCounties.length > 0) {
				
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					
				
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	
	public static void handWeatherResponse(Context context, String response){
		
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("result");
			JSONObject jsonObject2 = weatherInfo.getJSONObject("data");
			JSONObject jsonObject3 = jsonObject2.getJSONObject("realtime");
			
			String cityName = jsonObject3.getString("city_name");
			String weatherCode = jsonObject3.getString("city_code");
			//获取白天夜晚的温度
			JSONObject jsonObject4 = jsonObject3.getJSONObject("weather");
			
			String temp1 = jsonObject4.getString("temperature");
			
			String weatherDesp = jsonObject4.getString("info");
			String publishTime = jsonObject3.getString("time");
			
			saveWeatherInfo(context, cityName, weatherCode, temp1, weatherDesp, publishTime);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String weatherDesp, String publishTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
			editor.putBoolean("city_selected", true);
			editor.putString("weather_code", weatherCode);
			editor.putString("temp1", temp1);
			editor.putString("city_name", cityName);
			editor.putString("weather_desp", weatherDesp);
			editor.putString("publish_time", publishTime);
			editor.putString("current_data", sdf.format(new Date()));
			editor.commit();
	}
}
