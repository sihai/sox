/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.util;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * 常量
 * @author sihai
 *
 */
public class SoxConstants {
	
	// Session
	public static final String DEFAULT_CONFIG_FILE_NAME = "sox.properties";
	
	public static final String SOX_SESSION = "sox.session";
	public static final String SOX_SESSION_TIMEOUT = SOX_SESSION + ".timeout";
	
	public static final String SOX_ATTRIBUTS = "sox.attributes";
	public static final String SOX_ATTRIBUT = "sox.attribute";
	
	public static final String LOCATION = "Location";
	
	public static final String KEY_VALUE_SEPARATOR = "=";
	
	public static final String DEFAULT_CHARSET = "utf-8";			// 
	public static final String DEFAULT_KEY = "378206";				// 
	
	public static final String SOX_SESSION_ID = "_sox_session_id_"; // 内部session id的值
	public static final String SOX_LAST_VISIT_TIME = "_sox_last_visit_time_"; 
	
	public static final String PARAMETER_KEY = "encypt_key";		// 
	public static final String CONFIG_FILE = "configFile";			// 
	
	// cookie
	public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    public static final String COOKIE_DATE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss 'GMT'";
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(COOKIE_DATE_PATTERN, GMT_TIME_ZONE, Locale.US);
    
    public static final String SET_COOKIE = "Set-Cookie";			// 
    public static final String COOKIE_SEPARATOR = ";";				//
    public static final String COOKIE_DOMAIN = "Domain";			//
    public static final String COOKIE_PATH = "Path";				//
    public static final String COOKIE_EXPIRES = "Expires";			//
    public static final String COOKIE_HTTP_ONLY = "HttpOnly";		//
	public static final int DEFAULT_LIFE_CYCLE = 1800;				// 	
}
