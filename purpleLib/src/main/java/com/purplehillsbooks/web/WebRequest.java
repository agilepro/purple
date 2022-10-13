package com.purplehillsbooks.web;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.purplehillsbooks.json.JSONArray;
import com.purplehillsbooks.json.JSONException;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONTokener;
import com.purplehillsbooks.streams.StreamHelper;


/**
 * WebRequest provides a lot of convenience functions for handling web requests
 * especially when they are JSON objects being sent back and forth.
 * The posted JSON object is provided in JSONObject form, and WebRequest
 * can easily send JSONObjects as well.
 *
 * Most important thing to learn is the path parser.   Given a path like this
 *
 * http://server/app/p1/p2/p3/obj.json
 *
 * This will be parsed into separate tokens, starting with p1
 * (assuming this is the first path element beyond where the servlet is mounted).
 * Each token can be consumed one at a time with #consumePathToken()
 * This is intended for a recursive descent style parser which has a
 * method for p1, which consumes p2 and calls the method for p2 and so on.
 * This parser properly handles the accidental double slash character
 * and it decodes the path elements.
 */
public class WebRequest {
    public HttpServletRequest  request;
    public HttpServletResponse response;
    public HttpSession         session;
    public Writer              w;
    public OutputStream        outStream;
    public String              requestURL;
    private ArrayList<String>  path;
    private int pathPos = 0;
    private JSONObject postedObject = null;

    public WebRequest (HttpServletRequest _req, HttpServletResponse _resp) throws Exception {
        request = _req;
        response = _resp;
        session = request.getSession();
        setUpForCrossBrowser();
        parsePath();
        outStream = _resp.getOutputStream();
        w = new OutputStreamWriter(outStream);
        request.setAttribute("wrappedRequest", this);
    }


    /**
     * This constructor is used within a JSP file where the Writer has already been grabbed from the
     * Response object which only allows the writer to be gotten once.  Later attempts fail
     * and so this constructor does not attempt to do this.
     *
     * By getting the writer, we do not have access to teh outputstream.
     *
     * There are a couple methods that require the output stream and will not work when constructed
     * from this constructor.
     */
    public WebRequest (HttpServletRequest _req, HttpServletResponse _resp, Writer aw) throws Exception {
        request = _req;
        response = _resp;
        session = request.getSession();
        setUpForCrossBrowser();
        parsePath();
        if (aw==null) {
            throw new Exception("WebRequest constructor must be passed a non-null Writer object");
        }
        w = aw;
        request.setAttribute("wrappedRequest", this);
    }

    /**
     * This factory method is used within a JSP file where the Writer has already been grabbed from the
     * Response object which only allows the writer to be gotten once.  Later attempts fail
     * and so this factory method does not attempt to do this.
     *
     * By getting the writer, we do not have access to teh outputstream.
     *
     * There are a couple methods that require the output stream and will not work when constructed
     * from this constructor.
     */
    public static WebRequest findOrCreate(HttpServletRequest _req, HttpServletResponse _resp, Writer aw) throws Exception {
        WebRequest wr = (WebRequest) _req.getAttribute("wrappedRequest");
        if (wr == null) {
            wr = new WebRequest(_req, _resp, aw);
        }
        return wr;
    }

