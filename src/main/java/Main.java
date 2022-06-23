import Classes.Communication;
import Classes.Package.DataSegment;
import Classes.Receiver.ReceiverApplication;
import Classes.Receiver.ReceiverTransport;
import Classes.Segment;
import Classes.Sender.SenderApplication;
import Classes.Sender.SenderTransport;

public class Main {

    public static void main(String[] args) {
        //Criando comunicação
        Communication communication = new Communication();

        //Criando transporte Emissor e receptor contendo o mesmo meio de comunicação e as portas
        SenderTransport senderTransport = new SenderTransport(communication,1000);
        ReceiverTransport receiverTransport = new ReceiverTransport(communication,1002);

        //Criando Aplicação com mensagem
        SenderApplication sender = new SenderApplication(
                1000,
                """
                        Mussum Ipsum, cacilds vidis litro abertis. Nec orci ornare consequat. Praesent lacinia ultrices consectetur. Sed non ipsum felis.Diuretics paradis num copo é motivis de denguis.Suco de cevadiss, é um leite divinis, qui tem lupuliz, matis, aguis e fermentis.Per aumento de cachacis, eu reclamis.
                        Todo mundo vê os porris que eu tomo, mas ninguém vê os tombis que eu levo!Pra lá , depois divoltis porris, paradis.Atirei o pau no gatis, per gatis num morreus.Praesent malesuada urna nisi, quis volutpat erat hendrerit non. Nam vulputate dapibus.""",
                senderTransport);

        //Criando receptor
        ReceiverApplication receiver = new ReceiverApplication(
                1002,
                receiverTransport,
                sender.getPayload().length());

        //Realizando segmentação
        sender.getTransport().doSegmentation(sender.getPayload(), sender.getPort(), receiver.getPort());

        //Inicializando as Threads do transporte e enviando mensagem
        senderTransport.start();
        receiverTransport.start();

        /*
        Verifica constantemente se o tamanho do pacote está correto, se não tem nada corrompido
        e caso tudo esteja certo retorna a mensagem recebida e por fim
        Finaliza a execução do while.
        */
        while (true) {
            if (receiver.getTransport().getEntryBuffer().size() == sender.getPayload().length()
                    && sender.getTransport().getEntryBuffer().size() == sender.getPayload().length()

                    && receiver.getTransport().getEntryBuffer()
                    .stream().noneMatch(segment -> segment.isCorrupted() && segment.getNumber() == -1)

                    && sender.getTransport().getEntryBuffer()
                    .stream().noneMatch(segment -> segment.isCorrupted() && segment.getNumber() == -1)
            ) {
                //Reorganiza a ordem dos segmentos
                for (Segment segment : receiver.getTransport().getEntryBuffer()) {
                    if (segment instanceof DataSegment) {
                        //Faz o append na string da mensagem
                        receiver.addSegmentToOrderedArray(segment);
                    }
                }

                //Printa a mensagem
                System.out.println("\nMessage received: " + receiver.getFinalMessage() + "\n");
                break;
            }
        }

        //Finaliza as Threads do transporte
        senderTransport.setStop(true);
        receiverTransport.setStop(true);
    }
}
