/*
 * Copyright 2015 Fujitsu North America
 */

package com.purplehillsbooks.web;

import com.purplehillsbooks.json.JSONObject;

/**
 * This handler is instantiated for every request, so that it can hold onto
 * those values as data members, and not have to pass through all the methods.
 * This object instance represents the entire request, and the methods handle it.
 *
 * http://{machine:port}/{application}/xxx/    ==   {baseurl}
 *
 * The details of course depends upon the host and how to install
 * the application server.  All of that is represented by the
 * symbol {baseurl} for all the discussion below.
 *
 *
 *
 */

public class JSONHandler {

    protected WebRequest wr;
    protected SessionManager smgr;


    /**
     * Your class that extends JSONHandler will be constructed for every web request.
     * It is given the WebRequest and the global SessionManager objects
     */
    public JSONHandler(WebRequest _wr, SessionManager _smgr) throws Exception {
        wr = _wr;
        smgr = _smgr;
    }

    /**
     * This is the critical method to override for your specific server and it must
     * return on of three things
     *
     * 1. if processing correctly, return a JSONObject which will be streamed back to requester
     * 2. if processing failed, throw an exception, and the exception will be sent to requester
     * 3. if you need to send something other than JSON, you can stream that to the requester
     *    and then return a null to indicate that your code has already sent the response.
     *
     * The first time you call consumePathToken() you will get the first element in the path
     * Check that at this level, and call a method to handle that if there are more path
     * tokens expected.
     *
     * Is there are no more path tokens to consume then pathFinished() will return true
     * so you can know you are at the end without consuming the token.
     */
    public JSONObject handleRequest() throws Exception {
        if (wr.pathFinished()) {
            //must have at least one token so path finished here is an error
            throw new Exception("Program Logic Error: unexpected internal path is missing the 'api' entry from path");
        }
        String firstToken = wr.consumePathToken();
        if (!"api".equals(firstToken)) {
            //this should not be possible ... there should always be 'api'
            //this is just a consistency check
            throw new Exception("Program Logic Error: the first path element is expected to be 'api' but was instead '"+firstToken+"'");
        }

        throw new Exception("BPM API is unable to understand the first path element: "+firstToken);
    }


}
