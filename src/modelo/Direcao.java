package modelo;

/**
 * A ordem das direções é a mesma ordem das linhas nas folhas de sprites.
 */
public enum Direcao {
    BAIXO(0, 1),
    ESQUERDA(-1, 0),
    DIREITA(1, 0),
    CIMA(0, -1);

    private int passoX;
    private int passoY;

    private Direcao(int passoX, int passoY) {
        this.passoX = passoX;
        this.passoY = passoY;
    }

    public int getPassoX() {
        return passoX;
    }

    public int getPassoY() {
        return passoY;
    }
}
