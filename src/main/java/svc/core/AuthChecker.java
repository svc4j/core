package svc.core;


import com.alibaba.fastjson.JSONObject;

public interface AuthChecker {

	boolean checkAuthLevel(JSONObject args);
}
