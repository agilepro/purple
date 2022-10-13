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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import com.purplehillsbooks.json.Dom2JSON;
import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONDiff;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONTokener;
import com.purplehillsbooks.json.YAMLSupport;
import com.purplehillsbooks.testframe.TestRecorder;
import com.purplehillsbooks.testframe.TestRecorderText;
import com.purplehillsbooks.testframe.TestSet;
import com.purplehillsbooks.xml.Mel;

/*
 *
 * Author: Keith Swenson
 * Copyright: Keith Swenson, all rights reserved
 * License: This code is made available under the GNU Lesser GPL license.
 */
public class Test1 extends TestAbstract implements TestSet {

    public Test1() {
        super();
    }

    public void runTests(TestRecorder newTr) throws Exception {
        super.initForTests(newTr);

        testInvalidCharacters();
        TestUserProfileFile();
        TestEmpty();
        TestReadWrite();
        testGenSchema();
    }

    private void TestUserProfileFile() throws Exception {
        Mel me = Mel.readInputStream(getData1Stream(), Mel.class);

        List<Mel> userprofileList = me.getChildren("userprofile");

        Hashtable<String,Integer> hints = new Hashtable<String,Integer>();
        hints.put("userprofile", Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("data",        Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("idrec",       Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("container",   Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("attr",        Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("contains",    Dom2JSON.HINT_OBJECT_ARRAY);


        Mel userprofile = userprofileList.get(0);
        testNotNull(userprofile, "first user profile object");
        testVal(userprofile.getName(), "userprofile", "first user profile object: name");
        testVal(userprofile.getAttribute("id"), "MOBQNTGYF", "first user profile object: id");
        testVal(userprofile.getScalar(""), "", "first user profile object: lastlogin");
        testScalar(userprofile, "lastlogin", "1244434616875", "first user profile object");
        testScalar(userprofile, "lastupdated", "1241018683687", "first user profile object");
        testScalar(userprofile, "username", "AAA", "first user profile object");
        testScalar(userprofile, "nonexistantkey", "", "first user profile object");

        // get the second user profile element
        userprofile = userprofileList.get(1);
        testNotNull(userprofile, "second user profile object");
        testVal(userprofile.getName(), "userprofile", "second user profile object: name");
        testScalar(userprofile, "username", "BBB", "second user profile object");
        testScalar(userprofile, "nonexistantkey_xyz", "", "first user profile object");

        Mel idrec = userprofile.getChild("idrec", 0);
        testNotNull(idrec, "second user idrec object");
        testVal(idrec.getAttribute("loginid"), "aaa@gmail.com", "second user idrec object login id");

        // get the third user profile element
        userprofile = userprofileList.get(2);
        testNotNull(userprofile, "third user profile object");
        testVal(userprofile.getName(), "userprofile", "third user profile object: name");
        testScalar(userprofile, "username", "CCC", "third user profile object");
        testScalar(userprofile, "lastlogin", "1244512041541", "third user profile object");

        writeBothStylesAndCompare(me, "UP_Test001", hints);

        userprofile.setScalar("username", "Christopher Columbus");
        testScalar(userprofile, "username", "Christopher Columbus", "modified user profile object");
        testScalar(userprofile, "lastlogin", "1244512041541", "modified user profile object");

        writeBothStylesAndCompare(me, "UP_Test002", hints);
    }

    private void TestEmpty() throws Exception {
        Hashtable<String,Integer> hints = new Hashtable<String,Integer>();
        hints.put("book",    Dom2JSON.HINT_OBJECT_ARRAY);
        hints.put("library", new Integer(3));
        hints.put("stores", new Integer(1));
        hints.put("reading", new Integer(3));

        Mel testTree = Mel.createEmpty("library", Mel.class);
        writeBothStylesAndCompare(testTree, "constTest001", hints);

        Mel book1 = testTree.addChild("book", Mel.class);
        writeBothStylesAndCompare(testTree, "constTest002", hints);

        book1.setScalar("title", "The Black Swan");
        writeBothStylesAndCompare(testTree, "constTest003", hints);

        book1.setScalar("author", "Nicholas Taleb");
        writeBothStylesAndCompare(testTree, "constTest004", hints);

        List<String> stores = new ArrayList<String>();
        stores.add("Barnes & Noble");
        stores.add("Amazon");
        stores.add("Hicklebees");
        stores.add("Target");
        book1.setVector("stores", stores);
        writeBothStylesAndCompare(testTree, "constTest005", hints);

        Mel book2 = testTree.addChild("book", Mel.class);
        writeBothStylesAndCompare(testTree, "constTest006", hints);

        book2.setVector("stores", stores);
        writeBothStylesAndCompare(testTree, "constTest007", hints);

        book2.setScalar("author", "L Frank Baum");
        writeBothStylesAndCompare(testTree, "constTest008", hints);

        book2.setScalar("title", "Wizard of Oz");
        writeBothStylesAndCompare(testTree, "constTest009", hints);

        book2.setScalar("author", "L. Frank Baum");
        writeBothStylesAndCompare(testTree, "constTest010", hints);

        book1.setScalar("length", "225");
        book2.setScalar("length", "350");
        writeBothStylesAndCompare(testTree, "constTest011", hints);

        // now test that setting a value to a scalar removes it from the file
        book1.setScalar("author", null);
        writeBothStylesAndCompare(testTree, "constTest012", hints);

        Mel reading1 = book1.addChild("reading", Mel.class);
        Mel reading2 = book1.addChild("reading", Mel.class);
        Mel reading3 = book1.addChild("reading", Mel.class);

        reading1.setAttribute("date", "5/15/2009");
        reading3.setAttribute("date", "7/15/2009");
        writeBothStylesAndCompare(testTree, "constTest013", hints);

        reading1.setScalar("readby", "Mark");
        reading2.setScalar("readby", "Joe");
        reading3.setScalar("readby", "Alex");
        writeBothStylesAndCompare(testTree, "constTest014", hints);

        reading1.setScalar("rating", "A+");
        reading2.setScalar("rating", "B");
        reading3.setScalar("rating", "D-");
        writeBothStylesAndCompare(testTree, "constTest015", hints);

    }

    private void TestReadWrite() throws Exception {
        Hashtable<String,Integer> hints = new Hashtable<String,Integer>();
        hints.put("userprofile", new Integer(3));
        hints.put("servlet", new Integer(3));
        hints.put("servlet-mapping", new Integer(3));

        //these are the XPDL hints
        hints.put("Pool", new Integer(3));
        hints.put("NodeGraphicsInfo", new Integer(3));
        hints.put("Lane", new Integer(3));
        hints.put("TransitionRef", new Integer(3));
        hints.put("Activity", new Integer(3));
        hints.put("Transition", new Integer(3));
        hints.put("Coordinates", new Integer(3));
        hints.put("WorkflowProcess", new Integer(3));
        hints.put("TransitionRestriction", new Integer(3));
        hints.put("Transition", new Integer(3));
        hints.put("ExtendedAttribute", new Integer(3));
        hints.put("Participant", new Integer(3));
        hints.put("Connector", new Integer(3));



        File sourceFile = new File(sourceDataFolder, "UserProfiles.xml");
        Mel test = Mel.readFile(sourceFile, Mel.class);
        writeBothStylesAndCompare(test, "dataFile001", hints);

        test = Mel.readFile(new File(sourceDataFolder, "web.xml"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile002", hints);

        test = Mel.readFile(new File(sourceDataFolder, "TroubleTicket.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile003", hints);

        test = Mel.readFile(new File(sourceDataFolder, "FujitsuExample1_x2.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile004", hints);

        test = Mel.readFile(new File(sourceDataFolder, "simpleProcess2a_mod.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile005", hints);

        test = Mel.readFile(new File(sourceDataFolder, "simpleProcess2a.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile006", hints);

        test = Mel.readFile(new File(sourceDataFolder, "Loyalty_updated_Mar3.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile007", hints);

        test = Mel.readFile(new File(sourceDataFolder, "simplefuj2new_with2008.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile008", hints);

        test = Mel.readFile(new File(sourceDataFolder, "simplefuj2new_with2008b.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile009", hints);

        test = Mel.readFile(new File(sourceDataFolder, "Loyalty.xpdl"), Mel.class);
        writeBothStylesAndCompare(test, "dataFile010", hints);

    }

    private void testGenSchema() throws Exception {
        Hashtable<String,Integer> hints = new Hashtable<String,Integer>();
        hints.put("userprofile", new Integer(3));
        hints.put("servlet", new Integer(3));
        hints.put("servlet-mapping", new Integer(3));

        //these are the XPDL hints
        hints.put("Pool", new Integer(3));
        hints.put("NodeGraphicsInfo", new Integer(3));
        hints.put("Lane", new Integer(3));
        hints.put("TransitionRef", new Integer(3));
        hints.put("Activity", new Integer(3));
        hints.put("Transition", new Integer(3));
        hints.put("Coordinates", new Integer(3));
        hints.put("WorkflowProcess", new Integer(3));
        hints.put("TransitionRestriction", new Integer(3));
        hints.put("Transition", new Integer(3));
        hints.put("ExtendedAttribute", new Integer(3));
        hints.put("Participant", new Integer(3));

        //these are minimal schema elements
        hints.put("container", new Integer(3));
        hints.put("contains", new Integer(3));
        hints.put("attr", new Integer(3));

        //these are minimal rss elements
        hints.put("item", new Integer(3));
        hints.put("content", new Integer(3));
        hints.put("category", new Integer(3));



        Mel me = Mel.readFile(new File(sourceDataFolder, "Loyalty.xpdl"), Mel.class);
        writeBothStylesAndCompare(me, "GEN_Test001", hints);

        me = Mel.readFile(new File(sourceDataFolder, "RawFeed1.rss"), Mel.class);
        writeBothStylesAndCompare(me, "GEN_Test003", hints);

        me.eliminateCData();
        writeBothStylesAndCompare(me, "GEN_Test004", hints);

    }


    private void writeBothStylesAndCompare(Mel me, String fileNamePart, Hashtable<String,Integer> hints) throws Exception {
        writeFileAndCompare(me, fileNamePart + ".xml");
        writeJSONAndCompare(me, fileNamePart, hints);
    }


    private void writeFileAndCompare(Mel me, String fileName) throws Exception {
        me.reformatXML();

        File outputFile = new File(tr.getProperty("testoutput", null), fileName);
        me.writeToFile(outputFile);

        compareFiles(outputFile, fileName);
    }

    static private Random rand = new Random();

    /**
     * Convert the XML to JSON
     * write it to a file
     * read that file
     * write it again and compare to archive
     */
    private void  writeJSONAndCompare(Mel me, String fileNamePart, Hashtable<String,Integer> hints) throws Exception {
        String fileName = fileNamePart + ".json";
        JSONObject jsonRep = Dom2JSON.convertDomToJSON(me.getDocument(), hints);

        File outputFile = new File(tr.getProperty("testoutput", null), fileName);
        File intermediateFile = new File(tr.getProperty("testoutput", null), fileName+"$testtmp$");

        //randomly choose one method or the other.  I don't like randomness in tests
        //but there is too much overhead in testing every case both ways.
        //If they both work, they should be exactly equal....
        //we just want to know that they both work.  If this test starts to fail
        //we may need to isolate to one or the other.
        //
        // FIRST, write to intermediate file
        if (rand.nextBoolean()) {
            FileOutputStream fos = new FileOutputStream(intermediateFile);
            Writer w = new OutputStreamWriter(fos, "UTF-8");
            jsonRep.write(w, 2, 0);
            w.close();
            fos.close();
        }
        else {
            jsonRep.writeToFile(intermediateFile);
        }

        //SECOND read from intermediate
        if (rand.nextBoolean()) {
            FileInputStream fis = new FileInputStream(intermediateFile);
            JSONTokener jt = new JSONTokener(fis);
            jsonRep = new JSONObject(jt);
            fis.close();
        }
        else {
            jsonRep = JSONObject.readFromFile(intermediateFile);
        }

        //THIRD, write to final file
        if (rand.nextBoolean()) {
            FileOutputStream fos = new FileOutputStream(outputFile);
            Writer w = new OutputStreamWriter(fos, "UTF-8");
            jsonRep.write(w, 2, 0);
            w.close();
            fos.close();
        }
        else {
            jsonRep.writeToFile(outputFile);
        }

        //FOURTH write out the YML file and read it back in
        File yamlFile = new File(tr.getProperty("testoutput", null), fileNamePart + ".yml");
        JSONObject cleanedKeyObj = cleanUpKeys(jsonRep);
        
        YAMLSupport.writeYAMLFile(cleanedKeyObj, yamlFile);
        JSONObject yamlCopy = YAMLSupport.readYAMLFile(yamlFile);
        JSONDiff differ = new JSONDiff(false);
        List<List<String>> difRes = differ.createDiff(cleanedKeyObj, yamlCopy);
        if (difRes.size()>0) {
            //this means it failed
            File difOutFile = new File(tr.getProperty("testoutput", null), fileNamePart + ".yml.failure.txt");
            JSONDiff.dumpToCSV(difOutFile, difRes);
        }


        //FIFTH delete the intermediate so it is not in the archive
        intermediateFile.delete();

        compareFiles(outputFile, fileName);
    }


    private void compareFiles(File outputFile, String fileName) throws Exception {
        String testId = "Compare output to " + fileName;
        compareGeneratedTextFile(testId, fileName);
    }


    /**
     * YAML format does not allow colon characters in the keys, and will
     * convert the key to have a hyphen.  This makes it hard to do a
     * diff later.  Rather than write a special diff that ignores the
     * difference in colon converted to hyphen, this method converts all
     * the key values to have a hyphen so that the final comparison
     * does not see any differences.
     */
    private JSONObject cleanUpKeys(JSONObject inObj) throws Exception {
        JSONObject outObj = new JSONObject();
        for (String key : inObj.keySet()) {
            Object value = inObj.get(key);
            if (key.contains(":")) {
                key = key.replace(':', '-');
            }
            if (value instanceof JSONObject) {
                outObj.put(key, cleanUpKeys((JSONObject)value));
            }
            else if (value instanceof JSONArray) {
                outObj.put(key, cleanUpSubKeys((JSONArray)value));
            }
            else {
                outObj.put(key, value);
            }
        }
        return outObj;
    }
    private JSONArray cleanUpSubKeys(JSONArray inObj) throws Exception {
        JSONArray outArray = new JSONArray();
        for (int i=0; i<inObj.length(); i++) {
            Object value = inObj.get(i);
            if (value instanceof JSONObject) {
                outArray.put(cleanUpKeys((JSONObject)value));
            }
            else if (value instanceof JSONArray) {
                outArray.put(cleanUpSubKeys((JSONArray)value));
            }
            else {
                outArray.put(value);
            }
        }
        return outArray;
    }


    public static InputStream getData1Stream() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("<userprofiles>");
        sb.append("\n  <userprofile id=\"MOBQNTGYF\">");
        sb.append("\n    <homepage>http://web.com/processleaves/p/main/public.htm</homepage>");
        sb.append("\n    <lastlogin>1244434616875</lastlogin>");
        sb.append("\n    <lastupdated>1241018683687</lastupdated>");
        sb.append("\n    <username>AAA</username>");
        sb.append("\n  </userprofile>");
        sb.append("\n  <userprofile id=\"YVXIXTGYF\">");
        sb.append("\n    <idrec loginid=\"aaa@gmail.com\"/>");
        sb.append("\n    <username>BBB</username>");
        sb.append("\n  </userprofile>");
        sb.append("\n  <userprofile id=\"HMSBPXKYF\">");
        sb.append("\n    <idrec loginid=\"jjj@a.com\"/>");
        sb.append("\n    <idrec confirmed=\"true\" loginid=\"ddd@a.com\"/>");
        sb.append("\n    <lastlogin>1244512041541</lastlogin>");
        sb.append("\n    <username>CCC</username>");
        sb.append("\n  </userprofile>");
        sb.append("\n</userprofiles>");
        String sbx = sb.toString();
        byte[] buf = sbx.getBytes("UTF-8");
        return new ByteArrayInputStream(buf);
    }

    public static InputStream getData2Stream() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("<schema>");
        sb.append("\n  <root>userprofiles</root>");
        sb.append("\n  <container name=\"userprofiles\">");
        sb.append("\n    <contains plural=\"*\">userprofile</contains>");
        sb.append("\n  </container>");
        sb.append("\n  <container name=\"userprofile\">");
        sb.append("\n    <attr name=\"id\" type=\"String\"/>");
        sb.append("\n    <contains>homepage</contains>");
        sb.append("\n    <contains>lastlogin</contains>");
        sb.append("\n    <contains>lastupdated</contains>");
        sb.append("\n    <contains>username</contains>");
        sb.append("\n    <contains plural=\"*\">idrec</contains>");
        sb.append("\n  </container>");
        sb.append("\n  <container name=\"idrec\">");
        sb.append("\n    <attr name=\"loginid\"   type=\"String\"/>");
        sb.append("\n    <attr name=\"confirmed\" type=\"String\"/>");
        sb.append("\n  </container>");
        sb.append("\n  <data name=\"lastlogin\"   type=\"Integer\"/>");
        sb.append("\n  <data name=\"lastupdated\" type=\"Integer\"/>");
        sb.append("\n  <data name=\"username\"    type=\"String\"/>");
        sb.append("\n  <data name=\"homepage\"    type=\"String\"/>");
        sb.append("\n  <data name=\"username\"    type=\"String\"/>");
        sb.append("\n</schema>");
        String sbx = sb.toString();
        byte[] buf = sbx.getBytes("UTF-8");
        return new ByteArrayInputStream(buf);
    }


    private void testInvalidCharacters() throws Exception {
        Mel me = Mel.createEmpty("testroot", Mel.class);
        Mel firstContainer = me.addChild("charTesting", Mel.class);
        for (int i=0; i<32; i++) {
            firstContainer.setScalar("baddata"+i, "ABC"+((char)i)+"DEF");
            firstContainer.setAttribute("badatt"+i, "ABC"+((char)i)+"DEF");
            firstContainer.setScalar("badFirstChar"+i, ""+((char)i)+"DEF");
            firstContainer.setScalar("badLastChar"+i, "ABC"+((char)i));
            firstContainer.setScalar("badOnlyChar", ""+((char)i));
        }
        safeSerialization(me);
    }

    /**
     * Found out that the XML parser bombs out if you put $#2; into the
     * source XML file.   However, it writes this out just fine.   This means
     * data in memory can be written and no able to be read.  We need to assure
     * that everything written can be read without failure, so it means stripping
     * out the illegal characters while writing.   Those characters are lost, but
     * since they are not allowed, we can't allow them to be written.
     */
    private void safeSerialization(Mel me) throws Exception {
        //MemFile mf = new MemFile();
        //me.writeToOutputStream(mf.getOutputStream());
        //
        //Mel.readInputStream(mf.getInputStream(), Mel.class);
        File randomFile = new File(tr.getProperty("testoutput", null), "badData"+System.currentTimeMillis()+".xml");
        System.out.println("Writing to "+randomFile);
        me.writeToFile(randomFile);

        //all we have to know is whether it fails to read it
        Mel.readFile(randomFile, Mel.class);

    }




    public static void main(String args[]) {
        Test1 thisTest = new Test1();
        TestRecorderText.parseArgsRunTests(args, thisTest);
    }


}
