/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.session;

import com.ihome.soc.store.SocCookieStore;
import com.ihome.soc.store.SocSessionStore;
import com.ihome.soc.store.StoreType;

/**
 * Session Store Factory
 * @author sihai
 *
 */
public abstract class SocSessionStoreFactory {
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static SocSessionStore newInstance(StoreType type) {
		if(StoreType.COOKIE == type) {
			return new SocCookieStore();
		} else {
			throw new IllegalArgumentException(String.format("Unknown session store type: %s", type));
		}
	}
}
