/*
 * ihome inc.
 * soc
 */
package com.ihome.sox.session;

import com.ihome.sox.store.CookieStore;
import com.ihome.sox.store.SessionStore;
import com.ihome.sox.store.StoreType;

/**
 * Session Store Factory
 * @author sihai
 *
 */
public abstract class SessionStoreFactory {
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static SessionStore newInstance(StoreType type) {
		if(StoreType.COOKIE == type) {
			return new CookieStore();
		} else {
			throw new IllegalArgumentException(String.format("Unknown session store type: %s", type));
		}
	}
}
