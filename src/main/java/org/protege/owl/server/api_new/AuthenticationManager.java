package org.protege.owl.server.api_new;

import com.google.common.base.Optional;
import org.protege.owl.server.api.User;

/**
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface AuthenticationManager {

    /**
     * Register a user in the user registry
     *
     * @param user  User instance
     * @param email Email address
     * @param password  Password
     * @param salt  Password salt
     */
    void registerUser(User user, String email, String password, String salt);

    /**
     * Change password of a specified user to the given password
     *
     * @param userName  Username
     * @param password  New password
     */
    void changePassword(String userName, String password);

    /**
     * Check whether there exists a tuple consisting of the given username and password in the user registry
     *
     * @param userName  Username
     * @param password  Password
     * @return true if the credentials are valid, false otherwise
     */
    boolean hasValidCredentials(String userName, String password);

    /**
     * Get the salt data used for the given user's password hashing
     *
     * @param user  User instance
     * @return Salt used in the given user's password hashing
     */
    Optional<String> getSalt(User user);

}
