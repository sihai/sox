/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ihome.soc.SocHttpContext;
import com.ihome.soc.SocRequest;
import com.ihome.soc.crypter.BlowfishEncrypter;
import com.ihome.soc.util.SocConstants;

/**
 * SOCÈë¿ÚFilter
 * @author sihai
 *
 */
public class SocFilter extends AbstractFilter {

	@Override
	protected void init() throws ServletException {
		BlowfishEncrypter.setKey(getInitParameter(SocConstants.PARAMETER_KEY, SocConstants.DEFAULT_KEY));
	}

	@Override
	public void doFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		SocRequest req = new SocRequest(request);
        SocHttpContext httpContext = new SocHttpContext(req, response, getServletContext());

        req.setHttpContext(httpContext);
        req.setAttribute("LAZY_COMMIT_RESPONSE", Boolean.TRUE);

        chain.doFilter(req, response);       
	}

	@Override
	protected void releaseResource() {
	}
}
