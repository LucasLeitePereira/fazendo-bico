package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Desenha uma caixa de qualquer tamanho a partir de uma imagem pequena dividida em 3x3 pedaços:
 * os cantos ficam como estão, as bordas e o miolo são esticados. É a técnica "9-slice".
 */
public class CaixaNoveFatias {
    private BufferedImage imagem;
    private int pedaco;
    private Color corReserva;

    public CaixaNoveFatias(BufferedImage imagem, Color corReserva) {
        this.imagem = imagem;
        this.corReserva = corReserva;
        if (imagem != null) {
            pedaco = imagem.getWidth() / 3;
        }
    }

    public void desenhar(Graphics2D g, int x, int y, int largura, int altura) {
        if (imagem == null) {
            g.setColor(corReserva);
            g.fillRect(x, y, largura, altura);
            g.setColor(corReserva.darker().darker());
            g.drawRect(x, y, largura - 1, altura - 1);
            return;
        }

        int[] colunas = { x, x + pedaco, x + largura - pedaco, x + largura };
        int[] linhas = { y, y + pedaco, y + altura - pedaco, y + altura };
        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 3; coluna++) {
                int origemX = coluna * pedaco;
                int origemY = linha * pedaco;
                g.drawImage(imagem,
                        colunas[coluna], linhas[linha], colunas[coluna + 1], linhas[linha + 1],
                        origemX, origemY, origemX + pedaco, origemY + pedaco,
                        null);
            }
        }
    }
}
