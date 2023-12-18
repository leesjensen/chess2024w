import server.Server;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Server");
        var port = new Server().run(8080);
        System.out.printf("Running server on port %d\n", port);
    }

}