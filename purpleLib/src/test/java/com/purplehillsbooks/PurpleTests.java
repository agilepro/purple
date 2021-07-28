package com.purplehillsbooks;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.purplehillsbooks.testcase.JSONTest;
import com.purplehillsbooks.testcase.MemFileTester;
import com.purplehillsbooks.testcase.Test1;
import com.purplehillsbooks.testcase.Test2;
import com.purplehillsbooks.testcase.TestExceptions;
import com.purplehillsbooks.testcase.TestJSONDiff;
import com.purplehillsbooks.testcase.TestJSONSchema;
import com.purplehillsbooks.testcase.TestTemplates;

public class PurpleTests {
	
	File testSourceFolder = new File("d:\\Gradle\\purple\\purpleLib\\src\\test\\resources\\");
	File testOutputFolder = new File("d:\\Gradle\\purple\\testOut");
	String args[] = new String[2];
	
	
	@TestFactory
    public Collection<DynamicTest> PurpleTestsDynamicFactory() throws Exception {
	    Collection<DynamicTest> dynamicTests = new ArrayList<>();
	    TestRecorderJUnit trj = TestRecorderJUnit.parseArgsRunDynamic();
	    
	    Test1 thisTest = new Test1();
	    thisTest.runTests(trj);
	    
	    JSONTest thisJSONTest = new JSONTest();
	    thisJSONTest.runTests(trj);
	    
	    MemFileTester thisMemFileTester = new MemFileTester();
	    thisMemFileTester.runTests(trj);
	    
	    Test2 thisTest2 = new Test2();
	    thisTest2.runTests(trj);
	    
	    TestExceptions thisTestExceptions = new TestExceptions();
	    thisTestExceptions.runTests(trj);
	    
	    TestJSONDiff thisTestJSONDiff = new TestJSONDiff();
	    thisTestJSONDiff.runTests(trj);
	    
	    TestJSONSchema thisTestJSONSchema = new TestJSONSchema();
	    thisTestJSONSchema.runTests(trj);
	    
	    TestTemplates thisTestTemplates = new TestTemplates();
	    thisTestTemplates.runTests(trj);
	    
	    trj.outputResults();
	    dynamicTests.addAll(trj.getDynamicTests());
        return dynamicTests;
	}	
	
	
	@BeforeEach
	public void beforeAll() throws Exception {
		//testOutputFolder = folder.newFolder();
        args[0] = testSourceFolder.getAbsolutePath();
        args[1] = testOutputFolder.getAbsolutePath();
	}
	

}
