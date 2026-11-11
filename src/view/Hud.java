package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import modelo.Heroi;
import modelo.Partida;

/**
 * Informações por cima do jogo: corações, pontos e onda da raide.
 */
public class Hud {
    private static final int TAMANHO_DO_CORACAO = 10;
    private static final Font FONTE = new Font("SansSerif", Font.BOLD, 9);
    private static final Font FONTE_GRANDE = new Font("SansSerif", Font.BOLD, 14);

    private BufferedImage coracaoCheio;
    private BufferedImage coracaoVazio;

    public Hud(Imagens imagens) {
        coracaoCheio = imagens.carregar("/sprites/coracao.png");
        coracaoVazio = imagens.carregar("/sprites/coracao_vazio.png");
    }

    public void desenhar(Graphics2D g, Heroi heroi, Partida partida, boolean noCampo) {
        desenharCoracoes(g, heroi);

        g.setFont(FONTE);
        String pontos = "Pontos: " + partida.getPontos();
        FontMetrics medidas = g.getFontMetrics();
        Texto.escrever(g, pontos, TelaJogo.LARGURA - medidas.stringWidth(pontos) - 6, 14, Color.WHITE);

        if (partida.isRaideAtiva()) {
            Texto.escreverCentralizado(g, "Onda " + partida.getOnda(), 14, Color.ORANGE);
        }

        if (!noCampo && !partida.isMissaoAceita()) {
            Texto.escrever(g, "Converse com o prefeito", 6, 28, Color.YELLOW);
        }

        if (partida.getQuadrosTremendo() > 30 && partida.isRaideAtiva()) {
            g.setFont(FONTE_GRANDE);
            Texto.escreverCentralizado(g, "AS GALINHAS ESTÃO FURIOSAS!", TelaJogo.ALTURA / 2, Color.RED);
        }
    }

    private void desenharCoracoes(Graphics2D g, Heroi heroi) {
        for (int i = 0; i < Heroi.VIDA_MAXIMA; i++) {
            int x = 6 + i * (TAMANHO_DO_CORACAO + 2);
            int y = 6;
            boolean cheio = i < heroi.getVida();
            BufferedImage imagem = coracaoVazio;
            if (cheio) {
                imagem = coracaoCheio;
            }

            if (imagem != null) {
                g.drawImage(imagem, x, y, TAMANHO_DO_CORACAO, TAMANHO_DO_CORACAO, null);
            } else {
                if (cheio) {
                    g.setColor(new Color(220, 40, 60));
                } else {
                    g.setColor(new Color(70, 70, 70));
                }
                g.fillRect(x, y, TAMANHO_DO_CORACAO, TAMANHO_DO_CORACAO);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, TAMANHO_DO_CORACAO, TAMANHO_DO_CORACAO);
            }
        }
    }
}
