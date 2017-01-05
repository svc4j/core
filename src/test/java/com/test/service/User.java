package com.test.service;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;

import svc.core.CodeMessageException;
import svc.core.DB;
import svc.core.MyCallable;
import svc.core.MySession;
import svc.core.ann.*;
import svc.core.base.JSONUtil;

@Callable
public class User {

	@Action(authLevel = 0)
	public JSONObject login() {// @Check String account, @Check String password
		MySession.set("logined", true);
		return JSONUtil.obj("logined",true);
	}
	
	@Action
	public JSONObject toAdmin() {
		MySession.set("isAdmin", true);
		return JSONUtil.obj("isAdmin",true);
	}
	
	@Action
	public JSONObject logout() {
		MySession.set("logined", false);
		MySession.set("isAdmin", false);
		return JSONUtil.obj("logined",false,"isAdmin",false);
	}

	
	@Action(authLevel=2)
	public JSONObject getAdminInfo() {
		return JSONUtil.obj("name","Star Zhang","age",29);
	}

	@Action
	public JSONObject getUserFromDB(DB db) {
		JSONObject r = MyCallable.call("User.growUp", JSONUtil.obj("user",db.query("select * from user where id=1")));
		if( r.getIntValue("code") != 200 ){
			throw new CodeMessageException(r.getIntValue("code"), r.getString("message"));
		}
		return r.getJSONObject("data");
	}

	@Action
	public JSONObject getGrowUpAge( @Check Integer age) {
		return JSONUtil.obj("age",age,"newAge", age+1);
	}

	@Action
	public JSONObject growUp( @Check UserInfo user) {
		user.setAge(user.getAge() + 1);
		user.name += " Up";
		return JSONUtil.obj("user", user);
	}

	@Action
	public JSONObject growUpMulti(@Checks({ @Check("^\\[.+\\]$"), @Check(type="ChildRegex",value="^\\{\"name\":.+\\}$") }) List<UserInfo> users) {
		for (UserInfo user : users) {
			user.setAge(user.getAge() + 1);
			user.name += " Up";
		}
		return JSONUtil.obj("users", users);
	}

	@Action
	public JSONObject growUpSpecified(@Checks({ @Check("^\\{.+\\}$"), @Check(type="ChildRegex",value="^\\{\"name\":.+\\}$") }) Map<String, UserInfo> users, @Check String name) {
		UserInfo user = users.get(name);
		if( user != null ){
			user.setAge(user.getAge() + 1);
			user.name += " Up";
		}
		return JSONUtil.obj("users", users);
	}

	@Action
	public JSONObject getUsers() {
		return JSONUtil.obj("users",
				JSONUtil.arr(new UserInfo("Jane", 23), new UserInfo("Linda Ma", 8), new UserInfo("Terry Yu", 51)));
	}

	@Action
	public JSONObject getUsersByName() {
		return JSONUtil.obj("users", JSONUtil.obj("Jane", new UserInfo("Jane", 23), "Linda Ma",
				new UserInfo("Linda Ma", 8), "Terry Yu", new UserInfo("Terry Yu", 51)));
	}
	
	static public class UserInfo {
		
		public UserInfo() {
		}

		public UserInfo(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String name = "";
		private Integer age = 0;

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer Age) {
			this.age = Age;
		}
	}
}
