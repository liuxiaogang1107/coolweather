package util;
/**
 * �ص��ӿ�
 * */
public interface HttpCallbackListener {
	void onFinish(String response);
	
	void onError(Exception e);
}
