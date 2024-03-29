package com.purplehillsbooks.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2012-04-20
 */
public class JSONArray {


    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private final ArrayList<Object> myArrayList;


    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        this.myArrayList = new ArrayList<Object>();
    }

    /**
     * Construct a JSONArray object from a file.
     * Remember, the file has to start with a square brace.
     */
    public static JSONArray readFromFile(File inFile) throws Exception {
        FileInputStream fis = new FileInputStream(inFile);
        JSONTokener jt = new JSONTokener(fis);
        return new JSONArray(jt);
    }
    public void writeToFile(File outFile) throws Exception {
        File folder = outFile.getParentFile();
        File tempFile = new File(folder, "~"+outFile.getName()+"~tmp~");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        FileOutputStream fos = new FileOutputStream(tempFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        this.write(osw,2,0);
        osw.close();
        if (outFile.exists()) {
            outFile.delete();
        }
        tempFile.renameTo(outFile);
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     * @param x A JSONTokener
     * @throws SimpleException If there is a syntax error.
     */
    public JSONArray(JSONTokener x) {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() != ']') {
            x.back();
            for (;;) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(JSONObject.NULL);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == ']') {
                        return;
                    }
                    x.back();
                    break;
                case ']':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }


    /**
     * Construct a JSONArray from a source JSON text.
     * @param source     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws SimpleException If there is a syntax error.
     */
    public JSONArray(String source) {
        this(new JSONTokener(source));
    }


    /**
     * Construct a JSONArray from a Collection.
     * @param collection     A Collection.
     */
    public JSONArray(Collection<Object> collection) {
        this.myArrayList = new ArrayList<Object>();
        if (collection != null) {
            Iterator<Object> iter = collection.iterator();
            while (iter.hasNext()) {
                this.myArrayList.add(JSONObject.wrap(iter.next()));
            }
        }
    }


    /**
     * Construct a JSONArray from an array
     * @throws SimpleException If not an array.
     */
    public JSONArray(Object array) {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.put(JSONObject.wrap(Array.get(array, i)));
            }
        } else {
            throw new SimpleException("JSONArray initial value should be a string or collection or array.");
        }
    }


    /**
     * Get the object value associated with an index.
     * @param index
     *  The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws SimpleException If there is no value for the index.
     */
    public Object get(int index) {
        if (index < 0) {
            throw new SimpleException("JSONArray[%d] not found.  Negative index not allowed.", index);
        }
        if (index >= myArrayList.size()) {
            throw new SimpleException("JSONArray[%d] not found.  Array has only %d elements.", index, myArrayList.size());
        }
        Object object = this.opt(index);
        if (object == null) {
            throw new SimpleException("JSONArray[%d] not found.", index);
        }
        return object;
    }


    /**
     * Get the boolean value associated with an index.
     * The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     * @throws SimpleException If there is no value for the index or if the
     *  value is not convertible to boolean.
     */
    public boolean getBoolean(int index) {
        Object object = this.get(index);
        if (object.equals(Boolean.FALSE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new SimpleException("JSONArray[%d] is not a boolean.", index);
    }


    /**
     * Get the double value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     * @throws   SimpleException If the key is not found or if the value cannot
     *  be converted to a number.
     */
    public double getDouble(int index) {
        Object object = this.get(index);
        try {
            return object instanceof Number
                ? ((Number)object).doubleValue()
                : Double.parseDouble((String)object);
        } catch (Exception e) {
            throw new SimpleException("JSONArray[%d] is not a number.", index);
        }
    }


    /**
     * Get the int value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     * @throws   SimpleException If the key is not found or if the value is not a number.
     */
    public int getInt(int index) {
        Object object = this.get(index);
        try {
            return object instanceof Number
                ? ((Number)object).intValue()
                : Integer.parseInt((String)object);
        } catch (Exception e) {
            throw new SimpleException("JSONArray[%d] is not an integer.", index);
        }
    }


    /**
     * Get the JSONArray associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONArray value.
     * @throws SimpleException If there is no value for the index. or if the
     * value is not a JSONArray
     */
    public JSONArray getJSONArray(int index) {
        Object object = this.get(index);
        if (object instanceof JSONArray) {
            return (JSONArray)object;
        }
        throw new SimpleException("JSONArray[%d] is not a JSONArray.", index);
    }


    /**
     * Get the JSONObject associated with an index.
     * @param index subscript
     * @return      A JSONObject value.
     * @throws SimpleException If there is no value for the index or if the
     * value is not a JSONObject
     */
    public JSONObject getJSONObject(int index) {
        Object object = this.get(index);
        if (object instanceof JSONObject) {
            return (JSONObject)object;
        }
        throw new SimpleException("JSONArray[%d] is not a JSONObject.", index);
    }

    /**
     * Returns the entire JSONArray as a list of JSONObjects.
     * This makes it much easier to iterate a JSONArray, that is if all
     * the elements are expected to be JSONObjects.
     *
     * Elements of the array that are not JSONObjects are ignored, so
     * if there are no JSONObjects in the array, you will get an empty set back
     */
    public List<JSONObject> getJSONObjectSet() throws Exception {
    	List<JSONObject> ret = new ArrayList<JSONObject>();
    	for (int i=0; i<this.length(); i++) {
    		Object object = this.get(i);
            if (object instanceof JSONObject) {
                ret.add((JSONObject)object);
            }
    	}
    	return ret;
    }


    /**
     * Get the long value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     * @throws   SimpleException If the key is not found or if the value cannot
     *  be converted to a number.
     */
    public long getLong(int index) {
        Object object = this.get(index);
        try {
            return object instanceof Number
                ? ((Number)object).longValue()
                : Long.parseLong((String)object);
        } catch (Exception e) {
            throw new SimpleException("JSONArray[%d] is not a number.", index);
        }
    }


    /**
     * Get the string associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A string value.
     * @throws SimpleException If there is no string value for the index.
     */
    public String getString(int index) {
        Object object = this.get(index);
        if (object instanceof String) {
            return (String)object;
        }
        throw new SimpleException("JSONArray[%d] not a string.", index);
    }


    /**
     * Determine if the value is null.
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index) {
        return JSONObject.NULL.equals(this.opt(index));
    }


    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws SimpleException If the array contains an invalid number.
     */
    public String join(String separator) {
        int len = this.length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        return sb.toString();
    }


    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return this.myArrayList.size();
    }


    /**
     * Get the optional object value associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      An object value, or null if there is no
     *              object at that index.
     */
    public Object opt(int index) {
        if (index < 0) {
            throw new SimpleException("JSONArray[%d] not found.  Negative index not allowed.", index);
        }
        if (index >= this.length()) {
            // optional value silently returns null for too large index
            return null;
        }
        return this.myArrayList.get(index);
    }


    /**
     * Get the optional boolean value associated with an index.
     * It returns false if there is no value at that index,
     * or if the value is not Boolean.TRUE or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The truth.
     */
    public boolean optBoolean(int index)  {
        return this.optBoolean(index, false);
    }


    /**
     * Get the optional boolean value associated with an index.
     * It returns the defaultValue if there is no value at that index or if
     * it is not a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     A boolean default.
     * @return      The truth.
     */
    public boolean optBoolean(int index, boolean defaultValue)  {
        try {
            return this.getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional double value associated with an index.
     * NaN is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public double optDouble(int index) {
        return this.optDouble(index, Double.NaN);
    }


    /**
     * Get the optional double value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index subscript
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public double optDouble(int index, double defaultValue) {
        try {
            return this.getDouble(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional int value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public int optInt(int index) {
        return this.optInt(index, 0);
    }


    /**
     * Get the optional int value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public int optInt(int index, int defaultValue) {
        try {
            return this.getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional JSONArray associated with an index.
     * @param index subscript
     * @return      A JSONArray value, or null if the index has no value,
     * or if the value is not a JSONArray.
     */
    public JSONArray optJSONArray(int index) {
        Object o = this.opt(index);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }


    /**
     * Get the optional JSONObject associated with an index.
     * Null is returned if the key is not found, or null if the index has
     * no value, or if the value is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A JSONObject value.
     */
    public JSONObject optJSONObject(int index) {
        Object o = this.opt(index);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }


    /**
     * Get the optional long value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      The value.
     */
    public long optLong(int index) {
        return this.optLong(index, 0);
    }


    /**
     * Get the optional long value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      The value.
     */
    public long optLong(int index, long defaultValue) {
        try {
            return this.getLong(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return      A String value.
     */
    public String optString(int index) {
        return this.optString(index, "");
    }


    /**
     * Get the optional string associated with an index.
     * The defaultValue is returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      A String value.
     */
    public String optString(int index, String defaultValue) {
        Object object = this.opt(index);
        if (JSONObject.NULL.equals(object)) {
            return defaultValue;
        }
        return object.toString();
    }


    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public JSONArray put(boolean value) {
        this.put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param value A Collection value.
     * @return      this.
     */
    public JSONArray put(Collection<Object> value) {
        this.put(new JSONArray(value));
        return this;
    }


    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value A double value.
     * @throws SimpleException if the value is not finite.
     * @return this.
     */
    public JSONArray put(double value) {
        Double d = Double.valueOf(value);
        JSONObject.testValidity(d);
        this.put(d);
        return this;
    }


    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public JSONArray put(int value) {
        this.put(Integer.valueOf(value));
        return this;
    }


    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public JSONArray put(long value) {
        this.put(Long.valueOf(value));
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject which is produced from a Map.
     * @param value A Map value.
     * @return      this.
     */
    public JSONArray put(Map<String, Object> value) {
        this.put(new JSONObject(value));
        return this;
    }


    /**
     * Append an object value. This increases the array's length by one.
     * @param value An object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     */
    public JSONArray put(Object value) {
        this.myArrayList.add(value);
        return this;
    }


    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws SimpleException If the index is negative.
     */
    public JSONArray put(int index, boolean value) {
        this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param index The subscript.
     * @param value A Collection value.
     * @return      this.
     * @throws SimpleException If the index is negative or if the value is
     * not finite.
     */
    public JSONArray put(int index, Collection<Object> value) {
        this.put(index, new JSONArray(value));
        return this;
    }


    /**
     * Put or replace a double value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A double value.
     * @return this.
     * @throws SimpleException If the index is negative or if the value is
     * not finite.
     */
    public JSONArray put(int index, double value)  {
        this.put(index, Double.valueOf(value));
        return this;
    }


    /**
     * Put or replace an int value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws SimpleException If the index is negative.
     */
    public JSONArray put(int index, int value) {
        this.put(index, Integer.valueOf(value));
        return this;
    }


    /**
     * Put or replace a long value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws SimpleException If the index is negative.
     */
    public JSONArray put(int index, long value) {
        this.put(index, Long.valueOf(value));
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject that is produced from a Map.
     * @param index The subscript.
     * @param value The Map value.
     * @return      this.
     * @throws Exception If the index is negative or if the the value can not be converted to a JSONObject.
     */
    public JSONArray put(int index, Map<String, Object> value) {
        this.put(index, new JSONObject(value));
        return this;
    }


    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     *  than the length of the JSONArray, then null elements will be added as
     *  necessary to pad it out.
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     * @throws Exception If the index is negative or if the the value is an invalid number.
     */
    public JSONArray put(int index, Object value) {
        if (index < 0) {
            throw new SimpleException("JSONArray[%d] is unsettable.  Negative index is not allowed.", index);
        }
        JSONObject.testValidity(value);
        if (index < this.length()) {
            this.myArrayList.set(index, value);
        } else {
            while (index != this.length()) {
                this.put(JSONObject.NULL);
            }
            this.put(value);
        }
        return this;
    }


    /**
     * Remove an index and close the hole.
     * @param index The index of the element to be removed.
     * @return The value that was associated with the index,
     * or null if there was no value.
     */
    public Object remove(int index) {
        Object o = this.opt(index);
        this.myArrayList.remove(index);
        return o;
    }

    /**
     * Add all the elements from one JSONArray into the array this is called on.
     */
    public void addAll(JSONArray other) {
        int last = other.length();
        for (int i=0; i<last; i++) {
            put(other.get(i));
        }
    }


    /**
     * <p>
     * A convenience routine for use with native Java iterations.
     * If the JSONArray is populated exclusively by JSONObjects, then use this
     * method to get a List back, which can be iterated using:
     * </p><pre>
     *  for (JSONObject j : myList.getJSONObjectList()) {
     *     ...
     *  }
     * </pre><p>
     *  Returns all the elements as JSONObject in the same order
     *  they are in the array.
     *  Does not convert data from other data types.
     *  Will throw an exception if any element is NOT a JSONObject
     *  </p>
     */
    public List<JSONObject> getJSONObjectList() {
        ArrayList<JSONObject> res = new ArrayList<JSONObject>();
        for (int i=0; i<this.length(); i++) {
            try {
                res.add(this.getJSONObject(i));
            }
            catch (Exception e) {
                throw new SimpleException("Failure processing element number %d of the JSONArray", e, i);
            }
        }
        return res;
    }

    /**
     * <p>
     * A convenience routine for use with native Java iterations.
     * If the JSONArray is populated exclusively by Strings, then use this
     * method to get a List back, which can be iterated using:
     * </p>
     * <pre>
     *  for (String s : myList.getStringList()) {
     *     ...
     *  }
     * </pre>
     * <p>
     *  Returns all the elements as Strings in the same order
     *  they are in the array.
     *  Does not convert data from other data types.
     *  Will throw an exception if any element is NOT a String.
     *  </p>
     */
    public List<String> getStringList() {
        ArrayList<String> res = new ArrayList<String>();
        for (int i=0; i<this.length(); i++) {
            try {
                res.add(this.getString(i));
            }
            catch (Exception e) {
                throw new SimpleException("Failure string processing element number %d of the JSONArray", e, i);
            }
        }
        return res;
    }

    /**
     * If the JSONArray is filled only with string values,
     * then this method will test for the presence of a particular
     * value in that list.   If the value is found, it returns
     * true, otherwise false.
     * Will throw an exception if any element is NOT a String.
     */
    public boolean containsString(String testValue) {
        for (String value : getStringList()) {
            if (testValue.equals(value)) {
                return true;
            }
        }
        return false;
    }


    /**
     * <p>
     * Produce a JSONObject with values from this array,
     * named by values in the array passed in.
     * Say you have a JSONArray with 10 items in it.
     * Call this method with another JSONArray with 10 names in it.
     * Each named member of the generated object will be given the value that is found at the
     * same position in this array.
     * </p>
     *
     * @param names A JSONArray containing a list of key name. These will be
     * paired with the values according to position.
     * @return A JSONObject, or null if there are no names or if this JSONArray
     * has no values.
     * @throws SimpleException If any of the names are null.
     */
    public JSONObject toJSONObject(JSONArray names) {
        if (names == null || names.length() == 0 || this.length() == 0) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }


    /**
     * <p>
     * Make a JSON text of this JSONArray. For compactness, no
     * unnecessary whitespace is added.
     * </p><p>
     * This method swallows exceptions.  If it is not possible to produce a
     * syntactically correct JSON text, any exception that occurs during the
     * processing, then null will be returned instead.
     * </p><p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable
     *  representation of the array.
     */
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * <p>
     * Make a formatted JSON text of this JSONArray.
     * </p><p>
     * Think carefully before using this method!  Do you really need
     * a String in memory?  Since JSON is used as a data transport format
     * normally you are going to write the String out to some destination.
     * </p><p>
     * It is far more efficient to use the <code>write</code> operation
     * on this array directly.  Think about it: you have an array of JSON
     * objects in memory.  This method will make a copy of all that data
     * into a single string -- a second copy of the data in memory.
     * If all you are going to do is to write that string out to a file,
     * then use the write method to stream it directly to the file.
     * If you are going to send the data from a server to client browser,
     * the write directly to the output stream.  This reduces memory usage.
     * Sometimes you really do need a String, so the method is provided,
     * but use it sparingly.
     * </p><p>
     * The JSON is produced indented by an indentFactor amount specified.
     * If you specify zero indent, the output will be all on a single line.
     * The elements are alphabetized only if a positive indent is specified.
     * </p><p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *  with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws SimpleException
     */
    public String toString(int indentFactor) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            write(sw, indentFactor, 0);
            return sw.toString();
        }
    }

    /**
     * <p>
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added, and members are not put in
     * alphabetical order.
     * </p><p>
     * Warning: This method assumes that the data structure is acyclical.
     * </p>
     * @return The writer.
     * @throws SimpleException
     */
    public Writer write(Writer writer) {
        write(writer, 0, 0);
        return writer;
    }

    /**
     * <p>
     * Write the contents of the JSONArray as JSON text to a writer with
     * sub elements indented by the amount specified.  Array elements are
     * written in the order that they appear in the array (that is, array
     * order is preserved).   Members of objects within the array will have
     * the members sorted alphabetically so that a given JSON tree in memory
     * will always produce the same output.
     * </p><p>
     * Warning: This method assumes that the data structure is acyclical.
     * </p>
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @return The writer.
     * @throws SimpleException
     */
    public Writer write(Writer writer, int indentFactor, int indent) {
        try {
            boolean commanate = false;
            int length = this.length();
            writer.write('[');

            if (length == 1) {
                JSONObject.writeValue(writer, this.myArrayList.get(0),
                        indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    JSONObject.indent(writer, newindent);
                    JSONObject.writeValue(writer, this.myArrayList.get(i),
                            indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                JSONObject.indent(writer, indent);
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
           throw new SimpleException("Difficulty writing the JSON object at indent %d", e, indent);
        }
    }


    /**
     * Sorts the JSONArray according to the Comparator provided.
     * You can provide a comparator and sort the array.
     * You must provide a Comparator&lt;Object&gt; because that is what the
     * underlying array is, and your compare functions must handle
     * either the raw Objects, or cast appropriately to the objects you
     * expect to find in the array.
     */
    public void sortMembers(Comparator<Object> comp) {
        Collections.sort(myArrayList, comp);
    }

    /**
     * use this comparator to sort a JSONArray if all the elements are Strings
     * and you want them in case-sensitive incrementing order.
     * Anything in the JSONArray that is not a String will be essentially ignored
     * and placed after all the strings.
     *
     * @param isDescending 
     *            false if you want ascending order, true if you want descending
     * @param caseSensitive 
     *            false if you upper and lower case together, true if you want ASCII order
     */
    public static Comparator<Object> stringComparator(boolean isDescending, boolean caseSensitive) {
        return new StringComparator(isDescending, caseSensitive);
    }

    private static class StringComparator implements Comparator<Object> {
        boolean descending;
        boolean caseSense;

        StringComparator(boolean isDescending, boolean caseSensitive) {
            descending = isDescending;
            caseSense = caseSensitive;
        }


        @Override
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof String)) {
                if (o1 instanceof Integer) {
                    o1 = Integer.toString((Integer)o1);
                }
                else {
                    return 1;
                }
            }
            if (!(o2 instanceof String)) {
                if (o2 instanceof Integer) {
                    o1 = Integer.toString((Integer)o2);
                }
                else {
                    return -1;
                }
            }
            if (caseSense) {
                if (descending) {
                    return ((String)o2).compareTo((String)o1);
                }
                else {
                    return ((String)o1).compareTo((String)o2);
                }
            }
            else {
                if (descending) {
                    return ((String)o2).compareToIgnoreCase((String)o1);
                }
                else {
                    return ((String)o1).compareToIgnoreCase((String)o2);
                }
            }
        }
    }
}
