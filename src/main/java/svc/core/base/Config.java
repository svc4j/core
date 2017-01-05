package svc.core.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

// TODO 需要进行配置的测试
public class Config {
	private static HashMap<String, String> _CONFIG_MAP = new HashMap<String, String>();

	static {
		/**
		 * 环境变量的配置优先级最高，项目的环境变量优先级最低，后面的配置覆盖前面的配置
		 */
		try {
			// 获得项目环境变量
			_loadConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 系统环境变量
		_CONFIG_MAP.putAll(System.getenv());
		// 环境变量
		_CONFIG_MAP.putAll(_propertiesToMap(System.getProperties(), null));
	}

	static public void reloadProperties() {
		_CONFIG_MAP.putAll(System.getenv());
		_CONFIG_MAP.putAll(_propertiesToMap(System.getProperties(), null));
	}
	
	// 根据优先级获取配置信息（整数）
	static public int getInt(String name, String name2, int default_value) {
		return Integer.parseInt(get(name, name2, Integer.toString(default_value)));
	}

	// 根据优先级获取配置信息
	static public String get(String name, String name2, String default_value) {
		String value = get(name, null);
		if (value != null)
			return value;
		if (name2.equals(name))
			return default_value;
		return get(name2, default_value);
	}

	// 获取配置信息（整数）
	static public int getInt(String name, int default_value) {
		return Integer.parseInt(get(name, Integer.toString(default_value)));
	}

	static public String get(String name) {
		String value = _CONFIG_MAP.get(name);
		if (value != null && !"".equals(value)) {
			return value;
		}
		return null;
	}

	// 获取配置信息
	static public String get(String name, String default_value) {
		String value = _CONFIG_MAP.get(name);
		if (value != null && !"".equals(value)) {
			return value;
		}
		return default_value;
	}

	// 获取配置信息
	public static Map<String, String> getAll(String prefix) {
		if (prefix == null) {
			return null;
		}
		Map<String, String> result_map = new HashMap<String, String>();
		for (Entry<String, String> data : _CONFIG_MAP.entrySet()) {
			String key = data.getKey();
			String value = data.getValue();
			if (key.startsWith(prefix)) {
				result_map.put(key, value);
			}
		}
		return result_map;
	}

	/**
	 * 获取配置信息（返回值中的key不带前缀prefix） TODO(这里用一句话描述这个方法的作用)
	 * 
	 * @author: 胡欢
	 * @Title: getInfoNoPrefix
	 * @param prefix
	 * @return Map<String,String>
	 */
	public static Map<String, String> getAllNoPrefix(String prefix) {
		if (prefix == null) {
			return null;
		}

		int len = prefix.length();
		if (len > 0 && prefix.charAt(prefix.length() - 1) != '_') {
			len = len + 1;
		}

		Map<String, String> result_map = new HashMap<String, String>();
		for (Entry<String, String> data : _CONFIG_MAP.entrySet()) {
			String key = data.getKey();
			String value = data.getValue();
			if (key.startsWith(prefix)) {
				result_map.put(key.substring(len), value);
			}
		}
		return result_map;
	}

	/**
	 * 根据文件加载配置
	 * 
	 * @param f
	 * @return
	 */
	static private Properties loadConfig(File f) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			Properties cfg = new Properties();
			cfg.load(fis);
			return cfg;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加载除config.ini之外的配置文件
	 * 
	 * @throws Exception
	 */
	static private void _loadConfig() throws Exception {
		String path = Config.class.getClassLoader().getResource("").toURI().getPath();
		File file = new File(path);
		File[] files = file.listFiles();

		Map<String, String> configMap = null;
		for (File f : files) {
			String _fileName = f.getName().toUpperCase();
			if (_fileName.endsWith(".INI")) {
				Properties prop = loadConfig(f);

				// 优先加载config.ini配置文件
				if (_fileName.startsWith("CONFIG")) {
					configMap = _propertiesToMap(prop, null);
				} else {
					String _type = _fileName.substring(0, _fileName.indexOf("."));
					_CONFIG_MAP.putAll(_propertiesToMap(prop, _type + "_"));
				}
			}
		}
		// config.ini优先级高于普通配置文件
		if (configMap != null) {
			_CONFIG_MAP.putAll(configMap);
		}
	}

	/**
	 * 将Properties配置转成Map对象
	 * 
	 * @param cfg
	 * @param prefix
	 * @return
	 */
	static private Map<String, String> _propertiesToMap(Properties cfg, String prefix) {
		Map<String, String> result = new HashMap<String, String>();
		if (cfg != null) {
			Set<Map.Entry<Object, Object>> it = cfg.entrySet();
			for (Map.Entry<Object, Object> entry : it) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (prefix != null && !"".equals(prefix)) {
					result.put(prefix + key, value);
				} else {
					result.put(key, value);
				}
			}
		}
		return result;
	}
}
