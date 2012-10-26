/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.filter;

import java.io.IOException;
import java.sql.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ihome.sox.SoxHttpContext;
import com.ihome.sox.SoxRequest;
import com.ihome.sox.SoxResponse;
import com.ihome.sox.crypter.BlowfishEncrypter;
import com.ihome.sox.session.SoxSession;
import com.ihome.sox.session.SingletonSessionManagerFactory;
import com.ihome.sox.util.SoxConstants;

/**
 * SOC入口Filter
 * @author sihai
 *
 */
public class SoxFilter extends AbstractFilter {

	@Override
	protected void init() throws ServletException {
		// init session store factory
		String configFile = getInitParameter(SoxConstants.CONFIG_FILE, SoxConstants.CONFIG_FILE);
		if(StringUtils.isNotBlank(configFile)) {
			SingletonSessionManagerFactory.init(configFile);
		} else {
			SingletonSessionManagerFactory.init(configFile);
		}
		// init encrypter
		BlowfishEncrypter.setKey(getInitParameter(SoxConstants.PARAMETER_KEY, SoxConstants.DEFAULT_KEY));
	}

	@Override
	public void doFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		// 
		SoxRequest req = new SoxRequest(request);
		SoxResponse res = new SoxResponse(response, (SoxSession)req.getSession());
        SoxHttpContext httpContext = new SoxHttpContext(req, response, getServletContext());

        req.setHttpContext(httpContext);
        //req.setAttribute("LAZY_COMMIT_RESPONSE", Boolean.TRUE);
        
        try {
        	chain.doFilter(req, res);
        } finally {
        	SoxSession session = (SoxSession)req.getSession();
        	if(null != session) {
        		session.commit();
        	}
        	// commit response
    		res.commit();
        }
	}

	@Override
	protected void releaseResource() {
		
	}
}
