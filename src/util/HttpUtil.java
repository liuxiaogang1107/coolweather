package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * 服务器连接建立
 * */
public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				HttpURLConnection connection = null;
				
				try {
					URL url = new URL(address);
					Log.d("天气地址", address);
					connection = (HttpURLConnection) url.openConnection();
					
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Content-type", "text/html");
					connection.setRequestProperty("Charset", "UTF-8");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					
					
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					
					String line;
					while ((line = reader.readLine()) !=null) {
						response.append(line);
						Log.d("响应的内容",line+"YOU");
					}
					
					if (listener != null) {
						//回调onFinish()方法
						listener.onFinish(response.toString());
					}
						
					
					
				} catch (Exception e) {
					
					if (listener != null) {
						//回调onError()方法
						listener.onError(e);
					}
				}finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
		
		
	}
}
