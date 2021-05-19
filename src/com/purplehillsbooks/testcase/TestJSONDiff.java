package com.purplehillsbooks.testcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONDelta;
import com.purplehillsbooks.json.JSONDiff;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.streams.CSVHelper;
import com.purplehillsbooks.streams.MemFile;
import com.purplehillsbooks.streams.StreamHelper;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestRecorderText;
import com.purplehillsbooks.testframe.TestSet;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 */
public class TestJSONDiff extends TestAbstract implements TestSet {

    JSONDiff jdFull;
    JSONDiff jdLtd;

    public TestJSONDiff() {
    	super();
    }

    public void runTests(TestRecorder newTr) throws Exception {
    	initForTests(newTr);
        jdFull = new JSONDiff(true);
        jdLtd  = new JSONDiff(false);
        diffCases();
        deltaCases();
        deltaFileCases();
    }


    private void diffCases() throws Exception {
        JSONObject ob1 = new JSONObject();
        JSONObject ob2 = new JSONObject();

        //test completely empty objects
        doDiff("JSONDiff-test1", ob1, ob2);

        ob1.put("telephone", "555-444-3322");
        doDiff("JSONDiff-test2", ob1, ob2);

        ob2.put("telephone", "555-444-3322");
        doDiff("JSONDiff-test3", ob1, ob2);

        ob2.put("extra", "This is only in one object");
        doDiff("JSONDiff-test4", ob1, ob2);

        JSONObject weather = new JSONObject();
        weather.put("temp", "warm");
        weather.put("humidity", "98%");
        weather.put("wind", "light");

        ob1.put("weather", weather);
        doDiff("JSONDiff-test5", ob1, ob2);

        JSONObject weather2 = new JSONObject();
        weather2.put("temp", "warm");
        ob2.put("weather", weather2);
        doDiff("JSONDiff-test6", ob1, ob2);


        weather2.put("humidity", "98%");
        doDiff("JSONDiff-test7", ob1, ob2);


        weather2.put("wind", "light");
        doDiff("JSONDiff-test8", ob1, ob2);

        JSONObject arrayTest1 = new JSONObject();
        JSONObject arrayTest2 = new JSONObject();
        JSONArray ar1 = new JSONArray();
        JSONArray ar2 = new JSONArray();
        JSONObject tv1 = new JSONObject();
        JSONObject tv2 = new JSONObject();
        tv1.put("name", "Joe");
        tv1.put("city",  "Los Angeles");
        tv2.put("name", "Steve");
        tv2.put("city",  "New York");

        ar1.put(tv1);
        ar1.put(tv2);
        arrayTest1.put("people", ar1);

        ar2.put(tv2);
        ar2.put(tv1);
        arrayTest2.put("people", ar2);

        HashMap<String, String> listObjectKey = new HashMap<String, String>();
        listObjectKey.put("people", "name");
        jdFull.setObjectKeyMap(listObjectKey);
        jdLtd.setObjectKeyMap(listObjectKey);

        //these are the same, the objects are in a different order
        doDiff("JSONDiff-array1", arrayTest1, arrayTest2);

        //now make the second array have a single object
        //but the one matches the second in the other
        ar2 = new JSONArray();
        ar2.put(tv2);
        arrayTest2.put("people", ar2);
        doDiff("JSONDiff-array2", arrayTest1, arrayTest2);

        JSONObject tv3 = new JSONObject();
        tv3.put("name", "Joe");
        tv3.put("city",  "Mexico");  //different city
        ar2 = new JSONArray();
        ar2.put(tv2);
        ar2.put(tv3);
        arrayTest2.put("people", ar2);
        doDiff("JSONDiff-array3", arrayTest1, arrayTest2);

    }

    private void deltaFileCases() throws Exception {

        deltaJSONFiles("AllNodeTypes");

    }


