/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.util;

import org.apache.commons.codec.binary.Base64;
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
                byte[] decoded = Base64.decodeBase64(encoded.getBytes());
                result = new String(decoded);
            } catch (Exception e) {
            	logger.warn(" decode base error", e);
                result = "";
            }
        }

        return result;
    }
}
