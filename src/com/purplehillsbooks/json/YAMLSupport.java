package com.purplehillsbooks.json;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.purplehillsbooks.streams.ReadAhead;
import com.purplehillsbooks.streams.UTF8FileReader;
import com.purplehillsbooks.streams.UTF8FileWriter;


/**
 * Extends the JSONObject and JSONArray classes to being read from and
 * written to simplified YAML files.  Simplified because it only supports
 * key:value syntax where the key is not quoted and does not have unreasonable characters
 * in it, and the value can be quoted to include newlines and hash characters,
 * but most of the extreme hacker capabilities of YAML are not supported.
 * This is suitable for all config files and that sort of usage with user who
 * are not uber-hackers and simply want an easy way to enter values without being
 * concerned with a lot of syntax overhead.
 *
 * This implements a simplified YAML support assuming:
 * (1) None of the key values may contain a colon.  Attempt to write a
 *     JSON object with a colon in the key value will have a hyphen substituted for the colon.
 * (2) Key values must not be quoted.  Only plain key values are allowed, or on reading if
 *     the key is quoted the key will be taken exactly as in the file quotes and all.
 *     Of course newlines and other control characters are not allowd either.
 * (3) Mixed JSON is not allowed. plain key, colon, and plain values are all that is supported
 *     you can not put a braces and expect an object to be formed, and commas are never ignored
 *     you can not put a square bracket and expect an array to be formed,
 *     These will just give you values with the braces and square brackets in them.
 *     Reflexively, there is nothing special you have to do if you have a value with
 *     braces or square brackets.  They are just treated as any other character.
 * (4) Space characters on either side of key or value will be trimmed off
 * (5) "&" anchors and "*" aliases are not supported.  You must literally copy the value in the file
 *     when you need two or more copies of a value.
 * (6) Multi-line values are not supported, strings must be a single line.
 *     Neither the literal '|' nor the folded '>' supported, and no chomping either
 * (7) Colons do not require space after them, thus the first colon found terminates a key whether
 *     it is followed by a space or not.  Further colons on a line are treated as characters.
 * (8) triple-dash document start and triple-dot document end is not supported.   It is assumed
 *     that the entire file is a single YAML document
 * (9) Single-quote (apostrophe) quoting is not supported, use double quote only
 * (10) ONLY UTF-8 is supported (obviously) -- although you could set up your own Reader
 * (11) No support for directives '%' nor versions of YAML
 * (12) Characters '@' and '`' are not treated special in any way, they are just characters
 * (13) Line feed characters '\r' are stripped out both reading and writing
 * (14) Tabs are not supported, are not treated as white space and are never written out
 * (15) The only escaped characters are \n and \\ and \" when writing
 * (16) Newline \n is the only control character encoded and written out
 * (17) Unicode \ u#### and \ U######## are not supported, just use UTF-8 encoding for everything
 * (18) verbatim tags '!<' and '>' are not supported, these are treated as characters
 * (19) Actual newlines (multiple lines) are not allowed in quoted values, use \n instead
 * (20) Byte order mark is not supported and not needed in UTF-8
 *
 * CURRENTLY NOT SUPPORTED but expected to be supported shortly
 * (1) Quoted values should be unquoted, allowing for newlines and hash characters in string values
 * (2) Number and boolean conversions, currently everything is read as a string
 */
public class YAMLSupport {




    /**
     * Read a JSONObject from a YAML file.
     */
    public static JSONObject readYAMLFile(File yamlFile) throws Exception {
        //System.out.println("\n------------------\nREADING FILE: "+yamlFile.getAbsolutePath());
        UTF8FileReader reader = new UTF8FileReader(yamlFile);
        try {
            return readYAMLStream(reader);
        }
        catch (Exception e) {
            throw new JSONException("Failure to read YML file {0}", e, yamlFile.getAbsolutePath());
        }
        finally {
            reader.close();
        }
    }

    /**
     * Reads to the end of the stream, but does not close it
     */
    public static JSONObject readYAMLStream(Reader reader) throws Exception {
        ReadAhead readAhead = new ReadAhead(reader);
        determineIndent(readAhead);
        JSONObject jo = readObject(readAhead);
        return jo;
    }


