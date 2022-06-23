package Classes.Package;

import Classes.Segment;
import Classes.Timer;
import lombok.Getter;

@Getter
//Classe que extende Segment. Segmento que contém um dado da mensagem
public class DataSegment extends Segment {

    //Número esperado do pacote de resposta ack/nack
    private final int expectedResponseNumber;

    //Dado da mensagem
    private final char data;

    public DataSegment(int originPort, int destinationPort, int number, int expectedResponseNumber, char data,
                       Timer timer) {
        super(originPort, destinationPort, number, timer);
        this.data = data;
        this.expectedResponseNumber = expectedResponseNumber;
    }
}
