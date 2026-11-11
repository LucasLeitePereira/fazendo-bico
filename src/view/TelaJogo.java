package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;

import modelo.Cena;
import modelo.Galinha;
import modelo.Heroi;
import modelo.Mapa;
import modelo.Morador;
import modelo.Mundo;
import modelo.Partida;

/**
 * Desenha a cena atual. O jogo é desenhado em 320x240 e ampliado 2 vezes,
 * para os pixels ficarem grandes como nos jogos retrô.
 */
public class TelaJogo extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int LARGURA = 320;
    public static final int ALTURA = 240;
    public static final int ESCALA = 2;

    private static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONTE_MENU = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 9);
    private static final Color COR_DO_FUNDO = new Color(30, 30, 50);
    private static final int ELEVACAO_DO_VOO = 6;

    private Mundo mundo;
    private Random sorteio = new Random();
    private Hud hud;
    private CaixaDialogo caixaDialogo = new CaixaDialogo();

    private BufferedImage imagemTelaInicial;
    private BufferedImage imagemGolpe;
    private Animacao animacaoHeroi;
    private Animacao animacaoHeroiAtacando;
    private Animacao animacaoGalinha;
    private Animacao[] animacaoMoradores = new Animacao[5];
    private Map<Character, BufferedImage> imagensDosTiles = new HashMap<Character, BufferedImage>();

    public TelaJogo(Mundo mundo) {
        this.mundo = mundo;
        setPreferredSize(new Dimension(LARGURA * ESCALA, ALTURA * ESCALA));
        setBackground(COR_DO_FUNDO);

        Imagens imagens = new Imagens();
        hud = new Hud(imagens);
        imagemTelaInicial = imagens.carregar("/telas/inicial.png");
        imagemGolpe = imagens.carregar("/sprites/golpe.png");

        // Para sprites de outro tamanho, troque os dois números (largura e altura de cada quadro).
        animacaoHeroi = new Animacao(imagens.carregar("/sprites/heroi.png"), 16, 16, new Color(60, 170, 80));
        animacaoHeroiAtacando = new Animacao(imagens.carregar("/sprites/heroi_ataque.png"), 16, 16, new Color(60, 170, 80));
        animacaoGalinha = new Animacao(imagens.carregar("/sprites/galinha.png"), 16, 16, Color.WHITE);
        animacaoMoradores[1] = new Animacao(imagens.carregar("/sprites/morador1.png"), 16, 16, new Color(90, 90, 200));
        animacaoMoradores[2] = new Animacao(imagens.carregar("/sprites/morador2.png"), 16, 16, new Color(200, 90, 160));
        animacaoMoradores[3] = new Animacao(imagens.carregar("/sprites/morador3.png"), 16, 16, new Color(150, 110, 60));
        animacaoMoradores[4] = new Animacao(imagens.carregar("/sprites/morador4.png"), 16, 16, new Color(230, 160, 40));

        carregarTile(imagens, '.', "grama");
        carregarTile(imagens, ',', "flor");
        carregarTile(imagens, '#', "arvore");
        carregarTile(imagens, '~', "agua");
        carregarTile(imagens, '=', "caminho");
        carregarTile(imagens, 'C', "parede");
        carregarTile(imagens, 'T', "telhado");
        carregarTile(imagens, 'P', "porta");
        carregarTile(imagens, 'F', "cerca");
        carregarTile(imagens, 'S', "saida");
    }

    private void carregarTile(Imagens imagens, char tile, String nome) {
        imagensDosTiles.put(tile, imagens.carregar("/tiles/" + nome + ".png"));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(ESCALA, ESCALA);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Partida partida = mundo.getPartida();
        switch (partida.getCena()) {
            case INICIAL:
                desenharTelaInicial(g2, partida);
                break;
            case VILA:
                desenharMundo(g2, partida, false);
                break;
            case CAMPO:
                desenharMundo(g2, partida, true);
                break;
            case FIM:
                desenharTelaFinal(g2, partida);
                break;
        }
    }

    private boolean piscando(int milissegundos) {
        return (System.currentTimeMillis() / milissegundos) % 2 == 0;
    }

    // ---------- Tela inicial ----------

    private void desenharTelaInicial(Graphics2D g, Partida partida) {
        if (imagemTelaInicial != null) {
            g.drawImage(imagemTelaInicial, 0, 0, LARGURA, ALTURA, null);
        } else {
            g.setColor(COR_DO_FUNDO);
            g.fillRect(0, 0, LARGURA, ALTURA);
            g.setFont(FONTE_TITULO);
            Texto.escreverCentralizado(g, Janela.NOME_DO_JOGO, 80, Color.ORANGE);
        }

        g.setFont(FONTE_MENU);
        String[] opcoes = { "Jogar", "Sair" };
        for (int i = 0; i < opcoes.length; i++) {
            String texto = opcoes[i];
            Color cor = Color.WHITE;
            if (i == partida.getOpcaoDoMenu()) {
                texto = "> " + texto + " <";
                cor = Color.YELLOW;
            }
            Texto.escreverCentralizado(g, texto, 150 + i * 20, cor);
        }

        g.setFont(FONTE_PEQUENA);
        if (piscando(500)) {
            Texto.escreverCentralizado(g, "Pressione Enter", 210, Color.WHITE);
        }
        Texto.escrever(g, "Recorde: " + partida.getRecorde(), 6, ALTURA - 6, Color.LIGHT_GRAY);
    }

    // ---------- Vila e campo ----------

    private void desenharMundo(Graphics2D g, Partida partida, boolean noCampo) {
        Mapa mapa = mundo.getMapa();
        Heroi heroi = mundo.getHeroi();
        if (mapa == null || heroi == null) {
            return;
        }

        int cameraX = calcularCamera(heroi.getCentroX(), mapa.getLarguraEmPixels(), LARGURA);
        int cameraY = calcularCamera(heroi.getCentroY(), mapa.getAlturaEmPixels(), ALTURA);
        if (partida.getQuadrosTremendo() > 0) {
            cameraX = cameraX + sorteio.nextInt(7) - 3;
            cameraY = cameraY + sorteio.nextInt(7) - 3;
        }

        desenharTiles(g, mapa, cameraX, cameraY);
        desenharMoradores(g, cameraX, cameraY);
        desenharGalinhas(g, cameraX, cameraY);
        desenharHeroi(g, heroi, cameraX, cameraY);

        hud.desenhar(g, heroi, partida, noCampo);
        if (mundo.getDialogo().isAtivo()) {
            caixaDialogo.desenhar(g, mundo.getDialogo());
        }
    }

    /**
     * A câmera segue o herói, mas não mostra o que fica além da borda do mapa.
     */
    private int calcularCamera(float centroDoHeroi, int tamanhoDoMapa, int tamanhoDaTela) {
        if (tamanhoDoMapa <= tamanhoDaTela) {
            return (tamanhoDoMapa - tamanhoDaTela) / 2;
        }
        int camera = (int) centroDoHeroi - tamanhoDaTela / 2;
        if (camera < 0) {
            camera = 0;
        }
        if (camera > tamanhoDoMapa - tamanhoDaTela) {
            camera = tamanhoDoMapa - tamanhoDaTela;
        }
        return camera;
    }

    private void desenharTiles(Graphics2D g, Mapa mapa, int cameraX, int cameraY) {
        int tamanho = Mapa.TAMANHO_TILE;
        int primeiraColuna = Math.max(0, cameraX / tamanho);
        int ultimaColuna = Math.min(mapa.getColunas() - 1, (cameraX + LARGURA) / tamanho + 1);
        int primeiraLinha = Math.max(0, cameraY / tamanho);
        int ultimaLinha = Math.min(mapa.getLinhas() - 1, (cameraY + ALTURA) / tamanho + 1);

        for (int linha = primeiraLinha; linha <= ultimaLinha; linha++) {
            for (int coluna = primeiraColuna; coluna <= ultimaColuna; coluna++) {
                int x = coluna * tamanho - cameraX;
                int y = linha * tamanho - cameraY;
                char tile = mapa.getTile(coluna, linha);
                desenharTile(g, Mapa.GRAMA, x, y);
                if (tile != Mapa.GRAMA) {
                    desenharTile(g, tile, x, y);
                }
            }
        }
    }

    private void desenharTile(Graphics2D g, char tile, int x, int y) {
        int tamanho = Mapa.TAMANHO_TILE;
        BufferedImage imagem = imagensDosTiles.get(tile);
        if (imagem != null) {
            g.drawImage(imagem, x, y, tamanho, tamanho, null);
            return;
        }

        switch (tile) {
            case '.':
                g.setColor(new Color(110, 180, 80));
                g.fillRect(x, y, tamanho, tamanho);
                break;
            case ',':
                g.setColor(new Color(250, 230, 90));
                g.fillOval(x + 3, y + 4, 4, 4);
                g.setColor(new Color(240, 120, 160));
                g.fillOval(x + 9, y + 9, 4, 4);
                break;
            case '#':
                g.setColor(new Color(30, 100, 45));
                g.fillOval(x, y, tamanho, tamanho);
                break;
            case '~':
                g.setColor(new Color(70, 130, 220));
                g.fillRect(x, y, tamanho, tamanho);
                break;
            case '=':
            case 'S':
                g.setColor(new Color(205, 175, 115));
                g.fillRect(x, y, tamanho, tamanho);
                break;
            case 'C':
                g.setColor(new Color(225, 205, 165));
                g.fillRect(x, y, tamanho, tamanho);
                break;
            case 'T':
                g.setColor(new Color(185, 65, 55));
                g.fillRect(x, y, tamanho, tamanho);
                break;
            case 'P':
                g.setColor(new Color(110, 70, 40));
                g.fillRect(x + 3, y, tamanho - 6, tamanho);
                break;
            case 'F':
                g.setColor(new Color(160, 115, 70));
                g.fillRect(x, y + 5, tamanho, 3);
                g.fillRect(x + 2, y + 2, 3, tamanho - 4);
                g.fillRect(x + 11, y + 2, 3, tamanho - 4);
                break;
            default:
                g.setColor(Color.MAGENTA);
                g.fillRect(x, y, tamanho, tamanho);
                break;
        }
    }

    private void desenharMoradores(Graphics2D g, int cameraX, int cameraY) {
        Morador[] moradores = mundo.getMoradores();
        for (int i = 0; i < moradores.length; i++) {
            Morador morador = moradores[i];
            Animacao animacao = animacaoMoradores[morador.getNumero()];
            animacao.desenhar(g, morador, 0, cameraX, cameraY, 0);
        }
    }

    private void desenharGalinhas(Graphics2D g, int cameraX, int cameraY) {
        Galinha[] galinhas = mundo.getGalinhas();
        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (!galinha.isAtiva()) {
                continue;
            }
            if (galinha.estaPiscando() && galinha.getQuadrosPiscando() % 4 < 2) {
                continue;
            }

            int elevacao = 0;
            if (galinha.isVoando()) {
                elevacao = ELEVACAO_DO_VOO;
                desenharSombra(g, galinha.getArea(), cameraX, cameraY);
            }
            animacaoGalinha.desenhar(g, galinha, galinha.getContadorPassos(), cameraX, cameraY, elevacao);
        }
    }

    private void desenharSombra(Graphics2D g, Rectangle area, int cameraX, int cameraY) {
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(area.x - cameraX, area.y + area.height - 4 - cameraY, area.width, 5);
    }

    private void desenharHeroi(Graphics2D g, Heroi heroi, int cameraX, int cameraY) {
        boolean invisivelNestePiscar = heroi.estaInvencivel() && heroi.getQuadrosInvencivel() % 8 < 4;
        if (!invisivelNestePiscar) {
            if (heroi.estaAtacando() && animacaoHeroiAtacando.temImagem()) {
                animacaoHeroiAtacando.desenhar(g, heroi, heroi.getQuadrosDesdeOInicioDoGolpe(), cameraX, cameraY, 0);
            } else {
                animacaoHeroi.desenhar(g, heroi, heroi.getContadorPassos(), cameraX, cameraY, 0);
            }
        }

        if (heroi.estaAtacando()) {
            Rectangle golpe = heroi.getAreaDoGolpe();
            int x = golpe.x - cameraX;
            int y = golpe.y - cameraY;
            if (imagemGolpe != null) {
                g.drawImage(imagemGolpe, x, y, golpe.width, golpe.height, null);
            } else {
                g.setColor(new Color(255, 255, 255, 160));
                g.fillRect(x, y, golpe.width, golpe.height);
            }
        }
    }

    // ---------- Tela final ----------

    private void desenharTelaFinal(Graphics2D g, Partida partida) {
        g.setColor(COR_DO_FUNDO);
        g.fillRect(0, 0, LARGURA, ALTURA);

        g.setFont(FONTE_TITULO);
        if (partida.isFugiu()) {
            Texto.escreverCentralizado(g, "Você fugiu!", 60, Color.YELLOW);
        } else {
            Texto.escreverCentralizado(g, "Derrotado...", 60, Color.RED);
        }

        g.setFont(FONTE_MENU);
        Texto.escreverCentralizado(g, "Galinhas derrotadas: " + partida.getGalinhasDerrotadas(), 105, Color.WHITE);
        Texto.escreverCentralizado(g, "Pontos: " + partida.getPontos(), 125, Color.WHITE);
        Texto.escreverCentralizado(g, "Recorde: " + partida.getRecorde(), 145, Color.LIGHT_GRAY);
        if (partida.isNovoRecorde() && piscando(300)) {
            Texto.escreverCentralizado(g, "NOVO RECORDE!", 170, Color.ORANGE);
        }

        g.setFont(FONTE_PEQUENA);
        if (piscando(500)) {
            Texto.escreverCentralizado(g, "Enter para voltar", 215, Color.WHITE);
        }
    }
}
