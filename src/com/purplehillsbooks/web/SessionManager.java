package com.purplehillsbooks.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


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

}
