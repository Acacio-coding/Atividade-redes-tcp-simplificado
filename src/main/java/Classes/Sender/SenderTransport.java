package Classes.Sender;

import Classes.*;
import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import lombok.Getter;
import lombok.Setter;

//Transporte de envio extendendo transport
@Getter
@Setter
public class SenderTransport extends Transport {

    //Contador de pacotes
    private int packageCounter;

    //Controlador do while da thread
    public boolean stop;

    public SenderTransport(Communication communication, int port) {
        super(communication, port);
        packageCounter = 0;
        stop = false;
    }

    //Segmentação da mensagem em pacotes. 
    //Para cada letra na String é criado um novo pacote contendo:
    //Porta, destino, contador, numero de resposta esperada, conteudo e o timer do timeout
    public void doSegmentation(String message, int senderPort, int receiverPort) {
        for (int i = 0; i < message.length(); i++) {
            this.getExitBuffer().add(new DataSegment(senderPort, receiverPort,
                    packageCounter, (packageCounter + 1), message.toCharArray()[i], new Timer()));

            //Adicionando +1 ao contador do pacote
            setPackageCounter((packageCounter + 1));
        }
    }

    //Thread que rodará desde o inicio da aplicação
    @Override
    public void run() {
        while (!stop) {

            //Primeiro recebe os segmentos da comunicação (caso tenha)
            this.getCommunication().receiveSegment().forEach(currentReceived -> {

                //Para cada segmento é verificado se a porta é igual a da aplição
                //e se já não contem na buffer de entrada
                if (currentReceived.getDestinationPort() == this.getPort()
                        && !this.getEntryBuffer().contains(currentReceived)) {

                    //Adiciona no buffer de entrada e faz o Log
                    this.getEntryBuffer().add(currentReceived);
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

            //Verifica se o buffer de entrada algum segmento
            if (this.getEntryBuffer().size() > 0) {

                //Para cada segmento
                for (Segment segment : this.getEntryBuffer()) {
                    
                    //É verificado seu numero e se está corrompido
                    if (segment.isCorrupted() || segment.getNumber() == -1) {

                        //Removendo da comunicação
                        this.getCommunication().getSegments()
                                .removeIf(current -> current instanceof FlagSegment
                                        && current.getNumber() == segment.getNumber()
                                        && current.isCorrupted());

                        //Removendo segmento do buffer de entrada
                        this.getEntryBuffer().remove(segment);
                    } else {
                        
                        //Se for segmento de flag
                        if (segment instanceof FlagSegment) {

                            //Caso seja Ack remove do buffer de saida o segmento de data referente
                            if (segment.isAck()) {
                                this.getExitBuffer().removeIf(current ->
                                        ((DataSegment) current).getExpectedResponseNumber() == segment.getNumber());
                            }

                            //Caso seja Nack 
                            if (segment.isNack() || segment.getNumber() == 0) {

                                //Remove o pacote de data da Comunicação
                                this.getCommunication().getSegments()
                                        .removeIf(current -> current instanceof DataSegment
                                                && ((DataSegment) current)
                                                .getExpectedResponseNumber() == segment.getNumber()
                                                && current.isCorrupted());

                                //Remove a flag de nack da comunicação
                                this.getCommunication().getSegments()
                                        .removeIf(current -> current instanceof FlagSegment
                                                && current.isNack()
                                                && current.getNumber() == segment.getNumber());
                            }
                        }
                    }
                }
            }

            // Verifica se tem algo no buffer de saida
            if (this.getExitBuffer().size() > 0) {

                //Para cada Segmento
                for (Segment segment : this.getExitBuffer()) {

                    //Verifica se não existe o segmento na comunicação
                    if (!this.getCommunication().getSegments().contains(segment)) {
                        
                        //Verifica a Thread do Timer e inicia
                        try {
                            if (!segment.getTimer().isTimerEnded() && !segment.getTimer().isAlive()) {
                                segment.getTimer().start();
                            }

                            //Enviando pacote para a comunicação com o timer iniciado e fazendo o Log
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
