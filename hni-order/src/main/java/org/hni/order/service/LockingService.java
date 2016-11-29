package org.hni.order.service;

public interface LockingService {

	boolean acquireLock(String key);
	boolean acquireLock(String key, Long ttlMinutes);
	boolean isLocked(String key);
	void releaseLock(String key);
	void shutdown();
	
	void addCache(String key, Object object);
	Object getCache(String key);
}
