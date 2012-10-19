/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;

import com.ihome.soc.SocHttpContext;
import com.ihome.soc.store.SocSessionStore;
import com.ihome.soc.store.StoreType;

/**
 * session的实现，对未经过包装的方法，一律代理给原来的SESSION，主要实现取得属性的方法
 * 
 * @author sihai
 *
 */
public class SocSession implements HttpSession {
	
	private String             		sessionId           = null;
	HttpServletRequest 				request				= null;
    private SocHttpContext     		httpContext         = null;
	private Map<String, Boolean>	changedMarkMap      = new HashMap<String, Boolean>(); 	// 属性修改标记
    private Map<String, Object>     attributeMap        = new HashMap<String, Object>(); 					//session属性的内部保存
    private long               		createTime;
    private int                		maxInactiveInterval = 1800;
    private static String      		JSESSION_ID         = "_SOC_SESSION_ID_"; //sessionID的值

    // session的管理器
    SocSessionManager 	sessionManager  = null;
    // 
    Map<StoreType, SocSessionStore> sessionStoreMap = new HashMap<StoreType, SocSessionStore>();
    
    /**
     * 构造函数，以便将通过系统获得容器的SESSION
     *
     * @param session
     */
    public SocSession(HttpServletRequest request) {
        createTime   = System.currentTimeMillis();
        this.request = request;

        //初始化 SESSIONID        
        sessionId = (String) getAttribute(JSESSION_ID);
        
        if (StringUtils.isBlank( sessionId)) {
            sessionId = UUID.randomUUID().toString();

            //并写入COOKIE中
            setAttribute(JSESSION_ID, sessionId);
        }
    }
	    
	@Override
	public long getCreationTime() {
		return this.createTime;
	}

	@Override
	public String getId() {
		return sessionId;
	}

	@Override
	public long getLastAccessedTime() {
		// FIXME
		return this.createTime;
	}

	@Override
	public ServletContext getServletContext() {
		return httpContext.getServletContext();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException("No longer supported method: getSessionContext");
	}

	@Override
	public Object getAttribute(String name) {
		
		if (name == null) {
            return null;
        }

        //如果该属性已经存在，则直接返回        
        if (attributeMap.containsKey(name)) {
        	return attributeMap.get(name);
		}

        //如果不存在 则分两种情况：
        //1、session配置中是否存在  若不存在 则返回NULL
        if (!getSessionManager().isExistKey(name)) {
            return null;
        }

        //2、如果存在并改变了值 Why? 被删除了?
        if (changedMarkMap.containsKey(name)) {
			return null;
		}
        
        //3、如果配置中存在，则试图从指定的配置中读取该值, 并存入被包装的session中
        Object attribute = getSessionManager().getAttribute(name);

        if (attribute != null) {
        	attributeMap.put(name, attribute);
            return attribute;
        }

        return null;

	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(attributeMap.keySet().iterator());
	}

	@Override
	public String[] getValueNames() {
		
		List names = new ArrayList();
        for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
            names.add(e.nextElement());
        }
        return (String[]) names.toArray( new String[names.size()]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributeMap.put(name, value);
        changedMarkMap.put(name, Boolean.TRUE);
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
        changedMarkMap.put(name, Boolean.TRUE);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}
	
	/**
	 * 将SESSION失效，如果是COOKIE的话，将所有时间配置为-1的全部失效
	 */
	@Override
	public void invalidate() {
		getSessionManager().invalidate();
	}

	@Override
	public boolean isNew() {
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	private SocSessionManager getSessionManager() {
        if (sessionManager == null) {
        	sessionManager = (SocSessionManager) SocSingletonSessionManagerFactory.getInstance();
        	sessionManager.setSession(this);
        }

        return this.sessionManager;
    }
	
	/**
	 * 
	 * @return
	 */
	public SocHttpContext getHttpContext() {
		return httpContext;
	}
	
	/**
	 * 
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return httpContext.getRequest();
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public SocSessionStore getSessionStore(StoreType type) {
		return sessionStoreMap.get(type);
	}
	
	/**
	 * 
	 * @param type
	 * @param store
	 */
	public void setSessionStore(StoreType type, SocSessionStore store) {
		sessionStoreMap.put(type, store);
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Boolean> getChangedMarkMap() {
		return changedMarkMap;
	}
	
	/**
	 * 
	 * @param httpContext
	 */
	public void setHttpContext(SocHttpContext httpContext) {
		this.httpContext = httpContext;
	}
	
	/**
	 * 
	 */
	public void commit() {
		getSessionManager().save();
	}
	
}
