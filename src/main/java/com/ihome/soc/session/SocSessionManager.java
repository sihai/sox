/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.session;

import com.ihome.soc.store.SocSessionStore;
import com.ihome.soc.store.StoreType;


/**
 * Session的管理器
 * @author sihai
 *
 */
public interface SocSessionManager {
	
	String CONFIG_FILE_NAME = "soc.properties";
	
	String SOC_SESSION = "soc.session";
	String SOC_SESSION_TIMEOUT = SOC_SESSION + ".timeout";
	
	String SOC_ATTRIBUTS = "soc.attributes";
	
	String SOC_ATTRIBUT = "soc.attribute";
	
	/**
	 * 设置它所关联的session
	 * @param session
	 */
	void setSession(SocSession session);
	
	/**
	 * 取得manager所管理的session
	 * @return
	 */
	SocSession getSession();
	
	/**
	 * 保存
	 *
	 */
	void save();
	
	/**
	 * 使过期的session值失效
	 *invalidate
	 */
	void invalidate();
	
	/**
	 * 根据store的类型返回实现的store
	 * @param type
	 * @return
	 */
	SocSessionStore getSessionStore(StoreType type);
	
	/**
	 * 读取属性值
	 * @param key
	 * @return
	 */
	Object getAttribute(String key);
	
	
	/**
	 * 判断要求的key是否存在于配置文件之中
	 * @param key
	 * @return
	 */
	boolean isExistKey(String key);
}
