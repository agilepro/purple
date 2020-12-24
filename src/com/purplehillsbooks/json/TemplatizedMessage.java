package com.purplehillsbooks.json;


/**
 * This interface allows for common handling of objects that represent a
 * message that has a template and parameters to be substituted.
 * JSONException uses this to recognize templatized messages,
 * and to convert them properlty to JSON.
 */
public interface TemplatizedMessage {

    /**
     * Get a message template with the form of
     *
     *     "Problem {0} found in {1}"
     *
     * Parameters are zero based
     * Parameters are in curly braces
     */
    public String getStdTemplate();

    public String[] getParameters();

    public String getFormattedMessage();

}