    private void deltaCases() throws Exception {
        JSONObject ob1 = new JSONObject();
        JSONObject ob2 = new JSONObject();

        //test completely empty objects
        doDelta("JSONDelta-test1", ob1, ob2);

        ob1.put("telephone", "555-444-3322");
        doDelta("JSONDelta-test2a", ob1, ob2);
        doDelta("JSONDelta-test2b", ob2, ob1);

        ob2.put("telephone", "555-444-3322");
        doDelta("JSONDelta-test3", ob1, ob2);

        ob2.put("extra", "This is only in the second object");
        doDelta("JSONDelta-test4a", ob1, ob2);
        doDelta("JSONDelta-test4b", ob2, ob1);

        JSONObject weather = new JSONObject();
        weather.put("temp", "warm");
        weather.put("humidity", "98%");
        weather.put("wind", "light");

        ob1.put("weather", weather);
        doDelta("JSONDelta-test5a", ob1, ob2);
        doDelta("JSONDelta-test5b", ob2, ob1);

        JSONObject weather2 = new JSONObject();
        weather2.put("temp", "warm");
        ob2.put("weather", weather2);
        doDelta("JSONDelta-test6a", ob1, ob2);
        doDelta("JSONDelta-test6b", ob2, ob1);


        weather2.put("humidity", "98%");
        doDelta("JSONDelta-test7a", ob1, ob2);
        doDelta("JSONDelta-test7b", ob2, ob1);


        weather2.put("wind", "light");
        doDelta("JSONDelta-test8a", ob1, ob2);
        doDelta("JSONDelta-test8b", ob2, ob1);

        JSONObject person1 = new JSONObject();
        person1.put("id", "1324");
        person1.put("name", "Howard Jones");
        person1.put("age", 23);

        JSONObject person2 = new JSONObject();
        person2.put("id", "7777");
        person2.put("name", "Bill Bixby");
        person2.put("age", 24);

        JSONObject person3 = new JSONObject();
        person3.put("id", "6257");
        person3.put("name", "Alex Jones");
        person3.put("age", -3);

        JSONObject person4 = new JSONObject();
        person4.put("id", "5577");
        person4.put("name", "Bjorn Borg");
        person4.put("age", 66);

        JSONObject person5 = new JSONObject();
        person5.put("id", "8899");
        person5.put("name", "Bobby The Knife");
        person5.put("age", 43);

        ob1 = new JSONObject();
        ob2 = new JSONObject();
        ob1.put("overlap", "This is object 1");
        ob2.put("overlap", "This is object 2");
        ob1.put("single1", "This is object 1");
        ob2.put("single2", "This is object 2");

        JSONArray ar1 = new JSONArray();
        JSONArray ar2 = new JSONArray();
        ob1.put("people", ar1);
        ob2.put("people", ar2);

        doDelta("JSONDelta-testList1a", ob1, ob2);
        doDelta("JSONDelta-testList1b", ob2, ob1);

        ar1 = new JSONArray();
        ar2 = new JSONArray();
        ar1.put(person1);
        ar2.put(person1);
        ob1.put("people", ar1);
        ob2.put("people", ar2);

        doDelta("JSONDelta-testList2a", ob1, ob2);
        doDelta("JSONDelta-testList2b", ob2, ob1);

        ar1 = new JSONArray();
        ar2 = new JSONArray();
        ar1.put(person1);
        ar2.put(person2);
        ob1.put("people", ar1);
        ob2.put("people", ar2);

        doDelta("JSONDelta-testList3a", ob1, ob2);
        doDelta("JSONDelta-testList3b", ob2, ob1);


        ar1 = new JSONArray();
        ar2 = new JSONArray();
        ar1.put(person1);
        ar1.put(person3);
        ar1.put(person5);
        ar2.put(person1);
        ar2.put(person4);
        ar2.put(person5);
        ob1.put("people", ar1);
        ob2.put("people", ar2);

        doDelta("JSONDelta-testList4a", ob1, ob2);
        doDelta("JSONDelta-testList4b", ob2, ob1);

        JSONArray stringList1 = new JSONArray();
        JSONArray stringList2 = new JSONArray();
        stringList1.put("bread");
        stringList1.put("milk");
        stringList1.put("butter");
        stringList1.put("coffee");
        stringList2.put("butter");
        stringList2.put("milk");
        stringList2.put("tea");
        stringList2.put("muffin");

        //set the people objects the same, and the string lists as well
        ob1.put("people", ar1);
        ob2.put("people", ar1);
        ob1.put("food", stringList1);
        ob2.put("food", stringList1);

        doDelta("JSONDelta-strList1", ob1, ob2);

        ob1.put("food", stringList1);
        ob2.put("food", stringList2);

        doDelta("JSONDelta-strList2a", ob1, ob2);
        doDelta("JSONDelta-strList2b", ob2, ob1);


        stringList2.put("marmalade");

        doDelta("JSONDelta-strList3a", ob1, ob2);
        doDelta("JSONDelta-strList3b", ob2, ob1);

        JSONArray pointList1 = new JSONArray();
        JSONArray pointList2 = new JSONArray();
        pointList1.put(pointObject(10,15));
        pointList1.put(pointObject(20,25));
        pointList1.put(pointObject(30,35));
        pointList2.put(pointObject(10,15));
        pointList2.put(pointObject(120,25));
        pointList2.put(pointObject(30,35));
        ob1 = new JSONObject();
        ob2 = new JSONObject();
        ob1.put("overlap", "This is object 1");
        ob2.put("overlap", "This is object 2");
        ob1.put("points", pointList1);
        ob2.put("points", pointList1);

        doDelta("JSONDelta-strList4", ob1, ob2);

        ob1.put("points", pointList1);
        ob2.put("points", pointList2);

        doDelta("JSONDelta-strList4a", ob1, ob2);
        doDelta("JSONDelta-strList4b", ob2, ob1);
    }


