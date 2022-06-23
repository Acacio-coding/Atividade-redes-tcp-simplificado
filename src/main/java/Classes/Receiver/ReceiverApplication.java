package Classes.Receiver;

import Classes.Application;
import Classes.Package.DataSegment;
import Classes.Segment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
Aplicação receptora contendo o transporte a um array com os segmentos ordenados
do buffer de entrada do transporte e a mensagem final (StringBuilder)
 */
public class ReceiverApplication extends Application {

    private final ReceiverTransport transport;
    private char[] orderedSegments;
    private final StringBuilder message;

    public ReceiverApplication(int port, ReceiverTransport transport, int messageLength) {
        super(port);
        this.transport = transport;
        this.orderedSegments = new char[messageLength];
        this.message = new StringBuilder();
    }

    public void addSegmentToOrderedArray(Segment part) {
        orderedSegments[part.getNumber()] = ((DataSegment) part).getData();
    }

    public String getFinalMessage() {
        for (char orderedSegment : orderedSegments) {
            message.append(orderedSegment);
        }

        return String.valueOf(message);
    }
}
