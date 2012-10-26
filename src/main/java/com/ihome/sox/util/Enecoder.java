/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.util;

import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.sox.filter.AbstractFilter;

/**
 * 包装了一下
 * @author sihai
 *
 */
public class Enecoder {
	
	private static final Log logger  = LogFactory.getLog(AbstractFilter.class);
	
	/**
	 * 
	 * @param aStr
	 * @return
	 */
	public static String encode(String aStr) {
        String result = null;

        try {
            if (StringUtils.isNotBlank(aStr)) {
            	result = URLEncoder.encode(aStr, SoxConstants.DEFAULT_CHARSET);
            }
        } catch (Exception ex) {
        	logger.warn(" encode base error", ex);
        }

        return result;
    }
}
