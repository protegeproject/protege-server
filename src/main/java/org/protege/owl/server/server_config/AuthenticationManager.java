package org.protege.owl.server.server_config;

import com.google.common.base.Optional;
import org.protege.owl.server.api.User;

/**
 * A manager for everything authentication-related
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface AuthenticationManager {

    /**
     * Register a user in the user registry
     *
     * @param username  Username
     * @param email Email address
     * @param password  Password
     * @param salt  Password salt
     */
    void addUser(String username, String email, String password, String salt);

    /**
     * Remove user from user registry
     *
     * @param username  Username
     */
    void removeUser(String username);

    /**
     * Remove user from user registry
     *
     * @param user  User instance
     */
    void removeUser(User user);

    /**
     * Change password of a specified user to the given password
     *
     * @param username  Username
     * @param password  New password
     */
    void changePassword(String username, String password);

    /**
     * Send a password reminder to specified email
     *
     * @param username  Username
     * @param email Email address
     */
    void remindPassword(String username, String email);

    /**
     * Check whether there exists a tuple consisting of the given username and password in the user registry
     *
     * @param username  Username
     * @param password  Password
     * @return true if the credentials are valid, false otherwise
     */
    boolean hasValidCredentials(String username, String password);

    /**
     * Get the salt data used for the given user's password hashing
     *
     * @param user  User instance
     * @return Salt used in the given user's password hashing
     */
    Optional<String> getSalt(User user);

}