    private void deltaJSONFiles(String baseName) throws Exception {
        File inputFileA = new File(sourceDataFolder, baseName + "A.json");
        File inputFileB = new File(sourceDataFolder, baseName + "B.json");
        File inputFileHints = new File(sourceDataFolder, baseName + "HINTS.csv");
        File inputFileDelta = new File(sourceDataFolder, baseName + "DELTA.json");

        File outputDelta = new File(testOutputFolder, baseName + "DELTA.json");

        JSONObject objA = JSONObject.readFromFile(inputFileA);
        JSONObject objB = JSONObject.readFromFile(inputFileB);
        HashMap<String, String> hints = readHints(inputFileHints);

        JSONDelta fileDelta = new JSONDelta();
        fileDelta.setListKeyMap(hints);
        fileDelta.setDeletedValueIndicator("$deleteme$");
        JSONObject objDelta = fileDelta.createDelta(objA, objB);
        objDelta.writeToFile(outputDelta);

        compareFiles(inputFileDelta, outputDelta, "JSONDelta file test "+baseName);

    }

    private HashMap<String, String> readHints(File hintFile) throws Exception {
        FileInputStream fis = new FileInputStream(hintFile);
        InputStreamReader r = new InputStreamReader(fis, "UTF-8");
        HashMap<String, String> res = new HashMap<String, String>();

        List<String> line = CSVHelper.parseLine(r);
        while (line!=null) {
	        if (line.size()==2) {
	        	res.put(line.get(0), line.get(1));
	        }
	        line = CSVHelper.parseLine(r);
        }
        r.close();
        return res;
    }

    private JSONObject pointObject(int x, int y) throws Exception {
        JSONObject point = new JSONObject();
        point.put("x", x);
        point.put("y", y);
        return point;
    }

    private void doDiff(String rootFile, JSONObject ob1, JSONObject ob2) throws Exception {

        List<List<String>> table = jdFull.createDiff(ob1, ob2);
        compareToFile(rootFile+"a.csv", table);
        table = jdLtd.createDiff(ob1, ob2);
        compareToFile(rootFile+"b.csv", table);
    }



