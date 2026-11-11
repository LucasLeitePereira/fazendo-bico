package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import modelo.Entidade;

/**
 * Desenha uma entidade a partir de uma tira de sprites: os quadros da animação
 * ficam lado a lado numa única linha. O desenho é espelhado quando a entidade
 * olha para o lado contrário ao do desenho original.
 */
public class Animacao {
    private BufferedImage tira;
    private int larguraDoQuadro;
    private int alturaDoQuadro;
    private Color corReserva;
    private int peX;
    private int peY;
    private boolean desenhoOlhaParaEsquerda = false;
    private int quadrosDoJogoPorQuadro = 6;
    private boolean repetir = true;

    public Animacao(BufferedImage tira, int larguraDoQuadro, int alturaDoQuadro, Color corReserva) {
        this.tira = tira;
        this.larguraDoQuadro = larguraDoQuadro;
        this.alturaDoQuadro = alturaDoQuadro;
        this.corReserva = corReserva;
        peX = larguraDoQuadro / 2;
        peY = alturaDoQuadro - 1;
    }

    /**
     * Ponto do quadro onde ficam os pés do personagem. Ele é colocado
     * em cima da base da área da entidade.
     */
    public void definirPe(int x, int y) {
        peX = x;
        peY = y;
    }

    public void setDesenhoOlhaParaEsquerda(boolean desenhoOlhaParaEsquerda) {
        this.desenhoOlhaParaEsquerda = desenhoOlhaParaEsquerda;
    }

    /**
     * Quantos quadros do jogo cada quadro da animação fica na tela (o jogo roda a 60 por segundo).
     */
    public void setQuadrosDoJogoPorQuadro(int quadrosDoJogoPorQuadro) {
        this.quadrosDoJogoPorQuadro = quadrosDoJogoPorQuadro;
    }

    /**
     * Sem repetição, a animação para no último quadro (usado na morte do herói).
     */
    public void setRepetir(boolean repetir) {
        this.repetir = repetir;
    }

    public boolean temImagem() {
        return tira != null;
    }

    /**
     * O contador diz em que ponto da animação estamos. A elevação levanta o desenho
     * (usada nas galinhas voando).
     */
    public void desenhar(Graphics2D g, Entidade entidade, int contador, int cameraX, int cameraY, int elevacao) {
        int peNaTelaX = (int) entidade.getCentroX() - cameraX;
        int peNaTelaY = (int) (entidade.getY() + entidade.getAltura()) - cameraY - elevacao;

        if (tira == null) {
            desenharReserva(g, entidade, peNaTelaX, peNaTelaY);
            return;
        }

        int totalDeQuadros = tira.getWidth() / larguraDoQuadro;
        int quadro = contador / quadrosDoJogoPorQuadro;
        if (repetir) {
            quadro = quadro % totalDeQuadros;
        } else if (quadro >= totalDeQuadros) {
            quadro = totalDeQuadros - 1;
        }
        int origemX = quadro * larguraDoQuadro;

        boolean espelhar = entidade.isOlhandoParaEsquerda() != desenhoOlhaParaEsquerda;
        int destinoY = peNaTelaY - peY;

        if (espelhar) {
            int destinoX = peNaTelaX - (larguraDoQuadro - peX);
            g.drawImage(tira,
                    destinoX + larguraDoQuadro, destinoY, destinoX, destinoY + alturaDoQuadro,
                    origemX, 0, origemX + larguraDoQuadro, alturaDoQuadro,
                    null);
        } else {
            int destinoX = peNaTelaX - peX;
            g.drawImage(tira,
                    destinoX, destinoY, destinoX + larguraDoQuadro, destinoY + alturaDoQuadro,
                    origemX, 0, origemX + larguraDoQuadro, alturaDoQuadro,
                    null);
        }
    }

    /**
     * Sem imagem: um retângulo colorido com uma faixa escura do lado para onde a entidade olha.
     */
    private void desenharReserva(Graphics2D g, Entidade entidade, int peNaTelaX, int peNaTelaY) {
        int largura = entidade.getLargura();
        int altura = entidade.getAltura();
        int x = peNaTelaX - largura / 2;
        int y = peNaTelaY - altura;
        g.setColor(corReserva);
        g.fillRect(x, y, largura, altura);

        g.setColor(corReserva.darker().darker());
        if (entidade.isOlhandoParaEsquerda()) {
            g.fillRect(x, y, 3, altura);
        } else {
            g.fillRect(x + largura - 3, y, 3, altura);
        }
    }
}
