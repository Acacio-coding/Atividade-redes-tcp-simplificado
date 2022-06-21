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
public class ReceiverTransport extends Transport {

    public boolean stop = false;

    public ReceiverTransport(Communication communication, int port) {
        super(communication, port);
    }

    @Override
    public void run() {
        while(!stop) {
            this.getCommunication().receiveSegment().forEach(currentReceived -> {
                if (currentReceived.getDestinationPort() == this.getPort()
                        && !this.getEntryBuffer().contains(currentReceived)) {
                    this.getEntryBuffer().add(currentReceived);
                    System.out.println("\nReceiver ---------------------------");
                    System.out.println("Received package number: " + currentReceived.getNumber());
                    System.out.println("Type: " + currentReceived.getClass().getSimpleName());
                    System.out.println("Data: " + ((DataSegment) currentReceived).getData());
                }
            });

            if (this.getEntryBuffer().size() > 0) {
                for (Segment segment : this.getEntryBuffer()) {
                    if (segment instanceof DataSegment) {
                        Segment toSend = new FlagSegment(this.getPort(),
                                segment.getOriginPort(), (segment.getNumber() + 1), new Timer());

                        if (this.getCommunication().getSegments()
                                .stream()
                                .filter(current -> current instanceof FlagSegment)
                                .noneMatch(current -> current.getNumber() == toSend.getNumber())) {
                            if (!segment.isCorrupted() && segment.getNumber() != -1) {
                                toSend.setAck(true);

                                this.getExitBuffer()
                                        .removeIf(current ->
                                                current instanceof FlagSegment
                                                        && current.isNack()
                                                        && current.getNumber() == (segment.getNumber() + 1));

                                this.getExitBuffer().add(toSend);
                            } else {
                                toSend.setNack(true);
                                this.getExitBuffer().add(toSend);
                                this.getEntryBuffer().remove(segment);
                            }
                        }
                    }
                }
            }

            if (this.getExitBuffer().size() > 0) {
                for (Segment segment : this.getExitBuffer()) {
                    if (!this.getCommunication().getSegments().contains(segment)) {
                        try {
                            if (!segment.getTimer().isTimerEnded() && !segment.getTimer().isAlive()) {
                                segment.getTimer().start();
                            }

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