    /**
     * Writes JSONObject to a file encoded as a YAML file
     *
     * NOTE: writing YAML is mainly for testing purposes.
     * YAML files are designed to be written by humans,
     * and may contain comments and such.
     * The comments are completely ignored on reading and so of course
     * will not be present in anything written out.   If you are
     * reading and writing YAML files, you are probably defeating the
     * purpose of YAML in the first place, and should consider JSON instead.
     */
    public static void writeYAMLFile(JSONObject jo, File outFile) throws Exception {
        try {
            File folder = outFile.getParentFile();
            File tempFile = new File(folder, "~"+outFile.getName()+"~tmp~"+System.currentTimeMillis());
            if (tempFile.exists()) {
                tempFile.delete();
                if (tempFile.exists()) {
                    throw new Exception("Before writing YAML tmp file, unable to delete the old tmp file: "+tempFile);
                }
            }
            UTF8FileWriter ufw = new UTF8FileWriter(tempFile);
            try {
                writeYAMLStream(jo, ufw);
            }
            catch (Exception e) {
                throw new JSONException("Failure writing to YAML file: {0}", e, tempFile.getAbsolutePath());
            }
            finally {
                ufw.close();
            }

            Path sourcePath      = Paths.get(tempFile.toString());
            Path destinationPath = Paths.get(outFile.toString());

            boolean failedOnce = false;
            if (Files.exists(destinationPath)) {
                try {
                    Files.delete(destinationPath);
                }
                catch (Exception e) {
                    System.out.println("RETRYING-FAILURE deleting file ("+tempFile+") because: "+e.toString());
                    failedOnce = true;
                }
            }
            if (failedOnce) {
                //This is really gross, but if the delete fails, try it again, if needed.
                if (Files.exists(destinationPath)) {
                    Files.delete(destinationPath);
                }
            }


            failedOnce = false;
            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (Exception e) {
                System.out.println("RETRYING-FAILURE renaming file ("+tempFile+") because: "+e.toString());
                failedOnce = true;
            }

            //This is really gross, but it seems that this call will fail about 1% of the time,  we
            //suspect it is due to virus-scan or something else.   It does not fail every time, and it
            //is timing dependent, so hard to prove either way.   This causes a slight slow down in
            //maybe 1% of the cases, but there is no real way around it.
            if (failedOnce) {
                Thread.sleep(10);
                //after waiting just a moment, if this fails, then it fails for good
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }

            //on some shared file systems, this step was failing but there was no indication at the time of the fail.
            //So the following two steps check for that, and produce an error when it is detected.
            if (!outFile.exists()) {
                throw new Exception("Unable to rename the YAML tmp file ("+tempFile+") to the actual file name("+outFile+")");
            }
            if (!Files.exists(destinationPath)) {
                throw new Exception("Unable to rename the YAML tmp file ("+tempFile+") to the actual file name("+outFile+")");
            }
            if (tempFile.exists()) {
                throw new Exception("Very strange, the YAML tmp file ("+tempFile+") remains in the folder after renaming to the actual file.");
            }
        }
        catch (Exception e) {
            throw new Exception("Unable to write JSON objects to the file: "+outFile, e);
        }
    }


    /**
     * writes object to stream, but does not close it
     */
    public static void writeYAMLStream(JSONObject jo, Writer writer) throws Exception {
        writeYAMLObject(writer, jo, "\n");
    }






    private static int determineIndent(ReadAhead r) throws Exception {
        while (r.ch=='#' || r.ch=='\n') {
            r.skipToNewLine();
        }
        while (r.ch==' ') {
            r.read();

            //if the first non white character on a line is a hash
            //then the entire line is a comment and skip to determine
            //the indent of the next line
            //if the line is completely empty, ignore it and skip to next line
            //skip all the empty lines
            while (r.ch=='#' || r.ch=='\n') {
                r.skipToNewLine();
            }
        }
        return r.colNo();
    }

