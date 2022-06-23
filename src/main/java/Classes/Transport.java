package Classes;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

//Classe abstrata de Transport que contem um Thread
@Getter
public abstract class Transport extends Thread {

    //Contendo a comunicação, entrada, saida e sua porta
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
