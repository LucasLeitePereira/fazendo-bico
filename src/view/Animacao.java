package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import modelo.Direcao;
import modelo.Entidade;

/**
 * Desenha uma entidade a partir de uma folha de sprites.
 * Na folha, cada linha é uma direção (baixo, esquerda, direita, cima)
 * e cada coluna é um quadro da animação. Se a folha tiver só uma linha,
 * ela é usada para todas as direções.
 */
public class Animacao {
    private static final int QUADROS_DO_JOGO_POR_QUADRO_DA_ANIMACAO = 8;

    private BufferedImage folha;
    private int larguraDoQuadro;
    private int alturaDoQuadro;
    private Color corReserva;

    public Animacao(BufferedImage folha, int larguraDoQuadro, int alturaDoQuadro, Color corReserva) {
        this.folha = folha;
        this.larguraDoQuadro = larguraDoQuadro;
        this.alturaDoQuadro = alturaDoQuadro;
        this.corReserva = corReserva;
    }

    public boolean temImagem() {
        return folha != null;
    }

    /**
     * O sprite é desenhado com os pés na base da área da entidade.
     * A elevação levanta o desenho (usada nas galinhas voando).
     */
    public void desenhar(Graphics2D g, Entidade entidade, int contador, int cameraX, int cameraY, int elevacao) {
        int telaX = (int) entidade.getX() - cameraX;
        int telaY = (int) entidade.getY() - cameraY - elevacao;

        if (folha == null) {
            desenharReserva(g, entidade, telaX, telaY);
            return;
        }

        int colunas = folha.getWidth() / larguraDoQuadro;
        int linhas = folha.getHeight() / alturaDoQuadro;
        int coluna = (contador / QUADROS_DO_JOGO_POR_QUADRO_DA_ANIMACAO) % colunas;
        int linha = 0;
        if (linhas > 1) {
            linha = entidade.getDirecao().ordinal() % linhas;
        }

        int origemX = coluna * larguraDoQuadro;
        int origemY = linha * alturaDoQuadro;
        int destinoX = telaX + entidade.getLargura() / 2 - larguraDoQuadro / 2;
        int destinoY = telaY + entidade.getAltura() - alturaDoQuadro;

        g.drawImage(folha,
                destinoX, destinoY, destinoX + larguraDoQuadro, destinoY + alturaDoQuadro,
                origemX, origemY, origemX + larguraDoQuadro, origemY + alturaDoQuadro,
                null);
    }

    /**
     * Sem imagem: um retângulo colorido com uma faixa escura do lado para onde a entidade olha.
     */
    private void desenharReserva(Graphics2D g, Entidade entidade, int telaX, int telaY) {
        int largura = entidade.getLargura();
        int altura = entidade.getAltura();
        g.setColor(corReserva);
        g.fillRect(telaX, telaY, largura, altura);

        g.setColor(corReserva.darker().darker());
        Direcao direcao = entidade.getDirecao();
        if (direcao == Direcao.CIMA) {
            g.fillRect(telaX, telaY, largura, 3);
        } else if (direcao == Direcao.BAIXO) {
            g.fillRect(telaX, telaY + altura - 3, largura, 3);
        } else if (direcao == Direcao.ESQUERDA) {
            g.fillRect(telaX, telaY, 3, altura);
        } else {
            g.fillRect(telaX + largura - 3, telaY, 3, altura);
        }
    }
}
