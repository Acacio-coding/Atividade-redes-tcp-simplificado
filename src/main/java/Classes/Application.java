package Classes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
//Classe Abstrata de uma aplicação com variáveis em comum para os 2 tipos de aplicação possíveis
public abstract class Application {

    private final int port;
}
