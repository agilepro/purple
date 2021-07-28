package com.purplehillsbooks.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.purplehillsbooks.json.JSONException;
import com.purplehillsbooks.json.JSONObject;
import com.purplehillsbooks.streams.MemFile;
import com.purplehillsbooks.streams.StreamHelper;


/**
 * The purpose of this is to be the root-most singleton server object that
 * also manages Tenant-based user session limits.
 * It listens to the Servlet container for sessions being destroyed.
 *
 * This is a singleton object.
 *
 * It is also used as the object to get and maintain all the metadata about
 * the server and tenants.  It will read the configuration, and provide this
 * to the rest of the module.
 *
 * This object will maintain a single super session
 * with the server, and it will allow users' access to that super session
 * based on configuration settings.  This will be used to gather the list
 * of all the tenants in the server, and the list of users for each tenant.
 */
public class SessionManager implements HttpSessionListener {

	ServletContext sc;
	File appDataFolder;
	JSONObject config;

    private SessionManager(ServletContext _sc) throws Exception {
        try {
            sc = _sc;
        }
        catch(Exception e) {
            throw new Exception("Unable to initialize the server, something is probably wrong with "
                    +"the configuration or the operating environment.", e);
        }
    }

    public static SessionManager getSessionManagerSingleton(ServletContext sc) throws Exception {
        File webInfPath = new File(sc.getRealPath("/WEB-INF"));
        if (!webInfPath.exists()) {
            System.out.println("ServerCore.getServerCoreSingleton: The servlet context claims the WEB-INF location, but not there: "+webInfPath.getAbsolutePath());
        }
        SessionManager smgr = (SessionManager) sc.getAttribute("GlobalSessionManager");
        if (smgr == null) {
            smgr = new SessionManager(sc);
            sc.setAttribute("GlobalSessionManager", smgr);
            try {
                sc.addListener(smgr);
            }
            catch (Throwable e) {
                //The problem is that oldserver versions of the servlet standard did not have this
                //so you can't listen for the session timeouts.
                //That includes JBoss 5.1
                //However running on JBoss 5.1 for demo purpose is important
                System.out.println("FAILURE installing session listener in SessionLimitManager.  Session limits can NOT be enforced! "+e);
            }
        }
        else {
            //update the pointer to the servlet core object... and out of date one might have been persisted.
            smgr.sc = sc;
        }
        return smgr;
    }

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		//add to the count of logged in users
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		//decrement the count of logged in users
	}

	public File getAppDataFolder() throws Exception {
	    if (appDataFolder!=null) {
	        return appDataFolder;
	    }

	    File webInfFolder = new File(sc.getRealPath("WEB-INF"));
	    if (!webInfFolder.exists()) {
	        throw new JSONException("For some reason webInfFolder ({0}) does not exist!", webInfFolder.getAbsoluteFile());
	    }
	    File appDataPathFile = new File(webInfFolder, "appDataPath.txt");
	    if (appDataPathFile.exists()) {
    	    MemFile mf = new MemFile();
    	    mf.fillWithFile(appDataPathFile);
    	    File possibleDataPath = new File(mf.toString());
    	    possibleDataPath.mkdirs();
            if (possibleDataPath.exists()) {
                appDataFolder = possibleDataPath;
                initDataFolder(appDataFolder, webInfFolder);
                return appDataFolder;
            }
	    }

	    //if the above didn't work, then create a default inside the app
	    if (appDataFolder==null) {
	        File stdDataFolder = new File(webInfFolder.getParentFile(), "Data");
	        stdDataFolder.mkdirs();
            if (stdDataFolder.exists()) {
                appDataFolder = stdDataFolder;
                initDataFolder(appDataFolder, webInfFolder);
                return appDataFolder;
            }
	    }
	    return appDataFolder;
	}

	/*
	 * look inthe defaut data folder, and copy a file if it does not exist
	 * in the data folder
	 */
	private void initDataFolder(File dataFolder, File webInfFolder) throws Exception {
        File initDataFolder = new File(webInfFolder, "DefaultData");
        if (!initDataFolder.exists()) {
            //nothing to do if the init data folder does not exist.
            return;
        }
        for (File child : initDataFolder.listFiles()) {
            File destFile =  new File(dataFolder, child.getName());
            if (!destFile.exists()) {
                StreamHelper.copyFileToFile(child,destFile);
            }
        }
	}


	public JSONObject getConfigSettings() throws Exception {
	    if (config!=null) {
	        return config;
	    }

	    File dataFolder = getAppDataFolder();
	    File appConfigFile = new File(dataFolder, "config.json");
	    if (!appConfigFile.exists()) {
	        throw new JSONException("Config File ({0}) does not exist!", appConfigFile.getAbsoluteFile());
	    }
	    config = JSONObject.readFromFile(appConfigFile);
	    return config;
	}

	public String getConfigSetting(String name) throws Exception {
	    JSONObject config = getConfigSettings();
	    return config.getString(name);
	}

}