    private static String scanForKey(ReadAhead r) throws Exception {
        StringBuilder sb = new StringBuilder();
        while (r.ch!=':' && r.ch!='\n' && r.ch>0) {
            sb.append((char)r.nextChar());
            r.read();
        }
        String key = sb.toString();
        //System.out.println("KEY: "+key);
        return key;
    }
    private static String scanForValue(ReadAhead r) throws Exception {
        StringBuilder sb = new StringBuilder();
        while (r.nextChar()!='\n' && r.ch>0) {
            sb.append((char)r.nextChar());
            r.read();
            if (r.ch=='#') {
            	break;
            }
        }
        String val = sb.toString().trim();
        return val;
    }


    private static void readMapValue(ReadAhead r, JSONObject jo, String key, int myIndent) throws Exception {
        //read the colon or hyphen and skip any white space there
        //read until the first non-white char
        while (r.read()==' ') {
            //just the reading is all that is needed
        }
        if (r.ch!='\n' && r.ch!='#') {
            String value = scanForValue(r);
            jo.put(key, value);
            r.skipToNewLine();
            determineIndent(r);
            return;
        }

        //this is the case where the value follows on the next line, either object or array
        r.skipToNewLine();
        int indent = determineIndent(r);
        if (indent<=myIndent) {
            //in this case there simply was no value, a null string is intended
            //and this is NOT the beginning of a object or array
            jo.put(key, "");
            return;
        }
        if (r.ch=='-') {
            jo.put(key, readArray(r));
        }
        else {
            String subKey = scanForKey(r).trim();
            if (r.ch==':') {
                //we found a colon, so this is a name/value pair
                jo.put(key, readObjectAfterFirst(r, subKey, indent));
            }
            else {
                //no colon, so this is just a value
                jo.put(key, subKey);
                r.skipToNewLine();
                determineIndent(r);
            }
        }
    }

    private static void readArrayValue(ReadAhead r, JSONArray ja) throws Exception {
        //read the colon or hyphen and skip any white space there
        //read until the first non-white char
        while (r.read()==' ') {
            //just the reading is all that is needed
        }
        int myIndent = r.colNo();
        String key = scanForKey(r).trim();
        if (r.ch==':') {
            ja.put(readObjectAfterFirst(r, key, myIndent));
        }
        else {
            ja.put(key);
            r.skipToNewLine();
            determineIndent(r);
        }
    }




    private static JSONArray readArray(ReadAhead r) throws Exception {
        int myIndent = r.colNo();
        //System.out.println("START ARRAY, myIndent="+myIndent);
        JSONArray ja = new JSONArray();
        while (r.colNo()==myIndent && r.ch>0) {
            if (r.ch!='-') {
                throw new JSONException("Expected a hyphen, line {0} column {1}", r.lineNo(), r.colNo() );
            }
            readArrayValue(r, ja);
        }
        if (r.ch>0 && r.colNo() > myIndent) {
            throw new JSONException("Invalid indenting line {0} column {1}, must not be greater than the last array line", r.lineNo(), r.colNo() );
        }
        //we are popping out back to an previous level, so just return the created array
        return ja;
    }

    private static JSONObject readObject(ReadAhead r) throws Exception {
        int myIndent = r.colNo();
        String key = scanForKey(r).trim();
        return readObjectAfterFirst(r, key, myIndent);
    }

    private static JSONObject readObjectAfterFirst(ReadAhead r, String key, int myIndent) throws Exception {
        //System.out.println("START OBJECT, myIndent="+myIndent);
        JSONObject jo = new JSONObject();
        readMapValue(r, jo, key, myIndent);
        while (r.colNo()==myIndent && r.ch>0) {
            key = scanForKey(r).trim();
            if (r.ch!=':') {
                throw new JSONException("Failed to find a colon on line {0} and column {1}", r.lineNo(), r.colNo());
            }
            readMapValue(r, jo, key, myIndent);
        }
        if (r.ch>0 && r.colNo() > myIndent) {
            throw new JSONException("Invalid indenting line {0} column {1}, must not be greater than the last object map line", r.lineNo(), r.colNo() );
        }
        //we are popping out back to an previous level, so just return
        return jo;
    }






