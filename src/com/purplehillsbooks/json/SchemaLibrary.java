package com.purplehillsbooks.json;


/**
 * A SchemaLibrary maintains a collection of schemas which can be recalled by name.
 * This is so that one fragment of a schema can be reused in many other schemas.
 * 
 * An implementation of this is provided in FileSchemaLibrary that takes a path to a folder
 * full of files, and the files are all expected to be JSON schema files with a naming convention.
 * If you are storing schemas in someplace other than a file system, or if your naming convention 
 * is different, then provide a different library implementation and supply it to the JSONSchema 
 * object to use for looking up schemas.
 */
public interface SchemaLibrary {
    
    /**
     * Given the name of a schema, this returns the JSONObject for that schema
     * The name would be used in a #ref attribute of one schema, in order to 
     * refer to fragment of a schema returned by the library.
     * 
     * Throws an exception if no schema can be found for that name
     */
    JSONObject getSchema(String name) throws Exception;

}
