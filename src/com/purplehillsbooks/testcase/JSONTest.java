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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONTokener;
import com.purplehillsbooks.json.YAMLSupport;
import com.purplehillsbooks.streams.MemFile;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestRecorderText;
import com.purplehillsbooks.testframe.TestSet;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 */
public class JSONTest extends TestAbstract implements TestSet {

    TestRecorder tr;

    public JSONTest() {
    	super();
    }

    public void runTests(TestRecorder newTr) throws Exception {
        super.initForTests(newTr);
        tr = newTr;

        testAllWriteAndReadOperations();
        testLongValues();
        testSorting();
        testYMLReading();

    }


    private JSONObject constructCharacterJSON() throws Exception  {
        JSONObject allChars = new JSONObject();
        for (int line=0; line<150; line++) {
            StringBuffer lineBuf = new StringBuffer();
            for (int j=0; j<16; j++) {
                lineBuf.append((char)(33+(line*16)+j));
            }
            allChars.put("Val"+line, lineBuf.toString());
        }
        return allChars;
    }

    private void checkCharacterJSON(JSONObject allChars2) throws Exception {
        for (int line=0; line<150; line++) {
            StringBuffer lineBuf = new StringBuffer();
            for (int j=0; j<16; j++) {
                lineBuf.append((char)(33+(line*16)+j));
            }
            String comp = allChars2.getString("Val"+line);
            String expected = lineBuf.toString();
            if (!comp.equals(expected)) {
                tr.markFailed("character test line Val"+line, "Expected "+expected+" but got "+comp);
            }
            else {
                tr.markPassed("character test line Val"+line);
            }
        }
    }


    public void testAllWriteAndReadOperations() throws Exception {

        JSONObject allChars = constructCharacterJSON();

        File outputFile1 = new File(tr.getProperty("testoutput", null), "characterTest1.json");
        allChars.writeToFile(outputFile1);

        File outputFile2 = new File(tr.getProperty("testoutput", null), "characterTest2.json");
        FileOutputStream fos = new FileOutputStream(outputFile2);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        allChars.write(osw,2,0);
        osw.close();


        JSONObject allChars2 = JSONObject.readFromFile(outputFile1);
        checkCharacterJSON(allChars2);

        JSONObject allChars3 = JSONObject.readFromFile(outputFile2);
        checkCharacterJSON(allChars3);

        FileInputStream fis = new FileInputStream(outputFile1);
        JSONTokener jt = new JSONTokener(fis);
        JSONObject allChars4 = new JSONObject(jt);
        checkCharacterJSON(allChars4);

        fis = new FileInputStream(outputFile2);
        jt = new JSONTokener(fis);
        JSONObject allChars5 = new JSONObject(jt);
        checkCharacterJSON(allChars5);

        fis = new FileInputStream(outputFile1);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        jt = new JSONTokener(isr);
        JSONObject allChars6 = new JSONObject(jt);
        checkCharacterJSON(allChars6);

        fis = new FileInputStream(outputFile2);
        isr = new InputStreamReader(fis, "UTF-8");
        jt = new JSONTokener(isr);
        JSONObject allChars7 = new JSONObject(jt);
        checkCharacterJSON(allChars7);

    }


    public static void main(String args[]) {
        JSONTest thisTest = new JSONTest();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }


    private void testLongValues() throws Exception {
        testOneLongValue(-1);
        testOneLongValue(0);
        testOneLongValue(1);
        long big = 7;
        testOneLongValue(big); //3
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //4
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //5
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //6
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //7
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //8
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //9
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //10
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //11
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //12
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //13
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //14
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //15
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //16
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //17
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //18
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //19
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //20
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //21
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //22
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //23
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //24
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //25
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //26
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //27
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //28
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //29
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //30
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //31
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //32
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //33
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //34
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //35
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //36
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //37
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //38
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //39
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //40
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //41
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //42
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //43
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //44
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //45
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //46
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //47
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //48
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //49
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //50
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //51
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //52
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //53
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //54
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //55
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //56
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //57
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //58
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //59
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //60
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //61
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //62
        testOneLongValue(0-big);
        big = big * 2 + 1;
        testOneLongValue(big); //63 largest long
        testOneLongValue(0-big);
        testOneLongValue(0-big-1);  //the most negative number

        testOneLongValue(big+1);  //wrap around to most negative
        testOneLongValue(big+2);  //next most negative
    }

