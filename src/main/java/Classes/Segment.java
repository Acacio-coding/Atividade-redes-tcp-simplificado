package Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Segment {

    private final int originPort;
    private final int destinationPort;
    private int number;
    private boolean syn;
    private boolean fin;
    private boolean ack;
    private boolean nack;
    private boolean corrupted;
    private final Timer timer;

    public Segment(int originPort, int destinationPort, int number, Timer timer) {
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.number = number;
        this.timer = timer;
        this.syn = false;
        this.fin = false;
        this.ack = false;
        this.nack = false;
        this.corrupted = false;
    }
}
