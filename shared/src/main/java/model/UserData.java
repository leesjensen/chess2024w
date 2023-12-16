package model;

import com.google.gson.Gson;

/**
 * A User represents a player or observer.
 */
public record UserData(String username, String password, String email) {

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
