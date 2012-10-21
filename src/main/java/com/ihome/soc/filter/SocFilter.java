/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.filter;

import java.io.IOException;
import java.sql.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ihome.soc.SocHttpContext;
import com.ihome.soc.SocRequest;
import com.ihome.soc.crypter.BlowfishEncrypter;
import com.ihome.soc.session.SocSession;
import com.ihome.soc.session.SocSingletonSessionManagerFactory;
import com.ihome.soc.util.SocConstants;

/**
 * SOC入口Filter
 * @author sihai
 *
 */
public class SocFilter extends AbstractFilter {

	@Override
	protected void init() throws ServletException {
		// init session store factory
		String configFile = getInitParameter(SocConstants.CONFIG_FILE, SocConstants.CONFIG_FILE);
		if(StringUtils.isNotBlank(configFile)) {
			SocSingletonSessionManagerFactory.init(configFile);
		} else {
			SocSingletonSessionManagerFactory.init(configFile);
		}
		// init encrypter
		BlowfishEncrypter.setKey(getInitParameter(SocConstants.PARAMETER_KEY, SocConstants.DEFAULT_KEY));
	}

	@Override
	public void doFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		SocRequest req = new SocRequest(request);
        SocHttpContext httpContext = new SocHttpContext(req, response, getServletContext());

        req.setHttpContext(httpContext);
        //req.setAttribute("LAZY_COMMIT_RESPONSE", Boolean.TRUE);
        
        try {
        	chain.doFilter(req, response);
        } finally {
        	SocSession session = (SocSession)req.getSession();
        	session.setAttribute(SocConstants.SOC_LAST_VISIT_TIME, System.currentTimeMillis());
        	if(null != session) {
        		session.commit();
        		// 
        		if(response.containsHeader(SocConstants.SET_COOKIE) && !response.containsHeader("P3P")){
        			response.setHeader("P3P", "CP='CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR'");
        		}
        	}
        }
	}

	@Override
	protected void releaseResource() {
		
	}
}