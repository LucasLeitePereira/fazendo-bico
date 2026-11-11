package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import modelo.Dialogo;

public class CaixaDialogo {
    private static final Font FONTE = new Font("SansSerif", Font.PLAIN, 9);
    private static final Font FONTE_DO_NOME = new Font("SansSerif", Font.BOLD, 9);
    private static final int MARGEM = 8;
    private static final int ALTURA = 64;
    private static final int ESPACO_ENTRE_LINHAS = 11;

    public void desenhar(Graphics2D g, Dialogo dialogo) {
        int x = MARGEM;
        int y = TelaJogo.ALTURA - ALTURA - MARGEM;
        int largura = TelaJogo.LARGURA - MARGEM * 2;

        g.setColor(new Color(20, 20, 40, 220));
        g.fillRoundRect(x, y, largura, ALTURA, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, largura, ALTURA, 8, 8);

        int linhaY = y + 14;
        if (dialogo.getNome().length() > 0) {
            g.setFont(FONTE_DO_NOME);
            Texto.escrever(g, dialogo.getNome(), x + 8, linhaY, Color.YELLOW);
            linhaY = linhaY + ESPACO_ENTRE_LINHAS + 2;
        }

        g.setFont(FONTE);
        List<String> linhas = quebrarEmLinhas(g, dialogo.getFalaAtual(), largura - 16);
        for (int i = 0; i < linhas.size(); i++) {
            Texto.escrever(g, linhas.get(i), x + 8, linhaY, Color.WHITE);
            linhaY = linhaY + ESPACO_ENTRE_LINHAS;
        }

        boolean mostrarSeta = (System.currentTimeMillis() / 400) % 2 == 0;
        if (mostrarSeta) {
            Texto.escrever(g, "Espaço >", x + largura - 46, y + ALTURA - 6, Color.LIGHT_GRAY);
        }
    }

    private List<String> quebrarEmLinhas(Graphics2D g, String texto, int larguraMaxima) {
        FontMetrics medidas = g.getFontMetrics();
        String[] palavras = texto.split(" ");
        List<String> linhas = new ArrayList<String>();
        String linhaAtual = "";

        for (int i = 0; i < palavras.length; i++) {
            String tentativa = palavras[i];
            if (linhaAtual.length() > 0) {
                tentativa = linhaAtual + " " + palavras[i];
            }
            if (medidas.stringWidth(tentativa) > larguraMaxima && linhaAtual.length() > 0) {
                linhas.add(linhaAtual);
                linhaAtual = palavras[i];
            } else {
                linhaAtual = tentativa;
            }
        }
        if (linhaAtual.length() > 0) {
            linhas.add(linhaAtual);
        }
        return linhas;
    }
}