    /**
     * this assumes that the first line is already indented, and the
     * string passed in is only for the following lines if there are any.
     */
    private static void writeYAMLObject(Writer w, JSONObject jo, String indent) throws Exception {
        if (indent.length() > 100) {
            // it is useful to abort attempts to iterate a looped JSON tree
            // rather than run forever and getting a stack overflow.
            throw new JSONException("Too many levels of indent.  This JSON tree is probably linked in a loop, "
                    +"which causes an infinite recursion.  Aborting output.");
        }
        try {
            List<String> keys = jo.sortedKeySet();
            final String newindent = indent + "  ";
            boolean isFirst = true;
            for (String key : keys) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    w.write(indent);
                }
                writeKey(w, key);
                w.write(": ");
                writeValue(w, jo.get(key), newindent, false);
            }
        } catch (IOException exception) {
            throw new JSONException(
                    "Unable to write object JSONObject indent level: " + indent,
                    exception);
        }
    }
    private static void writeKey(Writer w, String key) throws Exception {
        key = key.trim();
        if (key.contains(":")) {
            //replace all colon characters with hyphen.  Colon characters in the key
            //will confuse the parser (and users) and so eliminate teh colon in case
            //any is included in the data to be written.
            key = key.replace(':', '-');
        }
        w.write(key);
    }

    private static void writeYAMLArray(Writer w, JSONArray jo, String indent) throws Exception {
        if (indent.length() > 100) {
            // it is useful to abort attempts to iterate a looped JSON tree
            // rather than run forever and getting a stack overflow.
            throw new JSONException("Too many levels of indent.  This JSON tree is probably linked in a loop, "
                    +"which causes an infinite recursion.  Aborting output.");
        }
        try {
            final String newindent = indent + "  ";
            for (int i=0; i<jo.length(); i++) {
                w.write(indent);
                w.write("- ");
                writeValue(w, jo.get(i), newindent, true);
            }
        } catch (IOException exception) {
            throw new JSONException(
                    "Unable to write object JSONObject indent level: " + indent,
                    exception);
        }
    }


    /**
     * This method assumes that the field key and whatever is needed
     * before it has already been output, and that is needed is the value.
     * An indent must be provided, but used only if the value is an
     * object or an array.
     *
     * A simple value will be output in the current place.
     * An object will have a newline added with the indent, and then
     * the output object called.
     * An array will call the array since each element of an array
     * gets a new line and indent before it.
     */
    private static final void writeValue(Writer w, Object value, String indent, boolean skipFirstIndent) throws Exception {
        if (value == null || value.equals(null)) {
            w.write("null");
        }
        else if (value instanceof JSONObject) {
            if (!skipFirstIndent) {
                w.write(indent);
            }
            writeYAMLObject(w, (JSONObject)value, indent);
        }
        else if (value instanceof JSONArray) {
            writeYAMLArray(w, (JSONArray)value, indent);
        }
        else if (value instanceof Map) {
            throw new Exception("Why am i getting a map here instead of a JSONOBject?");
        }
        else if (value instanceof Collection<?>) {
            throw new Exception("Why am i getting a Collection here instead of a JSONArray?");
        }
        else if (value.getClass().isArray()) {
            throw new Exception("Why am i getting a isArray here instead of a JSONArray?");
        }
        else if (value instanceof Number) {
            w.write(JSONObject.numberToString((Number) value));
        }
        else if (value instanceof Boolean) {
            w.write(value.toString());
        }
        else {
            writeMaybeQuote(w, value.toString());
        }
    }


    private static void writeMaybeQuote(Writer w, String val) throws Exception {
        val = val.trim();
        if (needsQuote(val)) {
            w.write("\"");
            for (int i=0; i<val.length(); i++) {
                char ch = val.charAt(i);
                if (ch=='\"') {
                    w.write("\\\"");
                }
                else if (ch=='\n') {
                    w.write("\\n");
                }
                else if (ch<32) {
                    //don't ever write out this line feed, form feed
                    //or anything else less than a space character
                }
                else {
                    w.write(ch);
                }
            }
            w.write("\"");
        }
        else {
            w.write(val);
        }
    }

    private static boolean needsQuote(String val) {
        for (int i=0; i<val.length(); i++) {
            char ch = val.charAt(i);
            if (ch=='#') {
                return true;
            }
            if (ch=='\"') {
                return true;
            }
            if (ch=='\n') {
                return true;
            }
        }
        return false;
    }
}
