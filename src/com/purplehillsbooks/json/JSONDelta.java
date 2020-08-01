package com.purplehillsbooks.json;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONObject;


/**
 * <p>The purpose of this class is to produce a "DELTA" comparison of two JSON
 * files.   
 *
 * <h3>JSON File 1:</h3>
 *
 * <pre>
 * {
 *     "title": "title",
 *     "pgNumTot": 84,
 *     "weight": 1.05,
 *     "dist": [
 *         {"id":"01", "country":"Italy"},
 *         {"id":"02", "country":"Brazil"},
 *         {"id":"08", "country":"Canada"},
 *         {"id":"09", "country":"Mexico"}
 *     ],
 *     "missing": "missing"
 * }
 * </pre>

 * <h3>JSON File 2:</h3>
 *
 * <pre>
 * {
 *     "title": "title",
 *     "pgNumTot": 84,
 *     "weight": 2.22,
 *     "wordCount": 16000,
 *     "dist": [
 *         {"id":"01", "country":"Italy"},
 *         {"id":"02", "country":"Mexico"},
 *         {"id":"05", "country":"France"},
 *         {"id":"08", "country":"Canada"}
 *     ],
 *     "z-extra": "superfluous"
 * }
 * </pre>
 *
 * <p>Basically, if an object member is the same between the two, then that member is omitted.
 * If a child object is exactly the same, it will be omitted.  Only the members that are changed
 * will be included in the delta.  If a new member is present then it is included.   If a member
 * value goes away, then it is represented as a null string ("").   If you have a list of simple
 * values, and the list changes, then the entire new list is included in the delta.</p>
 * 
 * <p>There is special handling for lists of objects.  The list must have a key field identified, and the 
 * objects in the list are compared by key.   If the object (and all children of that object) are the 
 * same, then it is omitted from the output.   If a new object appears with a new key value, 
 * then that new object is included so that it can be created along with all children.  To delete an object 
 * from a list, a special value is used that you can set, but it is @delete by default, and this causes 
 * the entire object to be deleted on the receiving side.
 * 
 * The output would then be as follows: the title and page num tot remained the same, so 
 * they will be omitted.  The weight changed, so the new weight will be included.  For the list, 
 * there must be a key field, and according to the key if the object with the same key exists,
 * and it is unchanged, then that object is omitted.  If there is a new object with a new key
 * the entire object will be included.  If there is an existing object</p>
 *
 * <pre>
 * {
 *     "weight": 2.22,
 *     "wordCount": 16000,
 *     "dist": [
 *         {"id":"09", "@op":"@delete"},
 *         {"id":"02", "country":"Mexico"},
 *         {"id":"05", "country":"France"},
 *     ],
 *     "missing": "",
 *     "z-extra": "superfluous"
 * }
 * </pre>
 *
 * <p></p>
 */
public class JSONDelta {

	private HashMap<String, String> listObjectKey = new HashMap<String, String>();
	private String deleteKey = "@delete";
	private String deleteValue = "";
	

 /**
 * 
 */
    public JSONDelta() {
    	
    }
    
