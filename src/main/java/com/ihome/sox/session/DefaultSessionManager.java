/*
 * ihome inc.
 * sox
 */
package com.ihome.sox.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.sox.store.SessionStore;
import com.ihome.sox.store.StoreType;
import com.ihome.sox.util.SoxConstants;

/**
 * 默认的SocSessionManager的实现
 * @author sihai
 *
 */
public class DefaultSessionManager implements SessionManager {

	private final Log logger  = LogFactory.getLog(getClass());
	
	// session存储的设置
    private Map<StoreType, SessionStore> storeMap = new HashMap<StoreType, SessionStore>();
    
    // 属性值的配置
    private Map<String, SessionAttributeConfig> sessionAttributeConfigMap = new HashMap<String, SessionAttributeConfig>(); //解析成对象的配置文件
    
	// 当前管理的session
	ThreadLocal<SoxSession> threadLocal = new ThreadLocal<SoxSession>();
	
	/**
	 * 使用默认的<code>CONFIG_FILE_NAME</code>初始化
	 */
	public void init() {
		init(SoxConstants.DEFAULT_CONFIG_FILE_NAME);
	}
	
	/**
	 * 使用指定的配置文件初始化, 配置文件位于classpath下
	 * @param configFileName
	 */
	public void init(String configFileName) {
		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName));
			init(properties);
		} catch (IOException e) {
			throw new IllegalArgumentException("SOC init failed", e);
		}
	}
	
	/**
	 * 使用制定的Properties初始化
	 * @param properties
	 */
	public void init(Properties properties) {
		
		int sessionTimeout = SoxConstants.DEFAULT_LIFE_CYCLE;
		String key = null;
		String value = null;
		
		// 全局配置
		key = SoxConstants.SOX_SESSION_TIMEOUT;
		value = properties.getProperty(key);
		if(StringUtils.isNotBlank(value)) {
			try {
				sessionTimeout = Integer.valueOf(StringUtils.trim(value));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Value of %s property only allow big than 0 integer", key));
			}
		}
		
		// 系统内部字段
		SessionAttributeConfig config = new SessionAttributeConfig();
		config.setName(SoxConstants.SOX_SESSION_ID);
		config.setAlias(SoxConstants.SOX_SESSION_ID);
		config.setDataType(DataType.String);
		config.setLifeTime(sessionTimeout);
		config.setEncrypt(true);
		config.setHttpOnly(true);
		config.setBase64(true);
		sessionAttributeConfigMap.put(config.getName(), config);
		
		config = new SessionAttributeConfig();
		config.setName(SoxConstants.SOX_LAST_VISIT_TIME);
		config.setAlias(SoxConstants.SOX_LAST_VISIT_TIME);
		config.setDataType(DataType.Long);
		config.setLifeTime(sessionTimeout);
		config.setEncrypt(true);
		config.setHttpOnly(true);
		config.setBase64(true);
		sessionAttributeConfigMap.put(config.getName(), config);
		
		// 拿到所有的字段
		key = null;
		value = properties.getProperty(SoxConstants.SOX_ATTRIBUTS);
		if(StringUtils.isBlank(value)) {
			throw new IllegalArgumentException(String.format("Please config %s property, or if you no need, please not use soc", SoxConstants.SOX_ATTRIBUTS));
		}
		
		// TODO 避免和系统保留字段重复
		
		String[] attributes = StringUtils.trim(value).split(",");
		if(0 == attributes.length) {
			throw new IllegalArgumentException(String.format("Please config %s property, or if you no need, please not use soc", SoxConstants.SOX_ATTRIBUTS));
		}
		
		for(String attribute : attributes) {
			config = new SessionAttributeConfig();
			config.setName(attribute);
			
			// alias 默认使用 name
			config.setAlias(attribute);
			value = properties.getProperty(String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.ALIAS));
			if(StringUtils.isNotBlank(value)) {
				config.setAlias(StringUtils.trim(value));
			}
			
			// DataType
			value = properties.getProperty(String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.DATA_TYPE));
			if(StringUtils.isNotBlank(value)) {
				config.setDataType(DataType.valueOf(StringUtils.trim(value)));
				if(null == config.getDataType()) {
					StringBuilder sb = new StringBuilder();
					for(StoreType s : StoreType.values()) {
						sb.append(StringUtils.lowerCase(s.toString()));
						sb.append(" or ");
					}
					throw new IllegalArgumentException(String.format("Value of %s property only allow: %s", key, sb.toString()));
				}
			}
			// storeType默认cookie
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.STORE_TYPE);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				StoreType type = StoreType.valueOf(StringUtils.upperCase(StringUtils.trim(value)));
				if(null == type) {
					StringBuilder sb = new StringBuilder();
					for(StoreType s : StoreType.values()) {
						sb.append(StringUtils.lowerCase(s.toString()));
						sb.append(" or ");
					}
					throw new IllegalArgumentException(String.format("Value of %s property only allow: %s", key, sb.toString()));
				}
				config.setStoreType(type);
			}
						
			// isBase64 默认false
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.IS_BASE64);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				if(StringUtils.trim(value).equals(Boolean.TRUE.toString())) {
					config.setBase64(true);
				} else if(StringUtils.trim(value).equals(Boolean.FALSE.toString())) {
					config.setBase64(false);
				} else {
					throw new IllegalArgumentException(String.format("Value of %s property only allow: %s or %s", key, Boolean.TRUE, Boolean.FALSE));
				}
			}
			
			// isEncrypt默认false
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.IS_ENCRYPT);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				if(StringUtils.trim(value).equals(Boolean.TRUE.toString())) {
					config.setEncrypt(true);
				} else if(StringUtils.trim(value).equals(Boolean.FALSE.toString())) {
					config.setEncrypt(false);
				} else {
					throw new IllegalArgumentException(String.format("Value of %s property only allow: %s or %s", key, Boolean.TRUE, Boolean.FALSE));
				}
			}
			
			// lifeTime
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.LIFE_TIME);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				try {
					config.setLifeTime(Integer.valueOf(StringUtils.trim(value)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(String.format("Value of %s property only allow big than 0 integer", key));
				}
			}
			
			// domain used by cookie store
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.DOMAIN);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				config.setDomain(StringUtils.trim(value));
			}
			
			// cookie path used by cookie store
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.COOKIE_PATH);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				config.setCookiePath(StringUtils.trim(value));
			}
			
			// isHttpOnly used by cookie store
			key = String.format("%s.%s.%s", SoxConstants.SOX_ATTRIBUT, attribute, SessionAttributeConfig.IS_HTTP_ONLY);
			value = properties.getProperty(key);
			if(StringUtils.isNotBlank(value)) {
				if(StringUtils.trim(value).equals(Boolean.TRUE.toString())) {
					config.setHttpOnly(true);
				} else if(StringUtils.trim(value).equals(Boolean.FALSE.toString())) {
					config.setHttpOnly(false);
				} else {
					throw new IllegalArgumentException(String.format("Value of %s property only allow: %s or %s", key, Boolean.TRUE, Boolean.FALSE));
				}
			}
			
			sessionAttributeConfigMap.put(config.getName(), config);
		}
	}
	
	@Override
	public void setSession(SoxSession session) {
		threadLocal.set(session);
	}

	@Override
	public SoxSession getSession() {
		return threadLocal.get();
	}

	@Override
	public void save() {
		
		logger.debug("start save session attribute!");
		SoxSession session = getSession();
		Map<String, Boolean> change = session.getChangedMarkMap();		
		
		for(Iterator<Entry<String, Boolean>> iterator = change.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Boolean> e = iterator.next();
			String key = e.getKey();
			if(e.getValue().booleanValue()) {
				//取得它的STORE, 保存它
				SessionAttributeConfig config = sessionAttributeConfigMap.get(key);
				
				if (null == config) {
					continue;
				}
				
				//取得该KEY配置的STORE        此处需防止没有取到STORE
		        StoreType type = config.getStoreType();
		        SessionStore store = getStore(type);
		        store.save(this.getSession().getHttpContext(), key);
			}			
		}		
	}

	@Override
	public void invalidate() {
		// 遍历配置，再转给保存的STORE处理
		for (Iterator<Entry<String, SessionAttributeConfig>> iterator = sessionAttributeConfigMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, SessionAttributeConfig> e = iterator.next();
			SessionAttributeConfig config = e.getValue();
			if (config.getLifeTime() <= 0 && null != config.getStoreType()) { // 说明需要处理
				getStore(config.getStoreType()).invalidate(e.getKey());
			}
		}	
	}

	@Override
	public SessionStore getSessionStore(StoreType type) {
		return storeMap.get(type);
	}
	
	@Override
	public Object getAttribute(String key) {
		//取得该key配置的store
        SessionAttributeConfig config = (SessionAttributeConfig)sessionAttributeConfigMap.get(key);
        StoreType storeType = config.getStoreType();
        SessionStore store = getStore(storeType);

        return store.getAttribute(key);
	}

	@Override
	public boolean isExistKey(String key) {
		if(sessionAttributeConfigMap.containsKey(key)) {
            return true;
        }

        return false;
	}
	
	/**
	 * 
	 * @param storeKey
	 * @return
	 */
	private SessionStore getStore(StoreType storeType) {
		SessionStore store = this.getSession().getSessionStore(storeType);

        //如果当前环境上下文中没有STORE，则新建一个。
        if(null == store) {
            store = (SessionStore)SessionStoreFactory.newInstance(storeType);
            Map<String, Object> context = new HashMap<String, Object>();
            context.put(SessionStore.SESSION, this.getSession());
            context.put(SessionStore.CONFIG, sessionAttributeConfigMap);
            store.init(context);
            getSession().setSessionStore(storeType, store);
        }
		return store;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DefaultSessionManager sm = new DefaultSessionManager();
		sm.init();
		System.out.println(sm);
	}
}
