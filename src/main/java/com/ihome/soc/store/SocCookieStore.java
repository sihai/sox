/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.store;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
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
 * ����cookie��session�־û�ʵ��
 * @author sihai
 *
 */
public class SocCookieStore implements SocSessionStore {

	private final Log logger  = LogFactory.getLog(getClass());
	
	public static final String DEFAULT_COOKIE_PATH    = "/";
	
	public static final Random  random = new SecureRandom();
	
	private Map<String, String> cookieMap        = new HashMap<String, String>();
    private Map<String, Object> cachesAttributeMap = new HashMap<String, Object>();
	
    private SocSession session;
    private Map<String, SessionAttributeConfig> sessionAttributeConfigMap;
    
	/**
     * ��ʼ��COOKIE��ֵ��
     */
    private void init(Cookie[] cookies) {
        //��cookies��ֵ���key->value
    	if (cookies == null) {
			return ;
		}
        for (int i = 0; i < cookies.length; i++) {
            String name  = cookies[i].getName();
            String value = cookies[i].getValue();

            cookieMap.put(name, value);
            cachesAttributeMap.put(name, value);
        }
    }
    
    /**
     * ��ʼ��
     */
    @Override
    public void init(Map<String, Object> context) {
        this.session  = (SocSession) context.get(SESSION);
        this.sessionAttributeConfigMap = (Map<String, SessionAttributeConfig>) context.get(CONFIG);
        this.init(session.getRequest().getCookies());
    }
    
	@Override
	public Object getAttribute(String key) {
		
		 //����ͼ����ʱ�洢�ռ��з���
        SessionAttributeConfig config = (SessionAttributeConfig) sessionAttributeConfigMap.get(key);

        if (null == config) {
            logger.warn(String.format("There is no config for key:%s", key));
            return null;
        }
        String alias = config.getAlias();
        Object value = (String) cachesAttributeMap.get(alias);
        if (null != value) {
        	if(!(value instanceof Null)) {
        		return value;
        	}
        }

        //��һ����COOKIE�н���
        String cookieValue = (String) cookieMap.get(alias);

        //���ֵ���յĻ�������������ļ����н���
        if (null != cookieValue) {
            Object v = parseValue(config, cookieValue);
            // cache
            cachesAttributeMap.put(key, null == v ? Null.getInstance() : v);
        }

        return null;
	}

	@Override
	public void save(SocHttpContext httpContext) {
		this.save(httpContext, null);
	}

	@Override
	public void save(SocHttpContext httpContext, String key) {
		//��������������SESSION��ֵ��д
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
     * ���������ļ���������ν�cookie�е��ַ������ֽ��������ɺ��ʵĶ���
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

        //����Ǽ��ܹ���
        if (config.isEncrypt()) {
            BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
            ret = encrypter.decrypt(ret);
            if (config.isBase64() && (ret != null) && (ret.length() > 6)) {
                //ȥ��BASE64ʱ���ӵ�ͷ
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
     * ���浥����ֵ��cookie��ȥ
     *
     * @param context
     * @param key
     *
     * @throws Exception
     */
    private void saveSingleKey(SocHttpContext httpContext, String key) throws Exception {
        
    	HttpServletResponse response = httpContext.getResponse();
        //������KEY�Ƿ������KEY
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

        //�õ�cookie��ֵ
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

        //����һЩCOOKIE�������������
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
        	// ��Servlet 3.0��Ͳ���Ҫ��������δ����ˣ�����ֱ��cookie.setHttpOnly(true)
        	// Ȼ��response.addCookie(cookie);
        	StringBuilder cookieBuilder = new StringBuilder();
        	cookieBuilder.append(cookieName);
        	cookieBuilder.append("=");
        	if(cookieValue != null)
        		cookieBuilder.append(cookieValue);
        	else
        		cookieBuilder.append("");
        	cookieBuilder.append(";Domain=");
        	cookieBuilder.append(cookie.getDomain());
        	cookieBuilder.append(";Path=");
        	cookieBuilder.append(cookie.getPath());
        	if(cookie.getMaxAge() > 0){
	        	cookieBuilder.append(";Max-Age=");
	        	cookieBuilder.append(cookie.getMaxAge());
        	}
        	cookieBuilder.append(";httpOnly");
        	response.addHeader("Set-Cookie", cookieBuilder.toString());
        } else {
        	response.addCookie(cookie);
        }
    }
    
    /**
     * ����cookie value
     *
     * @return
     *
     * @throws Exception
     */
    protected String getCookieValue(SessionAttributeConfig config) throws Exception {
        //�õ���Ҫ�����COOKIE��ֵ,�˴����һ�������Ƿ���Ҫ���л�
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
}