    private void testOneLongValue(long testVal) throws Exception {
        String testId = "Test JSON long value: "+testVal;
        JSONObject jo = new JSONObject();
        jo.put("dummy", "dummy");
        jo.put("testVal", testVal);
        MemFile mf = new MemFile();
        Writer w = mf.getWriter();
        jo.write(w);
        w.close();


        Reader r = mf.getReader();
        JSONObject jo2 = new JSONObject(new JSONTokener(r));
        r.close();

        long finalVal = jo2.getLong("testVal");

        if (finalVal == testVal) {
            tr.markPassed(testId);
        }
        else {
            tr.markFailed(testId, "Expected '" + testVal + "' but got '" + finalVal + "' instead.");
        }
    }


    private void testSorting() throws Exception {

        JSONArray list = new JSONArray();

        list.put("Grand Canyon");
        list.put("Miami Beach");
        list.put("New York");
        list.put("New Mexico");
        list.put("some other place");
        list.put("Albuquerque");
        list.put("Yellowstone");
        list.put("anywhere else");


        String testSet = "Sorting JavaArray ascending case sensitive";
        list.sortMembers(JSONArray.stringComparator(false, true));
        testArrayMember(list, 0, "Albuquerque", testSet);
        testArrayMember(list, 1, "Grand Canyon", testSet);
        testArrayMember(list, 2, "Miami Beach", testSet);
        testArrayMember(list, 3, "New Mexico", testSet);
        testArrayMember(list, 4, "New York", testSet);
        testArrayMember(list, 5, "Yellowstone", testSet);
        testArrayMember(list, 6, "anywhere else", testSet);
        testArrayMember(list, 7, "some other place", testSet);

        testSet = "Sorting JavaArray Descending case sensitive";
        list.sortMembers(JSONArray.stringComparator(true, true));
        testArrayMember(list, 7, "Albuquerque", testSet);
        testArrayMember(list, 6, "Grand Canyon", testSet);
        testArrayMember(list, 5, "Miami Beach", testSet);
        testArrayMember(list, 4, "New Mexico", testSet);
        testArrayMember(list, 3, "New York", testSet);
        testArrayMember(list, 2, "Yellowstone", testSet);
        testArrayMember(list, 1, "anywhere else", testSet);
        testArrayMember(list, 0, "some other place", testSet);

        testSet = "Sorting JavaArray ascending case INsensitive";
        list.sortMembers(JSONArray.stringComparator(false, false));
        testArrayMember(list, 0, "Albuquerque", testSet);
        testArrayMember(list, 1, "anywhere else", testSet);
        testArrayMember(list, 2, "Grand Canyon", testSet);
        testArrayMember(list, 3, "Miami Beach", testSet);
        testArrayMember(list, 4, "New Mexico", testSet);
        testArrayMember(list, 5, "New York", testSet);
        testArrayMember(list, 6, "some other place", testSet);
        testArrayMember(list, 7, "Yellowstone", testSet);

        testSet = "Sorting JavaArray Descending case INsensitive";
        list.sortMembers(JSONArray.stringComparator(true, false));
        testArrayMember(list, 7, "Albuquerque", testSet);
        testArrayMember(list, 6, "anywhere else", testSet);
        testArrayMember(list, 5, "Grand Canyon", testSet);
        testArrayMember(list, 4, "Miami Beach", testSet);
        testArrayMember(list, 3, "New Mexico", testSet);
        testArrayMember(list, 2, "New York", testSet);
        testArrayMember(list, 1, "some other place", testSet);
        testArrayMember(list, 0, "Yellowstone", testSet);


    }

    private void testArrayMember(JSONArray list, int index, String value, String testSet) throws Exception {
        String actual = list.getString(index);
        if (actual.equals(value)) {
            tr.markPassed(testSet+"["+index+"]");
        }
        else {
            tr.markFailed(testSet+"["+index+"]", "Expected '"+value+"' but found '"+actual+"' instead");
        }
    }
    
    
    private void testYMLReading() {
    	File testDataFolder = new File(tr.getProperty("source", null), "testdata");
    	
    	for (File child : testDataFolder.listFiles()) {
    		String name= child.getName();
    		try {
	    		if (!name.startsWith("YMLTest") || !name.endsWith("yml")) {
	    			//skip all the files except the YMLTest0001.yml files
	    			continue;
	    		}
	    		
	    		String jsonName = name.substring(0, 10) + ".json";
	    		JSONObject data = YAMLSupport.readYAMLFile(child);
	    		
	    		//File jsonFile = new File(testDataFolder, jsonName);
	    		File outputFile = new File(tr.getProperty("testoutput", null), jsonName);

	    		data.writeToFile(outputFile);
	    		compareGeneratedTextFile(name, jsonName);	    		
    		}
    		catch (Exception e) {
    			tr.markFatalError(e);
    		}
    	}
    }


}
