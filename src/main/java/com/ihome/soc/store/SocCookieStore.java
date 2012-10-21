/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.store;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.soc.SocHttpContext;
import com.ihome.soc.crypter.BlowfishEncrypter;
import com.ihome.soc.session.SessionAttributeConfig;
import com.ihome.soc.session.SocSession;
import com.ihome.soc.util.Decoder;
import com.ihome.soc.util.Enecoder;
import com.ihome.soc.util.Null;
import com.ihome.soc.util.SocConstants;

/**
 * 基于cookie的session持久化实现
 * @author sihai
 *
 */
public class SocCookieStore implements SocSessionStore {

	private final Log logger  = LogFactory.getLog(getClass());
	
	public static final String DEFAULT_COOKIE_PATH    = "/";
	
	public static final Random  random = new SecureRandom();
	
	private Map<String, String> cookieMap         = new HashMap<String, String>();
    private Map<String, Object> cacheAttributeMap = new HashMap<String, Object>();
	
    private SocSession session;
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
        this.session  = (SocSession) context.get(SESSION);
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
        }

        return null;
	}

	@Override
	public void save(SocHttpContext httpContext) {
		this.save(httpContext, null);
	}

	@Override
	public void save(SocHttpContext httpContext, String key) {
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
        
    	String ret = "";

        if (value == null) {
            return ret;
        }

        try {
            ret = URLDecoder.decode(value, "UTF-8");
        } catch (IllegalArgumentException e) {
        	logger.error(String.format("decode %s failed", config.getAlias()), e);
            return ret;
        } catch (UnsupportedEncodingException e) {
        	logger.error(String.format("decode %s failed", config.getAlias()), e);
            return ret;
        }

        //如果是加密过的
        if (config.isEncrypt()) {
            BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
            ret = encrypter.decrypt(ret);
            if (config.isBase64() && (ret != null) && (ret.length() > 6)) {
                //去掉BASE64时增加的头
                ret = ret.substring(6);
            }
        } else {
            if (config.isBase64()) {
                ret = Decoder.decode(ret);
            }
        }

        return ret;
    }
    
    /**
     * 保存单个的值到cookie中去
     *
     * @param context
     * @param key
     *
     * @throws Exception
     */
    private void saveSingleKey(SocHttpContext httpContext, String key) throws Exception {
        
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
        	cookieValue = URLEncoder.encode(cookieValue, SocConstants.DEFAULT_CHARSET);
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
        	StringBuilder cookieBuilder = new StringBuilder();
        	// kv
        	cookieBuilder.append(cookie.getName());
        	cookieBuilder.append(SocConstants.KEY_VALUE_SEPARATOR);
        	cookieBuilder.append(cookie.getValue());
        	// Domain
        	cookieBuilder.append(SocConstants.COOKIE_SEPARATOR);
        	cookieBuilder.append(SocConstants.COOKIE_DOMAIN);
        	cookieBuilder.append(cookie.getDomain());
        	// Path
        	cookieBuilder.append(SocConstants.COOKIE_SEPARATOR);
        	cookieBuilder.append(SocConstants.COOKIE_PATH);
        	cookieBuilder.append(cookie.getPath());
        	// Expries
        	if(cookie.getMaxAge() > 0){
        		cookieBuilder.append(SocConstants.COOKIE_EXPIRES);
	        	cookieBuilder.append(getCookieExpries(cookie));
        	}
        	
        	// Http Only
        	cookieBuilder.append(SocConstants.COOKIE_SEPARATOR);
        	cookieBuilder.append(SocConstants.COOKIE_HTTP_ONLY);
        	
        	// OK
        	response.addHeader("Set-Cookie", cookieBuilder.toString());
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
        } else {
            if (config.isBase64()) {
                attributeValue = Enecoder.encode(attributeValue);
            }
        }

        return attributeValue;
    }
    
    private static String getCookieExpries(Cookie cookie) {
    	
    	String result = null;
        int maxAge = cookie.getMaxAge();
        if (maxAge > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, maxAge);
            result = SocConstants.DATE_FORMAT.format(calendar);
        } else { // maxAge == 0
            result = SocConstants.DATE_FORMAT.format(0); // maxAge为0时表示需要删除该cookie，因此将时间设为最小时间，即1970年1月1日
        }

        return result;
    }
}
