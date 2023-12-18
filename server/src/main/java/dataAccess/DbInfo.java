package dataAccess;

import java.sql.*;

public class DbInfo {
    private static final String host = "localhost";
    private static final int port = 3306;
    private static final String user = "root";
    private static final String password = "monkeypie";

    /**
     * Create a connection to the database. Connections to the database should be
     * short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     *
     * @param databaseName the database catalog you want to use. If null, then the
     *                     catalog is not set.
     */
    static Connection getConnection(String databaseName) throws DataAccessException {
        try {
            var connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);

            var connection = DriverManager.getConnection(connectionUrl, user, password);
            if (databaseName != null) {
                connection.setCatalog(databaseName);
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
