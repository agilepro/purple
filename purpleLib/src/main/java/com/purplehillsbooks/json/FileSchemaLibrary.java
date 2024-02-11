package com.purplehillsbooks.json;

import java.io.File;
import java.util.HashMap;

/**
 * Given a file folder, when asked for a schema XYZ
 * this class will read a file 'Schema-XYZ.json' and
 * return the JSONObject to the caller.
 *
 * A cache of schema objects are kept in memory for reuse
 * as needed until you discard the SchemaLibrary.
 */
public class FileSchemaLibrary implements SchemaLibrary {

    File folder;

    HashMap<String, JSONObject> library;

    public FileSchemaLibrary(File schemaFolder) {
        folder = schemaFolder;
        library = new HashMap<String, JSONObject>();
    }

    @Override
    public JSONObject getSchema(String name) {
        try {
            if (!folder.exists()) {
                throw new SimpleException("FileSchemaLibrary did not find a file folder at (%s)",
                        folder.getAbsolutePath());
            }
            if (library.containsKey(name)) {
                return library.get(name);
            }
            File expectedFile = new File(folder, "Schema-"+name+".json");
            if (!expectedFile.exists()) {
                throw new SimpleException("Schema file does not exist: %s", expectedFile.getAbsolutePath());
            }
            JSONObject schema = JSONObject.readFromFile(expectedFile);
            library.put(name, schema);
            return schema;
        }
        catch (Exception e) {
            throw new SimpleException("Unable to return a schema named (%s)", e, name);
        }
    }

}
