package service;


import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import util.CodedException;

/**
 * Provides endpoints for authorizing access.
 */
public class AuthService {

    final private DataAccess dataAccess;
    final private BCryptPasswordEncoder encoder;

    public AuthService(DataAccess dataAccess) {
        encoder = new BCryptPasswordEncoder();
        this.dataAccess = dataAccess;
    }

    /**
     * Create a session for a user. If the user already has a session then
     * the previous session is invalidated.
     *
     * @param user to create a session for.
     * @return the authToken for the session.
     */
    public AuthData createSession(UserData user) throws CodedException {
        try {
            UserData storedUser = dataAccess.readUser(user.username());
            if (storedUser != null) {
                if (encoder.matches(user.password(), storedUser.password())) {
                    return dataAccess.writeAuth(storedUser.username());
                }
            }
            throw new CodedException(401, "Invalid username or password");
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error");
        }
    }

    /**
     * Deletes a user's session. If the token is not valid then no error is generated.
     *
     * @param authToken that currently represents a user.
     */
    public void deleteSession(String authToken) throws CodedException {
        try {
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error");
        }
    }

    public AuthData getAuthData(String authToken) throws CodedException {
        try {
            return dataAccess.readAuth(authToken);
        } catch (DataAccessException ignored) {
            throw new CodedException(500, "Internal server error");
        }
    }
}
