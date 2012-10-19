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
	
	private static boolean inited = false;
	
	/**
	 * 
	 * @param configFileName
	 */
	public synchronized static void init(String configFileName) {
		if(inited) {
			return;
		}
		sessionManager = new SocDefaultSessionManager();
		((SocDefaultSessionManager)sessionManager).init(configFileName);
		inited = true;
	}
	
	/**
	 * 返回单例的实例
	 */
	public static SocSessionManager getInstance() {
		return sessionManager;
	}
}
