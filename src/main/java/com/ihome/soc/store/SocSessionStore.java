/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.store;

import java.util.Map;

import com.ihome.soc.SocHttpContext;

/**
 * 用来持久化session attribute的存储。
 * @author sihai
 *
 */
public interface SocSessionStore {
	
	String SESSION = "session";
	String CONFIG = "config";
	
	/**
     * 初始化每个STORE的环境,包括两个部分：1、实时数据 2、配置
     *
     * @param context
     */
    void init(Map<String, Object> context);
    
	/**
     * 根据单个KEY返回值
     *
     * @param key
     *
     * @return
     */
    Object getAttribute(String key);
    
    /**
     * 将值写回存储
     * @param httpContext
     */
    void save(SocHttpContext httpContext);

    /**
     * 将指定的值写回存储
     * @param httpContext
     * @param key
     */
    void save(SocHttpContext httpContext, String key);
    
    /**
     * 过期失效
     * @param key
     */
    void invalidate(String key);
    
    /**
     * 过期失效全部
     */
    void invalidate();
}
