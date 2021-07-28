/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purplehillsbooks.testcase;

import java.util.Properties;
import com.purplehillsbooks.testframe.TestRecorder;

/**
 * TestRecorderJUnit implements the TestRecorder interface so that:
 * <ol>
 * <li>(1) Configuration information is read from a text file called
 * 'test.conf'. This file has one name-value pair per line as
 * parameter_name=parameter_value
 *
 * <li>(2) Test results are written to a text file, typically 'FinalLog.log'.
 * </ol>
 *
 * @see TestSet
 * @see TestDriver
 *
 *      Author: Keith Swenson
 */
public class TestRecorderJUnit implements TestRecorder {
	
	Properties props;
	
    /**
     * The test driver initializes this class and gives it to each test case.
     * For a run of the tests, only one recorder is constructed, so that it can
     * collect the results of all the tests.
     * <p>
     * A test class should never be creating a TestRecorder, only the TestDriver
     * should do so. This allows the tests themselves to be reusable in any
     * situation.  You can write your own test driver which calls all your tests,
     * or you can use the generic TestDriver class that reads what it needs from
     * input files in order to call the tests.
     */
    public TestRecorderJUnit(Properties configProps) throws Exception {
    	props = configProps;
    }



    /**
     * Read a property from the global properties file. If property does not
     * exist, then default is returned.
     */
    public String getProperty(String name, String defaultVal) {
        String val = props.getProperty(name);
        if (val != null) {
            return val;
        }
        return defaultVal;
    }

    /**
     * Read a property from the global properties file. The specified property
     * MUST exist in the properties file. Throws a nice exception if the
     * property is missing. Use this for properties like "serverName" where
     * there is no way to provide a reaonsable default. If you need a particular
     * configuration value, then if it is not configured, then this is a
     * configuration problem that must be addressed before running the tests.
     * Throwing an exception will cause termination of the testing due to a
     * fatal error, and it will display the reason for the fatal, which will
     * encourage the tester to set the configuration up correctly.
     */
    public String getRequiredProperty(String name) throws Exception {
        String val = props.getProperty(name);
        if (val != null) {
            return val;
        }
        throw new Exception("Unable to find the '" + name
                + "' property in the 'test.conf' configuration file.");
    }



    /**
     * When a simple test passes, for example a simple calculation performed,
     * and the result matched the expected result, then this is called to
     * indicate that the test has succeeded.
     * <p>
     * This will record the fact the that test succeeded and report it in the
     * final results. The id is a simple name or description that can be used to
     * uniquely identify this test. It might consist of the test number in a
     * particular test set.
     * <p>
     * When verbose=false (normal situations) this will not produce any output
     * to the log. The logic is that normally we will have hundreds of tests,
     * and they all pass so we don't really need to know about those.
     * <p>
     * When verbose=true, it will record this pass to the log, which allows you
     * to get a listing of all the tests that were run if you wish.
     */
	@Override
    public void markPassed(String id) {
        
    }

    /**
     * When a simple test fails, for example a simple calculation performed, and
     * the result does not matched the expected result, then this is called to
     * indicate that the test failed.
     * <p>
     * A log entry is printed to record the fact with the details regarless of
     * whether verbose==true or verbose==false. The rationale is that you always
     * want to know about failures. Not only is the id and details written out,
     * but also a line that include the parameters that were passed to the test,
     * as well as any log messages that were saved up since the last result
     * message.
     * <p>
     *
     * @param id
     *            follows the same rules as above (and it goes without saying
     *            that the same id should be used when indicating success as
     *            when indicating failure for a particular test.)
     * @param details
     *            is additional information explaining what was expected and
     *            what was wrong about it. For example to explain what was being
     *            done, what was unique about this test case, which might give a
     *            clue to why the program failed.
     */
	@Override
    public void markFailed(String id, String details) {

    }

	@Override
    public void testInt(String id, int value, int expected) {

    }

    /**
     * A "fatal error" is an error that is so bad that subsequent tests were
     * skipped. For example, a particular routine is expecting test 5
     * consistency constraints on the contents of a file, but it finds that the
     * file does not exist. There are two choices: call markFailed 5 times (so
     * that all failures are recorded) or record a single markFatalError to
     * indicate that something is so wrong it is not even worth continuing
     * testing.
     * <p>
     * Recording a fatal error means tests has been skipped in this run, and the
     * resulting statistics are meaningless. We don't have a complete record of
     * all the tests that might have failed. The presence of a single fatal test
     * should tell you that something is terribly broken in the tests, and the
     * results are not valid.
     * <p>
     * Normally, a test case will indicate a fatal error by throwing an
     * exception. The Test Framework will catch the exception, and use this
     * method to record that a test failed to the extent that further testing
     * had to be skipped. Throw a meaningful exception instead of calling this
     * routine.
     */
	@Override
    public void markFatalError(Exception e) {
    }

    /**
     * Writes the specified string to the log file, only when verbose == true.
     * This is used typically for debugging, and calls to this should be removed
     * when the test case is ready for prime time.
     * <p>
     * Do not use the log method to write out whether a test passes or fails! It
     * is remarkable how many time programmers write tests that require a person
     * to read the log a determine whether the test succeeded or not. In normal
     * testing situation, nobody reads the logs! In the case of automated
     * builds, the automated run of a test produces a log, but if no error is
     * indicated using markFailure, then nobody ever looks at the log files. So
     * use it only for debug information while developing the test.
     * <p>
     * Note: there is a special capability to reduce clutter and still get
     * meaningful results. When the log method is called, and verbose==false the
     * line is not written to output, but is is saved up in a collection. If the
     * next test result is "pass" then those lines are thrown away. But if the
     * next result is "fail" then those saved up log items are output just
     * before the failure message. So if you are outputting information using
     * log statements before reporting the results of a test, those statements
     * will be seen even with verbose==false if the test fails.
     */
	@Override
    public void log(String s) {
    }



	@Override
	public boolean isVerbose() {
		return false;
	}



	@Override
	public String getTestDir() {
		return null;
	}



	@Override
	public Properties getProps() {
		return null;
	}



	@Override
	public void setProps(Properties props) {
		
	}



	@Override
	public String[] getCommandLineArgs() {
		return null;
	}



	@Override
	public void setCommandLineArgs(String[] args) {
		
	}


}
