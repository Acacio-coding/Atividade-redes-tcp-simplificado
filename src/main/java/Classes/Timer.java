package Classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Classe Timer contendo uma Thread
public class Timer extends Thread{

    //Variável de controle se o tempo acabou
    private boolean timerEnded = false;

    //Variável de controle para remoção do meio de comunicação
    private boolean wasRemovedOnce = false;

    //Variável de controle genérica para parar o timer
    private boolean stop = false;

    @Override
    public void run() {
        while (!stop) {
            //Pega tempo atual
            long start = System.currentTimeMillis();
            //Gera um tempo no futuro com 100 milissegundos a mais em relação ao tempo capturado anteriormente
            long end = start + 100;
            //long end = start + 2 * 1000;

            //Roda enquanto o tempo atual seja menor que o tempo futuro
            while (System.currentTimeMillis() < end) {
                timerEnded = false;
            }

            //Para a thread e seta o controle do timer para indicar que o tempo acabou
            stop = true;
            timerEnded = true;
        }
    }
}
