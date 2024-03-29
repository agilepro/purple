package com.purplehillsbooks.json;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSONException is mainly a class that has a few helpful methods for handling
 * exceptions, such as:
 * to get the full message of a chain of exceptions,
 * to test if a particular message is anywhere in a chain,
 * to convert an exception to JSON in a standard way
 * to trace an exception to the output stream in a standard way.
 */
public class JSONException extends Exception implements TemplatizedMessage {
    private static final long serialVersionUID = 0;
    public  String   template;
    public  String[] params;

    //Very long string parameters will be truncated to this length
    //Update this static value if you want longer or shorter parameters
    public static int MAXIMUM_PARAM_LENGTH = 999;

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
        params = new String[0];
    }

    /**
     * Constructs a JSONException with an explanatory message.
     * Wraps the passed throwable as the 'cause' exception.
     * @param message Detail about the reason for the exception.
     */
   public JSONException(String message, Throwable cause) {
        super(message, cause);
        params = new String[0];
    }

    /**
     * Construct the exception with a template by using variable parameters
     * Use a template like this:
     *
     * <pre>JSONException("Error when contemplating {0} in context of {1}", value0, value1)</pre>
     *
     * Tokens are braces with a single digit numeral between them.
     * The message will ultimately include the main string template with the values
     * substituted, however this gives the option to translate the template before
     * substitution and get a translated message.
     *
     * Parameters can be any object, but they are immediately conferted to a string
     * using toString so if you want different formatting convert to a string before
     * passing the value in.  Parameters are truncated to MAXIMUM_PARAM_LENGTH.
     * The static value MAXIMUM_PARAM_LENGTH can be modified by containing program.
     */
    public JSONException(String template, Object ... params) {
        super(formatString(template, stringifyParams(params)));
        this.template = template;
        this.params = stringifyParams(params);
    }
    /**
     * Construct the exception with a template by using variable parameters
     * with a pattern like this:
     *
     * <pre>JSONException("Error when contemplating {0} in context of {1}", ex, value0, value1)</pre>
     *
     * See JSONException constructor without Throwable for more details.
     */
    public JSONException(String template, Throwable cause, Object ... params) {
        super(formatString(template, stringifyParams(params)), cause);
        this.template = template;
        this.params = stringifyParams(params);
    }


    public static String[] stringifyParams(Object[] input) {
        String[] output = new String[input.length];
        for (int i=0; i<input.length; i++) {
            output[i] = input[i].toString();
            if (output[i].length()>MAXIMUM_PARAM_LENGTH) {
                output[i] = output[i].substring(0, MAXIMUM_PARAM_LENGTH);
            }
        }
        return output;
    }

    public static String formatString(String template, String[] params) {
        if (template==null) {
            return "Undefined Exception";
        }
        if (params==null || params.length==0) {
            return template;
        }
        StringBuilder sb = new StringBuilder();

        int start = 0;
        int pos = template.indexOf("{");
        while (pos>0)  {
            sb.append(template.substring(start, pos));
            int endPos = template.indexOf("}",pos);
            if (endPos<pos+2) {
                //no end of the token, or malformed token with no contents
                //just append the rest and return it
                sb.append(template.substring(pos));
                return sb.toString();
            }
            int param = safeConvertInt(template.substring(pos+1,endPos));
            if (param>=params.length) {
                sb.append("unknown_param_"+param);
            }
            else {
                String pstr = params[param];

                //make sure that the string is not too long
                if (MAXIMUM_PARAM_LENGTH>0 && pstr.length()>MAXIMUM_PARAM_LENGTH) {
                    pstr = pstr.substring(0,MAXIMUM_PARAM_LENGTH);
                }
                sb.append(pstr);
            }
            start = endPos+1;
            pos = template.indexOf("{", start);
        }
        sb.append(template.substring(start));
        return sb.toString();
    }

    public static int safeConvertInt(String val) {
        if (val == null) {
            return 0;
        }
        int res = 0;
        int last = val.length();
        for (int i = 0; i < last; i++) {
            char ch = val.charAt(i);
            if (ch >= '0' && ch <= '9') {
                res = res * 10 + ch - '0';
            }
        }
        return res;
    }

    /**
    * Walks through a chain of exception objects, from the first, to each
    * "cause" in turn, creating a single combined string message from all
    * the exception objects in the chain, with newline characters between
    * each exception message.
    */
    public static String getFullMessage(Throwable e)
    {
        StringBuffer retMsg = new StringBuffer();
        while (e != null) {
            retMsg.append(e.toString());
            retMsg.append("\n");
            e = e.getCause();
        }
        return retMsg.toString();
    }

    /**
    * When an exception is caught, you will want to test whether the exception, or any of
    * the causing exceptions contains a particular string fragment.  This routine searches
    * the entire cascading chain of exceptions and return true if the string fragment is
    * found in any of the exception, and false if the fragment is not found anywhere.
    */
    public static boolean containsMessage(Throwable t, String fragment) {
        while (t!=null) {
            String msg = t.getMessage();
            if (msg!=null && msg.contains(fragment)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }


    public String substituteParams() {
        String msg = getMessage();
        if (params.length==0) {
            return msg;
        }
        String result = String.format(msg, (Object[]) params);
        return result;
    }


    /**
     * In any kind of JSON protocol, you need to return an exception back to the caller.
     * How should the Java exception be encoded into JSON?
     * This method offers a convenient way to convert ANY exception
     * into a stardard form proposed by OASIS:
     *
     * http://docs.oasis-open.org/odata/odata-json-format/v4.0/errata02/os/odata-json-format-v4.0-errata02-os-complete.html#_Toc403940655
     *
     * Also see:
     *
     * https://agiletribe.wordpress.com/2015/09/16/json-rest-api-exception-handling/
     *
     * @param context allows you to include some context about the operation that was being performed
     *                when the exception occurred.
     */
    public static JSONObject convertToJSON(Throwable e, String context) throws Exception {
        JSONObject responseBody = new JSONObject();
        JSONObject errorTag = new JSONObject();
        responseBody.put("error", errorTag);

        errorTag.put("code", 400);
        errorTag.put("context", context);

        JSONArray detailList = new JSONArray();
        errorTag.put("details", detailList);

        Throwable nextRunner = e;
        List<ExceptionTracer> traceHolder = new ArrayList<ExceptionTracer>();
        while (nextRunner!=null) {
            //doing this at the top allows 'continues' statements to be safe
            Throwable runner = nextRunner;
            nextRunner = runner.getCause();

            String className =  runner.getClass().getName();
            String msg =  runner.toString();

            //trim the classname off the message since that is usually unimportant to communicating the problem
            if (msg.startsWith(className) && msg.length()>className.length()+5) {
                int skipTo = className.length();
                while (skipTo<msg.length()) {
                    char ch = msg.charAt(skipTo);
                    if (ch != ':' && ch != ' ') {
                        break;
                    }
                    skipTo++;
                }
                msg = msg.substring(skipTo);
            }

            ExceptionTracer et = new ExceptionTracer(runner);
            et.msg = msg;
            et.captureTrace();
            traceHolder.add(et);

            JSONObject detailObj = new JSONObject();
            if (runner instanceof TemplatizedMessage) {
                TemplatizedMessage jrun = (TemplatizedMessage)runner;
                String[] params = jrun.getParameters();
                //getParameters should never return a null, but this is an interface and might
                //have been implemented by someone else not follinwg this rule, so always
                //check for null just to be sure.
                if (params!=null && params.length>0) {
                    detailObj.put("template", jrun.getStdTemplate());
                    for (int i=0; i<params.length; i++) {
                        detailObj.put("param"+i,params[i]);
                    }
                }
            }
            detailObj.put("message",msg);
            int dotPos = className.lastIndexOf(".");
            if (dotPos>0) {
                className = className.substring(dotPos+1);
            }
            detailObj.put("code",className);
            detailList.put(detailObj);
        }

        JSONObject innerError = new JSONObject();
        errorTag.put("innerError", innerError);

        //now do them in the opposite order for the stack trace.
        JSONArray stackList = new JSONArray();
        for (int i=traceHolder.size()-1;i>=0;i--) {
            ExceptionTracer et = traceHolder.get(i);
            if (i>0) {
                ExceptionTracer lower = traceHolder.get(i-1);
                et.removeTail(lower.trace);
            }
            else {
            	//if at the root level, then remove the current trace tail because everything
            	//below the level of the final catch is not relevant.  This creates dramatically
            	//smaller stack traces when the underlying tech is profligate in their use
            	//of setup methods.
            	ExceptionTracer bottom = new ExceptionTracer( new Exception("eliminate the bottom most stack trace"));
            	bottom.captureTrace();
            	et.removeTail(bottom.trace);
            	et.sayContinued = false;
            }
            et.insertIntoArray(stackList);
        }
        errorTag.put("stack", stackList);

        return responseBody;
    }

    /**
     * This grabs a copy of the trace so that later it can be compared with
     * other stack traces, and truncated to eliminate the redundant parts.
     * Records the fact that it was truncated.
     */
    static class ExceptionTracer {
        public Throwable t;
        public String msg;
        public List<String> trace = new ArrayList<String>();
        public boolean sayContinued = false;

        public ExceptionTracer(Throwable _t) {
        	t = _t;
        }

        public void captureTrace() {
            for (StackTraceElement ste : t.getStackTrace()) {
                String line = "    "+ste.getFileName() + ": " + ste.getMethodName() + ": " + ste.getLineNumber();
                trace.add(line);
            }
        }
        
        //This does not work perfectly in the case that the exception does not
        //have the full stack trace in the first place.  Previously truncated
        //stack traces (such as reconstituted exceptions) will falsely get extra
        //truncated IF the source trace is highly repetitive.  That occurs in the
        //test cases, but rarely in real life.
        //but works in most normal cases.
        public void removeTail(List<String> lower) {
            int offUpper = trace.size()-1;
            int offLower = lower.size()-1;
            //System.out.println("\n~~~~COMPARING~~~1");
            //for (String line : trace) {
            //	System.out.println(line);
            //}
            //System.out.println("\n~~~~~~~~TO~~~2");
            //for (String line : lower) {
            //	System.out.println(line);
            //}
            //int count = 0;
            while (offUpper>0 && offLower>0
                    && trace.get(offUpper).equals(lower.get(offLower))) {
                trace.remove(offUpper);
                offUpper--;
                offLower--;
                sayContinued = true;
                //count++;
            }
            //System.out.println("\n~~~~~~~~~~~~~~~ REMOVED "+ count + "lines");
        }

        public void insertIntoArray(JSONArray ja) {
            ja.put(msg);
            for (String line : trace) {
                ja.put(line);
            }
            if (sayContinued) {
                ja.put("    (continued below)");
            }
        }
    }

    /**
     * A standardized way to trace a given exception to the system out.
     */
    public static JSONObject traceException(Throwable e, String context) {
        JSONObject errOB = traceException(System.out, e, context);
        return errOB;
    }

    /**
     * A standardized way to trace a given exception.
     *
     * Because this is a method designed to be used during exception
     * handling, it is written to avoid throwing any exceptions.
     * All errors in the running of this routine are reported in a
     * way to avoid causing more problems.
     */
    public static JSONObject traceException(PrintStream out, Throwable e, String context) {
        if (e==null) {
            e = new Exception("$$$$$$$$ traceException called without an exception");
        }
        if (out==null) {
            out = System.out;
            out.println("$$$$$$$$ traceException requires an out parameter");
        }
        if (context==null || context.length()==0) {
            out.println("$$$$$$$$ traceException requires a context parameter");
            context = "Missing value provided to define context of exception";
        }
        try {
            JSONObject errOb = convertToJSON(e, context);
            traceConvertedException(out, errOb);
            return errOb;
        }
        catch (Exception eee) {
            out.println("$$$$$$$$ FAILURE TRACING AN EXCEPTION TO JSON");
            eee.printStackTrace(out);
            out.println("$$$$$$$$ ORIGINAL EXCEPTION THAT FAILED TO CONVERT TO JSON");
            e.printStackTrace(out);
            return null;
        }
    }

    /**
     * A standardized way to trace a given exception to the system out.
     */
    public static JSONObject traceException(Writer w, Throwable e, String context) {
        if (e==null) {
            e = new Exception("$$$$$$$$ traceException called without an exception");
        }
        if (w==null) {
            System.out.println("$$$$$$$$ traceException requires an out parameter");
            return traceException(System.out, e, context);
        }
        if (context==null || context.length()==0) {
            System.out.println("$$$$$$$$ traceException requires a context parameter");
            context = "Missing value provided to define context of exception";
        }
        try {
            JSONObject errOb = convertToJSON(e, context);
            traceConvertedException(w, errOb);
            return errOb;
        }
        catch (Exception eee) {
            PrintWriter pw = new PrintWriter(w);
            pw.println("$$$$$$$$ FAILURE TRACING AN EXCEPTION TO JSON\n");
            eee.printStackTrace(pw);
            pw.println("$$$$$$$$ ORIGINAL EXCEPTION THAT FAILED TO CONVERT TO JSON");
            e.printStackTrace(pw);
            return null;
        }
    }


    /**
     * If you have already converted to a JSONOBject, you can use this method to
     * get a standard trace of that object to the output writer.
     */
    public static void traceConvertedException(PrintStream out, JSONObject errOb) {
        try {
            out.println(getTraceExceptionFormat(errOb));
        }
        catch (Exception eee) {
            System.out.println("$$$$$$$$ FAILURE TRACING A CONVERTED EXCEPTION");
            eee.printStackTrace();
        }
    }

    public static void traceConvertedException(Writer w, JSONObject errOb) {
        try {
            w.write(getTraceExceptionFormat(errOb));
        }
        catch (Exception eee) {
            System.out.println("$$$$$$$$ FAILURE TRACING A CONVERTED EXCEPTION");
            eee.printStackTrace();
        }
    }

    /**
     * If you have already converted to a JSONOBject, you can use this method to
     * get a standard trace of that object to the output writer.
     *
     * This returns a string because it was too difficult to sort out the
     * PrintWriter, Writer, PrintStream differences, and because all of the
     * exceptions within exception need to be handled with output streams.
     * Clearly returning a string is not efficient memory-wise, but exceptions
     * should be rare, so don't worry about it.
     */
    public static String getTraceExceptionFormat(JSONObject errOb) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n~~~~~~~~~~~~~EXCEPTION~~~~~~~~~~~~~~~~ ");
        String exceptionTime = dateFormatter.format(new Date());
        sb.append(exceptionTime);
        sb.append("\n");
        sb.append(errOb.toString(2));
        sb.append("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");
        return sb.toString();
    }
    static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Given a JSON representation of an exception, this re-constructs the Exception
     * chain (linked cause exceptions) and returns the Exception object.
     * The main purpose of this is when calling a web service, if it returns
     * this standard kind of JSON representation, then this converts it back
     * to exception objects so that can be thrown.  Thus the exception on the server
     * is reproduced to the client.
     *
     * This copies the stack to the final (rootmost) exception object and does not try
     * to allocate stack to each of the individual exception objects in the chain
     *
     **/
    public static Exception convertJSONToException(JSONObject ex) {
        Exception trailer = null;
        Map<String, Exception> msgMap = new HashMap<String, Exception>();
        try {
            if (ex==null) {
                return new Exception("Failure converting JSONToException: Null parameter to JSONToException");
            }
            if (!ex.has("error")) {
                return new Exception("Failure converting JSONToException: no 'error' member in object. "+ex.toString());
            }
            JSONObject error = ex.getJSONObject("error");
            if (!error.has("details")) {
                return new Exception("Failure converting JSONToException: no 'error.details' member in object. "+ex.toString());
            }
            JSONArray details = error.getJSONArray("details");
            for (int i=details.length()-1; i>=0; i--) {
                JSONObject oneDetail = details.getJSONObject(i);
                String message = oneDetail.getString("message");
                String template = oneDetail.optString("template", null);
                int paramCount = 0;
                while (oneDetail.has("param"+paramCount)) {
                    paramCount++;
                }
                String[] params = new String[paramCount];
                for (int ii=0; ii<paramCount; ii++) {
                    params[ii] = oneDetail.getString("param"+ii);
                }
                if (trailer!=null) {
                    if (template!=null && paramCount>0) {
                        trailer = new JSONException(template, trailer, (Object[]) params);
                    }
                    else {
                        trailer = new Exception(message, trailer);
                    }
                }
                else {
                    if (template!=null && paramCount>0) {
                        trailer = new JSONException(template, (Object[]) params);
                    }
                    else {
                        trailer = new Exception(message);
                    }
                }
                //store this associated with the message which appears in the trace below
                msgMap.put(message, trailer);
                //zero out the stack traces on these objects
                trailer.setStackTrace(new StackTraceElement[0]);
            }

            if (trailer==null) {
                return new Exception("Failure converting JSONToException: no details of the error. "+ex.toString());
            }
            if (error.has("stack")) {
                JSONArray stack = error.getJSONArray("stack");
                ArrayList<StackTraceElement> newStack = new  ArrayList<StackTraceElement>();
                Exception currentException = trailer;
                for (int i=0; i<stack.length(); i++) {
                    String line = stack.getString(i);
                    //now parse this into part according to the form
                    //   "    "+FileName + ": " + MethodName + ": " + LineNumber
                    String fileName = "";
                    String methodName = "";
                    int lineNumber = 0;
                    if (line.length()<4) {
                        //ignore very short lines
                        continue;
                    }
                    if (!"  ".equals(line.substring(0,2))) {
                        //lines without space indicate that this is an exception message
                        //so find that exception and make it current
                        Exception matchingException = msgMap.get(line);
                        if (matchingException!=null) {
                            putStackTraceOnException(currentException, newStack);

                            //we assume that each exception appears only once and have no stack trace
                            newStack = new  ArrayList<StackTraceElement>();
                            currentException = matchingException;
                        }
                        continue;
                    }
                    int pos = line.lastIndexOf(":");
                    if (pos<0) {
                        //ignore lines without any colons in them
                        //that includes lines that say '(continued below)'
                        continue;
                    }
                    lineNumber = safeConvertInt(line.substring(pos+1));
                    line = line.substring(0, pos);
                    pos = line.lastIndexOf(":");
                    if (pos>0) {
                        methodName = line.substring(pos+1).trim();
                        line = line.substring(0, pos);
                    }
                    fileName = line.trim();
                    newStack.add(new StackTraceElement("", methodName, fileName, lineNumber));
                }
                putStackTraceOnException(currentException, newStack);
            }
            return trailer;
        }
        catch (Exception xxx) {
            return new Exception("Failure converting JSONToException: "+ex.toString(), xxx);
        }
    }
    
    private static void putStackTraceOnException(Exception e, ArrayList<StackTraceElement> newStack) {
        if (newStack.size()==0) {
            //ignore empty lists
            return; 
        }
        StackTraceElement[] newStackArray = new StackTraceElement[newStack.size()];
        for (int i=0; i<newStack.size(); i++) {
            newStackArray[i] = newStack.get(i);
        }
        e.setStackTrace(newStackArray);
    }

    @Override
    public String getStdTemplate() {
        return template;
    }

    @Override
    public String[] getParameters() {
        return params;
    }

    @Override
    public String getFormattedMessage() {
        return formatString(template, params);
    }

}
