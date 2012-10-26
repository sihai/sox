/*
 * ihome inc.
 * sox
 */
package com.ihome.sox.util;

import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.sox.filter.AbstractFilter;

/**
 * 包装了一下
 * @author sihai
 *
 */
public class Decoder {
	
	private static final Log logger  = LogFactory.getLog(AbstractFilter.class);
	
	/**
	 * 
	 * @param encoded
	 * @return
	 */
	public static String decode(String encoded) {
		
        String result = null;
        if (StringUtils.isNotBlank(encoded)) {
            try {
            	result = URLDecoder.decode(encoded, SoxConstants.DEFAULT_CHARSET);
            } catch (Exception e) {
            	logger.warn(" decode base error", e);
                result = "";
            }
        }

        return result;
    }
}
