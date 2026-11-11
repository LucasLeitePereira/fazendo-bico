package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import modelo.Heroi;
import modelo.Partida;

/**
 * Informações por cima do jogo: corações, moedas, onda da raide e avisos.
 */
public class Hud {
    private static final int ESPACO_ENTRE_CORACOES = 11;
    private static final Color COR_DAS_MOEDAS = new Color(255, 220, 110);
    private static final Color COR_DA_ETIQUETA = new Color(70, 40, 25);

    private CaixaNoveFatias caixaEscura;
    private BufferedImage coracaoCheio;
    private BufferedImage coracaoVazio;
    private BufferedImage moeda;
    private BufferedImage etiquetaEsquerda;
    private BufferedImage etiquetaMeio;
    private BufferedImage etiquetaDireita;
    private BufferedImage balaoAlerta;

    public Hud(Imagens imagens) {
        caixaEscura = new CaixaNoveFatias(imagens.carregar("/interface/caixa_escura.png"), new Color(60, 40, 30));
        coracaoCheio = imagens.carregar("/interface/coracao.png");
        coracaoVazio = imagens.carregar("/interface/coracao_vazio.png");
        moeda = imagens.carregar("/interface/moeda.png");
        etiquetaEsquerda = imagens.carregar("/interface/etiqueta_esquerda.png");
        etiquetaMeio = imagens.carregar("/interface/etiqueta_meio.png");
        etiquetaDireita = imagens.carregar("/interface/etiqueta_direita.png");
        balaoAlerta = imagens.carregar("/interface/balao_alerta.png");
    }

    public void desenhar(Graphics2D g, Heroi heroi, Partida partida, boolean noCampo) {
        desenharCoracoes(g, heroi);
        desenharMoedas(g, partida);

        if (partida.isRaideAtiva()) {
            g.setFont(Texto.NORMAL);
            String onda = "ONDA " + partida.getOnda();
            int largura = Texto.largura(g, onda) + 12;
            desenharEtiqueta(g, onda, (TelaJogo.LARGURA - largura) / 2, 5, largura);
        }

        if (!noCampo && !partida.isMissaoAceita()) {
            g.setFont(Texto.NORMAL);
            String aviso = "Fale com o prefeito";
            int largura = Texto.largura(g, aviso) + 18;
            desenharEtiqueta(g, aviso, 4, 22, largura);
            if (balaoAlerta != null) {
                g.drawImage(balaoAlerta, 4 + largura - 9, 24, null);
            }
        }

        if (partida.getQuadrosTremendo() > 30 && partida.isRaideAtiva()) {
            desenharFaixaDaRaide(g, partida);
        }
    }

    private void desenharCoracoes(Graphics2D g, Heroi heroi) {
        caixaEscura.desenhar(g, 3, 3, Heroi.VIDA_MAXIMA * ESPACO_ENTRE_CORACOES + 7, 15);
        for (int i = 0; i < Heroi.VIDA_MAXIMA; i++) {
            int x = 7 + i * ESPACO_ENTRE_CORACOES;
            int y = 7;
            boolean cheio = i < heroi.getVida();
            BufferedImage imagem = coracaoVazio;
            if (cheio) {
                imagem = coracaoCheio;
            }
            if (imagem != null) {
                g.drawImage(imagem, x, y, null);
            } else {
                if (cheio) {
                    g.setColor(new Color(220, 40, 60));
                } else {
                    g.setColor(new Color(70, 70, 70));
                }
                g.fillRect(x, y, 9, 8);
            }
        }
    }

    /**
     * A missão paga em moedas: cada galinha vale os pontos da partida.
     */
    private void desenharMoedas(Graphics2D g, Partida partida) {
        g.setFont(Texto.MEDIA);
        String texto = "" + partida.getPontos();
        int largura = Texto.largura(g, texto) + 22;
        int x = TelaJogo.LARGURA - largura - 3;
        caixaEscura.desenhar(g, x, 3, largura, 15);
        if (moeda != null) {
            g.drawImage(moeda, x + 5, 7, null);
        } else {
            g.setColor(COR_DAS_MOEDAS);
            g.fillOval(x + 5, 7, 7, 7);
        }
        Texto.escrever(g, texto, x + 16, 14.5f, COR_DAS_MOEDAS);
    }

    /**
     * Etiqueta de papel com texto, montada com as pontas e o meio esticado.
     */
    private void desenharEtiqueta(Graphics2D g, String texto, int x, int y, int largura) {
        int altura = 13;
        if (etiquetaMeio != null && etiquetaEsquerda != null && etiquetaDireita != null) {
            g.drawImage(etiquetaEsquerda, x, y, null);
            g.drawImage(etiquetaMeio, x + 4, y, largura - 8, altura, null);
            g.drawImage(etiquetaDireita, x + largura - 4, y, null);
        } else {
            g.setColor(new Color(230, 190, 150));
            g.fillRect(x, y, largura, altura);
        }
        g.setFont(Texto.NORMAL);
        Texto.escreverSemSombra(g, texto, x + 6, y + 9.5f, COR_DA_ETIQUETA);
    }

    private void desenharFaixaDaRaide(Graphics2D g, Partida partida) {
        int tremor = 0;
        if (partida.getQuadrosTremendo() % 4 < 2) {
            tremor = 1;
        }
        int largura = 264;
        int x = (TelaJogo.LARGURA - largura) / 2 + tremor;
        int y = 36;
        caixaEscura.desenhar(g, x, y, largura, 34);
        g.setFont(Texto.GRANDE);
        String texto = "AS GALINHAS ESTÃO FURIOSAS!";
        Texto.escreverComContorno(g, texto, Texto.centralizar(g, texto, x + largura / 2), y + 17,
                new Color(255, 120, 60), new Color(60, 20, 10), 0.5f);
        g.setFont(Texto.NORMAL);
        String dica = "Elas vêm de todos os lados. Aguente o quanto puder!";
        Texto.escrever(g, dica, Texto.centralizar(g, dica, x + largura / 2), y + 28, new Color(255, 230, 200));
    }
}
