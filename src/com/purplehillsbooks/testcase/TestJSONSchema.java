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

        FileTest("one-string object schema", "basic1");
        FileTest("simple object schema", "basic2");
        FileTest("list schema", "basic3");
        FileTest("multi-list schema", "basic4");
        FileTest("nested object schema", "basic5");
    }

    private void FileTest(String testId, String coreName) throws Exception {
        File sampleFile = new File(sourceDataFolder, coreName+".sample.json");
        JSONObject sample = JSONObject.readFromFile(sampleFile);

        File outputFile = new File(testOutputFolder, coreName+".schema.json");

        JSONObject generatedSchema = JSONSchema.generateSchema(sample);
        generatedSchema.writeToFile(outputFile);

        //we just generated it, so it had better validate correctly
        JSONSchema validator = new JSONSchema();
        List<String> checkList = validator.checkSchema(sample, generatedSchema);

        if (checkList.size()>0) {
            File failureList = new File(testOutputFolder, coreName+".failureList.txt");
            Writer fw = new OutputStreamWriter(new FileOutputStream(failureList), "UTF-8");
            int count = 0;
            for (String line : checkList) {
                fw.write(Integer.toString(count++));
                fw.write(": ");
                fw.write(line);
                fw.write("\n");
            }
            fw.close();
            tr.markFailed(testId, "Validation of generated schema failed see: "+failureList);
            return;
        }

        this.compareGeneratedFile(testId, coreName+".schema.json");
    }



    public static void main(String args[]) {
        TestJSONSchema thisTest = new TestJSONSchema();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }


}
