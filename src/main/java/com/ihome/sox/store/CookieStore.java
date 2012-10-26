/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.store;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.sox.SoxHttpContext;
import com.ihome.sox.crypter.BlowfishEncrypter;
import com.ihome.sox.session.DataType;
import com.ihome.sox.session.SessionAttributeConfig;
import com.ihome.sox.session.SoxSession;
import com.ihome.sox.util.Decoder;
import com.ihome.sox.util.Enecoder;
import com.ihome.sox.util.Null;
import com.ihome.sox.util.SoxConstants;

/**
 * 基于cookie的session持久化实现
 * @author sihai
 *
 */
public class CookieStore implements SessionStore {

	private final Log logger  = LogFactory.getLog(getClass());
	
	public static final String DEFAULT_COOKIE_PATH    = "/";
	
	public static final Random  random = new SecureRandom();
	
	private Map<String, String> cookieMap         = new HashMap<String, String>();
    private Map<String, Object> cacheAttributeMap = new HashMap<String, Object>();
	
    private SoxSession session;
    private Map<String, SessionAttributeConfig> sessionAttributeConfigMap;
    
	/**
     * 初始化COOKIE的值对
     */
    private void init(Cookie[] cookies) {
        //将cookies的值拆成key->value
    	if (cookies == null) {
			return ;
		}
        for (int i = 0; i < cookies.length; i++) {
            String name  = cookies[i].getName();
            String value = cookies[i].getValue();
            cookieMap.put(name, value);
        }
    }
    
    /**
     * 初始化
     */
    @Override
    public void init(Map<String, Object> context) {
        this.session  = (SoxSession) context.get(SESSION);
        this.sessionAttributeConfigMap = (Map<String, SessionAttributeConfig>) context.get(CONFIG);
        this.init(session.getRequest().getCookies());
    }
    
	@Override
	public Object getAttribute(String key) {
		
		 //先试图从临时存储空间中返回
        SessionAttributeConfig config = (SessionAttributeConfig) sessionAttributeConfigMap.get(key);

        if (null == config) {
            logger.warn(String.format("There is no config for key:%s", key));
            return null;
        }
        String alias = config.getAlias();
        Object value = (String) cacheAttributeMap.get(alias);
        if (null != value) {
        	if(!(value instanceof Null)) {
        		return value;
        	}
        }

        //进一步从COOKIE中解析
        String cookieValue = (String) cookieMap.get(alias);

        //如果值不空的话，则根据配置文件进行解析
        if (null != cookieValue) {
            Object v = parseValue(config, cookieValue);
            // cache
            cacheAttributeMap.put(key, null == v ? Null.getInstance() : v);
            return v;
        }

        return null;
	}

	@Override
	public void save(SoxHttpContext httpContext) {
		this.save(httpContext, null);
	}

	@Override
	public void save(SoxHttpContext httpContext, String key) {
		//根据整个配置与SESSION的值重写
        if (null != key) {
            try {
                this.saveSingleKey(httpContext, key);
            } catch (Exception e) {
                logger.error(String.format("Save value for key:%s to cookie failed", key), e);
            }
        } else {
            for(String k : sessionAttributeConfigMap.keySet()) {
            	try {
                    this.saveSingleKey(httpContext, k);
                } catch (Exception e) {
                    logger.error(String.format("Save value for key:%s to cookie failed", k), e);
                }
            }
        }
	}

	@Override
	public void invalidate(String key) {
		SessionAttributeConfig config = (SessionAttributeConfig)sessionAttributeConfigMap.get(key);
        if (null != config) {
            session.setAttribute(key, null);
        }
	}
	
	@Override
	public void invalidate() {
        for(String key : sessionAttributeConfigMap.keySet()) {
        	invalidate(key);
        }
	}

	/**
     * 根据配置文件，决定如何将cookie中的字符串或字节流解析成合适的对象
     *
     * @param value
     *
     * @return
     */
    private Object parseValue(SessionAttributeConfig config, String value) {
        
    	String result = null;
        if (value == null) {
            return result;
        }
        
        try {
        	result = Decoder.decode(value);
        } catch (IllegalArgumentException e) {
        	logger.error(String.format("decode %s failed", config.getAlias()), e);
            return result;
        } catch (Exception e) {
        	logger.error(String.format("decode %s failed", config.getAlias()), e);
            return result;
        }

        //如果是加密过的
        if (config.isEncrypt()) {
            BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
            result = encrypter.decrypt(result);
            if (config.isBase64() && (result != null) && (result.length() > 6)) {
                //去掉BASE64时增加的头
            	result = result.substring(6);
            }
        } else if (config.isBase64()) {
            result = new String(Base64.decodeBase64(result));
        }
        
        if(config.getDataType() == DataType.String) {
        	return result;
        } else if (config.getDataType() == DataType.Byte) {
        	return Byte.valueOf(result);
        } else if (config.getDataType() == DataType.Short) {
        	return Short.valueOf(result);
        } else if (config.getDataType() == DataType.Integer) {
        	return Integer.valueOf(result);
        } else if (config.getDataType() == DataType.Long) {
        	return Long.valueOf(result);
        } else if (config.getDataType() == DataType.Float) {
        	return Float.valueOf(result);
        } else if (config.getDataType() == DataType.Double) {
        	return Double.valueOf(result);
        }  else if (config.getDataType() == DataType.Boolean) {
        	return Boolean.valueOf(result);
        } 
        return result;
    }
    
