package svc.core.base;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public abstract class JSONUtil {
	static public JSONObject loadJSONObject(String path) {
		String json_str = _loadFile(path);
		// Log.debug(json_str);
		if (json_str == null)
			return new JSONObject();
		try {
			return toJSONObject(json_str);
		} catch (JSONException e) {
			// Log.warn("JSON 数据加载失败", e);
			return new JSONObject();
		}
	}

	static public JSONArray loadJSONArray(String path) {
		String json_str = _loadFile(path);
		if (json_str == null)
			return new JSONArray();
		try {
			return toJSONArray(json_str);
		} catch (JSONException e) {
			// Log.warn("JSON 数据加载失败", e);
			return new JSONArray();
		}
	}

	static private String _loadFile(String path) {
		// Log.debug(ClassLoader
		// .getSystemResource("com/hfjy/doc/service/test2.Hello"));
		try (InputStream in = ClassLoader.getSystemResourceAsStream(path)) {
			byte[] buf = new byte[102400];
			int read_len = in.read(buf, 0, 102400);
			return new String(buf, 0, read_len);
		} catch (Exception e) {
			// Log.warn("无法读取 JSON 文件", path, e);
			return null;
		}
	}

	static public JSONObject obj(String key1, Object value1, Object... args) {
		JSONObject o = new JSONObject();
		o.put(key1, value1);
		if (args.length == 1 && args[0] instanceof Object[])
			args = (Object[]) args[0];
		int args_num = args.length;
		if (args_num % 2 == 1)
			args_num--;
		for (int i = 0; i < args_num; i += 2)
			o.put(args[i] instanceof String ? (String) args[i] : args[i].toString(), args[i + 1]);
		return o;
	}

	static public JSONArray arr(Object value1, Object... args) {
		JSONArray o = new JSONArray();
		o.add(value1);
		if (args.length == 1 && args[0] instanceof Object[])
			args = (Object[]) args[0];
		int args_num = args.length;
		for (int i = 0; i < args_num; i++)
			o.add(args[i]);
		return o;
	}

	static public <E> E toObject(String text, Class<E> cls) {
		return (E) JSON.parseObject(text, cls);
	}

	static public <E> List<E> toList(String text, Class<E> cls) {
		return JSON.parseArray(text, cls);
	}

	static public <E> E toObject(JSON obj, Class<E> cls) {
		return (E) JSON.toJavaObject(obj, cls);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> toList(JSONArray array, Class<E> cls) {
		List<E> list = new ArrayList<E>();
		if (JSONObject.class.isAssignableFrom(cls)) {
			for (Object item : array) {
				list.add((E) JSON.toJSON(item));
			}
		} else {
			for (Object item : array) {
				if( JSONObject.class.isAssignableFrom(item.getClass()) && !Object.class.isPrimitive() ){
					list.add( JSONUtil.toObject((JSONObject)item, cls) );
				}else{
					list.add(TypeUtil.cast(cls, item));
				}
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <E> Map<String, E> toMap(JSONObject data, Class<E> cls) {
		Map<String, E> map = new HashMap<String, E>();
		Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
		if (JSONObject.class.isAssignableFrom(cls)) {
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				map.put(entry.getKey(), (E) JSON.toJSON(entry.getValue()));
			}
		} else {
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				if( JSONObject.class.isAssignableFrom(entry.getValue().getClass()) && !Object.class.isPrimitive() ){
					map.put(entry.getKey(), JSONUtil.toObject((JSONObject)entry.getValue(), cls));
				}else{
					map.put(entry.getKey(), TypeUtil.cast(cls, entry.getValue()));
				}
			}
		}
		return map;
	}

	static public JSONObject toJSONObject(String text) {
		return JSON.parseObject(text);
	}

	static public JSONObject toJSONObject(Object obj) {
		return (JSONObject) JSON.toJSON(obj);
	}

	static public JSONObject toJSONObject(Map<String, String> data) {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}
		return obj;
	}

	static public JSONArray toJSONArray(String text) {
		return JSON.parseArray(text);
	}

	static public JSONArray toJSONArray(List<?> list) {
		return (JSONArray) JSON.toJSON(list);
	}

	static public String toString(JSON json) {
		return json.toJSONString();
	}

	static public String toString(List<?> list) {
		return toJSONArray(list).toJSONString();
	}

	static public String toString(Object obj) {
		return toJSONObject(obj).toJSONString();
	}

	public static <E> Object toArray(JSONArray array, Class<E> cls) {
		Object resObj = Array.newInstance(cls, array.size());
		int index = 0;
		for (Object item : array) {
			Array.set(resObj, index, TypeUtil.cast(cls, item));
			index++;
		}
		return resObj;
	}

	public static boolean isGoodJson(String json) {
		try {
			JSON.parse(json);
			return true;
		} catch (Exception e) {
			System.err.println("bad json: " + json);
			return false;
		}
	}
}
