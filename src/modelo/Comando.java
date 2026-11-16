package modelo;

/**
 * As ações que o jogador pode fazer. Cada uma pode ter teclas e um botão do controle (veja Controles).
 */
public enum Comando {
    CIMA("Cima"),
    BAIXO("Baixo"),
    ESQUERDA("Esquerda"),
    DIREITA("Direita"),
    ACAO("Atacar / falar"),
    CONFIRMAR("Confirmar"),
    VOLTAR("Voltar");

    private String nome;

    private Comando(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    /**
     * No controle, as direções vêm sempre do direcional e do analógico esquerdo.
     */
    public boolean isDirecao() {
        return this == CIMA || this == BAIXO || this == ESQUERDA || this == DIREITA;
    }
}
