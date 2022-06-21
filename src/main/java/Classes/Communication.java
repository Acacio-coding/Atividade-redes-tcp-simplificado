package Classes;

import Classes.Package.DataSegment;
import Classes.Package.FlagSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
@Setter
public class Communication {

    private Queue<Segment> segments = new LinkedBlockingDeque<>();
    private Random random = new Random();
    private boolean corruptedOnce = false;
    private int validSegmentNumber;

    public synchronized Queue<Segment> receiveSegment() {
        for (Segment segment : segments) {
            segments.removeIf(current -> current.getTimer().isTimerEnded()
                    && !current.getTimer().isWasRemoveOnce() && current == segment);
            segment.getTimer().setWasRemoveOnce(true);
        }

        return segments;
    }

    public synchronized void sendSegment(Segment segment) throws InterruptedException {
        if (!this.segments.contains(segment)) {
            if (!corruptedOnce) {
                setValidSegmentNumber(segment.getNumber());

                if (random.nextInt(100) > 50) {
                    segment.setCorrupted(true);
                } else {
                    segment.setCorrupted(true);
                    segment.setNumber(-1);
                }

                setCorruptedOnce(true);
                this.segments.add(segment);
            } else {
                if (segment.getNumber() == -1) {
                    segment.setNumber(validSegmentNumber);
                } else if (segment instanceof FlagSegment && segment.getNumber() == 0) {
                    segment.setNumber((validSegmentNumber + 1));
                }

                if (segment.isCorrupted()) {
                    segment.setCorrupted(false);
                } else {
                    this.segments.add(segment);
                }
            }
        }
    }
}
