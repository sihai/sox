/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.session;

import com.ihome.soc.store.StoreType;

/**
 * session配置
 * @author sihai
 *
 */
public class SessionAttributeConfig {

    public static String ALIAS   		= "alias";
    public static String STORE_TYPE  	= "storeType";
    public static String IS_ENCRYPT  	= "isEncrypt";
    public static String IS_BASE64   	= "isBase64";
    public static String DOMAIN      	= "cookie.domain";
    public static String LIFE_TIME   	= "lifeTime";
    public static String COOKIE_PATH  	= "cookie.path";
    public static String IS_HTTP_ONLY 	= "isHttpOnly";
    
    private String       		name;
    private String       		alias;
    private StoreType 			storeType 	= StoreType.COOKIE;
    private boolean      		isEncrypt   = false; 	//是否需要加密
    private boolean      		isBase64    = false; 	//是否做BASE64
    private String       		domain;
    private boolean      		isDirty     = false;
    private int          		lifeTime	 = -1;
    private String       		cookiePath  ="/";
    private boolean      		isHttpOnly = false;
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public boolean isEncrypt() {
        return isEncrypt;
    }

    public void setEncrypt(boolean isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isBase64() {
        return isBase64;
    }

    public void setBase64(boolean isBase64) {
        this.isBase64 = isBase64;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

	public int getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(int lifeTime) {
		this.lifeTime = lifeTime;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}
	
	public boolean isHttpOnly() {
		return isHttpOnly;
	}

	public void setHttpOnly(boolean isHttpOnly) {
		this.isHttpOnly = isHttpOnly;
	}
}
