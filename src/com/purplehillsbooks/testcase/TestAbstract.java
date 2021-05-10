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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.purplehillsbooks.streams.MemFile;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestSet;
import com.purplehillsbooks.xml.Mel;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 */
public abstract class TestAbstract implements TestSet {

    TestRecorder tr;
    File sourceDataFolder;     //input data for tests
    File testOutputFolder;     //output from tests
    File testCompareFolder;    //contains 'correct' output to compare actual output to

    public TestAbstract() {
    }

    public void initForTests(TestRecorder newTr) throws Exception {
        tr = newTr;
        sourceDataFolder = new File(tr.getProperty("source", null), "testdata");
        testCompareFolder = new File(tr.getProperty("source", null), "testoutput");
        testOutputFolder = new File(tr.getProperty("testoutput", null));
    }


    /*
     * Specify a testId for reporting the problems
     * The fileName is just the fileName end of the path, and the two files
     * being compared are on in the test output folder, and the other
     * in the source tree test output (comparison) folder.
     */
    public void compareGeneratedTextFile(String testId, String fileName) throws Exception {

        File sourceFile = new File(testOutputFolder, fileName);
        if (!sourceFile.exists()) {
            throw new Exception("Source file to compare does not exist: "+sourceFile.getAbsolutePath());
        }
        File compareFile = new File(testCompareFolder, fileName);
        if (!compareFile.exists()) {
            //remember, when new tests are created there won't yet be comparison files checked in
            tr.markFailed(testId, "file to compare to is missing from: " + compareFile.getAbsolutePath());
            return;
        }

        FileInputStream fis1 = new FileInputStream(sourceFile);
        FileInputStream fis2 = new FileInputStream(compareFile);

        int b1 = fis1.read();
        int b2 = fis2.read();
        int charCount = 1;
        int lineCount = 1;
        while (b1 >= 0 && b2 >= 0) {
            if (b1 == '\r') {
                //ignore line feeds
                b1 = fis1.read();
                continue;
            }
            if (b2 == '\r') {
                //ignore line feeds
                b2 = fis2.read();
                continue;
            }
            if (b1 != b2) {
                tr.markFailed(testId, "Difference at line " + lineCount + " and character "+charCount+" of file "+sourceFile.getAbsolutePath());
                fis1.close();
                fis2.close();
                return;
            }
            if (b1=='\n') {
                lineCount++;
                charCount = 1;
            }
            else {
                charCount++;
            }
            b1 = fis1.read();
            b2 = fis2.read();
        }

        fis1.close();
        fis2.close();

        if (b1 >= 0) {
        	System.out.println("FAIL: "+testId);
            tr.markFailed(testId, "new file has more characters in it than the old file: "+sourceFile.getAbsolutePath());
            return;
        }
        if (b2 >= 0) {
        	System.out.println("FAIL: "+testId);
            tr.markFailed(testId, "old file has more characters in it than the new file: "+sourceFile.getAbsolutePath());
            return;
        }

        tr.markPassed(testId);
    }