    private void setUpForCrossBrowser() {
        //this is an API to be read by others, so you have to set the CORS to
        //allow scripts to read this data from a browser.
        String origin = request.getHeader("Origin");
        if (origin==null || origin.length()==0) {
            //this does not always work, but what else can we do?
            origin="*";
        }
        response.setHeader("Access-Control-Allow-Origin",      origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods",     "GET, POST, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers",     "Origin, X-Requested-With, Content-Type, Accept, Authorization, If-Modified-Since");
        response.setHeader("Access-Control-Max-Age",           "1");
        response.setHeader("Vary",                             "*");

        //default content type is JSON  set it otherwise if you need something different
        response.setContentType("application/json; charset=utf-8");
    }

    /**
     * This is the base URL for the application, which means it has
     * the protocol, server, port, and application name in the path.
     * Everything up to the root of where the application is.
     */
    public String appBaseUrl() {
        int amtToTrim = request.getServletPath().length() + request.getPathInfo().length();
        String appBase = requestURL.substring(0, requestURL.length()-amtToTrim);
        return appBase;
    }

    private void parsePath() throws Exception {
        String ctxtroot = request.getContextPath();
        requestURL = request.getRequestURL().toString();
        int indx = requestURL.indexOf(ctxtroot);
        int start = indx + ctxtroot.length() + 1;

        ArrayList<String> decoded = new ArrayList<String>();
        int pos = requestURL.indexOf("/", start);
        while (pos>=start) {
            addIfNotNull(decoded, requestURL, start, pos);
            start = pos + 1;
            pos = requestURL.indexOf("/", start);
        }
        addIfNotNull(decoded, requestURL, start, requestURL.length());
        path = decoded;
    }

    public String consumePathToken() {
        return path.get(pathPos++);
    }
    public boolean pathFinished() {
        return pathPos >= path.size();
    }

    private void addIfNotNull(ArrayList<String> dest, String source, int start, int pos) throws Exception {
        if (pos<=start) {
            return;
        }
        String token = source.substring(start, pos).trim();
        if (token.length()>0) {
            dest.add(URLDecoder.decode(token, "UTF-8"));
        }
    }

    public boolean isGet() {
        return "get".equalsIgnoreCase(request.getMethod());
    }
    public boolean isPost() {
        return "post".equalsIgnoreCase(request.getMethod());
    }
    public boolean isPut() {
        return "put".equalsIgnoreCase(request.getMethod());
    }
    public boolean isDelete() {
        return "delete".equalsIgnoreCase(request.getMethod());
    }
    public boolean isOptions() {
        return "options".equalsIgnoreCase(request.getMethod());
    }

    public JSONObject getPostedObject() throws Exception {
    	if (isGet()) {
    		return new JSONObject();
    	}
        if (postedObject!=null) {
            //important to only read the object once!
            return postedObject;
        }
        try {
            InputStream is = request.getInputStream();
            JSONTokener jt = new JSONTokener(is);
            postedObject = new JSONObject(jt);
            is.close();
            return postedObject;
        }
        catch (Exception e) {
            throw new Exception("Failure to read an expected JSON object from the POST stream for this web request: "+requestURL, e);
        }
    }

    /**
     * Reads the uploaded PUT body, and stores it to the specified
     * file (using a temp name, and deleting whatever file might
     * have been there before.)
     */
    public void storeContentsToFile(File destination) throws Exception {
        InputStream is = request.getInputStream();
        StreamHelper.copyStreamToFile(is, destination);
    }

    public void streamJSON(JSONObject jo) throws Exception {
        jo.write(w,2,0);
        w.flush();
    }

    public void streamException(Throwable e, SessionManager score) {
        try {
            //all exceptions are delayed by 3 seconds if the duration of the
            //session is less than 3 seconds.
            //AgileSession bsess = AgileSession.getBPMSession(request, score);
            //Thread.sleep(bsess.properErrorDelay());
            streamException(e, request, response, w);
        }
        catch (Exception xxx) {
            JSONException.traceException(xxx, "FATAL EXCEPTION WHILE STREAMING EXCEPTION");
        }

    }
    public static void streamException(Throwable e, HttpServletRequest request,
            HttpServletResponse response, Writer w) {
        try {
            if (w==null) {
                JSONException.traceException(e, "a null writer object was passed into streamException");
                throw new Exception("a null writer object was passed into streamException");
            }
            if (e==null) {
                throw new Exception("a null exception object was passed into streamException");
            }
            JSONObject responseBody = JSONException.convertToJSON(e, "Web request for: "+request.getRequestURI());

            //remove the bottom of the stack trace below the HttpServlet.service call
            //because it is all arbitrary garbage below that point and usually quite a lot of noise.
            if (responseBody.has("error")) {
                JSONObject error = responseBody.getJSONObject("error");
                if (error.has("stack")) {
                    JSONArray stack = error.getJSONArray("stack");
                    JSONArray truncStack = new JSONArray();
                    boolean notFound = true;
                    for (int i=0; i<stack.length() && notFound; i++) {
                        String line = stack.getString(i);
                        truncStack.put(line);
                        if (line.contains("HttpServlet") && line.contains("service")) {
                            //this is the last one that will be added
                            notFound = false;
                        }
                    }
                    error.put("stack", truncStack);
                }
            }

            responseBody.put("requestURL", request.getRequestURI());
            responseBody.put("exceptionTime", System.currentTimeMillis());

            response.setContentType("application/json");
            response.setStatus(400);

            JSONException.traceConvertedException(System.out, responseBody);
            responseBody.write(w, 2, 0);
            w.flush();
        } catch (Exception eeeee) {
            // nothing we can do here...
            JSONException.traceException(eeeee, "EXCEPTION_WITHIN_EXCEPTION");
        }
    }


    public void streamFile(File fullPath) throws Exception {
        if (!fullPath.exists()) {
            throw new Exception("Program Logic Error: WebRequest.streamFile was asked to stream a file that does not exist: "+fullPath);
        }
        String fileName = fullPath.getName();

        if(fileName.endsWith(".pdf")) {
            //This would allow PDF document to be opened inside browser or PDF viewer
            response.setContentType("application/pdf");
        } else {
            //Actually serve up the file contents here, and this mime type
            //tells the receiver to put the contents into a file without displaying
            response.setContentType("application/octet-stream");
        }

        //It seems that there is no way to get the length of the file
        //from the API.  It really should include in the response header.
        StreamHelper.copyFileToOutput(fullPath, outStream);
    }

    public void streamAttachment(String attachmentName, InputStream content) throws Exception {
        if(attachmentName.endsWith(".pdf")) {
            //This would allow PDF document to be opened inside browser or PDF viewer
            response.setContentType("application/pdf");
        } else {
            //Actually serve up the file contents here, and this mime type
            //tells the receiver to put the contents into a file without displaying
            response.setContentType("application/octet-stream");
        }
        StreamHelper.copyInputToOutput(content, outStream);
    }


    /**
     * During the course of this session, if setAuthUserId has been called, then this
     * will return the value that was given at that time.  You can implement your own login methods
     * and this simply records the result in the current session.
     * Returns null if not authenticated.
     */
    public String getAuthUserId() {
        return (String) session.getAttribute("userId");
    }
    /**
     * You can implement your own login methods
     * and this simply records the result in the current session.
     * During the course of this session, if this has been called, then getAuthUserId
     * will return the value that was given at that time.
     * Log a person OUT by setting this to null.
     */
    public void setAuthUserId(String newId) {
        session.setAttribute("userId", newId);
    }


    public String getSessionProperty(String propName) {
        return (String) session.getAttribute(propName);
    }
    public void setSessionProperty(String propName, String newVal) {
        session.setAttribute(propName, newVal);
    }

    public String getConfigSetting(String name) throws Exception {
        SessionManager smgr = (SessionManager) request.getSession().getServletContext().getAttribute("GlobalSessionManager");
        return smgr.getConfigSetting(name);
    }

}
