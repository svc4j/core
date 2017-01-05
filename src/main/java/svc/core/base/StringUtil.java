package svc.core.base;

public class StringUtil {

	static public String str(Object... args) {
		return buildString(args, -1, null);
	}

	static public String strWith(String space, Object... args) {
		return buildString(args, -1, space);
	}

	static public String buildString(Object[] args, int args_num, String space) {
		StringBuilder sb = new StringBuilder();
		boolean is_first = true;
		if (args_num < 0 || args_num > args.length)
			args_num = args.length;
		for (int i = 0; i < args_num; i++) {
			if (space != null) {
				if (is_first)
					is_first = false;
				else
					sb.append(space);
			}
			sb.append(args[i]);
		}
		return sb.toString();
	}
	
	static public String buildString(Object[] args, int begin_index, int end_index,String space) {
		StringBuilder sb = new StringBuilder();
		boolean is_first = true;
		if (begin_index < 0 || begin_index > args.length)
			begin_index = 0;
		if(end_index < begin_index || end_index > args.length ){
			end_index = args.length;
		}
		for (int i = begin_index; i < end_index; i++) {
			if (space != null) {
				if (is_first)
					is_first = false;
				else
					sb.append(space);
			}
			sb.append(args[i]);
		}
		return sb.toString();
	}

	static public String join(String space, Iterable<?> args) {
		StringBuilder sb = new StringBuilder();
		boolean is_first = true;
		for (Object obj : args) {
			if (space != null) {
				if (is_first)
					is_first = false;
				else
					sb.append(space);
			}
			sb.append(obj);
		}
		return sb.toString();
	}

}
