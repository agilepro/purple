package com.purplehillsbooks.testcase;

import java.io.File;
import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONException;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestRecorderText;
import com.purplehillsbooks.testframe.TestSet;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 *
 * NOTE: these tests are very sensitive to the line numbers of the test framework.
 * If you add or remove lines in the test framework, you may need to regenerate
 * the test results files.
 */
public class TestExceptions extends TestAbstract implements TestSet {

    public TestExceptions() {
    }

    public void runTests(TestRecorder newTr) throws Exception {
    	initForTests(newTr);
        if (!sourceDataFolder.exists()) {
            System.out.println("Source data folder does not exist: "+sourceDataFolder);
            return;
        }
        testExceptionStackTrace("11121112333", "ExTest1.json");
        testExceptionStackTrace("3",           "ExTest2.json");
        testExceptionStackTrace("1222",        "ExTest3.json");
        testExceptionStackTrace("2",           "ExTest4.json");


        compareString(newTr, "testing format 1", "test whether it works", String.format("test %s it works", "whether"));
        compareString(newTr, "testing format 2", "test whether it works two", String.format("test %s it works %s",  "whether", "two"));
        String[] myBundle = new String[] {"whether", "two"};
        compareString(newTr, "testing format 3", "test whether it works two", String.format("test %s it works %s",  (Object []) myBundle));



        JSONException je = new JSONException("this is a {0} good template", "very");
        compareString(newTr, "JSONException one parameter", "this is a very good template", je.getMessage());
        je = new JSONException("this has {0} and {1} value", "first", "second");
        compareString(newTr, "JSONException two params", "this has first and second value", je.getMessage());
        je = new JSONException("three {0} values {1} to {2} see", "nogood", "inevibatly", "hardly");
        compareString(newTr, "JSONException three params", "three nogood values inevibatly to hardly see", je.getMessage());
        JSONException je2 = new JSONException("NESTED {0} values {1} to {2} see", je, "nogood", "inevibatly", "hardly");
        compareString(newTr, "JSONException three params NESTED", "NESTED nogood values inevibatly to hardly see", je2.getMessage());
    }

    public void compareString(TestRecorder tr, String caseDesc, String s1, String s2) {
        if (s1.equals(s2)) {
            tr.markPassed(caseDesc);
        }
        else {
            tr.markFailed(caseDesc, "Expected ("+s1+") but got ("+s2+")");
        }
    }
    private void sampleMethod1(String course) throws Exception {
        callHigher(course);
    }

    private void sampleMethod2(String course) throws Exception {
        try {
            callHigher(course);
        }
        catch (Exception e) {
            throw new JSONException("caught and rethrow from level: {0}", e, course);
        }
    }

    private void sampleMethod3(String course) throws Exception {
        callHigher(course);
    }

    private void callHigher(String course) throws Exception {
        if (course==null || course.length()==0) {
            throw new Exception("top level exception thrown by exception test when course string ran out");
        }
        char ch = course.charAt(0);
        if (ch=='1') {
            sampleMethod1(course.substring(1));
        }
        else if (ch=='2') {
            sampleMethod2(course.substring(1));
        }
        else if (ch=='3') {
            sampleMethod3(course.substring(1));
        }
        else {
            throw new Exception("course string in exception test had invalid character: "+ch);
        }
    }

    private void compareExceptions(String testCase, JSONObject ex1, JSONObject ex2) throws Exception {
        try {
            JSONObject error1 = ex1.getJSONObject("error");
            JSONObject error2 = ex2.getJSONObject("error");

            JSONArray details1 = error1.getJSONArray("details");
            JSONArray details2 = error2.getJSONArray("details");

            int detailNumber = details1.length();
            if (detailNumber != details2.length()) {
                tr.markFailed(testCase, "first exception has "+detailNumber+" details, the second has "+details2.length());
                return;
            }
            for (int i=0; i<detailNumber; i++) {
                JSONObject detail1 = details1.getJSONObject(i);
                JSONObject detail2 = details2.getJSONObject(i);
                if (!detail1.getString("message").equals(detail2.getString("message"))) {
                    tr.markFailed(testCase, "messages don't match: first exception has "+detail1.getString("message")+" != "+detail2.getString("message"));
                    return;
                }
            }
            tr.markPassed(testCase);

        }
        catch (Exception e) {
            tr.markFailed(testCase, "Exception while processing comparison: "+e.toString());
        }
    }


    private void testExceptionStackTrace(String course, String fileName) {
        File outFolder = new File(tr.getProperty("testoutput", null));
        if (!outFolder.exists()) {
            tr.markFatalError(new Exception ("testExceptionStackTrace: Test output folder does not exist: "+outFolder));
            return;
        }
        Exception testException = null;
        File expectedExceptionFile = new File(sourceDataFolder, fileName);
        if (!expectedExceptionFile.exists()) {
            tr.markFailed(fileName, "testExceptionStackTrace: the test file is missing from source folder: "+expectedExceptionFile);
            return;
        }
        
        //this generates an exception will varying levels of calls in it.   We want an exception
        //with a detailed stack trace to make sure that the stack is being handled appropriately.
        //the method callHigher is a recursive method that then throws and catches/rethrow at various levels
        try {
            callHigher(course);
        }
        catch (Exception e) {
            testException = e;
        }

        try {

            JSONObject receivedExceptionForm = JSONException.convertToJSON(testException, "Test file "+fileName+" for course "+course);
            JSONObject expectedExceptionForm = JSONObject.readFromFile(expectedExceptionFile);
            //dumpTraceFile(outFolder, fileName, testException);
            File receivedExceptionFile = new File(outFolder, fileName);
            receivedExceptionForm.writeToFile(receivedExceptionFile);
            String s1 = receivedExceptionForm.toString();
            String s2 = expectedExceptionForm.toString();
            if (s1.equals(s2)) {
                tr.markPassed(fileName);
            }
            else {
                tr.markFailed(fileName, "testExceptionStackTrace: these files do not match ("+receivedExceptionFile+") and ("+expectedExceptionFile+")");
            }

            String secondFileName = fileName.substring(0,fileName.length()-5)+"-2.json";
            Exception reconvert = JSONException.convertJSONToException(receivedExceptionForm);
            JSONObject reExObj = JSONException.convertToJSON(reconvert, "Test file "+fileName+" for course "+course);
            compareExceptions(secondFileName, receivedExceptionForm, reExObj);
            //dumpTraceFile(outFolder, secondFileName, reconvert);
            File outputFile3 = new File(outFolder, secondFileName);
            reExObj.writeToFile(outputFile3);

        }
        catch (Exception e) {
            System.out.println("FATAL ERROR: "+e);
        }
    }

    public static void main(String args[]) {
        TestExceptions thisTest = new TestExceptions();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }

}
