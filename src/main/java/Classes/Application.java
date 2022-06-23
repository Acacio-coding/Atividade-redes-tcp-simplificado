package Classes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//Classe abstrata com Getters e Construtor apartir do lombok
@RequiredArgsConstructor
@Getter
public abstract class Application {

    private final int port;
}
