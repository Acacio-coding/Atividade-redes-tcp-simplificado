import Classes.Communication;
import Classes.Package.DataSegment;
import Classes.Receiver.ReceiverApplication;
import Classes.Receiver.ReceiverTransport;
import Classes.Segment;
import Classes.Sender.SenderApplication;
import Classes.Sender.SenderTransport;

public class Main {

    public static void main(String[] args) {
        Communication communication = new Communication();

        SenderTransport senderTransport = new SenderTransport(communication,1000);
        ReceiverTransport receiverTransport = new ReceiverTransport(communication,1002);

        SenderApplication sender = new SenderApplication(
                1000,
                "teste",
                senderTransport);

        ReceiverApplication receiver = new ReceiverApplication(
                1002,
                receiverTransport,
                sender.getPayload().length());

        sender.getTransport().doSegmentation(sender.getPayload(), sender.getPort(), receiver.getPort());

        senderTransport.start();
        receiverTransport.start();

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

        senderTransport.setStop(true);
        receiverTransport.setStop(true);
    }
}
