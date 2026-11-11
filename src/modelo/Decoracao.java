package modelo;

import java.awt.Rectangle;

/**
 * Um objeto parado do cenário: árvore, barril, poço, moinho... O tipo é o nome da imagem
 * (por exemplo "arvore1"). A posição é o canto de cima, à esquerda, da imagem no mapa.
 */
public class Decoracao {
    /** Desenhada logo depois do chão, embaixo de todo mundo (sombras). */
    public static final int NO_CHAO = 0;
    /** Desenhada na ordem da profundidade, junto com os personagens. */
    public static final int EM_PE = 1;
    /** Desenhada por cima de tudo (pás do moinho, fumaça das chaminés). */
    public static final int POR_CIMA = 2;

    private String tipo;
    private int x;
    private int y;
    private int largura;
    private int altura;
    private boolean espelhada;
    private int camada;
    private Rectangle base;

    public Decoracao(String tipo, int x, int y, int largura, int altura, boolean espelhada, int camada) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.espelhada = espelhada;
        this.camada = camada;
    }

    /**
     * Torna sólida uma faixa no pé da decoração (o tronco da árvore, a base do barril).
     */
    public void definirBaseSolida(int larguraDaBase, int alturaDaBase) {
        int baseX = x + (largura - larguraDaBase) / 2;
        int baseY = y + altura - alturaDaBase;
        base = new Rectangle(baseX, baseY, larguraDaBase, alturaDaBase);
    }

    public boolean bloqueia(Rectangle area) {
        return base != null && base.intersects(area);
    }

    public String getTipo() {
        return tipo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    /**
     * Altura do pé no mapa, usada para saber quem fica na frente de quem.
     */
    public int getPeY() {
        return y + altura;
    }

    public boolean isEspelhada() {
        return espelhada;
    }

    public int getCamada() {
        return camada;
    }

    public Rectangle getBase() {
        return base;
    }
}
