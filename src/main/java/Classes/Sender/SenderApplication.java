package Classes.Sender;

import Classes.Application;
import lombok.Getter;

//Aplicação contendo o payload e o transporte
@Getter
public class SenderApplication extends Application {

    private final String payload;
    private final SenderTransport transport;

    public SenderApplication(int port, String payload, SenderTransport transport) {
        super(port);
        this.payload = payload;
        this.transport = transport;
    }
}
