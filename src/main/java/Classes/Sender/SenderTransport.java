package Classes.Sender;

import Classes.*;
import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SenderTransport extends Transport {

    private int packageCounter;
    public boolean stop;

    public SenderTransport(Communication communication, int port) {
        super(communication, port);
        packageCounter = 0;
        stop = false;
    }

    public void doSegmentation(String message, int senderPort, int receiverPort) {
        for (int i = 0; i < message.length(); i++) {
            this.getExitBuffer().add(new DataSegment(senderPort, receiverPort,
                    packageCounter, (packageCounter + 1), message.toCharArray()[i], new Timer()));

            setPackageCounter((packageCounter + 1));
        }
    }

    @Override
    public void run() {
        while (!stop) {
            this.getCommunication().receiveSegment().forEach(currentReceived -> {
                if (currentReceived.getDestinationPort() == this.getPort()
                        && !this.getEntryBuffer().contains(currentReceived)) {
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

            if (this.getEntryBuffer().size() > 0) {
                for (Segment segment : this.getEntryBuffer()) {
                    if (segment.isCorrupted() || segment.getNumber() == -1) {
                        this.getCommunication().getSegments()
                                .removeIf(current -> current instanceof FlagSegment
                                        && current.getNumber() == segment.getNumber()
                                        && current.isCorrupted());

                        this.getEntryBuffer().remove(segment);
                    } else {
                        if (segment instanceof FlagSegment) {
                            if (segment.isAck()) {
                                this.getExitBuffer().removeIf(current ->
                                        ((DataSegment) current).getExpectedResponseNumber() == segment.getNumber());
                            }

                            if (segment.isNack() || segment.getNumber() == 0) {
                                this.getCommunication().getSegments()
                                        .removeIf(current -> current instanceof DataSegment
                                                && ((DataSegment) current)
                                                .getExpectedResponseNumber() == segment.getNumber()
                                                && current.isCorrupted());

                                this.getCommunication().getSegments()
                                        .removeIf(current -> current instanceof FlagSegment
                                                && current.isNack()
                                                && current.getNumber() == segment.getNumber());
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
