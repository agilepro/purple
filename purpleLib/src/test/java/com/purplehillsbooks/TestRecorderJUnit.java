package com.purplehillsbooks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;

import com.purplehillsbooks.json.JSONException;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestResultRecord;

public class TestRecorderJUnit implements TestRecorder {
	
    Collection<DynamicTest> dynamicTests = new ArrayList<>();
    File purpleLibPath;
    File testOutput;
    File testSource;
    public ArrayList<TestResultRecord> resultSet = new ArrayList<TestResultRecord>();
    
    // save away the time that the test recorder was created so that time
    // can be recorded as a difference to this.
    private long creationTime = System.currentTimeMillis();

    /**
     * This is simply the count of tests that have been recorded as passed. It
     * starts at zero, and when all tests are done the result is reported.
     */
    private int passedTests = 0;
    /**
     * This is simply the count of tests that have been recorded as failing. It
     * starts at zero, and when all tests are done the result is reported.
     */
    private int failedTests = 0;
    /**
     * This is simply the count of tests that have been terminated prematurely.
     * It starts at zero. Generally speaking, this should either be a zero or 1.
     * If an exception is thrown from a test routine, it is a fatal error, and
     * should stop all subsequent testing activity.
     */
    private int fatalTests = 0;
    
    private PrintWriter logWriter;
    
    private Properties props = new Properties();
    
    
	public TestRecorderJUnit() throws Exception {
		
		File classLocation = new File(getClass().getResource("").toURI());
		purpleLibPath = classLocation.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
		testOutput  = new File(purpleLibPath, "build/test-results/test-output");
		testSource   = new File(purpleLibPath, "src/test/resources/");
		System.out.println("purpleLibPath is: "+purpleLibPath.getAbsolutePath());
		System.out.println("testOutput is: "+testOutput.getAbsolutePath());
		System.out.println("testData is: "+testSource.getAbsolutePath());
		
		if (!testSource.exists()) {
			throw new Exception("Something is wrong, testSource folder does not exist: "+testSource.getAbsolutePath());
		}
		if (!testOutput.exists()) {
			testOutput.mkdirs();
		}
		
		props.put("source", testSource.getAbsolutePath());
		props.put("testoutput", testOutput.getAbsolutePath());

        File outputFile = new File(testOutput, "_output_ALLTESTS.txt");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        Writer w  = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        logWriter = new PrintWriter(w);
	}
	
	public Collection<DynamicTest> getDynamicTests() {
		logWriter.close();
		return dynamicTests;
	}

    /**
     * Write out the number of seconds since the test started in the format
     * sss.mmm where sss is a three digit number of seconds, and mmm is the
     * three digit milliseconds. First, make sure that duration is less than 1
     * million, then add a million so you have a 7 digit number of milliseconds.
     * Then pick three digits for seconds and three digits for millis.
     */
    private void timeStamp() {
        long dur = System.currentTimeMillis() - creationTime;
        String rep;
        if (dur > 999999) {
            dur = dur % 1000000;
        }
        dur = dur + 1000000;
        rep = Long.toString(dur);
        logWriter.write(rep.substring(1, 4));
        logWriter.write(".");
        logWriter.write(rep.substring(4, 7));
        logWriter.write(" ");
    }
	
	
	@Override
    public void markPassed(String id) {
        passedTests++;
        TestResultRecord trr = new TestResultRecord(id, "", true);
        resultSet.add(trr);

        try {
            logWriter.write("[PASS  ] ");
            timeStamp();
            logWriter.write(id);
            logWriter.write("\n");
            logWriter.flush();
        }
        catch (Exception e) {
            JSONException.traceException(logWriter, e, "Exception during markPassed");
        }
	    DynamicTest d1 = DynamicTest.dynamicTest(id, () -> Assertions.assertTrue(true));
	    dynamicTests.add(d1);
    }
	@Override
    public void markFailed(String id, String details) {
        failedTests++;
        try {
            logWriter.write("[  FAIL] ");
            timeStamp();
            logWriter.write(id);
            logWriter.write(": ");
            logWriter.write(details);
            logWriter.write("\n");
            logWriter.flush();

            TestResultRecord trr = new TestResultRecord(id, "", false);
            trr.failureMessage = details;
            resultSet.add(trr);
        }
        catch (Exception e) {
            JSONException.traceException(logWriter, e, "Exception during markFailed");
        }
	    DynamicTest d1 = DynamicTest.dynamicTest(id + ":" + details, () -> Assertions.assertTrue(false));
	    dynamicTests.add(d1);
	    
	    System.out.println("\n\nFAILURE");
	    JSONException.traceException(new Exception("FAILURE "+id+": "+details), "markFailed");
    }
    
    public static TestRecorderJUnit parseArgsRunDynamic() throws Exception {
    	return new TestRecorderJUnit();
    }
    
    public void outputResults() {
        System.out.print("\n\n\n====================================================");
        System.out.print("\n               FINISHED RUN for ALLTESTS");
        System.out.print("\n====================================================");
        System.out.print("\n Number PASSED: "+passedTests);
        System.out.print("\n Number FAILED: "+failedTests);
        System.out.print("\n Number FATAL:  "+fatalTests);
        System.out.print("\n====================================================\n");
    }

	@Override
	public void markFatalError(Exception e) {
		fatalTests++;
		JSONException.traceException(System.out, e, "FATAL TEST ERROR");
		markFailed("FATAL TEST ERROR", e.toString());
	}

	@Override
	public void log(String s) {
        try {
            logWriter.write("[      ] ");
            timeStamp();
            logWriter.write(s);
            logWriter.write("\n");
            logWriter.flush();
        }
        catch (Exception e) {
            JSONException.traceException(logWriter, e, "Exception during logging");
        }
	}

	@Override
	public boolean isVerbose() {
		return true;
	}

	@Override
	public String getProperty(String name, String defaultVal) {
		String propVal = props.getProperty(name);
		if (propVal == null) {
			return defaultVal;
		}
		return propVal;
	}

	@Override
	public String getRequiredProperty(String name) throws Exception {
		String propVal = props.getProperty(name);
		if (propVal == null) {
			throw new Exception("Missing property ("+name+") from test configuration");
		}
		return propVal;
	}

	@Override
	public String getTestDir() {
		return testSource.getAbsolutePath();
	}

	@Override
	public void testInt(String id, int value, int expected) {
        if (value == expected) {
            markPassed(id);
        }
        else {
            markFailed(id, "Expected '" + expected + "' but got '" + value + "' instead.");
        }
	}

	@Override
	public Properties getProps() {
		return props;
	}

	@Override
	public void setProps(Properties _props) {
		props = _props;
	}

	@Override
	public String[] getCommandLineArgs() {
		return new String[0];
	}

	@Override
	public void setCommandLineArgs(String[] args) {
	}
}
