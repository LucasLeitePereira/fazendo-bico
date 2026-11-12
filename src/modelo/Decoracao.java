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
    private Rectangle[] colisores = new Rectangle[0];

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
     * Partes sólidas da decoração (o tronco da árvore, a base do barril), desenhadas no
     * Editor de Colisão do Tiled. Os retângulos vêm medidos na imagem original
     * (larguraDaImagem x alturaDaImagem) e são levados para a posição no mapa,
     * acompanhando o espelhamento e o tamanho que o objeto tiver no mapa.
     */
    public void definirColisores(Rectangle[] naImagem, int larguraDaImagem, int alturaDaImagem) {
        colisores = new Rectangle[naImagem.length];
        for (int i = 0; i < naImagem.length; i++) {
            Rectangle original = naImagem[i];
            int esquerda = original.x;
            if (espelhada) {
                esquerda = larguraDaImagem - original.x - original.width;
            }
            int colisorX = x + esquerda * largura / larguraDaImagem;
            int colisorY = y + original.y * altura / alturaDaImagem;
            int colisorLargura = original.width * largura / larguraDaImagem;
            int colisorAltura = original.height * altura / alturaDaImagem;
            colisores[i] = new Rectangle(colisorX, colisorY, colisorLargura, colisorAltura);
        }
    }

    public boolean bloqueia(Rectangle area) {
        for (int i = 0; i < colisores.length; i++) {
            if (colisores[i].intersects(area)) {
                return true;
            }
        }
        return false;
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

    public Rectangle[] getColisores() {
        return colisores;
    }
}
