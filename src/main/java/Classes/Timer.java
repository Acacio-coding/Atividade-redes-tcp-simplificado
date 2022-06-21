package Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Timer extends Thread{

    private boolean timerEnded;
    private boolean wasRemoveOnce = false;

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long end = start + 10;
        //long end = start + 2 * 1000;

        while (System.currentTimeMillis() < end) {
            timerEnded = false;
        }

        timerEnded = true;
    }
}
