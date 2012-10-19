/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.soc.filter.AbstractFilter;

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
                result = new String(Base64.encodeBase64(aStr.getBytes()));
            }
        } catch (Exception ex) {
        	logger.warn(" encode base error", ex);
        }

        return result;
    }
}
