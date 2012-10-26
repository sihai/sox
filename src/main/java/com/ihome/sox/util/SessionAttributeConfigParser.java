/*
 * ihome inc.
 * sox
 */
package com.ihome.sox.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.ihome.sox.session.DataType;
import com.ihome.sox.session.SessionAttributeConfig;
import com.ihome.sox.store.StoreType;

/**
 * 
 * @author sihai
 *
 */
public class SessionAttributeConfigParser {

	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static Map<String, SessionAttributeConfig> parse(Properties properties) {
		Map<String, SessionAttributeConfig> sessionAttributeConfigMap = new HashMap<String, SessionAttributeConfig>();
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
		
		return sessionAttributeConfigMap;
	}
}
