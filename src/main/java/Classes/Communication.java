package Classes;

import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
@Setter
public class Communication {

    //Lista tipo LinkedBlockingDeque
    private Queue<Segment> segments = new LinkedBlockingDeque<>();

    //Random para criar um numero aleatório
    private Random random = new Random();

    //Flag para deixar apenas um pacote corromper
    private boolean corruptedOnce = false;
    
    //Numero de segmento Válido
    private int validSegmentNumber;

    //Função syncronized para receber Segmentos
    public synchronized Queue<Segment> receiveSegment() {

        //Percorre os segmentos verificando o timer e settando ele como removido
        for (Segment segment : segments) {
            segments.removeIf(current -> current.getTimer().isTimerEnded()
                    && !current.getTimer().isWasRemoveOnce() && current == segment);
            segment.getTimer().setWasRemoveOnce(true);
        }
        //Retornando Segmentos
        return segments;
    }

    //Função synchronized para enviar segmentos
    public synchronized void sendSegment(Segment segment) throws InterruptedException {
        //Verifica se o pacote não está na fila
        if (!this.segments.contains(segment)) {

            //Verifica se algum pacote já foi corrompido
            if (!corruptedOnce) {
                setValidSegmentNumber(segment.getNumber());

                //Cria um numero aleatorio para ver se vai corromper
                if (random.nextInt(100) > 50) {
                    segment.setCorrupted(true);
                } else {
                    segment.setCorrupted(true);
                    segment.setNumber(-1);
                }

                //Settando para não corromper mais e adicionando segmento
                setCorruptedOnce(true);
                this.segments.add(segment);
            } else {
                if (segment.getNumber() == -1) {
                    segment.setNumber(validSegmentNumber);
                } else if (segment instanceof FlagSegment && segment.getNumber() == 0) {
                    segment.setNumber((validSegmentNumber + 1));
                }

                //Se o pacote já vier corrompido ele apenas desmarca se não adiciona nos segmentos
                if (segment.isCorrupted()) {
                    segment.setCorrupted(false);
                } else {
                    this.segments.add(segment);
                }
            }
        }
    }
}
