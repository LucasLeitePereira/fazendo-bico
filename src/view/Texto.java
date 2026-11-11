package view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Ajuda a escrever na tela com sombra, para o texto aparecer sobre qualquer fundo.
 */
public class Texto {

    public static void escrever(Graphics2D g, String texto, int x, int y, Color cor) {
        g.setColor(Color.BLACK);
        g.drawString(texto, x + 1, y + 1);
        g.setColor(cor);
        g.drawString(texto, x, y);
    }

    public static void escreverCentralizado(Graphics2D g, String texto, int y, Color cor) {
        FontMetrics medidas = g.getFontMetrics();
        int x = (TelaJogo.LARGURA - medidas.stringWidth(texto)) / 2;
        escrever(g, texto, x, y, cor);
    }
}
