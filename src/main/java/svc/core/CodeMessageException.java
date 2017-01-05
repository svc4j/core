package svc.core;

import svc.core.base.StringUtil;

public class CodeMessageException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int _code = 0;

	public CodeMessageException(Throwable e, int p_code, Object... args) {
		super(StringUtil.buildString(args, -1, " "), e);
		_code = p_code;
	}

	public CodeMessageException(int p_code, Object... args) {
		super(StringUtil.buildString(args, -1, " "));
		_code = p_code;
	}

	public CodeMessageException(Integer code, String message) {
		super(message);
		_code = code;
	}

	public int getCode() {
		return _code;
	}

}
