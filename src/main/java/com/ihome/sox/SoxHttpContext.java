/*
 * ihome inc.
 * soc
 */
package com.ihome.sox;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 上下文
 * @author sihai
 *
 */
public class SoxHttpContext {

	private HttpServletRequest  request;			//	HTTP 请求
    private HttpServletResponse response;			//  HTTP 响应
    private ServletContext      servletContext; 	//	Servlet上下文

    public SoxHttpContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    	this.request  = request;
        this.response = response;
        this.servletContext  = servletContext;
    }

    public HttpServletRequest getRequest() {
        return  request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
}