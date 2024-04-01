import ui.ChessClient;

import java.util.Scanner;

import static util.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        try {
            var serverName = args.length > 0 ? args[0] : "localhost:8080";

            ChessClient client = new ChessClient(serverName);
            System.out.println("👑 Welcome to 240 chess. Type Help to get started. 👑");
            Scanner scanner = new Scanner(System.in);

            var result = "";
            while (!result.equals("quit")) {
                client.printPrompt();
                String input = scanner.nextLine();

                try {
                    result = client.eval(input);
                    System.out.print(RESET_TEXT_COLOR + result);
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to connect to the server");
        }
    }
}