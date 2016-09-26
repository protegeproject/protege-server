package org.protege.editor.owl.server.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.protege.editor.owl.server.security.LoginTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import edu.stanford.protege.metaproject.api.AuthToken;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class TokenTable {

	private static final Logger logger = LoggerFactory.getLogger(TokenTable.class);

	private final Cache<String, AuthToken> tokenCache;

	/**
	 * Builds a token table with a custom cache storage.
	 *
	 * @param tokenCache
	 *            A custom cache to store user tokens
	 */
	public TokenTable(@Nonnull Cache<String, AuthToken> tokenCache) {
		this.tokenCache = checkNotNull(tokenCache);
	}

	/**
	 * Creates a token table that has a period of timeout session. The token
	 * table will have a periodical clean up activity that will remove idle
	 * tokens (e.g., due to inactive user interactions)
	 *
	 * @param timeoutInMilliseconds
	 *            A timeout interval that decides user session period (in
	 *            milliseconds)
	 * @return A token table
	 */
	public static TokenTable create(long timeoutInMilliseconds) {
		Cache<String, AuthToken> tokenCache = CacheBuilder.newBuilder()
				.expireAfterAccess(timeoutInMilliseconds, TimeUnit.MILLISECONDS)
				.removalListener((RemovalNotification<String, AuthToken> notification) -> {
						AuthToken authToken = notification.getValue();
						logger.info(String.format(
								"Unregister %s (%s) from the server due to inactivity",
								authToken.getUser().getId().get(),
								authToken.getUser().getName().get()));
				}).build();
		createTokenCleanupThread(timeoutInMilliseconds, tokenCache);
		return new TokenTable(tokenCache);
	}

	/**
	 * Puts user token with the key string into the token table.
	 *
	 * @param key
	 *            User credential string to retrieve the user token
	 * @param token
	 *            The user token.
	 */
	public synchronized void put(@Nonnull String key, @Nonnull AuthToken token) {
		checkNotNull(key);
		checkNotNull(token);
		tokenCache.put(key, token);
	}

	/**
	 * Gets the user token given the key string.
	 *
	 * @param key
	 *            User credential string to retrieve the user token
	 * @return The user token
	 * @throws LoginTimeoutException
	 *             If the user token is expired
	 */
	@Nonnull
	public AuthToken get(String key) throws LoginTimeoutException {
		AuthToken token = tokenCache.getIfPresent(key);
		if (token == null) {
			throw new LoginTimeoutException();
		}
		return token;
	}

	/**
	 * Gets the number of current active user tokens.
	 *
	 * @return The number of current active user tokens
	 */
	public int size() {
		return (int) tokenCache.size();
	}

	private static void createTokenCleanupThread(long timeout, Cache<String, AuthToken> tokenCache) {
		final ScheduledExecutorService executorService = Executors
				.newSingleThreadScheduledExecutor(r -> {
					Thread th = new Thread(r, "User Token Maintainer Thread");
					th.setDaemon(false);
					return th;
				});
		executorService.scheduleAtFixedRate(() -> {
			if (tokenCache.size() > 0) {
				tokenCache.cleanUp();
			}
		} , timeout, timeout, TimeUnit.MILLISECONDS);
	}
}
