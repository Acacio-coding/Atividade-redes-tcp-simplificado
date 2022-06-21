package Classes;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
public abstract class Transport extends Thread {

    private final Communication communication;
    private final Queue<Segment> exitBuffer;
    private final Queue<Segment> entryBuffer;
    private final int port;

    public Transport(Communication communication, int port) {
        this.communication = communication;
        this.exitBuffer = new LinkedBlockingDeque<>();
        this.entryBuffer = new LinkedBlockingDeque<>();
        this.port = port;
    }
}
