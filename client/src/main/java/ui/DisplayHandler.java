package ui;

import model.GameData;

public interface DisplayHandler {
    void updateBoard(GameData game);

    void message(String message);

    void error(String message);
}
