package Classes.Receiver;

import Classes.Communication;
import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import Classes.Segment;
import Classes.Timer;
import Classes.Transport;
import lombok.Getter;
import lombok.Setter;

//Extendendo a classe de Transporte
@Getter
@Setter
public class ReceiverTransport extends Transport {

    //Controlador da thread
    public boolean stop = false;

    public ReceiverTransport(Communication communication, int port) {
        super(communication, port);
    }

    //Thread que ficará em loop por toda a execução
    @Override
    public void run() {
        while(!stop) {

            //Recebendo todos os segmentos da comunicação (caso tenha)
            this.getCommunication().receiveSegment().forEach(currentReceived -> {

                //Verificando se a porta de destino do pacote é igual a porta do transporte
                //e se esse pacote já não foi adicionado no buffer de entrada
                if (currentReceived.getDestinationPort() == this.getPort()
                        && !this.getEntryBuffer().contains(currentReceived)) {
                    
                    //Adicionando no buffer de entrada e fazendo o Log
                    this.getEntryBuffer().add(currentReceived);
                    System.out.println("\nReceiver ---------------------------");
                    System.out.println("Received package number: " + currentReceived.getNumber());
                    System.out.println("Type: " + currentReceived.getClass().getSimpleName());
                    System.out.println("Data: " + ((DataSegment) currentReceived).getData());
                }
            });

            //Verificando se tem algo no buffer de entrada
            if (this.getEntryBuffer().size() > 0) {

                //Para cada
                for (Segment segment : this.getEntryBuffer()) {

                    //Se for segmento de Data
                    if (segment instanceof DataSegment) {

                        //Cria o esqueleto de um Segmento Flag que será enviado para a mesma porta do transmissor
                        Segment toSend = new FlagSegment(this.getPort(),
                                segment.getOriginPort(), (segment.getNumber() + 1), new Timer());

                        //Filtrando os segmentos da comunicação
                        if (this.getCommunication().getSegments()
                                .stream()
                                .filter(current -> current instanceof FlagSegment)
                                .noneMatch(current -> current.getNumber() == toSend.getNumber())) {

                            //Se o Segmento não está corrompido e o numero não for negativo
                            if (!segment.isCorrupted() && segment.getNumber() != -1) {
                                toSend.setAck(true);

                                //Remove do buffer de saida a flag correspondente
                                this.getExitBuffer()
                                        .removeIf(current ->
                                                current instanceof FlagSegment
                                                        && current.isNack()
                                                        && current.getNumber() == (segment.getNumber() + 1));

                                //Adicionando flag alterada no buffer de saida
                                this.getExitBuffer().add(toSend);
                            } else {

                                //Settando a flag com Nack, adicionando no buffer de saida e removendo da entrada
                                toSend.setNack(true);
                                this.getExitBuffer().add(toSend);
                                this.getEntryBuffer().remove(segment);
                            }
                        }
                    }
                }
            }

            //Verifica se tem algo no buffer de saida
            if (this.getExitBuffer().size() > 0) {

                //Para cada
                for (Segment segment : this.getExitBuffer()) {

                    //Verifica se o segmento não está na comunicação
                    if (!this.getCommunication().getSegments().contains(segment)) {

                        try {
                            //Inicializando o timer do pacote
                            if (!segment.getTimer().isTimerEnded() && !segment.getTimer().isAlive()) {
                                segment.getTimer().start();
                            }

                            //Fazendo o log
                            System.out.println("\nReceiver ---------------------------");
                            System.out.println("Sending package number: " + segment.getNumber());
                            System.out.println("Type: " + segment.getClass().getSimpleName());
                            System.out.println("Related to package number: " + (segment.getNumber() - 1));

                            if (segment.isAck()) {
                                System.out.println("Flags: ");
                                System.out.println("ACK: " + segment.isAck());
                            }

                            if (segment.isNack()) {
                                System.out.println("Flags: ");
                                System.out.println("NACK: " + segment.isNack());
                            }

                            //enviando Semento para comunicação
                            this.getCommunication().sendSegment(segment);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
