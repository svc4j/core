package svc.core.base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class Log {
	static private int LEVEL_DEBUG = 0;
	static private int LEVEL_INFO = 1;
	static private int LEVEL_WARN = 2;
	static private int LEVEL_ERROR = 3;
	static private int LEVEL_OFF = 4;
	static private List<String> LEVELS = Arrays.asList("DEBUG", "INFO", "WARN", "ERROR","OFF");
	static private int LOG_LEVEL = -1;
	static private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static public void debug(Object... args) {
		_log(LEVEL_DEBUG, args);
	}

	static public void info(Object... args) {
		_log(LEVEL_INFO, args);
	}

	static public void warn(Object... args) {
		_log(LEVEL_WARN, args);
	}

	static public void error(Object... args) {
		_log(LEVEL_ERROR, args);
	}

	static public void setLevel(String level) {
		LOG_LEVEL = LEVELS.indexOf(level.toUpperCase());
		if (LOG_LEVEL < 0)
			LOG_LEVEL = 0;
	}

	static private void _log(int level, Object[] args) {
		if (LOG_LEVEL == -1) {
			setLevel(Config.get("LOG_LEVEL", "INFO"));
		}
		if (level < LOG_LEVEL)
			return;
		int args_num = args.length;
		Throwable ex = null;
		if (args_num > 0 && (args[args_num - 1] == null || args[args_num - 1] instanceof Throwable)) {
			ex = (Throwable) args[args_num - 1];
			args_num--;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(DATE_FORMAT.format(new Date()));
		sb.append("\t").append(LEVELS.get(level));
		for (int i = 0; i < args_num; i++) {
			sb.append("\t").append(args[i]);
		}
		if (ex != null) {
			sb.append("\n").append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n\t")
					.append(StringUtil.buildString(ex.getStackTrace(), -1, "\n	"));
			Throwable th = ex.getCause();
			while(th!=null){
				sb.append("\nCaused by: ").append(th.getClass().getName()).append("\n\t")
				.append(StringUtil.buildString(th.getStackTrace(), -1, "\n	"));
				th = th.getCause();
			}
		}

		System.out.println(sb.toString());
	}

}
