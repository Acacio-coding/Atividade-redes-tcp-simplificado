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
                "teste",
                senderTransport);

        //Criando receptor
        ReceiverApplication receiver = new ReceiverApplication(
                1002,
                receiverTransport,
                sender.getPayload().length());

        //Enviando mensagem
        sender.getTransport().doSegmentation(sender.getPayload(), sender.getPort(), receiver.getPort());

        //Inicializando as Threads do transporte
        senderTransport.start();
        receiverTransport.start();

        //Verifica constantemente se o tamanho do pacote está correto, se não tem nada corrompido
        //e caso tudo esteja certo ele faz a Demultiplexação, retorna a mensagem recebida e por fim
        //Finaliza a execução do while
        while (true) {
            if (receiver.getTransport().getEntryBuffer().size() == sender.getPayload().length()
                    && sender.getTransport().getEntryBuffer().size() == sender.getPayload().length()

                    && receiver.getTransport().getEntryBuffer()
                    .stream().noneMatch(segment -> segment.isCorrupted() && (segment.getNumber() == -1))

                    && sender.getTransport().getEntryBuffer()
                    .stream().noneMatch(segment -> segment.isCorrupted() && (segment.getNumber() == -1))
            ) {
                for (Segment segment : receiver.getTransport().getEntryBuffer()) {
                    if (segment instanceof DataSegment) {
                        receiver.addSegmentToOrderedArray(segment);
                    }
                }

                System.out.println("\nMessage received: " + receiver.getFinalMessage() + "\n");
                break;
            }
        }

        //Parando as Threads
        senderTransport.setStop(true);
        receiverTransport.setStop(true);
    }
}
