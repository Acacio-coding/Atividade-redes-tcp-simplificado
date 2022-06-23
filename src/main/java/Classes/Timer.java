package Classes;

import lombok.Getter;
import lombok.Setter;

//Classe Times contendo uma Thread
@Getter
@Setter
public class Timer extends Thread{

    private boolean timerEnded;
    private boolean wasRemoveOnce = false;

    //Ao rodar ele pega o tempo atual e adiciona + 10 ao final e verifica
    //se esse tempo já de expirou
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
