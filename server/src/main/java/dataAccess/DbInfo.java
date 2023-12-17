package dataAccess;

public record DbInfo(
        String databaseName,
        String username,
        String password,
        String connectionUrl) {
}
