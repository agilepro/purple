package com.purplehillsbooks.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.purplehillsbooks.json.JSONException;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.json.JSONTokener;
import com.purplehillsbooks.json.SimpleException;

public class WebClient {


    /**
     * Get a JSONObject back from the server.
     */
    public JSONObject getFromRemote(URL url) throws Exception {
        return sendRequestToRemote(url, null, "GET", null, "application/json");
    }
    /**
     * Delete a JSONObject from the server.
     */
    public JSONObject deleteFromRemote(URL url) throws Exception {
        return sendRequestToRemote(url, null, "DELETE", null, "application/json");
    }
    /**
     * Send a JSONObject to this server as a POST and
     * get a JSONObject back with the response.
     */
    public JSONObject postToRemote(URL url, JSONObject msg) throws Exception {
        return sendRequestToRemote(url, msg, "POST", null, "application/json");
    }
    /**
     * Send a String to this server as a POST and
     * get a JSONObject back with the response.
     */
    public JSONObject postToRemote(URL url, String msg) throws Exception {
        return sendRequestToRemote(url, msg, "POST", null, "application/x-ndjson");
    }
    /**
     * Send a JSONObject to this server as a PUT and
     * get a JSONObject back with the response.
     */
    public JSONObject putToRemote(URL url, JSONObject msg) throws Exception {
        return sendRequestToRemote(url, msg, "PUT", null, "application/json");
    }

    private String getStringFromErrorStream(HttpURLConnection connection) {
        String error = "";
        if(connection.getErrorStream() != null)  {
            try {
                // Buffer the result into a string
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getErrorStream(),"UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                error = sb.toString();
            } catch (Exception e) {
                error = e.toString();
            }
        }
        return error;
    }

    /**
     * Send a Object to this server as a POST and
     * get a JSONObject back with the response.
     */
    private JSONObject sendRequestToRemote(URL url, Object msg, String method, String auth, String contentType) throws Exception {
        HttpURLConnection httpCon = null;
        InputStream is = null;
        OutputStreamWriter osw = null;
        try {
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setDoInput(true);
            httpCon.setUseCaches(false);
            httpCon.setRequestProperty( "Content-Type", contentType );
            httpCon.setRequestProperty("Origin", "http://bogus.example.com/");
            if(auth != null && !(auth.trim().isEmpty())) {
                httpCon.setRequestProperty("AgileAuth", auth);
            }

            httpCon.setRequestMethod(method);
            httpCon.connect();
            if (!"GET".equals(method) && !"DELETE".equals(method)) {
                osw = new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8");
                if(msg instanceof JSONObject) {
                    ((JSONObject)msg).write(osw, 2, 0);
                } else {
                    osw.write(""+msg);
                }
                osw.flush();
                osw.close();
            }

            is = httpCon.getInputStream();

            JSONTokener jt = new JSONTokener(is);
            JSONObject resp = new JSONObject(jt);

            return resp;
        }
        catch (Exception e) {
            JSONObject jo = new JSONObject();
            if (msg != null) {
                jo.put("request", msg);
            } else {
                //don't use null, just omit the member
                //jo.put("request", JSONObject.NULL);
            }
            if(httpCon != null) {
                String responseString = getStringFromErrorStream(httpCon);
                try {
                    jo.put("response", new JSONObject(new JSONTokener(responseString)));
                } catch (Exception ee) {
                    jo.put("response", responseString);
                }
            }
            throw new SimpleException("WebClient failed to send %s request to server url %s. The request and response is %s", 
                    e, method, url.toString(), jo.toString());
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    SimpleException.traceException(ioe, "WebClient encountered an error when closing input stream for url " + url);
                }
            }
            if(osw != null) {
                try {
                    osw.close();
                } catch (IOException ioe2) {
                    SimpleException.traceException(ioe2, "WebClient encountered an error when closing output stream for url " + url);
                }
            }
        }
    }

    public JSONObject requestToRemote(URL url, JSONObject msg, String method, String auth) throws Exception {
        return sendRequestToRemote(url, msg, method, auth, "application/json");
    }


    /**
     * Performs a HEAD operation to test if something exists or not.
     * Return true if the response code is 200
     * False if 400 or anything else.
     */
    public boolean existTest(URL url) throws Exception {
        try {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setDoInput(true);
            httpCon.setUseCaches(false);
            httpCon.setRequestProperty("Content-Type", "text/plain" );
            httpCon.setRequestProperty("Origin", "http://bogus.example.com/");

            httpCon.setRequestMethod("HEAD");
            httpCon.connect();

            int responseCode = httpCon.getResponseCode();
            if(responseCode==200) {
                return true;
            } else if(responseCode==404) {
                return false;
            } else {
                throw new Exception("WebClient existTest for url " + url + " encountered a http error code " + responseCode);
            }
        }
        catch (Exception e) {
            SimpleException.traceException(e, "WebClient existTest for url " + url + " encountered an exception");
            throw new SimpleException("WebClient existTest for url (%s) encountered an exception", e, url);
        }
    }


}
