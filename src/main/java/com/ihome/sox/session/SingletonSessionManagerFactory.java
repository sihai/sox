/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.session;

/**
 * 单例SocSessionManager工厂
 * @author sihai
 *
 */
public abstract class SingletonSessionManagerFactory {
	
	private static SessionManager sessionManager;
	
	private static boolean inited = false;
	
	/**
	 * 
	 * @param configFileName
	 */
	public synchronized static void init(String configFileName) {
		if(inited) {
			return;
		}
		sessionManager = new DefaultSessionManager();
		((DefaultSessionManager)sessionManager).init(configFileName);
		inited = true;
	}
	
	/**
	 * 返回单例的实例
	 */
	public static SessionManager getInstance() {
		return sessionManager;
	}
}
