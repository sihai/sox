/*
 * ihome inc.
 * soc
 */
package com.ihome.sox;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import com.ihome.sox.session.SoxSession;

/**
 * 
 * @author sihai
 *
 */
public class SoxRequest extends HttpServletRequestWrapper {

	private SoxSession socSession = null;
	
	/**
	 * 
	 * @param request
	 */
	public SoxRequest(HttpServletRequest request) {
		super(request);
		socSession = new SoxSession(request);
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
    public void setHttpContext(SoxHttpContext httpContext){
    	if (null != socSession) {
			socSession.setHttpContext(httpContext);
		}
    }
}