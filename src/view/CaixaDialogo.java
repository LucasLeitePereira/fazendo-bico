package view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import modelo.Dialogo;

/**
 * Caixa de conversa na parte de baixo da tela: retrato de quem fala,
 * etiqueta com o nome, o texto e um indicador piscando para continuar.
 */
public class CaixaDialogo {
    private static final int MARGEM = 6;
    private static final int ALTURA = 58;
    private static final int ESPACO_ENTRE_LINHAS = 10;
    private static final int TAMANHO_DO_RETRATO = 40;
    private static final Color COR_DO_TEXTO = new Color(60, 38, 25);
    private static final Color COR_DO_NOME = new Color(70, 40, 25);

    private CaixaNoveFatias caixa;
    private CaixaNoveFatias moldura;
    private BufferedImage etiquetaEsquerda;
    private BufferedImage etiquetaMeio;
    private BufferedImage etiquetaDireita;
    private BufferedImage indicador;
    private BufferedImage[] retratos;

    /**
     * Os retratos vêm na ordem dos números dos moradores (a posição 0 fica vazia).
     */
    public CaixaDialogo(Imagens imagens, BufferedImage[] retratos) {
        caixa = new CaixaNoveFatias(imagens.carregar("/interface/caixa_clara.png"), new Color(225, 190, 150));
        moldura = new CaixaNoveFatias(imagens.carregar("/interface/caixa_branca.png"), new Color(210, 215, 230));
        etiquetaEsquerda = imagens.carregar("/interface/etiqueta_esquerda.png");
        etiquetaMeio = imagens.carregar("/interface/etiqueta_meio.png");
        etiquetaDireita = imagens.carregar("/interface/etiqueta_direita.png");
        indicador = imagens.carregar("/interface/indicador.png");
        this.retratos = retratos;
    }

    public void desenhar(Graphics2D g, Dialogo dialogo) {
        int x = MARGEM;
        int y = TelaJogo.ALTURA - ALTURA - MARGEM;
        int largura = TelaJogo.LARGURA - MARGEM * 2;
        caixa.desenhar(g, x, y, largura, ALTURA);

        int textoX = x + 9;
        BufferedImage retrato = null;
        if (dialogo.getRetrato() > 0 && dialogo.getRetrato() < retratos.length) {
            retrato = retratos[dialogo.getRetrato()];
        }
        if (retrato != null) {
            int retratoY = y + (ALTURA - TAMANHO_DO_RETRATO) / 2;
            moldura.desenhar(g, x + 7, retratoY - 2, TAMANHO_DO_RETRATO + 4, TAMANHO_DO_RETRATO + 4);
            g.drawImage(retrato, x + 9, retratoY, TAMANHO_DO_RETRATO, TAMANHO_DO_RETRATO, null);
            textoX = x + TAMANHO_DO_RETRATO + 18;
        }

        if (dialogo.getNome().length() > 0) {
            desenharNome(g, dialogo.getNome(), textoX - 4, y - 8);
        }

        g.setFont(Texto.FALA);
        int linhaY = y + 16;
        int larguraDoTexto = x + largura - textoX - 10;
        List<String> linhas = quebrarEmLinhas(g, dialogo.getFalaAtual(), larguraDoTexto);
        for (int i = 0; i < linhas.size(); i++) {
            Texto.escreverSemSombra(g, linhas.get(i), textoX, linhaY, COR_DO_TEXTO);
            linhaY = linhaY + ESPACO_ENTRE_LINHAS;
        }

        // O indicador sobe e desce para mostrar que dá para continuar.
        int pulo = (int) ((System.currentTimeMillis() / 250) % 2);
        int indicadorX = x + largura - 16;
        int indicadorY = y + ALTURA - 12 + pulo;
        if (indicador != null) {
            g.drawImage(indicador, indicadorX, indicadorY, null);
        } else {
            g.setColor(COR_DO_TEXTO);
            g.fillRect(indicadorX, indicadorY, 6, 4);
        }
    }

    private void desenharNome(Graphics2D g, String nome, int x, int y) {
        g.setFont(Texto.NORMAL);
        int largura = Texto.largura(g, nome) + 14;
        int altura = 13;
        if (etiquetaMeio != null && etiquetaEsquerda != null && etiquetaDireita != null) {
            g.drawImage(etiquetaEsquerda, x, y, null);
            g.drawImage(etiquetaMeio, x + 4, y, largura - 8, altura, null);
            g.drawImage(etiquetaDireita, x + largura - 4, y, null);
        } else {
            g.setColor(new Color(230, 190, 150));
            g.fillRect(x, y, largura, altura);
        }
        Texto.escreverSemSombra(g, nome, x + 7, y + 9.5f, COR_DO_NOME);
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
