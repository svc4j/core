package svc.core.base;

import java.util.Date;

public class TypeUtil {

	static public Class<?> fixClassType(Class<?> cls) {
		if (cls == int.class)
			return Integer.class;
		if (cls == short.class)
			return Short.class;
		if (cls == long.class)
			return Long.class;
		if (cls == float.class)
			return Float.class;
		if (cls == double.class)
			return Double.class;
		if (cls == boolean.class)
			return Boolean.class;
		if (cls == byte.class)
			return Byte.class;
		if (cls == char.class)
			return Character.class;
		return cls;
	}

	static public Class<?> fixArrayClassType(Class<?> cls) {
		if (cls == int[].class)
			return Integer[].class;
		if (cls == short[].class)
			return Short[].class;
		if (cls == long[].class)
			return Long[].class;
		if (cls == float[].class)
			return Float[].class;
		if (cls == double[].class)
			return Double[].class;
		if (cls == boolean[].class)
			return Boolean[].class;
		if (cls == byte[].class)
			return Byte[].class;
		if (cls == char[].class)
			return Character[].class;
		return cls;
	}

	@SuppressWarnings("unchecked")
	static public <E> E cast(Class<E> real_to, Object obj) {
		// 将原始类型转换为对象类型
		Class<?> to = fixClassType(real_to);

		// null 时返回各种类型的默认值
		if (obj == null) {
			if (Number.class.isAssignableFrom(to))
				return (E) (Number) 0;
			else if (Boolean.class.isAssignableFrom(to))
				return (E) Boolean.FALSE;
			else if (String.class.isAssignableFrom(to))
				return (E) "";
			else
				return null;
		}

		try{
			if (to.isAssignableFrom(obj.getClass()))
				return (E) obj;
			if (to == Integer.class) {
				if (obj instanceof Number)
					return (E) (Integer) (((Number) obj).intValue());
				else
					return (E) (Integer) Integer.parseInt(obj.toString());
			} else if (to == Long.class) {
				if (obj instanceof Number)
					return (E) (Long) (((Number) obj).longValue());
				else
					return (E) (Long) Long.parseLong(obj.toString());
			} else if (to == Double.class) {
				if (obj instanceof Number)
					return (E) (Double) (((Number) obj).doubleValue());
				else
					return (E) (Double) Double.parseDouble(obj.toString());
			} else if (to == Float.class) {
				if (obj instanceof Number)
					return (E) (Float) (((Number) obj).floatValue());
				else
					return (E) (Float) Float.parseFloat(obj.toString());
			} else if (to == Boolean.class) {
				if (obj instanceof Number)
					return (E) (Boolean) (((Number) obj).intValue() != 0);
				else
					return (E) (Boolean) ("".equals(obj.toString()));
			} else if (to == String.class) {
				if (obj instanceof String)
					return (E) obj;
				else
					return (E) obj.toString();
			}
			return (E) obj;
		}catch(Exception e){
			return (E) obj;
		}
	}

	public static boolean isJavaObjectType(Class<?> classInfo) {
		Class<?>[] cls = new Class[] { Object.class, Byte.class, byte.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class, Float.class, float.class, Double.class,
				double.class, String.class, char.class, Character.class, boolean.class, Boolean.class, Date.class };
		boolean isJavaType = false;
		for (Class<?> c : cls) {
			isJavaType = classInfo == c ? true : isJavaType;
			if (isJavaType) {
				return isJavaType;
			}
		}
		return isJavaType;
	}

	public static boolean isJavaArrayType(Class<?> classInfo) {
		Class<?>[] cls = new Class[] { Object[].class, Byte[].class, byte[].class, Short[].class, short[].class, Integer[].class, int[].class, Long[].class, long[].class, Float[].class, float[].class, Double[].class,
				double[].class, String[].class, char[].class, Character[].class, boolean[].class, Boolean[].class};
		boolean isJavaType = false;
		for (Class<?> c : cls) {
			isJavaType = classInfo == c ? true : isJavaType;
			if (isJavaType) {
				return isJavaType;
			}
		}
		return isJavaType;
	}

}
