package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Ajuda a escrever na tela. A tela é ampliada 2 vezes, mas o texto é desenhado
 * na resolução da janela, então fica nítido mesmo com fonte pequena.
 */
public class Texto {
    public static final Font NORMAL = new Font("Dialog", Font.BOLD, 8);
    public static final Font FALA = new Font("Dialog", Font.PLAIN, 8);
    public static final Font MEDIA = new Font("Dialog", Font.BOLD, 11);
    public static final Font GRANDE = new Font("Dialog", Font.BOLD, 14);
    public static final Font TITULO = new Font("Serif", Font.BOLD, 26);

    private static final float SOMBRA = 0.5f;

    /**
     * Texto com uma sombra fina, para aparecer sobre qualquer fundo.
     */
    public static void escrever(Graphics2D g, String texto, float x, float y, Color cor) {
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(texto, x + SOMBRA, y + SOMBRA);
        g.setColor(cor);
        g.drawString(texto, x, y);
    }

    public static void escreverSemSombra(Graphics2D g, String texto, float x, float y, Color cor) {
        g.setColor(cor);
        g.drawString(texto, x, y);
    }

    /**
     * Texto com contorno em volta, para títulos.
     */
    public static void escreverComContorno(Graphics2D g, String texto, float x, float y, Color cor, Color contorno, float espessura) {
        g.setColor(contorno);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(texto, x + dx * espessura, y + dy * espessura);
                }
            }
        }
        g.drawString(texto, x, y + espessura * 2);
        g.setColor(cor);
        g.drawString(texto, x, y);
    }

    public static void escreverCentralizado(Graphics2D g, String texto, int y, Color cor) {
        escrever(g, texto, centralizar(g, texto, TelaJogo.LARGURA / 2), y, cor);
    }

    /**
     * Posição x para o texto ficar centralizado em volta de centroX.
     */
    public static float centralizar(Graphics2D g, String texto, float centroX) {
        return centroX - largura(g, texto) / 2.0f;
    }

    public static int largura(Graphics2D g, String texto) {
        FontMetrics medidas = g.getFontMetrics();
        return medidas.stringWidth(texto);
    }
}