    public static boolean compareStringIgnoringCR(String s1, String s2) {
        int i1 = 0;
        int i2 = 0;
        while (i1 < s1.length() && i2 < s2.length()) {
            char c1 = s1.charAt(i1++);
            while (c1 == 13 && i1 < s1.length()) {
                c1 = s1.charAt(i1++);
            }
            char c2 = s2.charAt(i2++);
            while (c2 == 13 && i2 < s2.length()) {
                c2 = s2.charAt(i2++);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    public void testOutput(Mel me, String note, String expectedVal) throws Exception {
        MemFile mf = new MemFile();
        me.writeToOutputStream(mf.getOutputStream());
        String actualVal = mf.toString();

        if (expectedVal.equals(actualVal)) {
            tr.markPassed(note);
        }
        else {
            tr.markFailed(note, "values do not match");
            writeLiteralValue("expected", expectedVal);
            writeLiteralValue("actual", actualVal);
        }
    }

    public void testRawXML(Mel me, String note, String expectedVal) throws Exception {
        String actualVal = me.getRawDOM();

        if (expectedVal.equals(actualVal)) {
            tr.markPassed(note);
        }
        else {
            tr.markFailed(note, "values do not match");
            writeLiteralValue("expected", expectedVal);
            writeLiteralValue("actual", actualVal);
        }
    }

    public void testNotNull(Object value, String description) throws Exception {
        if (value != null) {
            tr.markPassed("Not Null: " + description);
        }
        else {
            tr.markFailed("Not Null: " + description,
                    "Test failure, got an unexpected null for the situation: " + description);
        }
    }

    public void testNull(Object value, String description) throws Exception {
        if (value == null) {
            tr.markPassed("Is Null: " + description);
        }
        else {
            tr.markFailed("Is Null: " + description,
                    "Test failure, expected a null but did not get one for the situation: "
                            + description);
        }
    }

    public void testVal(String value, String expectedValue, String description) throws Exception {
        if (value != null && value.equals(expectedValue)) {
            tr.markPassed("Value: " + description);
        }
        else {
            tr.markFailed("Value: " + description, "Test failure, expected the value '"
                    + expectedValue + "' but instead got the value '" + value
                    + "' for the situation: " + description);
            writeLiteralValue("expected", expectedValue);
            writeLiteralValue("actual", value);
        }
    }

    public void testScalar(Mel me, String eName, String expectedValue, String description)
            throws Exception {
        String value = me.getScalar(eName);
        if (value != null && value.equals(expectedValue)) {
            tr.markPassed("testScalar (" + eName + "): " + description);
        }
        else {
            tr.markFailed("testScalar (" + eName + "): " + description,
                    "Test failure, expected the value '" + expectedValue
                            + "' but instead got the value '" + value + "' for the scaler value '"
                            + eName + "' for  " + description);
            writeLiteralValue("expected", expectedValue);
            writeLiteralValue("actual", value);
        }
    }

    public void writeShortLiteralValue(StringBuffer sb, String value) {
        sb.append("\"");
        for (int i = 0; i < value.length() && i < 20; i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                sb.append("\\\"");
            }
            else if (ch == '\\') {
                sb.append("\\\\");
            }
            else if (ch == '\n') {
                sb.append("\\n");
            }
            else if (ch == (char) 13) {
                // do output anything ... ignore these
            }
            else if (ch < 32 || ch > 128) {
                sb.append("\\u");
                addHex(sb, (ch / 16 / 16 / 16) % 16);
                addHex(sb, (ch / 16 / 16) % 16);
                addHex(sb, (ch / 16) % 16);
                addHex(sb, ch % 16);
            }
            else {
                sb.append(ch);
            }
        }
        sb.append("\"");
    }

    public void writeLiteralValue(String varname, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append(varname);
        sb.append(" = \"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                sb.append("\\\"");
            }
            else if (ch == '\\') {
                sb.append("\\\\");
            }
            else if (ch == '\n') {
                sb.append("\"\n     +\"\\n");
            }
            else if (ch == (char) 13) {
                // strange workaround for literal problem
                sb.append("\"+(char)13+\"");
            }
            else if (ch < 32 || ch > 128) {
                sb.append("\\u");
                addHex(sb, (ch / 16 / 16 / 16) % 16);
                addHex(sb, (ch / 16 / 16) % 16);
                addHex(sb, (ch / 16) % 16);
                addHex(sb, ch % 16);
            }
            else {
                sb.append(ch);
            }
        }
        sb.append("\";\n");
        tr.log(sb.toString());
    }

    private void addHex(StringBuffer sb, int val) {
        if (val >= 0 && val < 10) {
            sb.append((char) (val + '0'));
        }
        else if (val >= 0 && val < 16) {
            sb.append((char) (val + 'A' - 10));
        }
        else {
            sb.append('?');
        }
    }

    public void writeListToFile(List<String> list, File outputFile) throws Exception {
        Writer fw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        int count = 0;
        for (String line : list) {
            fw.write(Integer.toString(count++));
            fw.write(": ");
            fw.write(line);
            fw.write("\n");
        }
        fw.close();
    }


}
