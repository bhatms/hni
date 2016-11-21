package org.hni.order.service;

import java.io.IOException;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

@Component
public class RedisLockingService implements LockingService, Lifecycle {
	private static final Logger logger = LoggerFactory.getLogger(RedisLockingService.class);
	private RedissonClient redisson;
	
	public RedisLockingService(){
	}
	
	public void setConfigLocation(String configLocation)  throws IOException {
		Config config;
		config = Config.fromJSON(this.getClass().getResourceAsStream(configLocation));
		redisson = Redisson.create(config);	
	}
	
	@Override
	public boolean acquireLock(String key) {
		RLock lock = redisson.getLock(key);
		if (lock.isLocked()) {
			return false;
		}
		lock.lock();
		return true;
	}

	@Override
	public boolean isLocked(String key) {
		RLock lock = redisson.getLock(key);
		return lock.isLocked();
	}

	@Override
	public void releaseLock(String key) {
		RLock lock = redisson.getLock(key);
		if (lock.isLocked()) {
			lock.forceUnlock();
		}
	}

	@Override
	public void shutdown() {
		logger.info("Shutting down RedisLockingService...");
		redisson.shutdown();
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		shutdown();
	}

}