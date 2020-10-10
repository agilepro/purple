package com.purplehillsbooks.json;

import java.util.ArrayList;
import java.util.List;


/**
 * The JSONSChema class has a static method for creating a schema object, and
 * can be instantiated to perform schema validations.
 */
public class JSONSchema {
    
    SchemaLibrary schemaLib;

    /**
     * during validation, errors will be collected up to this limit,
     * and the validation will return when the limit is reached.
     * If you just want to find the first error, set the limit to 1.
     */
    public int errorLimit = 100;

    /**
     * if this is set false, then success (correct) validation is silent
     * if set to true, then every comparison is announced
     */
    public boolean recordSuccess = false;

    private List<String> errors = new ArrayList<String>();

    public JSONSchema() {
        //
    }
    
    /**
     * If the schema has a #ref token, you will need to supply a 
     * SchemaLibrary that will retrieve the reference for the schema
     * that is named.
     * 
     * Any attempt to validate a schema which inludes a #ref will fail if there
     * is no schema library to supply the references schema.
     */
    public void setSchemaLibrary(SchemaLibrary sLib) {
        schemaLib = sLib;
    }

    /**
     * Generates a schema object from the given instance.
     * This is a default schema which will probably have to be edited to
     * take into consideration variances.
     * OF course, it will only include data members that exist.
     * It assumes that all members are required.
     * It uses the type of value found in the first instance of a
     * member to set the type of that members.
     * If there is an array, it walks through the array and finds the
     * union of all the members on the objects.
     * If you want to allow additional members you need to create your
     * own additionalProperties entry.
     * If you want to allow members to have varying types, you will need
     * to remove the type indicator.
     */
    public static JSONObject generateSchema(JSONObject data) throws Exception {
        JSONObject res = new JSONObject();
        res.put("type", "object");
        JSONObject properties = new JSONObject();
        res.put("properties",  properties);
        JSONArray requiredList = new JSONArray();
        for (String key : data.keySet()) {
            requiredList.put(key);
            Object o = data.get(key);
            if (o instanceof JSONArray) {
                properties.put(key,  genSchemaArrayStd((JSONArray)o));
            }
            else if (o instanceof JSONObject) {
                properties.put(key,  generateSchema((JSONObject)o));
            }
            else  {
                properties.put(key,  genSchemaSimpleStd(o));
            }
        }
        //every property is required by default.   Remove them for optional properties.
        if (requiredList.length()>0) {
            //OpenAPI Swagger schema documentation specifically says an empty array is not allowed for required.
            //so only add it if there are properties found
            res.put("required", requiredList);
        }
        return res;
    }




