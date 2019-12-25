package com.purplehillsbooks.testcase;

import java.io.File;

import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONSchema;
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

    public TestJSONSchema() {
        super();
    }

    public void runTests(TestRecorder newTr) throws Exception {
        super.initForTests(newTr);

        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("genSchemaTest") && childName.endsWith(".sample.json")) {
                //strip the ".sample.json" from the end
                generatedSchemaTest("Gen schema for "+childName, childName.substring(0, childName.length()-12));
            }
        }
        
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("schemaTest") && childName.endsWith(".schema.json")) {
                //strip the ".sample.json" from the end
                schemaTests("Schema validation "+childName, childName.substring(0, childName.length()-12));
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
        if (!validator.checkSchema(sample, generatedSchema)) {
            File failureListFile = new File(testOutputFolder, coreName+".failureList.txt");
            writeListToFile(validator.getErrorList(), failureListFile);
            tr.markFailed(testId, "Validation of generated schema failed see: "+failureListFile);
            return;
        }

        this.compareGeneratedTextFile(testId, coreName+".schema.json");
    }


    
    public void schemaTests(String testId, String coreName) throws Exception {
        File schemaFile = new File(sourceDataFolder, coreName+".schema.json");
        JSONObject schema = JSONObject.readFromFile(schemaFile);
        
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith(coreName) && childName.endsWith(".bad.json")) {
                //strip the ".sample.json" from the end
                schemaFailOne(testId+" & "+childName, schema, child);
            }
        }
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith(coreName) && childName.endsWith(".ok.json")) {
                //strip the ".sample.json" from the end
                schemaSuccess(testId+" & "+childName, schema, child);
            }
        }
    }
    
    private void schemaFailOne(String testId, JSONObject schema, File testFile) throws Exception {
        JSONObject testObject = JSONObject.readFromFile(testFile);
        
        JSONSchema validator = new JSONSchema();
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
        
        JSONSchema validator = new JSONSchema();
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
