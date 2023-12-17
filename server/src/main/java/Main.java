import server.Server;
import util.AppConfig;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Server");
        var port = new Server().run(AppConfig.props.httpPort());
        System.out.printf("Running server on port %d\n", port);
    }

}