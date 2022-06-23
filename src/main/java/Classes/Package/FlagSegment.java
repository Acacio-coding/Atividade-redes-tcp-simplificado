package Classes.Package;

import Classes.Segment;
import Classes.Timer;

//Classe que extende Segment. Segmento que contem apenas flags
public class FlagSegment extends Segment {

    public FlagSegment(int originPort, int destinationPort, int number, Timer timer) {
        super(originPort, destinationPort, number, timer);
    }
}
