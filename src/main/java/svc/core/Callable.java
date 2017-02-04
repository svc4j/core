package svc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import svc.core.ann.*;
import svc.core.base.JSONUtil;
import svc.core.base.StringUtil;
import svc.core.base.TypeUtil;

import svc.core.base.Log;

/**
 * 服务或控制器类 所有对服务或控制器的调用都经过此类做代理 用反射技术对输入输出数据做校验和统一
 * 
 * @author star
 */
public class Callable {
	private Map<Class<?>, Inject> _injects = new HashMap<>();
	private ThreadLocal<Map<String, InjectSet>> _localSessions = new ThreadLocal<>();
	private Map<String, CallableInfo> _objects = new HashMap<String, CallableInfo>();
	private Map<Integer, AuthChecker> _authCheckers = new HashMap<>();
	private Map<String, ParameterChecker> _parmCheckers = new HashMap<>();
	private Pattern _authLevelPattern = Pattern.compile("authLevel=(.*?)[,\\)]");

	public Callable() {
		setParmChecker("Regex", new RegexChecker());
		setParmChecker("ChildRegex", new ChildRegexChecker());
	}

	public void setAuthChecker(int level, AuthChecker checker) {
		_authCheckers.put(level, checker);
	}

	public void setParmChecker(String type, ParameterChecker checker) {
		_parmCheckers.put(type, checker);
	}

	public void setInject(Class<?> type, Inject inject) {
		_injects.put(type, inject);
	}

