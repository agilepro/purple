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

public abstract class JSONHandler {

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
     *
     * routine will look something like this:
     *
     * <pre>
     * JSONObject handleOnePathElement() {
     *   if (wr.pathFinished()) {
     *      return constructObjectForThisElement();
     *   }
     *   String thisToken = wr.consumePathToken();
     *   try {
     *     if ("foo".equals(thisToken)) {
     *       return handleFoo();
     *     }
     *     else if ("bar".equals(thisToken)) {
     *       return handleBar();
     *     }
     *     else {
     *       throw new Exception("No idea what "+thisToken+" means at this point in the path");
     *     }
     *   }
     *   catch (Exception e) {
     *     throw new Exception("error while handling "+thisToken, e);
     *   }
     * }
     * </pre>
     *
     */
    public abstract JSONObject handleRequest() throws Exception;


}
