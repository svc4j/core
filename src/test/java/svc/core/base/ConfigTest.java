package svc.core.base;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConfigTest extends TestCase {
	public ConfigTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		System.setProperty("DB_AAA_PWD", "EEEaaappp");
		return new TestSuite(ConfigTest.class);
	}

	public void testConfig() {
		assertNull(Config.get("DB_NOT_EXISTS"));
		assertEquals(Config.get("DB_NOT_EXISTS", "123"), "123");

		assertEquals(Config.get("DB_MODE", "JNDI"), "DIRECT");
		assertEquals(Config.get("DB_DSN"), "jdbc:mysql://127.0.0.1/test?useUnicode=true&amp;characterEncoding=utf8");
		assertEquals(Config.get("DB_USER"), "root");
		assertEquals(Config.get("DB_PWD", ""), "diTxhnbnFuiIzWQ");

		assertEquals(Config.get("DB_AAA_USER", "DB_USER", "user"), "root");
		assertEquals(Config.get("DB_AAA_PWD", "DB_PWD", "pwd"), "EEEaaappp");

		assertEquals(Config.getAllNoPrefix("DB").get("PWD"), "diTxhnbnFuiIzWQ");
		assertEquals(Config.getAllNoPrefix("DB_").get("PWD"), "diTxhnbnFuiIzWQ");
		assertEquals(Config.getAllNoPrefix("DB_AAA").get("PWD"), "EEEaaappp");
		assertEquals(Config.getAllNoPrefix("DB_AAA_").get("PWD"), "EEEaaappp");
	}

}
