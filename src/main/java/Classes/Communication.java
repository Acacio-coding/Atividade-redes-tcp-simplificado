package Classes;

import Classes.Package.DataSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
@Setter
public class Communication {

    //Estrutura de dado compartilhada entre as Threads
    private Queue<Segment> segments = new LinkedBlockingDeque<>();

    //Para gerar um número randômico e corromper um pacote
    private Random random = new Random();

    //Variável de controle para corromper pacote
    private boolean drawedNumber = false;

    //Número válido do pacote corrompido se corromper o número de sequência
    private int validSegmentNumber;

    //Número de pacote que vai ser corrompido
    private int toCorruptNumber;

    //Número máximo de acordo com o tamanho da mensagem
    private int MAXNUMBER;

    private boolean corruptedOnce = false;

    public synchronized Queue<Segment> receiveSegment() {
        // Itera entre os segmentos na queue e remove caso o timer tenha finalizado
        for (Segment segment : segments) {
            if (segments.removeIf(current -> current.getTimer().isTimerEnded()
                    && !current.getTimer().isWasRemovedOnce() && current == segment)) {
                segment.getTimer().setWasRemovedOnce(true);
            }
        }

        return segments;
    }

    public synchronized void sendSegment(Segment segment) throws InterruptedException {
        if (!this.segments.contains(segment)) {
            if (!drawedNumber) {
                toCorruptNumber = random.nextInt(MAXNUMBER);

                //Seta a variável de controle de sorteio de número para true para não sortar novamente
                setDrawedNumber(true);
            } else if (segment.getNumber() == toCorruptNumber && !corruptedOnce) {
                //Corrompe o segmento
                segment.setCorrupted(true);
                setValidSegmentNumber(toCorruptNumber);

                System.err.println("\n\n\n\n\nCorrompendo PACOTE: " + segment.getNumber() + "\n\n\n\n\n");

                //Corrompe o número de sequência
                if (random.nextInt(1, 3) == 1) {
                    segment.setNumber(-1);
                }

                //Seta a variável de controle de pacote corrompido para true evitando corromper novamente
                setCorruptedOnce(true);

                //Adiciona o segmento na queue
                this.segments.add(segment);
            } else {
                //Descorrompe o segmento e/ou seta o número válido do segmento
                if (segment.isCorrupted()) {
                    segment.setCorrupted(false);
                } else if (segment.getNumber() == -1) {
                    if (segment instanceof DataSegment) {
                        segment.setNumber(validSegmentNumber);
                    } else {
                        segment.setNumber(validSegmentNumber + 1);
                    }
                } else {
                    //Adiciona o segmento na queue
                    this.segments.add(segment);
                }
            }
        }
    }
}
