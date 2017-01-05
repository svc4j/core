package svc.core;


import com.alibaba.fastjson.JSONObject;
import com.test.service.User;

import svc.core.base.JSONUtil;

public class DB {

	public JSONObject query( String sql ){
		return JSONUtil.toJSONObject(new User.UserInfo("Noname", 10));
	}
	
	static public class Factory implements Inject {

		public Object fetch() {
//			Log.debug("fetch DB");
			return new DB();
		}

		public void give(Object obj) {
//			Log.debug("give DB");
		}

	}

}
