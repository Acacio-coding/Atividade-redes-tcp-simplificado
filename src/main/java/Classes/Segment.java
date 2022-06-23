package Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Classe Abstrata de um segmento com variáveis em comum para os 2 tipos de segmento possíveis
public abstract class Segment {

    private final int originPort;
    private final int destinationPort;
    private int number;
    private boolean ack;
    private boolean nack;
    private boolean corrupted;

    //Timer é uma classe com uma Thread referente ao Timeout do pacote
    private final Timer timer;

    public Segment(int originPort, int destinationPort, int number, Timer timer) {
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.number = number;
        this.timer = timer;
        this.ack = false;
        this.nack = false;
        this.corrupted = false;
    }
}
