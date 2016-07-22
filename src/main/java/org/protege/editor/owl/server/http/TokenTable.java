package org.protege.editor.owl.server.http;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.protege.editor.owl.server.http.exception.ServerException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import edu.stanford.protege.metaproject.api.AuthToken;

public class TokenTable {

	private static final Logger logger = Logger.getLogger(TokenTable.class.getName());

	public static final int DEFAULT_TABLE_TIMEOUT = 30*60*1000;

	private ScheduledExecutorService executorService;

	private AuthToken tokenToCache;

	private LoadingCache<String, AuthToken> tokenCache;

	public TokenTable() {
		this(DEFAULT_TABLE_TIMEOUT);
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
		createTokenCleanupThread(10*60*1000); // every 10 minutes do cleanup
	}

	public synchronized void put(String key, AuthToken token) {
		tokenToCache = token;
		tokenCache.refresh(key); // load it to the cache storage
		tokenToCache = null;
	}

	public AuthToken get(String key) throws ServerException {
		try {
			return tokenCache.getUnchecked(key);
		}
		catch (InvalidCacheLoadException e) {
			/*
			 * 440 Login Timeout. Reference: https://support.microsoft.com/en-us/kb/941201
			 */
			throw new ServerException(440, "User session has expired. Please relogin");
		}
	}

	private AuthToken getAuthToken() {
		return tokenToCache;
	}

	private void createTokenCleanupThread(long timeout) {
		executorService = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread th = new Thread(r, "Token Cache Cleanup Detail");
				th.setDaemon(false);
				return th;
			}
		);
		executorService.scheduleAtFixedRate(() -> {
			tokenCache.cleanUp();
		}, timeout, timeout, TimeUnit.MILLISECONDS);
	}
}
