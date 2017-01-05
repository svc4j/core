package svc.core.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LogTest extends TestCase {
	PrintStream console;
	ByteArrayOutputStream bytes;

	public LogTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(LogTest.class);
	}

	@org.junit.Before
	public void setUp() throws Exception {
		bytes = new ByteArrayOutputStream();
		console = System.out;
		System.setOut(new PrintStream(bytes));
	}

	@org.junit.After
	public void tearDown() throws Exception {
		System.setOut(console);
	}

	public void testLog() throws IOException {

		Log.setLevel( "info" );
		bytes.reset();
		Log.debug("A", 1, true);
		bytes.flush();
		assertEquals("DEBUG	(DEBUG)", bytes.toString(), "" );

		bytes.reset();
		Log.info("A", 1, true);
		bytes.flush();
		assertTrue("DEBUG	(INFO)",  bytes.toString().indexOf("\tA\t1\ttrue") != -1 );

		bytes.reset();
		Log.warn("A", 1, true);
		bytes.flush();
		assertTrue("DEBUG	(WARN)",  bytes.toString().indexOf("\tA\t1\ttrue") != -1 );
		
		bytes.reset();
		Log.error("A", 1, true);
		bytes.flush();
		assertTrue("DEBUG	(ERROR)",  bytes.toString().indexOf("\tA\t1\ttrue") != -1 );

		
		Log.setLevel( "warn" );

		bytes.reset();
		Log.debug("A", 1, true);
		bytes.flush();
		assertEquals("DEBUG	(WARN)", bytes.toString(), "");

		bytes.reset();
		Log.info("A", 1, true);
		bytes.flush();
		assertEquals("INFO	(WARN)",  bytes.toString(), "" );

		bytes.reset();
		Log.warn("A", 1, true);
		bytes.flush();
		assertTrue("WARN	(WARN)",  bytes.toString().indexOf("\tA\t1\ttrue") != -1 );
		
		bytes.reset();
		Log.error("A", 1, true);
		bytes.flush();
		assertTrue("ERROR	(WARN)",  bytes.toString().indexOf("\tA\t1\ttrue") != -1 );


		Log.setLevel( "INFO" );

		bytes.reset();
		Log.debug("A", 1, true);
		bytes.flush();
		assertEquals( bytes.toString(), "" );
		
		bytes.reset();
		Log.info("A", 1, true);
		bytes.flush();
		assertTrue( bytes.toString().indexOf("\tA\t1\ttrue") != -1 );
		
		bytes.reset();
		Log.warn("A", 1, true);
		bytes.flush();
		assertTrue( bytes.toString().indexOf("\tA\t1\ttrue") != -1 );
	}

}