    public void setDeletedValueIndicator(String newValue) {
    	deleteValue = newValue;
    }
    public void setListItemDeleteIndicator(String newValue) {
    	deleteKey = newValue;
    }
    public void setListKeyMap(HashMap<String, String> newValue) {
    	listObjectKey = newValue;
    }

/**
* <p>Creates a JSONObject that represents the delta of the two JSON objects
* passed in.  Each member that is the same is omitted, and only the members
* that are new or changed in the second parameter are returned.</p>
*
*/
    public JSONObject createDelta(JSONObject oldObj, JSONObject newObj) throws Exception {
        JSONObject result = new JSONObject();
        
        //first, lets find the items that are deleted, and missing in the new object
        for (String key : oldObj.keySet()) {
        	if (!newObj.has(key)) {
        		result.put(key, deleteValue);
        	}
        }
        
        for (String key : newObj.keySet()) {
    		Object newValue = newObj.get(key);
        	if (!oldObj.has(key)) {
        		result.put(key, newValue);
        	}
        	else {
        		Object oldValue = oldObj.get(key);
        		
        		if (newValue instanceof Integer) {
        			compareInteger(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof String) {
        			compareString(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof Double) {
        			compareDouble(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof Long) {
        			compareLong(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof Boolean) {
        			compareBoolean(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof JSONObject) {
        			compareJSONObject(result, key, oldValue, newValue);
        		}
        		else if (newValue instanceof JSONArray) {
        			compareJSONArray(result, key, oldValue, newValue);
        		}
        	}
        }
        
        return result;
    }

    
    private void compareInteger(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof Integer)) {
    		result.put(key, newValue);
    	}
    	else if ((Integer)oldValue != (Integer)newValue) {
    		//they are different, so include the value in result
    		result.put(key, newValue);
    	}
    }

    private void compareString(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof String)) {
    		result.put(key, newValue);
    	}
    	else if (!((String)oldValue).equals((String)newValue)) {
    		//they are different, so include the value in result
    		result.put(key, newValue);
    	}
    }

    private void compareDouble(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof Double)) {
    		result.put(key, newValue);
    	}
    	else if ((Double)oldValue != (Double)newValue) {
    		//they are different, so include the value in result
    		result.put(key, newValue);
    	}
    }
    
    private void compareLong(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof Long)) {
    		result.put(key, newValue);
    	}
    	else if ((Long)oldValue != (Long)newValue) {
    		//they are different, so include the value in result
    		result.put(key, newValue);
    	}
    }
    private void compareBoolean(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof Boolean)) {
    		result.put(key, newValue);
    	}
    	else if ((Boolean)oldValue != (Boolean)newValue) {
    		//they are different, so include the value in result
    		result.put(key, newValue);
    	}
    }

    private void compareJSONObject(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof JSONObject)) {
    		result.put(key, newValue);
    	}
    	else {
    		JSONObject oldObj = (JSONObject)oldValue;
    		JSONObject newObj = (JSONObject)newValue;
    		JSONObject delta = createDelta(oldObj, newObj);
    		if (delta.keySet().size()>0) {
    			//only store the delta if the delta object is not empty
    			result.put(key, delta);
    		}
    	}
    }
    
    
    private void compareJSONArray(JSONObject result, String key, Object oldValue, Object newValue) throws Exception {
    	if (!(oldValue instanceof JSONArray)) {
        	System.out.println("     LIST did not appear on the old object");
    		result.put(key, newValue);
    		return;
    	}
    	JSONArray oldList = (JSONArray)oldValue;
    	JSONArray newList = (JSONArray)newValue;
    	String keyMember = listObjectKey.get(key);
    	if (keyMember==null) {
    		keyMember = "id";
    	}
    	boolean allAreJSONObjects = true;
    	List<String> allIdValues = new ArrayList<String>();
    	for (int i=0; i<oldList.length(); i++) {
    		Object member = oldList.get(i);
    		if (!(member instanceof JSONObject)) {
    			allAreJSONObjects = false;
    		}
    		else if (!((JSONObject)member).has(keyMember)) {
    			allAreJSONObjects = false;
    		}
    		else {
    			String idValue = ((JSONObject)member).getString(keyMember);
    			if (!allIdValues.contains(idValue)) {
    				allIdValues.add(idValue);
    			}
    		}
    	}
    	for (int i=0; i<newList.length(); i++) {
    		Object member = newList.get(i);
    		if (!(member instanceof JSONObject)) {
    			allAreJSONObjects = false;
    		}
    		else if (!((JSONObject)member).has(keyMember)) {
    			allAreJSONObjects = false;
    		}
	 		else {
				String idValue = ((JSONObject)member).getString(keyMember);
				if (!allIdValues.contains(idValue)) {
					allIdValues.add(idValue);
				}
			}
     	}
    	if (!allAreJSONObjects) {
    		result.put(key, newList);
    		return;
    	}
    	
    	//at this point we have verified that both lists contain ONLY JSONObjects
    	JSONArray deltaList = new JSONArray();
    	for (String testId : allIdValues) {
        	System.out.println("TESTING id value: "+key);
    		JSONObject newListMember = findKeyedObject(newList, keyMember, testId);
    		JSONObject oldListMember = findKeyedObject(oldList, keyMember, testId);
    		if (oldListMember==null) {
    			//only the new one exists, so add it completely
            	System.out.println("     only the new one exists, so add it completely");
    			deltaList.put(newListMember);
    		}
    		else if (newListMember==null) {
            	System.out.println("     only the OLD one exists, so create a deleter");
    			JSONObject destroyerObject = new JSONObject();
    			destroyerObject.put(keyMember, testId);
    			destroyerObject.put(deleteKey, deleteKey);
    			deltaList.put(destroyerObject);
    		}
    		else {
            	System.out.println("     both exists, so create a delta");
    			JSONObject delta = createDelta(oldListMember, newListMember);
    			if (delta.keySet().size()>0) {
	    			delta.put(keyMember, testId);
	    			deltaList.put(delta);
    			}
    		}
    	}
    	if (deltaList.length()>0) {
    		//only store the list if it is non-empty
    		result.put(key, deltaList);
    	}
    	else {

        	System.out.println("     LIST was empty, so eliminated");
    	}
    }
    
    private JSONObject findKeyedObject(JSONArray source, String keyMember, String idValue) throws Exception {
    	for (int i=0; i<source.length(); i++) {
    		Object listItem = source.get(i);
    		if (!(listItem instanceof JSONObject)) {
    			continue;
    		}
    		JSONObject listObject = (JSONObject)listItem;
    		if (!listObject.has(keyMember)) {
    			continue;
    		}
    		String thisIdVal = listObject.getString(keyMember);
    		if (idValue.equals(thisIdVal)) {
    			return listObject;
    		}
    	}
    	//it was not found so return null
    	return null;
    }
        
 /**
 *
 * <p>The main routine can be called as a command-line command
 * where you pass the names of JSON files.  The files are read, and the result is
 * written out as a CSV file.</p>
 *
 * <pre>JSONDelta {First-File.json}  {Second-File.json} [-a]</pre>
 *
 * <p>First parameter and second parameter are the two files to read as JSON files and
 * to compare.  If either file is not a valid JSON syntax you will get an error.
 * There is a third, optional parameter (-a) which controls the reportAll setting.</p>
 *
 * <p>The output will be written to second file name with "delta.json" on the end.
 * In the example above, the file would be written to <tt>Second-File.JSONDelta.csv</tt></p>
 */
    public static void main(String[] args) {
        try {
            String fileName1 = null;
            String fileName2 = null;
            if (args.length>0) {
                fileName1 = args[0];
            }
            if (args.length>1) {
                fileName2 = args[1];
            }
            File file1 = new File(fileName1);
            if (!file1.exists()) {
                throw new Exception("Can't file first file: "+file1.getCanonicalPath());
            }
            JSONObject obj1 = JSONObject.readFromFile(file1);
            JSONObject obj2 = new JSONObject();
            File file2 = new File(fileName2);
            if (file2.exists()) {
                obj2 = JSONObject.readFromFile(file2);
            }
            JSONDelta jdiff = new JSONDelta();
            JSONObject delta = jdiff.createDelta(obj1, obj2);

            File fileOut = new File(fileName2+"delta.json");
            if (fileOut.exists()) {
                fileOut.delete();
            }
            delta.writeToFile(fileOut);
        }
        catch (Exception e) {
            System.out.println("##### FATAL ENDING OF JSONDelta #####");
            e.printStackTrace();
        }
    }



}
