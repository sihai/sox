/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.session;

/**
 * 单例SocSessionManager工厂
 * @author sihai
 *
 */
public abstract class SocSingletonSessionManagerFactory {
	
	private static SocSessionManager sessionManager;
	
	static {
		sessionManager = new SocDefaultSessionManager();
	}
	
	/**
	 * 返回单例的实例
	 */
	public static SocSessionManager getInstance() {
		return sessionManager;
	}
}
