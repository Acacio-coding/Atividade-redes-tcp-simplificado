package Classes.Package;

import Classes.Segment;
import Classes.Timer;
import lombok.Getter;

//Classe que extende Segment. Expecialização para transferencia de dados
@Getter
public class DataSegment extends Segment {

    private final int expectedResponseNumber;
    private final char data;

    public DataSegment(int originPort, int destinationPort, int number, int expectedResponseNumber, char data,
                       Timer timer) {
        super(originPort, destinationPort, number, timer);
        this.data = data;
        this.expectedResponseNumber = expectedResponseNumber;
    }
}