    private void compareToFile(String fileName, List<List<String>> table) throws Exception {
        MemFile mf = new MemFile();
        Writer w = mf.getWriter();
        CSVHelper.writeTable(w, table);
        w.flush();
        w.close();


        File outputFile1 = new File(testOutputFolder, fileName);
        StreamHelper.copyReaderToFile(mf.getReader(), outputFile1, "UTF-8");

        File inputFile1 = new File(sourceDataFolder, fileName);
        if (!inputFile1.exists()) {
            tr.markFailed("JSONDiff test"+fileName, "test data file is missing: "+inputFile1);
            return;
        }
        FileInputStream fis = new FileInputStream(inputFile1);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        Reader r2 = mf.getReader();
        int ch = isr.read();
        int charPos = 0;
        while (ch>=0) {
            //consume the stupid line feed characters
            while (ch=='\r') {
                ch = isr.read();
            }
            charPos++;
            int ch2 = r2.read();
            //consume the stupid line feed characters
            while (ch2=='\r') {
                ch2 = r2.read();
            }
            if (ch!=ch2) {
                isr.close();
                tr.markFailed("JSONDiff test"+fileName, "comparison failed on character "+charPos);
                return;
            }
            ch = isr.read();
        }
        tr.markPassed("JSONDiff test"+fileName);
        isr.close();
    }

    public static void main(String args[]) {
        TestJSONDiff thisTest = new TestJSONDiff();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }

    private void doDelta(String compFile, JSONObject oldObj, JSONObject newObj) throws Exception {;
    	JSONDelta deltaMaker = new JSONDelta();
    	deltaMaker.setDeletedValueIndicator("@deleteMe@");
    	JSONObject delta = deltaMaker.createDelta(oldObj, newObj);

        File outputFile = new File(testOutputFolder, compFile+".json");
        File inputFile = new File(testCompareFolder, compFile+".json");
    	delta.writeToFile(outputFile);

        if (!inputFile.exists()) {
            tr.markFailed("JSONDelta test"+compFile, "missing test data file not found: "+inputFile);
            doDeltaLog(compFile, oldObj, newObj, delta);
            return;
        }

        compareFiles(inputFile, outputFile, "JSONDelta test"+compFile);
        doDeltaLog(compFile, oldObj, newObj, delta);

    }

    private boolean compareFiles(File file1, File file2, String testDesc) throws Exception  {
    	if (!file1.exists()) {
    		tr.markFailed(testDesc, "first file does not exist: "+file1);
            return false;
    	}
    	if (!file2.exists()) {
    		tr.markFailed(testDesc, "Second file does not exist: "+file2);
            return false;
    	}
        FileInputStream fis1 = new FileInputStream(file1);
        InputStreamReader inputReader = new InputStreamReader(fis1, "UTF-8");
        FileInputStream fis2 = new FileInputStream(file2);
        InputStreamReader outputReader = new InputStreamReader(fis2, "UTF-8");
        int ch = inputReader.read();
        int charPos = 0;
        while (ch>=0) {
            charPos++;
            int ch2 = outputReader.read();
            if (ch!=ch2) {
            	inputReader.close();
            	outputReader.close();
                tr.markFailed(testDesc, "comparison failed on character "+charPos);
                return false;
            }
            ch = inputReader.read();
        }
        tr.markPassed(testDesc);
        inputReader.close();
        outputReader.close();
        return true;
    }


    private void doDeltaLog(String compFile, JSONObject oldObj, JSONObject newObj, JSONObject delta) throws Exception  {
        File outputFile = new File(testOutputFolder, compFile+"-LOG.txt");
    	System.out.println("*** wrote log file: "+outputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
        w.write("JDON Delta TEST OUTPUT\n\n==============================\nOLD object:\n");
        w.write(oldObj.toString(2));
        w.write("\n\n==============================\nNEW object:\n");
        w.write(newObj.toString(2));
        w.write("\n\n==============================\nDELTA object:\n");
        w.write(delta.toString(2));
        w.write("\n\n==============================\n");
        w.close();
    }


}
