package service;


import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import util.CodedException;

/**
 * Provides endpoints for administrating the application.
 */
public class AdminService {

    private final DataAccess dataAccess;

    public AdminService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Clears the database. Removes all users, games, and authTokens. This is only
     * useful for testing purposes. In production this endpoint should never be
     * called.
     */
    public void clearApplication() throws CodedException {
        try {
            dataAccess.clear();
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }
}
