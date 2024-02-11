package com.purplehillsbooks.json;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.RuntimeException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimpleException is a class that constucts an exception 
 * with message that is composed from a list of object values
 * using the standard String.Format syntax.  No conversions 
 * are made until the constructor is calls, and at that time
 * the values are embedded into the complete message.
 * 
 * It allows for chaining exceptions together.
 * 
 * It has a useful stack trace report that truncates
 * the trace at the point where the trace request was
 * called, eliminating much of the useless trace lines.
 * 
 * This class does not provide support for translating
 * the message to other languages with a template and 
 * array of separate values.
 */
public class SimpleException extends RuntimeException  {
    private static final long serialVersionUID = 86753091;

    /**
     * Constructs a SimpleException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public SimpleException(String message, Object ... values) {
        super(String.format(message, values));
    }

    /**
     * Constructs a SimpleException with an explanatory message.
     * Wraps the passed throwable as the 'cause' exception.
     * @param message Detail about the reason for the exception.
     */
   public SimpleException(String message, Throwable cause, Object ... values) {
       super(String.format(message, values), cause);
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
            while (offUpper>0 && offLower>0
                    && trace.get(offUpper).equals(lower.get(offLower))) {
                trace.remove(offUpper);
                offUpper--;
                offLower--;
                sayContinued = true;
            }
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
                if (trailer!=null) {
                    trailer = new Exception(message, trailer);
                }
                else {
                    trailer = new Exception(message);
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

}
