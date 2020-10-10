package com.purplehillsbooks.testcase;

import java.io.File;

import com.purplehillsbooks.json.FileSchemaLibrary;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONSchema;
import com.purplehillsbooks.json.SchemaLibrary;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestRecorderText;
import com.purplehillsbooks.testframe.TestSet;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 */
public class TestJSONSchema extends TestAbstract implements TestSet {
    
    SchemaLibrary schemaLib;

    public TestJSONSchema() {
        super();
    }

    public void runTests(TestRecorder newTr) throws Exception {
        super.initForTests(newTr);
        
        File schemaFolder = new File(this.sourceDataFolder, "schemas");
        if (!schemaFolder.exists()) {
            throw new Exception("The sourceDataFolder/schemas for tests does not exist: "+schemaFolder.getAbsolutePath());
        }
        schemaLib = new FileSchemaLibrary(schemaFolder);

        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("genSchemaTest") && childName.endsWith(".sample.json")) {
                //strip the ".sample.json" from the end
                generatedSchemaTest("Gen schema for "+childName, childName.substring(0, childName.length()-12));
            }
        }
        
        for (File child : schemaFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("Schema-") && childName.endsWith(".json")) {
                //strip the ".sample.json" from the end
                String testName = childName.substring(7, childName.length()-5);
                schemaTests("Schema validation "+testName, testName);
            }
        }
    }

    
    
    private void generatedSchemaTest(String testId, String coreName) throws Exception {
        File sampleFile = new File(sourceDataFolder, coreName+".sample.json");
        JSONObject sample = JSONObject.readFromFile(sampleFile);

        File outputFile = new File(testOutputFolder, coreName+".schema.json");

        JSONObject generatedSchema = JSONSchema.generateSchema(sample);
        generatedSchema.writeToFile(outputFile);

        //we just generated it, so it had better validate correctly
        JSONSchema validator = new JSONSchema();
        validator.setSchemaLibrary(schemaLib);
        validator.errorLimit = 4000;
        
        //the generated schema should ALWAYS validate the source of the generation
        if (!validator.checkSchema(sample, generatedSchema)) {
            File failureListFile = new File(testOutputFolder, coreName+".failureList.txt");
            writeListToFile(validator.getErrorList(), failureListFile);
            tr.markFailed(testId, "Validation of generated schema failed see: "+failureListFile);
            return;
        }

        this.compareGeneratedTextFile(testId, coreName+".schema.json");
    }


    
    public void schemaTests(String testId, String coreName) throws Exception {
        JSONObject schema = schemaLib.getSchema(coreName);

        System.out.println("Schema test for: "+coreName);
        
        String testFileStart = "schema"+coreName;
        
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith(testFileStart) && childName.endsWith(".bad.json")) {
                //strip the ".sample.json" from the end
                schemaFailOne(testId+" & "+childName, schema, child);
            }
        }
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith(testFileStart) && childName.endsWith(".ok.json")) {
                //strip the ".sample.json" from the end
                schemaSuccess(testId+" & "+childName, schema, child);
            }
        }
    }
    
    private void schemaFailOne(String testId, JSONObject schema, File testFile) throws Exception {
        JSONObject testObject = JSONObject.readFromFile(testFile);
        System.out.println("     FAIL test for: "+testId);
        
        JSONSchema validator = new JSONSchema();
        validator.setSchemaLibrary(schemaLib);
        validator.errorLimit = 4000;
        
        if (validator.checkSchema(testObject, schema)) {
            tr.markFailed(testId, "Supposed to be testing validator errors, but did not find any errors");
            return;
        }
        
        String childName = testFile.getName();
        String coreName = childName.substring(0, childName.length()-9);
        
        File failureListFile = new File(testOutputFolder, coreName+".failureList.txt");
        writeListToFile(validator.getErrorList(), failureListFile);
        this.compareGeneratedTextFile(testId, failureListFile.getName());
    }
    private void schemaSuccess(String testId, JSONObject schema, File testFile) throws Exception {
        JSONObject testObject = JSONObject.readFromFile(testFile);
        System.out.println("     SUCCESS test for: "+testId);
        
        JSONSchema validator = new JSONSchema();
        validator.setSchemaLibrary(schemaLib);
        validator.errorLimit = 4000;
        validator.recordSuccess = true;
        
        if (!validator.checkSchema(testObject, schema)) {
            tr.markFailed(testId, "Supposed to be passing validation, but found errors");
        }
        
        String childName = testFile.getName();
        String coreName = childName.substring(0, childName.length()-8);
        
        File verifyFile = new File(testOutputFolder, coreName+".verify.txt");
        writeListToFile(validator.getErrorList(), verifyFile);
        this.compareGeneratedTextFile(testId, verifyFile.getName());
    }

    public static void main(String args[]) {
        TestJSONSchema thisTest = new TestJSONSchema();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }


}
