package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import spark.utils.StringUtils;
import util.CodedException;

/**
 * Provides endpoints for registering a user.
 */
public class UserService {
    final private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    /**
     * Persistently registerUser.
     *
     * @param user to add.
     * @return the authorization information for the new user.
     * @throws CodedException if a user with the same username already exists
     */
    public AuthData registerUser(UserData user) throws CodedException {
        if (StringUtils.isEmpty(user.username())) throw new CodedException(400, "missing username");
        if (StringUtils.isEmpty(user.password())) throw new CodedException(400, "missing password");

        try {
            user = dataAccess.writeUser(user);
            return dataAccess.writeAuth(user.username());
        } catch (DataAccessException ex) {
            throw new CodedException(403, "Unable to register user");
        }
    }
}
