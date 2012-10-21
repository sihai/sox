/*
 * ihome inc.
 * soc
 */
package com.ihome.soc;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author sihai
 *
 */
public class SocResponse extends HttpServletResponseWrapper {

	private boolean flushed;
	
	/**
     * 默认构造函数
     *
     * @param response
     */
    public SocResponse(HttpServletResponse response) {
        super(response);
        this.flushed = false;
    }
}
