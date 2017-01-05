package svc.core;

import com.alibaba.fastjson.JSONObject;

import svc.core.AuthChecker;
import svc.core.Callable;
import svc.core.base.Log;

public abstract class MyCallable {

	static Callable _callable = new Callable();
	static{
		_callable.setAuthChecker(1, new LoginAuthChecker());
		_callable.setAuthChecker(2, new AdminAuthChecker());
		_callable.setInject(DB.class, new DB.Factory());
	}

	private static boolean _ENABLED_ACCESSLOG = true;
	public static void switchAccessLog( boolean is_enabled ) {
		_ENABLED_ACCESSLOG = is_enabled;
	}

	static public JSONObject call(String target) {
		return call(target, null);
	}

	static public JSONObject call(String target, JSONObject args) {
		long start_time = System.currentTimeMillis();
		JSONObject result = new JSONObject();

		Exception ex = null;
		try {
			JSONObject data = (JSONObject) _callable.call("com.test.service." + target, args,
					svc.core.ann.Callable.class, JSONObject.class);
			result.put("code", 200);
			result.put("message", "");
			result.put("data", data);
		} catch (CodeMessageException e) {
			ex = e;
			result.put("code", e.getCode());
			result.put("message", e.getMessage());
		} catch (Exception e) {
			ex = e;
			result.put("code", 500);
			result.put("message", e.getMessage());
		} finally {
			if (_ENABLED_ACCESSLOG) {
				long used_time = System.currentTimeMillis() - start_time;
				Log.debug("Call", target, result.getInteger("code"),
						result == null ? 0 : result.toString().length(), used_time, ex);
			}
		}

		return result;
	}

	static public class LoginAuthChecker implements AuthChecker{

		@Override
		public boolean checkAuthLevel(JSONObject args) {
			return MySession.getBoolean("logined");
		}
		
	}
	
	static public class AdminAuthChecker implements AuthChecker{
		
		@Override
		public boolean checkAuthLevel(JSONObject args) {
			return MySession.getBoolean("isAdmin");
		}
		
	}
}
