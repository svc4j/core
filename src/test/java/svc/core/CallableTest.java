package svc.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.test.service.User.UserInfo;

import junit.framework.TestCase;
import svc.core.base.JSONUtil;

public class CallableTest extends TestCase {

	public void testService() {
		JSONObject r, user;
		JSONArray users;
		JSONObject usersByName;

		MyCallable.switchAccessLog(false);
		MySession.init("123456");

		// 获取用户列表（未登录）
		r = MyCallable.call("User.getUsers");
		assertEquals(r.getIntValue("code"), 403);

		// 获取管理员信息（未登录）
		r = MyCallable.call("User.getAdminInfo");
		assertEquals(r.getIntValue("code"), 403);

		// 登录
		r = MyCallable.call("User.login");
		assertEquals(r.getIntValue("code"), 200);

		// 获取用户列表（已登录）
		r = MyCallable.call("User.getUsers");
		assertEquals(r.getIntValue("code"), 200);

		// 获取管理员信息（已登录但无权限）
		r = MyCallable.call("User.getAdminInfo");
		assertEquals(r.getIntValue("code"), 403);

		// 获得Admin权限
		r = MyCallable.call("User.toAdmin");
		assertEquals(r.getIntValue("code"), 200);

		// 获取管理员信息（已登录但有权限）
		r = MyCallable.call("User.getAdminInfo");
		assertEquals(r.getIntValue("code"), 200);

		// 获取用户列表
		r = MyCallable.call("User.getUsers");
		assertEquals(r.getIntValue("code"), 200);
		users = r.getJSONObject("data").getJSONArray("users");
		user = users.getJSONObject(1);
		assertEquals(user.getString("name"), "Linda Ma");
		assertEquals(user.getIntValue("age"), 8);

		// 获取用户列表（By Name）
		r = MyCallable.call("User.getUsersByName");
		assertEquals(r.getIntValue("code"), 200);
		usersByName = r.getJSONObject("data").getJSONObject("users");
		user = usersByName.getJSONObject("Linda Ma");
		assertEquals(user.getString("name"), "Linda Ma");
		assertTrue(user.getInteger("age") == 8);

		// 获取长大一岁的年龄，不传参数
		r = MyCallable.call("User.getGrowUpAge");
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("age is null") != -1);

