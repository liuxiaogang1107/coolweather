package util;
/**
 * »Øµô½Ó¿Ú
 * */
public interface HttpCallbackListener {
	void onFinish(String response);
	
	void onError(Exception e);
}