	public Object call(String target, JSONObject args, Class<? extends Annotation> callable_ann,
			Class<?> return_type) throws CodeMessageException {
		
		String call_class = null;
		String method_name = null;
		int pos = target.lastIndexOf('.');
		if( pos > 0 ){
			call_class = target.substring(0, pos);
			method_name = target.substring(pos+1);
		}

		if (call_class == null || method_name == null) {
			throw new CodeMessageException(404, "Not available call target");
		}

		CallableInfo call_info = null;
		// 判断缓存是否存在这个类，若不存在则加载，然后存入缓存中
		synchronized (_objects) {
			call_info = _objects.get(call_class);
			if (call_info == null) {
				call_info = _load(call_class, callable_ann, return_type);
				_objects.put(call_class, call_info);
			}
		}

		if (call_info == null || call_info.obj == null) {
			throw new CodeMessageException(404, "Target not exists");
		}

		// 允许无参数调用
		if (args == null) {
			args = new JSONObject();
		}
		// 开始处理
		// boolean is_top_db = false;
		// boolean db_is_ok = false;
		// DB db = null;
		List<String> topInjects = new ArrayList<>();
		try {
			ActionInfo m = call_info.fetch(method_name);
			int level = m.authLevel;
			// 只要有任何一个大于要求级别的验证通过就允许访问
			if (level > 0) {
				boolean auth_ok = false;
				for (Map.Entry<Integer, AuthChecker> item : _authCheckers.entrySet()) {
					if (item.getKey() < level)
						continue;
					AuthChecker ac = item.getValue();
					if (ac != null && ac.checkAuthLevel(args)) {
						auth_ok = true;
						break;
					}
				}
				// AuthChecker ac = _authCheckers.get(level);
				// if (ac != null && !ac.checkAuthLevel(args)) {
				// throw new CallableException(403, "No access");
				// }
				if (auth_ok == false) {
					throw new CodeMessageException(403, "No access");
				}
			}

			// 对注解Parm进行校验和赋值
			List<Object> real_args = new ArrayList<Object>();
			List<String> error_args = new ArrayList<String>();
			// boolean is_first_db_parm = true;
			for (ParmInfo parm_info : m.parmInfos) {
				if (parm_info.name.equals("args") && parm_info.type == JSONObject.class) {
					real_args.add(args);
					continue;
				}

				// if (parm_info.type == DB.class) {
				// if (is_first_db_parm) {
				// Log.trace("2", parm_info.dbName, Config.getAll("DB"));
				//
				// db = (DB) localSession.get();
				// if (db == null) {
				// db = DB.getDB(parm_info.dbName);
				// localSession.set(db);
				// is_top_db = true;
				//// db.begin();
				// }
				// real_args.add(db);
				// } else {
				// real_args.add(DB.getDB(parm_info.dbName));
				// is_first_db_parm = false;
				// }
				// continue;
				// }

				Object tmp_parm = args.get(parm_info.name);
				Class<?> tmpClass = null;
				Object arg = null;
				try {
					// 类型自动转换
					if (tmp_parm != null) {
						tmpClass = tmp_parm.getClass();
						if (tmpClass.equals(parm_info.type)) {
							arg = tmp_parm;
						} else {
							if (parm_info.type.isArray()) {
								JSONArray tmpArray = new JSONArray();
								if (tmpClass.isArray() || Collection.class.isAssignableFrom(tmpClass)) {
									tmpArray = args.getJSONArray(parm_info.name);
								} else if (tmp_parm instanceof String && ((String) tmp_parm).matches("^\\[.*\\]$")) {
									tmpArray = JSONUtil.toJSONArray((String) tmp_parm);
								} else {
									tmpArray.add(tmp_parm);
								}
								arg = JSONUtil.toArray(tmpArray, parm_info.subType);
							} else if (JSONObject.class.isAssignableFrom(parm_info.type)) {
								arg = JSONUtil.toJSONObject(args.getString(parm_info.name));
							} else if (JSONArray.class.isAssignableFrom(parm_info.type)) {
								arg = JSONUtil.toJSONArray(args.getString(parm_info.name));
							} else if (List.class.isAssignableFrom(parm_info.type)) {
								JSONArray tmpArray = new JSONArray();
								if (tmpClass.isArray() || Collection.class.isAssignableFrom(tmpClass)) {
									tmpArray = args.getJSONArray(parm_info.name);
								} else if (tmp_parm instanceof String && ((String) tmp_parm).matches("^\\[.*\\]$")) {
									tmpArray = JSONUtil.toJSONArray((String) tmp_parm);
								} else {
									tmpArray.add(tmp_parm);
								}
								arg = JSONUtil.toList(tmpArray, parm_info.subType);
							} else if (Map.class.isAssignableFrom(parm_info.type)) {
								if (Map.class.isAssignableFrom(tmpClass)) {
									arg = JSONUtil.toMap(args.getJSONObject(parm_info.name), parm_info.subType);
								} else if (tmp_parm instanceof String && ((String) tmp_parm).matches("^\\{.*\\}$")) {
									arg = JSONUtil.toMap(JSONUtil.toJSONObject((String) tmp_parm), parm_info.subType);
								} else {
									arg = tmp_parm;
								}
							} else {
								arg = TypeUtil.cast(parm_info.type, tmp_parm);
								// cast to java object
								if (arg.getClass().getName().indexOf("JSONObject") != -1
										&& parm_info.type.getName().indexOf("JSONObject") == -1) {
									arg = JSONUtil.toObject((JSONObject) arg, parm_info.type);
								}
							}
						}
					} else {
						// 没有的参数从线程本地化中获取
						String tl_key = parm_info.type + "::" + parm_info.name;
						
						Map<String, InjectSet> thread_injects = _localSessions.get();
						if (thread_injects == null) {
							thread_injects = new HashMap<>();
							_localSessions.set(thread_injects);
						}

						InjectSet inject_set = thread_injects.get(tl_key);
						Object inject_object = null;
						if (inject_set != null) {
							inject_object = inject_set.obj;
						} else {
							// 第一次从工厂获取
							Inject factory = _injects.get(parm_info.type);
							if (factory != null) {
								inject_object = factory.fetch();
								if (inject_object != null) {
									inject_set = new InjectSet(factory, inject_object);
									thread_injects.put(tl_key, inject_set);
									topInjects.add(tl_key);
								}
							}
						}
						arg = inject_object;
					}
					// 验证参数
					String error;
					if ((error = _checkParameter(arg, parm_info)) != null) {
						error_args.add(error);
					} else {
						real_args.add(arg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					error_args.add(StringUtil.str(parm_info.name, " Parameter type not match, from ",
							JSON.toJSONString(tmp_parm), " to ", parm_info.type.getTypeName(),
							parm_info.subType != null ? "<" + parm_info.subType.getTypeName() + ">" : ""));
				}
			}
			if (error_args.size() > 0) {
				throw new CodeMessageException(400,
						StringUtil.str("Parameter check faild: ", StringUtil.join(";", error_args)));
			}
			Object result = m.method.invoke(call_info.obj, real_args.toArray());
			// db_is_ok = true;
			return result;
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new CodeMessageException(e, 500, e.getMessage());
		} catch (CodeMessageException e) {
			// Log.error(e, e.getMessage());
			throw e;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CodeMessageException) {
				CodeMessageException call_e = (CodeMessageException) e.getTargetException();
				throw call_e;
			} else {
				throw new CodeMessageException(e.getTargetException(), 500, e.getTargetException().getMessage());
			}
		} catch (Exception e) {
			Log.error(e, e.getMessage());
			if (e instanceof SQLException) {
				throw new CodeMessageException(e, 510, "Sql error！");
			} else {
				throw new CodeMessageException(e, 500, e.getMessage());
			}
		} finally {
			for (String tl_key : topInjects) {
				Map<String, InjectSet> thread_injects = _localSessions.get();
				if (thread_injects != null) {
					InjectSet inject_set = thread_injects.get(tl_key);
					inject_set.factory.give(inject_set.obj);
					thread_injects.remove(tl_key);
				}
			}
		}
	}

	private class InjectSet {
		public InjectSet(Inject factory, Object obj) {
			this.factory = factory;
			this.obj = obj;
		}

		public Inject factory;
		public Object obj;
	}

	private String _checkParameter(Object obj, ParmInfo info) {
		if (obj == null && info.orgType.isPrimitive()) {
			return StringUtil.strWith(" ", info.name, "is primitive", info.orgType, ",can't be null");
		}
		Check[] checks = info.checks;

		// 验证参数是否存在
		if (checks != null && obj == null) {
			// 要求的参数不存在
			return StringUtil.str(info.name, " is null");
		}

		if (obj == null)
			return null;

		// 验证参数的类型是否匹配
		if (obj.getClass() != info.type && !info.type.isAssignableFrom(obj.getClass())) {
			// 参数类型不匹配
			return StringUtil.str(info.name, " type is ", obj.getClass(), " require ", info.type);
		}

		// 验证集合的子类型
		Collection<Object> list = getChilds(obj);
		for (Object item : list) {
			if (item.getClass() != info.subType && !info.subType.isAssignableFrom(item.getClass())) {
				// 参数类型不匹配
				return StringUtil.str(info.name, " subType is ", item.getClass(), " require ", info.subType);
			}
		}

		if (checks != null) {
			for (Check check : checks) {
				ParameterChecker checker = _parmCheckers.get(check.type());
				if (checker != null) {
					try {
						if (checker.check(obj, check.value()) == false) {
							return StringUtil.str(info.name, " ", obj.toString(), " check failed with ", check.type(),
									" require ", check.value());
						}
					} catch (Exception e) {
						return StringUtil.str(info.name, " check failed with ", check.type(), " require ",
								check.value(), "	", e.getMessage());
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private CallableInfo _load(String call_class, Class<? extends Annotation> callable_ann, Class<?> return_type)
			throws CodeMessageException {
		// 用反射机制读取服务或控制器信息
		CallableInfo call_info = new CallableInfo();
		call_info.classPath = call_class;
		try {
			// 获得此类的class
			call_info.cls = (Class<Object>) Class.forName(call_class);
			// 没有 Action 注解的忽略
			if (!call_info.cls.isAnnotationPresent(callable_ann)) {
				throw new CodeMessageException(404, call_class, "Not Callable Object，Lost ", callable_ann.getName(),
						" Annotation");
			}
			// 创建对象
			call_info.obj = call_info.cls.newInstance();

			// 开始处理类注解
			Annotation callable_ann_object = call_info.cls.getAnnotation(callable_ann);

			call_info.authLevel = 1;
			try {
				Matcher m = _authLevelPattern.matcher(callable_ann_object.toString());
				if (m.find()) {
					call_info.authLevel = Integer.parseInt(m.group(1));
				}
			} catch (Exception e) {
			}

			// 遍历说有方法
			Method[] methods = call_info.cls.getMethods();
			for (Method tmp_method : methods) {
				// Service 必须返回 JSONObject
				if (!return_type.isAssignableFrom(tmp_method.getReturnType())) {
					continue;
				}

				// 处理方法，生成 MethodInfo
				String tmp_method_name = tmp_method.getName();
				Parameter[] parms = tmp_method.getParameters();
				ActionInfo action_info = new ActionInfo();
				// 开始处理参数注解
				{
					Action action_ann = tmp_method.getAnnotation(Action.class);
					if (action_ann != null && action_ann.authLevel() >= 0) {
						action_info.authLevel = action_ann.authLevel();
					} else {
						action_info.authLevel = call_info.authLevel;
					}

					action_info.parmInfos = new ArrayList<ParmInfo>();
					for (int i = 0; i < parms.length; i++) {
						Checks ann_checks = parms[i].getAnnotation(Checks.class);
						Check ann_check = parms[i].getAnnotation(Check.class);
						// DBName ann_db = parms[i].getAnnotation(DBName.class);

						ParmInfo parm_info = new ParmInfo();
						parm_info.name = parms[i].getName();
						parm_info.orgType = parms[i].getType();
						parm_info.type = TypeUtil.fixClassType(parms[i].getType());
						parm_info.subType = null;

						if (parm_info.type.isArray()) {
							parm_info.subType = parm_info.type.getComponentType();
						} else {
							// 读取集合类型的子类型
							String sub_type = parms[i].getParameterizedType().getTypeName();
							if (sub_type.startsWith("java.util.List<")) {
								sub_type = sub_type.substring(15, sub_type.length() - 1).trim();
							} else if (sub_type.startsWith("java.util.Map<java.lang.String,")) {
								sub_type = sub_type.substring(31, sub_type.length() - 1).trim();
							} else {
								sub_type = "";
							}

							if (!("".equals(sub_type))) {
								parm_info.subType = Class.forName(sub_type);
							}
						}
						if (parm_info.subType == null) {
							parm_info.subType = Object.class;
						}

						parm_info.checks = null;
						if (ann_checks != null) {
							parm_info.checks = ann_checks.value();
						} else if (ann_check != null) {
							parm_info.checks = new Check[] { ann_check };
						}
						// parm_info.dbName = ann_db == null ? null :
						// ann_db.value();
						action_info.parmInfos.add(parm_info);
					}

				}

				action_info.method = tmp_method;
				call_info.actions.put(tmp_method_name, action_info);
			}

			return call_info;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new CodeMessageException(e, 404, call_class, "not exists");
		}
	}

	static protected String getJSONStringForCheck(Object obj) {
		if (JSON.class.isAssignableFrom(obj.getClass()) || String.class.isAssignableFrom(obj.getClass())) {
			return obj.toString();
		} else {
			return JSONUtil.toString(obj);
		}
	}

	@SuppressWarnings("unchecked")
	static protected Collection<Object> getChilds(Object obj) {
		Class<?> cls = obj.getClass();
		if (Collection.class.isAssignableFrom(cls)) {
			return (Collection<Object>) obj;
		}

		if (cls.isArray()) {
			return Arrays.asList(obj);
		}

		if (Map.class.isAssignableFrom(cls)) {
			return ((Map<String, Object>) obj).values();
		}
		return Arrays.asList(obj);
	}

	static protected class CallableInfo {
		protected String classPath = null;
		protected Class<Object> cls = null;
		protected Object obj = null;
		int authLevel;
		protected Map<String, ActionInfo> actions = new HashMap<String, ActionInfo>();

		protected ActionInfo fetch(String method_name) throws CodeMessageException {
			if (method_name == null)
				throw new CodeMessageException(404, classPath, method_name, "not exists");
			ActionInfo m = actions.get(method_name);
			if (m == null)
				throw new CodeMessageException(404, classPath, method_name, "not exists or lost @Action");
			return m;
		}
	}

	static protected class ActionInfo {
		Method method;
		int authLevel;
		List<ParmInfo> parmInfos;
	}

	static protected class ParmInfo {
		String name;
		Class<?> orgType;
		Class<?> type;
		Class<?> subType;
		protected Check[] checks;
		// protected CheckType[] checkTypes;
		String dbName;
	}

	static protected class RegexChecker implements ParameterChecker {

		public Boolean check(Object obj, String check) {
			return obj.toString().matches(check);
		}
	}

	static protected class ChildRegexChecker implements ParameterChecker {

		public Boolean check(Object obj, String check) throws Exception {
			Collection<Object> list = getChilds(obj);
			for (Object item : list) {
				String json_str = getJSONStringForCheck(item);
				if (json_str.matches(check) == false) {
					throw new Exception(json_str);
				}
			}
			return true;
		}
	}

}
