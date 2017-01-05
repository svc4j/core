package svc.core;

import java.util.HashMap;
import java.util.Map;

import svc.core.base.TypeUtil;

public abstract class MySession {

	private static Map<String, Map<String,Object> > _SESSIONS = new HashMap<>();
	private static ThreadLocal<Map<String,Object>> _LOCAL_SESSION = new ThreadLocal<>();
	
	public static void init( String session_id ){
		Map<String,Object> sess = _SESSIONS.get(session_id);
		if( sess == null ){
			sess = new HashMap<>();
			_SESSIONS.put(session_id, sess);
		}
		_LOCAL_SESSION.set(sess);
	}

	public static Object get( String key ){
		return _LOCAL_SESSION.get().get(key);
	}
	
	public static Integer getInteger( String key ){
		return TypeUtil.cast(Integer.class, _LOCAL_SESSION.get().get(key));
	}
	
	public static Boolean getBoolean( String key ){
		return TypeUtil.cast(Boolean.class, _LOCAL_SESSION.get().get(key));
	}
	
	public static String getString( String key ){
		return TypeUtil.cast(String.class, _LOCAL_SESSION.get().get(key));
	}

	public static Map<String,Object> getSession(){
		return _LOCAL_SESSION.get();
	}

	public static void set( String key, Object value ){
		_LOCAL_SESSION.get().put(key, value);
	}

}
