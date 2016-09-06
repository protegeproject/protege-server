package org.protege.editor.owl.server.http;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.protege.editor.owl.server.security.LoginTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import edu.stanford.protege.metaproject.api.AuthToken;

public class TokenTable {

	public static final long DEFAULT_TIMEOUT_PERIOD = 1800000; // 30 minutes
	public static final long DEFAULT_CLEANUP_INTERVAL = 600000; // 10 minutes

	private static final Logger logger = LoggerFactory.getLogger(TokenTable.class);

	private final LoadingCache<String, AuthToken> tokenCache;

	private AuthToken tokenToCache;

	public TokenTable() {
		this(DEFAULT_TIMEOUT_PERIOD);
	}

	public TokenTable(long timeout) {
		tokenCache = CacheBuilder.newBuilder()
				.expireAfterAccess(timeout, TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<String, AuthToken>() {
					public void onRemoval(RemovalNotification<String, AuthToken> notification) {
						logger.info(String.format("Unregister %s (%s) from the server due to inactivity",
								notification.getValue().getUser().getId().get(),
								notification.getValue().getUser().getName().get()));
					}
				})
				.build(
					new CacheLoader<String, AuthToken>() {
						public AuthToken load(String key) {
							return getAuthToken();
						}
					});
		createTokenCleanupThread(DEFAULT_CLEANUP_INTERVAL);
	}

	public synchronized void put(String key, AuthToken token) {
		tokenToCache = token;
		tokenCache.refresh(key); // load it to the cache storage
		tokenToCache = null;
	}

	public AuthToken get(String key) throws LoginTimeoutException {
		try {
			return tokenCache.getUnchecked(key);
		}
		catch (InvalidCacheLoadException e) {
			throw new LoginTimeoutException(); // Signal the exception as login timeout exception
		}
	}

	private AuthToken getAuthToken() {
		return tokenToCache;
	}

	private void createTokenCleanupThread(long timeout) {
		final ScheduledExecutorService executorService = 
				Executors.newSingleThreadScheduledExecutor(r -> {
					Thread th = new Thread(r, "User Token Maintainer Thread");
					th.setDaemon(false);
					return th;
				}
			);
		executorService.scheduleAtFixedRate(() -> {
			if (tokenCache.size() > 0) {
				tokenCache.cleanUp();
			}
		}, timeout, timeout, TimeUnit.MILLISECONDS);
	}
}
