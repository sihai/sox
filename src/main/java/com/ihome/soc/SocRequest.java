/*
 * ihome inc.
 * soc
 */
package com.ihome.soc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import com.ihome.soc.session.SocSession;

/**
 * 
 * @author sihai
 *
 */
public class SocRequest extends HttpServletRequestWrapper {

	private SocSession socSession = null;
	
	/**
	 * 
	 * @param request
	 */
	public SocRequest(HttpServletRequest request) {
		super(request);
		socSession = new SocSession(request);
	}
	
	/**
     * 返回经过封装的SESSION
     */
    public HttpSession getSession() {
        return socSession;
    }
    
    /**
     * 返回经过封装的SESSION
     */
    public HttpSession getSession(boolean create) {
    	return socSession;
    }
    
	/**
     * 
     * @param context
     */
    public void setHttpContext(SocHttpContext httpContext){
    	if (null != socSession) {
			socSession.setHttpContext(httpContext);
		}
    }
}