    /**
     * 保存单个的值到cookie中去
     *
     * @param context
     * @param key
     *
     * @throws Exception
     */
    private void saveSingleKey(SoxHttpContext httpContext, String key) throws Exception {
        
    	HttpServletResponse response = httpContext.getResponse();
        //分析该KEY是否是组合KEY
        SessionAttributeConfig config = (SessionAttributeConfig) sessionAttributeConfigMap.get(key);
        this.saveCookie(response, config);
    }
    
    /**
     * @param response
     * @param config
     * @param value
     *
     * @throws Exception
     */
    private void saveCookie(HttpServletResponse response, SessionAttributeConfig config) throws Exception {
        String cookieName = config.getAlias();
        int    lifeTime = config.getLifeTime();

        //得到cookie的值
        String cookieValue = getCookieValue(config);
        Cookie cookie = null;

        if (cookieValue != null) {
            cookie = new Cookie(cookieName, cookieValue);
        } else {
            cookie = new Cookie(cookieName, "");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("cookie name: %s, cookie value: %s", cookieName, cookieValue));
        }

        //设置一些COOKIE的其它相关属性
        String cookiePath = DEFAULT_COOKIE_PATH;
        if (config.getCookiePath() != null) {
            cookiePath = config.getCookiePath();
        }

        cookie.setPath(cookiePath);
        
        if (lifeTime > 0) {
            cookie.setMaxAge(lifeTime);
        }

        String domain = config.getDomain();
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        }

        if(config.isHttpOnly()) {
        	// 到Servlet 3.0后就不需要用下面这段代码了，可以直接cookie.setHttpOnly(true)
        	// 然后response.addCookie(cookie);
        	StringBuilder sb = new StringBuilder();
        	// kv
        	sb.append(cookie.getName());
        	sb.append(SoxConstants.KEY_VALUE_SEPARATOR);
        	sb.append(cookie.getValue());
        	
        	// Domain
        	if(StringUtils.isNotBlank(cookie.getDomain())) {
        		sb.append(SoxConstants.COOKIE_SEPARATOR);
        		sb.append(SoxConstants.COOKIE_DOMAIN);
        		sb.append(SoxConstants.KEY_VALUE_SEPARATOR);
        		sb.append(cookie.getDomain());
        	}
        	
        	// Path
        	sb.append(SoxConstants.COOKIE_SEPARATOR);
        	sb.append(SoxConstants.COOKIE_PATH);
        	sb.append(SoxConstants.KEY_VALUE_SEPARATOR);
        	sb.append(cookie.getPath());
        	
        	// Expries
        	if(cookie.getMaxAge() > 0){
        		sb.append(SoxConstants.COOKIE_SEPARATOR);
        		sb.append(SoxConstants.COOKIE_EXPIRES);
        		sb.append(SoxConstants.KEY_VALUE_SEPARATOR);
	        	sb.append(getCookieExpries(cookie));
        	}
        	
        	// Http Only
        	sb.append(SoxConstants.COOKIE_SEPARATOR);
        	sb.append(SoxConstants.COOKIE_HTTP_ONLY);
        	
        	// OK
        	response.addHeader(SoxConstants.SET_COOKIE, sb.toString());
        } else {
        	response.addCookie(cookie);
        }
    }
    
    /**
     * 构造cookie value
     *
     * @return
     *
     * @throws Exception
     */
    protected String getCookieValue(SessionAttributeConfig config) throws Exception {
        //得到需要保存的COOKIE的值,此处需进一步考虑是否需要序列化
        String attributeValue = null;
        Object obj = session.getAttribute(config.getName());

        if(obj == null) {
            return null;
        }

        if (obj instanceof String) {
            attributeValue = (String) obj;
        } else {
            attributeValue = obj.toString();
        }

        if (StringUtils.isBlank(attributeValue)) {
            return attributeValue;
        }

        if (config.isEncrypt()) {
            //add encryption here
            if (config.isBase64()) {
                attributeValue = random.nextInt(10) + "a#b$^" + attributeValue;
            }
            BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
            attributeValue = encrypter.encrypt(attributeValue);
        } else if (config.isBase64()) {
        	attributeValue = new String(Base64.encodeBase64(attributeValue.getBytes()));
        }
        
        attributeValue = Enecoder.encode(attributeValue);

        return attributeValue;
    }
    
    private static String getCookieExpries(Cookie cookie) {
    	
    	String result = null;
        int maxAge = cookie.getMaxAge();
        if (maxAge > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, maxAge);
            result = SoxConstants.DATE_FORMAT.format(calendar);
        } else { // maxAge == 0
            result = SoxConstants.DATE_FORMAT.format(0); // maxAge为0时表示需要删除该cookie，因此将时间设为最小时间，即1970年1月1日
        }

        return result;
    }
}
