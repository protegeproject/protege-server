package org.protege.owl.server.api_new;

import com.google.common.base.Optional;
import org.protege.owl.server.api.User;

import java.util.Set;

/**
 * A manager for users and user details
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface UserManager {

    /**
     * Get all users
     *
     * @return Set of all users
     */
    Set<User> getUsers();

    /**
     * Get the user with the specified username
     *
     * @param username  Username
     * @return User instance
     */
    User getUser(String username);

    /**
     * Set the username of a given user
     *
     * @param user  User instance
     * @param username  Username
     */
    void setUsername(User user, String username);

    /**
     * Get the email of a user
     *
     * @param user  User instance
     * @return Email address
     */
    Optional<String> getEmail(User user);

    /**
     * Get the email of a user
     *
     * @param username  Username
     * @return Email address
     */
    Optional<String> getEmail(String username);

    /**
     * Set the email address of a user
     *
     * @param user  User instance
     * @param email Email address
     */
    void setEmail(User user, String email);

    /**
     * Set the email address of a user
     *
     * @param username  Username
     * @param email Email address
     */
    void setEmail(String username, String email);

}
