package Classes.Receiver;

import Classes.Application;
import Classes.Package.DataSegment;
import Classes.Segment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
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
        orderedSegments[((DataSegment) part).getExpectedResponseNumber() -1] = ((DataSegment) part).getData();
    }

    public String getFinalMessage() {
        for (char orderedSegment : orderedSegments) {
            message.append(orderedSegment);
        }

        return String.valueOf(message);
    }
}
