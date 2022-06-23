package Classes.Receiver;

import Classes.Communication;
import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import Classes.Segment;
import Classes.Timer;
import Classes.Transport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
Transporte receptor
 */
public class ReceiverTransport extends Transport {

    //Variável de controle para finalizar a Thread
    public boolean stop = false;

    public ReceiverTransport(Communication communication, int port) {
        super(communication, port);
    }

    @Override
    public void run() {
        while(!stop) {
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
                    System.out.println("\nReceiver ---------------------------");
                    System.out.println("Received package number: " + currentReceived.getNumber());
                    System.out.println("Type: " + currentReceived.getClass().getSimpleName());
                    System.out.println("Data: " + ((DataSegment) currentReceived).getData());
                }
            });

            //Verifica se o buffer de entrada possuí algum segmento
            if (this.getEntryBuffer().size() > 0) {
                for (Segment segment : this.getEntryBuffer()) {
                    if (segment instanceof DataSegment) {
                        /*
                        Instancia uma novo segmento de resposta e seta o número deste para o número do
                        segmento de dado recebido + 1, ou caso esse número seja -1 seta o número do segmento
                        de resposta para -1 também
                         */
                        Segment toSend = new FlagSegment(this.getPort(),
                                segment.getOriginPort(),
                                segment.getNumber() == -1 ? -1 : (segment.getNumber() + 1), new Timer());

                        //Verifica se não existe nenhum pacote com número igual ao que está prestes a ser enviado
                        if (this.getCommunication().getSegments()
                                .stream()
                                .filter(current -> current instanceof FlagSegment)
                                .noneMatch(current -> current.getNumber() == toSend.getNumber())) {

                            //Verifica se o pacote não está corrompido ou com o número de sequência comprometido
                            if (!segment.isCorrupted() && segment.getNumber() != -1) {
                                toSend.setAck(true);

                                /*
                                Verifica se existe algum pacote NACK no buffer de saída para o pacote atual
                                e remove se for verdadeiro
                                 */
                                this.getExitBuffer()
                                        .removeIf(current ->
                                                current instanceof FlagSegment
                                                        && current.isNack()
                                                        && current.getNumber() == (segment.getNumber() + 1)
                                                        || current.getNumber() == -1);

                                //Adiciona o pacote de resposta com ACK no buffer de saída
                                this.getExitBuffer().add(toSend);
                            } else {
                                toSend.setNack(true);

                                //Adiciona o pacote de resposta com NACK no buffer de saída
                                this.getExitBuffer().add(toSend);

                                //Remove o pacote corrompido do meio de comunicação
                                this.getCommunication().getSegments()
                                        .removeIf(current ->
                                                current.getNumber() == segment.getNumber() && current.isCorrupted());

                                //Remove o pacote corrompido do buffer de entrada
                                this.getEntryBuffer().remove(segment);
                            }
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
                            System.out.println("\nReceiver ---------------------------");
                            System.out.println("Sending package number: " + segment.getNumber());
                            System.out.println("Type: " + segment.getClass().getSimpleName());
                            System.out.println("Related to package number: "
                                    + (segment.getNumber() == -1 ? -1 : segment.getNumber() - 1));

                            this.getCommunication().sendSegment(segment);

                            //Remove do buffer de saída caso seja um pacote com ACK
                            if (segment.isAck()) {
                                System.out.println("Flags: ");
                                System.out.println("ACK: " + segment.isAck());
                                this.getExitBuffer().remove(segment);
                            }

                            if (segment.isNack()) {
                                System.out.println("Flags: ");
                                System.out.println("NACK: " + segment.isNack());
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
