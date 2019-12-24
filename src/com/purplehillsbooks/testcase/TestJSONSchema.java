package com.purplehillsbooks.testcase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

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

        generatedSchemaTest("one-string object schema", "basic1");
        generatedSchemaTest("simple object schema", "basic2");
        generatedSchemaTest("list schema", "basic3");
        generatedSchemaTest("multi-list schema", "basic4");
        generatedSchemaTest("nested object schema", "basic5");

        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("genSchemaTest") && childName.endsWith(".sample.json")) {
                //strip the ".sample.json" from the end
                generatedSchemaTest("Gen schema for "+childName, childName.substring(0, childName.length()-12));
            }
        }
        
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith("schemaFail") && childName.endsWith(".schema.json")) {
                //strip the ".sample.json" from the end
                schemaFailTest("Schema validation fail "+childName, childName.substring(0, childName.length()-12));
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


    
    public void schemaFailTest(String testId, String coreName) throws Exception {
        File schemaFile = new File(sourceDataFolder, coreName+".schema.json");
        JSONObject schema = JSONObject.readFromFile(schemaFile);
        
        for (File child : this.sourceDataFolder.listFiles()) {
            String childName = child.getName();
            
            if (childName.startsWith(coreName) && childName.endsWith(".bad.json")) {
                //strip the ".sample.json" from the end
                schemaFailOne(testId+" & "+childName, schema, child);
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

    public static void main(String args[]) {
        TestJSONSchema thisTest = new TestJSONSchema();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }


}