    /**
     * This walks through the array looking at each member, getting the schema for
     * each member, and merging the results together.   If the objects are the same,
     * then this should have the effect of collecting all of the optional elements that
     * exist in the data.  If objects are different, then this might produce a mess,
     * but that would invalidate the idea of schema in the first place.
     *
     * If there are differences in the array items, then the LAST item will take
     * the most precedence.
     */
    private static JSONObject genSchemaArrayStd(JSONArray array) throws Exception {
        JSONObject res = new JSONObject();
        res.put("type", "array");
        if (array.length()==0) {
            return res;
        }
        JSONObject merged = new JSONObject();
        for (int i=0; i<array.length(); i++) {
            Object o = array.get(i);
            JSONObject oneScheme = null;
            if (o instanceof JSONArray) {
                oneScheme = genSchemaArrayStd((JSONArray)o);
            }
            else if (o instanceof JSONObject) {
                oneScheme = generateSchema((JSONObject)o);
            }
            else  {
                oneScheme = genSchemaSimpleStd(o);
            }
            deepMerge(merged, oneScheme);
        }
        res.put("items", merged);
        return res;
    }
    public static JSONObject genSchemaSimpleStd(Object o) throws Exception {
        JSONObject res = new JSONObject();
        if (o instanceof String) {
            res.put("type",  "string");
        }
        else if (o instanceof Float) {
            res.put("type",  "number");
        }
        else if (o instanceof Double) {
            res.put("type",  "number");
        }
        else if (o instanceof Integer)  {
            res.put("type",  "integer");
        }
        else if (o instanceof Boolean)  {
            res.put("type",  "boolean");
        }
        else if (o instanceof Long)  {
            //both long and integer are categorized as integer
            res.put("type",  "integer");
        }
        else {
            res.put("type",  "unknown");
        }
        return res;
    }
    private static void deepMerge(JSONObject merged, JSONObject oneScheme) throws Exception {
        for (String key : oneScheme.keySet()) {
            //if the dest object does not have the key yet, just add exactly what you have
            if (!merged.has(key)) {
                merged.put(key, oneScheme.get(key));
                continue;
            }
            Object mo = merged.get(key);
            Object os = oneScheme.get(key);
            if (mo instanceof JSONObject && os instanceof JSONObject) {
                deepMerge((JSONObject)mo, (JSONObject)os);
                continue;
            }
            if (mo instanceof JSONArray && os instanceof JSONArray) {
                arrayMerge((JSONArray)mo, (JSONArray)os);
                continue;
            }
            //don't merge any other scalar data types...
        }
    }
    private static void arrayMerge(JSONArray merged, JSONArray oneScheme) throws Exception {
        for (int i=0; i<oneScheme.length(); i++) {
            Object os = oneScheme.get(i);
            boolean found = false;

            //eliminate duplicates, but only if string.
            //I think JSON schema only has arrays of strings.
            if (os instanceof String) {
                for (int j=0; j<merged.length(); j++) {
                    Object mo = merged.get(j);
                    if (mo instanceof String) {
                        if (((String)mo).equals((String)os)) {
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                merged.put(os);
            }
        }
    }


    private void addLog(String line) {
        if (recordSuccess) {
            errors.add(line);
        }
    }
    private void addErrorLog(String line) {
        errors.add(line);
    }

    private boolean checkSchemaRunner(String path, JSONObject data, JSONObject schema) throws Exception {
        if (errors.size()>this.errorLimit) {
            return false;
        }
        if (schema.has("$ref")) {
            String schemaName = schema.getString("$ref");
            addLog("   ~Retrieving schema named '"+schemaName);
            schema = schemaLib.getSchema(schemaName);
        }
        if (!schema.has("type")) {
            //we don't know the type, so don't check any further.
            addLog("Found Object at '"+path+"' but skipping check becaue schema does not specify type");
            return true;
        }
        String mainType = schema.getString("type");
        if (!"object".equals(mainType)) {
            addErrorLog("@"+path+" - found an object but schema is expecting a "+mainType);
            return false;
        }
        //it is a json object, so there have to be properties
        if (!schema.has("properties") && !schema.has("additionalProperties")) {
            addErrorLog("@"+path+" - schema does not have any properties for this object");
            return false;
        }
        addLog(" "+path+" - Found Object");
        JSONObject properties = schema.getJSONObject("properties");
        boolean resultCode = true;
        for (String key : data.keySet()) {
            if (errors.size()>this.errorLimit) {
                return false;
            }
            Object d = data.get(key);
            JSONObject p = null;
            if (properties.has(key)) {
                p = properties.getJSONObject(key);
            }
            else if (schema.has("additionalProperties")) {
                //so, if you have a MAP (associative array) then all those can have
                //any name, but they must be the same type, and they must be
                //declared with this additionalProperties item in the schema
                p = schema.getJSONObject("additionalProperties");
            }
            else {
                addErrorLog("@"+path+key+" - No entry in schema corresponding to this.");
                resultCode = false;
                continue;
            }
            if (p.has("$ref")) {
                String schemaName = p.getString("$ref");
                addLog("   ~Retrieving property '"+key+"' schema named '"+schemaName);
                p = schemaLib.getSchema(schemaName);
            }
            if (!p.has("type")) {
                //the schema does not specify the type, so don't check this property at all
                //just go on to the next property
                addLog("Found property at '"+path+key+"' but skipping check becaue schema does not specify type");                
                continue;
            }
            String dataType = p.getString("type");
            if (d instanceof JSONArray) {
                if (!checkSchemaArray(path+key+".", ((JSONArray)d), p)) {
                    resultCode = false;
                }
            }
            else if (d instanceof JSONObject) {
                if (!checkSchemaRunner(path+key+".", ((JSONObject)d), p)) {
                    resultCode = false;
                }
            }
            else if (d instanceof String) {
                if (!"string".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a string but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found string");
                }
            }
            else if (d instanceof Integer) {
                if (!"integer".equals(dataType) && !"long".equals(dataType) && !"number".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a integer but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found number");
                }
            }
            else if (d instanceof Long) {
                if (!"integer".equals(dataType) && !"long".equals(dataType) && !"number".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a long but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found number");
                }
            }
            else if (d instanceof Float) {
                if (!"number".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a number but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found number");
                }
            }
            else if (d instanceof Double) {
                if (!"number".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a number but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found number");
                }
            }
            else if (d instanceof Boolean) {
                if (!"boolean".equals(dataType)) {
                    addErrorLog("@"+path+key+". - found a boolean but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+key+" - Found boolean");
                }
            }
            else {
                //something unexpected happened, marking as a problem now
                addErrorLog("@"+path+key+". unhandled property of type: "+(d.getClass().getName()));
                resultCode = false;
            }
        }
        if (schema.has("required")) {
            JSONArray required = schema.getJSONArray("required");
            for (int i=0; i<required.length(); i++) {
                String requiredPropertyName = required.getString(i);
                if (!data.has(requiredPropertyName)) {
                    addErrorLog("@"+path+requiredPropertyName+" required property not found");
                    resultCode = false;
                }
            }
        }
        return resultCode;
    }


    private boolean checkSchemaArray(String path, JSONArray data, JSONObject schema) throws Exception {
        if (errors.size()>this.errorLimit) {
            return false;
        }
        boolean resultCode = true;
        addLog(" "+path+" - Found Array");
        if (schema.has("$ref")) {
            String schemaName = schema.getString("$ref");
            addLog("   ~Retrieving schema named '"+schemaName);
            schema = schemaLib.getSchema(schemaName);
        }
        if (!"array".equals(schema.getString("type"))) {
            addErrorLog("@"+path+" - found an array, but schema expects it to be: "+schema.getString("type"));
            return false;
        }
        if (data.length()==0) {
            //nothing else to do if there are no elements
            return true;
        }
        if (!schema.has("items")) {
            addErrorLog("Schema array declaration missing an 'items' member at path: "+path);
            return false;
        }
        JSONObject s = schema.getJSONObject("items");
        if (s.has("$ref")) {
            String schemaName = s.getString("$ref");
            addLog("   ~Retrieving item schema named '"+schemaName);
            s = schemaLib.getSchema(schemaName);
        }
        if (!s.has("type")) {
            //the schema does not specify the type, so don't check this array items
            return true;
        }
        String dataType = s.getString("type");
        for (int i=0; i<data.length(); i++) {
            if (errors.size()>this.errorLimit) {
                return false;
            }
            Object d = data.get(i);
            if (d instanceof JSONArray) {
                if (!checkSchemaArray(path+"["+i+"].", ((JSONArray)d), s)) {
                    resultCode = false;
                }
            }
            else if (d instanceof JSONObject) {
                if (!checkSchemaRunner(path+"["+i+"].", ((JSONObject)d), s)) {
                    resultCode = false;
                }
            }
            else if (d instanceof String) {
                if (!"string".equals(dataType)) {
                    addErrorLog("@"+path+"["+i+"]. - found a string but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+"["+i+"]. - Found string");
                }
            }
            else if (d instanceof Integer) {
                if (!"integer".equals(dataType) && !"number".equals(dataType)) {
                    addErrorLog("@"+path+"["+i+"]. - found a integer but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+"["+i+"]. - Found number");
                }
            }
            else if (d instanceof Float) {
                if (!"number".equals(dataType)) {
                    addErrorLog("@"+path+"["+i+"]. - found a number but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+"["+i+"]. - Found number");
                }
            }
            else if (d instanceof Boolean) {
                if (!"boolean".equals(dataType)) {
                    addErrorLog("@"+path+"["+i+"]. - found a boolean but expecting a "+dataType);
                    resultCode = false;
                }
                else {
                    addLog(" "+path+"["+i+"]. - Found boolean");
                }
            }
            else {
                //something unexpected happened, marking as a problem now
                addErrorLog("@"+path+"["+i+"]. - Unidentified property of type: "+(d.getClass().getName()));
                resultCode = false;
            }
        }
        return resultCode;
    }

    public boolean checkSchema(JSONObject data, JSONObject schema) throws Exception {
        return checkSchemaRunner("", data, schema);
    }

    /**
     * if the schema validation returns false, then you can ask for a 
     * detailed list of what went wrong with this method.
     * 
     * Each string in the list is a separate validation error.
     * 
     * You will only get up to the number of errors specified in
     * the errorLimit variable.
     */
    public List<String> getErrorList() {
        return errors;
    }
        

}
