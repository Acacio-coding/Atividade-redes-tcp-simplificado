package Classes.Sender;

import Classes.*;
import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
Transporte emissor
 */
public class SenderTransport extends Transport {

    //Variável de controle para finalizar a Thread
    public boolean stop;

    public SenderTransport(Communication communication, int port) {
        super(communication, port);
        stop = false;
    }

    /*
    Segmentação da mensagem em pacotes.
    Para cada letra na String é criado um novo pacote de instância DataSegment, contendo:
    Porta, destino, contador, numero de resposta esperada, conteudo e o timer do timeout
    */
    public void doSegmentation(String message, int senderPort, int receiverPort) {
        for (int i = 0; i < message.length(); i++) {
            this.getExitBuffer().add(new DataSegment(senderPort, receiverPort,
                    i, (i + 1), message.toCharArray()[i], new Timer()));
        }
    }

    @Override
    public void run() {
        while (!stop) {
            //Recepção dos pacotes destinados a esse transporte
            this.getCommunication().receiveSegment().forEach(currentReceived -> {
                /*
                Para cada segmento é verificado se a porta é igual a da aplição
                e se já não contém na buffer de entrada
                 */
                if (currentReceived.getDestinationPort() == this.getPort()
                        && !this.getEntryBuffer().contains(currentReceived)) {
                    /*
                    Adiciona no buffer de entrada, para o timer do pacote e seta a variável que evita remoção
                    da comunicação para true
                     */
                    this.getEntryBuffer().add(currentReceived);
                    currentReceived.getTimer().setStop(true);
                    currentReceived.getTimer().setWasRemovedOnce(true);
                    System.out.println("\nSender ---------------------------");
                    System.out.println("Received package number: " + currentReceived.getNumber());
                    System.out.println("Type: " + currentReceived.getClass().getSimpleName());
                    System.out.println("Related to package number: " + (currentReceived.getNumber() - 1));

                    if (currentReceived.isAck()) {
                        System.out.println("Flags: ");
                        System.out.println("ACK: " + currentReceived.isAck());
                    }

                    if (currentReceived.isNack()) {
                        System.out.println("Flags: ");
                        System.out.println("NACK: " + currentReceived.isNack());
                    }
                }
            });

            //Verifica se o buffer de entrada possuí algum segmento
            if (this.getEntryBuffer().size() > 0) {
                for (Segment segment : this.getEntryBuffer()) {
                    if (segment instanceof FlagSegment) {

                        //Caso seja Ack remove do buffer de saída o segmento de data referente
                        if (segment.isAck()) {
                            this.getExitBuffer().removeIf(current -> current instanceof DataSegment &&
                                    ((DataSegment) current).getExpectedResponseNumber() == segment.getNumber());
                        } else if (segment.isNack()) {
                            //Remove o pacote com o nack da comunicação
                            this.getCommunication().getSegments()
                                    .removeIf(current -> current instanceof FlagSegment
                                            && current.isNack()
                                            && current.getNumber() == segment.getNumber());

                            //Remove o nack do buffer de entrada
                            this.getEntryBuffer().remove(segment);
                        }
                    }
                }
            }

            //Verifica se o buffer de saída possuí algum segmento
            if (this.getExitBuffer().size() > 0) {
                for (Segment segment : this.getExitBuffer()) {
                    //Verifica se não existe o segmento na comunicação
                    if (!this.getCommunication().getSegments().contains(segment)) {
                        try {
                            //Verifica se o timer já não foi iniciado para inicia-lo
                            if (!segment.getTimer().isStop() && !segment.getTimer().isAlive()) {
                                segment.getTimer().start();
                            }

                            //Enviando pacote para a comunicação
                            this.getCommunication().sendSegment(segment);
                            System.out.println("\nSender ---------------------------");
                            System.out.println("Sending package number: " + segment.getNumber());
                            System.out.println("Type: " + segment.getClass().getSimpleName());
                            System.out.println("Data: " + ((DataSegment) segment).getData());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