		// 获取长大一岁的年龄，不传参数
		r = MyCallable.call("User.getGrowUpAge", JSONUtil.obj("age", true));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("age type is class java.lang.Boolean") != -1);

		// 获取长大一岁的年龄，传整数字符串
		r = MyCallable.call("User.getGrowUpAge", JSONUtil.obj("age", "99"));
		assertEquals(r.getIntValue("code"), 200);
		assertEquals(r.getJSONObject("data").getIntValue("newAge"), 100);

		// 获取长大一岁的年龄，传浮点数
		r = MyCallable.call("User.getGrowUpAge", JSONUtil.obj("age", 99.91));
		assertEquals(r.getIntValue("code"), 200);
		assertEquals(r.getJSONObject("data").getIntValue("newAge"), 100);

		// 长大一岁，不传参数
		r = MyCallable.call("User.growUp");
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("user is null") != -1);

		// 长大一岁，传错误的参数
		r = MyCallable.call("User.growUp", JSONUtil.obj("user", 123));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("user type is class java.lang.Integer") != -1);

		// 长大一岁，正常
		r = MyCallable.call("User.growUp", JSONUtil.obj("user", user));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONObject("user");
		assertEquals(user.getString("name"), "Linda Ma Up");
		assertTrue(user.getInteger("age") == 9);

		// 长大一岁，正常（JSONObject）
		r = MyCallable.call("User.growUp", JSONUtil.obj("user", JSONUtil.obj("name", "Star Liu", "age", 13)));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONObject("user");
		assertEquals(user.getString("name"), "Star Liu Up");
		assertTrue(user.getInteger("age") == 14);

		// 多人长大一岁，不传参数
		r = MyCallable.call("User.growUpMulti");
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users is null") != -1);

		// 多人长大一岁，传错误参数
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users", 123));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users subType is class java.lang.Integer") != -1);

		// 多人长大一岁，传错误子类型参数
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users", JSONUtil.arr("abc", 123)));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users subType is class java.lang.String") != -1);

		// 多人长大一岁，正常（JSONObject）
		r = MyCallable.call("User.growUpMulti",
				JSONUtil.obj("users", JSONUtil.arr(JSONUtil.obj("name", "Star Liu"), new UserInfo("Linda Ma", 8))));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONArray("users").getJSONObject(0);
		assertEquals(user.getString("name"), "Star Liu Up");
		assertTrue(user.getInteger("age") == 1);
		user = r.getJSONObject("data").getJSONArray("users").getJSONObject(1);
		assertEquals(user.getString("name"), "Linda Ma Up");
		assertTrue(user.getInteger("age") == 9);

		// 多人长大一岁，正常
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users", users));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONArray("users").getJSONObject(2);
		assertEquals(user.getString("name"), "Terry Yu Up");
		assertTrue(user.getInteger("age") == 52);

		// 多人长大一岁，正常（直接传入单个对象）
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users", new UserInfo("Linda Ma", 8)));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONArray("users").getJSONObject(0);
		assertEquals(user.getString("name"), "Linda Ma Up");
		assertTrue(user.getInteger("age") == 9);

		// 多人长大一岁，正常（传入JSON字符串）
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users",
				JSONUtil.arr(JSONUtil.obj("name", "Star Liu"), new UserInfo("Linda Ma", 8)).toJSONString()));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONArray("users").getJSONObject(1);
		assertEquals(user.getString("name"), "Linda Ma Up");
		assertTrue(user.getInteger("age") == 9);

		// 多人长大一岁，空对象
		r = MyCallable.call("User.growUpMulti", JSONUtil.obj("users", new ArrayList<>()));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users [] check failed with Regex") != -1);

		// 指定多人长大一岁，不传参数
		r = MyCallable.call("User.growUpSpecified");
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users is null") != -1);

		// 指定多人长大一岁，传错误参数
		r = MyCallable.call("User.growUpSpecified", JSONUtil.obj("users", 123));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users type is class java.lang.Integer") != -1);

		// 指定多人长大一岁，传错误子类型参数
		r = MyCallable.call("User.growUpSpecified", JSONUtil.obj("users", JSONUtil.obj("abc", 123)));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users subType is class java.lang.Integer") != -1);

		r = MyCallable.call("User.growUpSpecified",
				JSONUtil.obj("users", JSONUtil.obj("Linda Ma", new UserInfo("Linda Ma", 8))));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("name is null") != -1);

		// 指定多人长大一岁，空对象
		r = MyCallable.call("User.growUpSpecified", JSONUtil.obj("name", "Star Liu", "users", new HashMap<>()));
		assertEquals(r.getIntValue("code"), 400);
		assertTrue(r.getString("message").indexOf("users {} check failed with Regex") != -1);

		// 指定多人长大一岁，正常（传入JSON字符串）
		r = MyCallable.call("User.growUpSpecified",
				JSONUtil.obj("name", "Star Liu", "users", JSONUtil
						.obj("Star Liu", JSONUtil.obj("name", "Star Liu"), "Linda Ma", new UserInfo("Linda Ma", 8))
						.toJSONString()));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONObject("users").getJSONObject("Star Liu");
		assertEquals(user.getString("name"), "Star Liu Up");
		assertTrue(user.getInteger("age") == 1);

		// 指定多人长大一岁，正常
		r = MyCallable.call("User.growUpSpecified", JSONUtil.obj("name", "Star Liu", "users",
				JSONUtil.obj("Star Liu", JSONUtil.obj("name", "Star Liu"), "Linda Ma", new UserInfo("Linda Ma", 8))));
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONObject("users").getJSONObject("Star Liu");
		assertEquals(user.getString("name"), "Star Liu Up");
		assertTrue(user.getInteger("age") == 1);

		// 注入DB
//		MyCallable.switchAccessLog(true);
		r = MyCallable.call("User.getUserFromDB");
		assertEquals(r.getIntValue("code"), 200);
		user = r.getJSONObject("data").getJSONObject("user");
		assertEquals(user.getString("name"), "Noname Up");
		assertTrue(user.getInteger("age") == 11);
		
		// 退出登录
		r = MyCallable.call("User.logout");
		assertEquals(r.getIntValue("code"), 200);

		// 获取用户列表（已登录）
		r = MyCallable.call("User.getUsers");
		assertEquals(r.getIntValue("code"), 403);

		// 获取管理员信息（已登录但无权限）
		r = MyCallable.call("User.getAdminInfo");
		assertEquals(r.getIntValue("code"), 403);

	}
}
