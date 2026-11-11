package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JPanel;

import modelo.Animal;
import modelo.Decoracao;
import modelo.Dialogo;
import modelo.Efeito;
import modelo.Galinha;
import modelo.Heroi;
import modelo.Mapa;
import modelo.Morador;
import modelo.Mundo;
import modelo.Partida;

/**
 * Desenha a cena atual. O jogo é desenhado em 320x240 e ampliado 2 vezes,
 * para os pixels ficarem grandes como nos jogos retrô.
 *
 * Para um personagem passar atrás de uma árvore e na frente de outra, tudo o que fica
 * "em pé" no mapa entra numa fila e é desenhado de cima para baixo, pela altura do pé.
 */
public class TelaJogo extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int LARGURA = 320;
    public static final int ALTURA = 240;
    public static final int ESCALA = 2;

    private static final Color COR_DO_FUNDO = new Color(30, 30, 50);
    private static final Color COR_DO_TEXTO_ESCURO = new Color(70, 42, 25);
    private static final Color COR_DO_TITULO = new Color(255, 214, 90);
    private static final Color COR_DAS_MOEDAS = new Color(255, 220, 110);
    private static final int ELEVACAO_DO_VOO = 6;
    private static final int VIDA_DA_GALINHA_NO_CHAO = 3;

    private Mundo mundo;
    private Random sorteio = new Random();
    private Cenario cenario;
    private Hud hud;
    private CaixaDialogo caixaDialogo;

    private BufferedImage imagemTelaInicial;
    private BufferedImage imagemGolpe;
    private BufferedImage sombra;
    private BufferedImage balaoAlerta;
    private BufferedImage balaoConversa;
    private BufferedImage balaoAmor;
    private BufferedImage balaoRaiva;
    private BufferedImage barraDeVida;
    private BufferedImage mao;
    private BufferedImage moeda;
    private BufferedImage espada;
    private BufferedImage brilho;
    private CaixaNoveFatias caixaClara;
    private CaixaNoveFatias caixaEscura;

    private Animacao animacaoHeroiParado;
    private Animacao animacaoHeroiCorrendo;
    private Animacao animacaoHeroiAtacando;
    private Animacao animacaoHeroiComDano;
    private Animacao animacaoHeroiMorrendo;
    private Animacao animacaoGalinhaAndando;
    private Animacao animacaoGalinhaVoando;
    private Animacao[] animacaoMoradores = new Animacao[5];

    // Fila de desenho, ordenada pela altura do pé. Só a tela usa.
    private Object[] fila = new Object[256];
    private int[] alturaDoPe = new int[256];
    private int tamanhoDaFila;

    public TelaJogo(Mundo mundo) {
        this.mundo = mundo;
        setPreferredSize(new Dimension(LARGURA * ESCALA, ALTURA * ESCALA));
        setBackground(COR_DO_FUNDO);

        Imagens imagens = new Imagens();
        cenario = new Cenario(imagens);
        hud = new Hud(imagens);
        imagemTelaInicial = imagens.carregar("/telas/inicial.png");
        imagemGolpe = imagens.carregar("/sprites/golpe.png");
        sombra = imagens.carregar("/objetos/sombra.png");
        balaoAlerta = imagens.carregar("/interface/balao_alerta.png");
        balaoConversa = imagens.carregar("/interface/balao_conversa.png");
        balaoAmor = imagens.carregar("/interface/balao_amor.png");
        balaoRaiva = imagens.carregar("/interface/balao_raiva.png");
        barraDeVida = imagens.carregar("/interface/barra_vermelha.png");
        mao = imagens.carregar("/interface/mao.png");
        moeda = imagens.carregar("/interface/moedas.png");
        espada = imagens.carregar("/interface/espada.png");
        brilho = imagens.carregar("/efeitos/brilho.png");
        caixaClara = new CaixaNoveFatias(imagens.carregar("/interface/caixa_clara.png"), new Color(225, 190, 150));
        caixaEscura = new CaixaNoveFatias(imagens.carregar("/interface/caixa_escura.png"), new Color(60, 40, 30));

        Color verde = new Color(60, 170, 80);
        animacaoHeroiParado = criarAnimacaoDePessoa(imagens, "heroi_parado", verde, 8);
        animacaoHeroiCorrendo = criarAnimacaoDePessoa(imagens, "heroi_correndo", verde, 5);
        animacaoHeroiAtacando = criarAnimacaoDePessoa(imagens, "heroi_ataque", verde, 2);
        animacaoHeroiComDano = criarAnimacaoDePessoa(imagens, "heroi_dano", verde, 2);
        animacaoHeroiMorrendo = criarAnimacaoDePessoa(imagens, "heroi_morte", verde, 6);
        animacaoHeroiMorrendo.setRepetir(false);

        BufferedImage[] retratos = new BufferedImage[5];
        Color[] cores = { null, new Color(90, 90, 200), new Color(200, 90, 160), new Color(150, 110, 60), new Color(230, 160, 40) };
        int[] velocidades = { 0, 9, 10, 11, 7 };
        for (int numero = 1; numero <= 4; numero++) {
            BufferedImage folha = imagens.carregar("/sprites/morador" + numero + ".png");
            animacaoMoradores[numero] = new Animacao(folha, 96, 64, cores[numero]);
            animacaoMoradores[numero].definirPe(48, 39);
            animacaoMoradores[numero].setQuadrosDoJogoPorQuadro(velocidades[numero]);
            if (folha != null) {
                // O retrato é o rosto e o peito do primeiro quadro.
                retratos[numero] = folha.getSubimage(38, 19, 20, 20);
            }
        }
        caixaDialogo = new CaixaDialogo(imagens, retratos);

        BufferedImage imagemDaGalinha = imagens.carregar("/sprites/galinha.png");
        animacaoGalinhaAndando = criarAnimacaoDeGalinha(imagemDaGalinha, 8);
        animacaoGalinhaVoando = criarAnimacaoDeGalinha(imagemDaGalinha, 3);
    }

    /**
     * Os personagens do pacote Sunnyside World têm quadros de 96x64
     * com os pés no ponto (48, 39) e olham para a direita.
     */
    private Animacao criarAnimacaoDePessoa(Imagens imagens, String nome, Color corReserva, int velocidade) {
        Animacao animacao = new Animacao(imagens.carregar("/sprites/" + nome + ".png"), 96, 64, corReserva);
        animacao.definirPe(48, 39);
        animacao.setQuadrosDoJogoPorQuadro(velocidade);
        return animacao;
    }

    /**
     * A galinha tem quadros de 32x32, com os pés no ponto (15, 24), e olha para a esquerda.
     */
    private Animacao criarAnimacaoDeGalinha(BufferedImage imagem, int velocidade) {
        Animacao animacao = new Animacao(imagem, 32, 32, Color.WHITE);
        animacao.definirPe(15, 24);
        animacao.setDesenhoOlhaParaEsquerda(true);
        animacao.setQuadrosDoJogoPorQuadro(velocidade);
        return animacao;
    }

    /**
     * Relógio da tela, para animações que rodam sempre (personagens parados, árvores).
     */
    private int quadroAtualDaTela() {
        return (int) (System.currentTimeMillis() / 16);
    }

    private boolean piscando(int milissegundos) {
        return (System.currentTimeMillis() / milissegundos) % 2 == 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(ESCALA, ESCALA);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Partida partida = mundo.getPartida();
        switch (partida.getCena()) {
            case INICIAL:
                desenharTelaInicial(g2, partida);
                break;
            case VILA:
                desenharJogo(g2, partida, false);
                break;
            case CAMPO:
                desenharJogo(g2, partida, true);
                break;
            case FIM:
                desenharTelaFinal(g2, partida);
                break;
        }
    }

    // ---------- Vila e campo ----------

    private void desenharJogo(Graphics2D g, Partida partida, boolean noCampo) {
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

        desenharCena(g, mapa, cameraX, cameraY, true);

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

    /**
     * Desenha o mapa na ordem certa: chão, sombras, tudo o que fica em pé (ordenado pelo pé),
     * a camada de cima, o que fica por cima de tudo e os efeitos.
     */
    private void desenharCena(Graphics2D g, Mapa mapa, int cameraX, int cameraY, boolean comPersonagens) {
        int relogio = quadroAtualDaTela();
        cenario.desenharChao(g, mapa, cameraX, cameraY);

        Decoracao[] decoracoes = mapa.getDecoracoes();
        tamanhoDaFila = 0;
        for (int i = 0; i < decoracoes.length; i++) {
            Decoracao decoracao = decoracoes[i];
            if (!cenario.estaNaTela(decoracao, cameraX, cameraY)) {
                continue;
            }
            if (decoracao.getCamada() == Decoracao.NO_CHAO) {
                cenario.desenharDecoracao(g, decoracao, cameraX, cameraY, relogio);
            } else if (decoracao.getCamada() == Decoracao.EM_PE) {
                colocarNaFila(decoracao, decoracao.getPeY());
            }
        }

        if (comPersonagens) {
            colocarPersonagensNaFila(g, cameraX, cameraY);
        }

        ordenarFila();
        for (int i = 0; i < tamanhoDaFila; i++) {
            desenharDaFila(g, fila[i], cameraX, cameraY, relogio);
            fila[i] = null;
        }

        cenario.desenharCima(g, mapa, cameraX, cameraY);
        for (int i = 0; i < decoracoes.length; i++) {
            Decoracao decoracao = decoracoes[i];
            if (decoracao.getCamada() == Decoracao.POR_CIMA && cenario.estaNaTela(decoracao, cameraX, cameraY)) {
                cenario.desenharDecoracao(g, decoracao, cameraX, cameraY, relogio);
            }
        }

        if (comPersonagens) {
            Efeito[] efeitos = mundo.getEfeitos();
            for (int i = 0; i < efeitos.length; i++) {
                if (efeitos[i].isAtivo()) {
                    cenario.desenharEfeito(g, efeitos[i], cameraX, cameraY);
                }
            }
            desenharBaloes(g, cameraX, cameraY);
        }
    }

    /**
     * Põe animais, moradores, galinhas e herói na fila. As sombras são desenhadas
     * já aqui, porque ficam no chão, embaixo de tudo.
     */
    private void colocarPersonagensNaFila(Graphics2D g, int cameraX, int cameraY) {
        Animal[] animais = mundo.getAnimais();
        for (int i = 0; i < animais.length; i++) {
            colocarNaFila(animais[i], pe(animais[i].getY(), animais[i].getAltura()));
        }

        Morador[] moradores = mundo.getMoradores();
        for (int i = 0; i < moradores.length; i++) {
            desenharSombra(g, moradores[i].getArea(), cameraX, cameraY);
            colocarNaFila(moradores[i], pe(moradores[i].getY(), moradores[i].getAltura()));
        }

        Galinha[] galinhas = mundo.getGalinhas();
        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (galinha.isAtiva()) {
                desenharSombra(g, galinha.getArea(), cameraX, cameraY);
                colocarNaFila(galinha, pe(galinha.getY(), galinha.getAltura()));
            }
        }

        Heroi heroi = mundo.getHeroi();
        if (heroi != null) {
            if (heroi.estaVivo()) {
                desenharSombra(g, heroi.getArea(), cameraX, cameraY);
            }
            colocarNaFila(heroi, pe(heroi.getY(), heroi.getAltura()));
        }
    }

    private int pe(float y, int altura) {
        return (int) y + altura;
    }

    private void colocarNaFila(Object item, int alturaDoPeDoItem) {
        if (tamanhoDaFila == fila.length) {
            Object[] filaMaior = new Object[fila.length * 2];
            int[] alturasMaior = new int[fila.length * 2];
            for (int i = 0; i < fila.length; i++) {
                filaMaior[i] = fila[i];
                alturasMaior[i] = alturaDoPe[i];
            }
            fila = filaMaior;
            alturaDoPe = alturasMaior;
        }
        fila[tamanhoDaFila] = item;
        alturaDoPe[tamanhoDaFila] = alturaDoPeDoItem;
        tamanhoDaFila++;
    }

    /**
     * Ordenação por inserção: quem tem o pé mais acima é desenhado primeiro.
     */
    private void ordenarFila() {
        for (int i = 1; i < tamanhoDaFila; i++) {
            Object item = fila[i];
            int altura = alturaDoPe[i];
            int j = i - 1;
            while (j >= 0 && alturaDoPe[j] > altura) {
                fila[j + 1] = fila[j];
                alturaDoPe[j + 1] = alturaDoPe[j];
                j--;
            }
            fila[j + 1] = item;
            alturaDoPe[j + 1] = altura;
        }
    }

    private void desenharDaFila(Graphics2D g, Object item, int cameraX, int cameraY, int relogio) {
        if (item instanceof Decoracao) {
            cenario.desenharDecoracao(g, (Decoracao) item, cameraX, cameraY, relogio);
        } else if (item instanceof Animal) {
            cenario.desenharAnimal(g, (Animal) item, cameraX, cameraY, relogio);
        } else if (item instanceof Morador) {
            Morador morador = (Morador) item;
            animacaoMoradores[morador.getNumero()].desenhar(g, morador, relogio, cameraX, cameraY, 0);
        } else if (item instanceof Galinha) {
            desenharGalinha(g, (Galinha) item, cameraX, cameraY);
        } else if (item instanceof Heroi) {
            desenharHeroi(g, (Heroi) item, cameraX, cameraY, relogio);
        }
    }

    private void desenharSombra(Graphics2D g, Rectangle area, int cameraX, int cameraY) {
        int largura = area.width + 4;
        int x = area.x - 2 - cameraX;
        int y = area.y + area.height - 4 - cameraY;
        if (sombra != null) {
            // A sombra do pacote ocupa as linhas 4 a 12 da imagem de 16x16.
            g.drawImage(sombra, x, y, x + largura, y + 6, 1, 4, 15, 13, null);
        } else {
            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(x, y, largura, 6);
        }
    }

    private void desenharGalinha(Graphics2D g, Galinha galinha, int cameraX, int cameraY) {
        if (galinha.estaPiscando() && galinha.getQuadrosPiscando() % 4 < 2) {
            return;
        }
        if (galinha.isVoando()) {
            animacaoGalinhaVoando.desenhar(g, galinha, galinha.getContadorPassos(), cameraX, cameraY, ELEVACAO_DO_VOO);
            return;
        }
        animacaoGalinhaAndando.desenhar(g, galinha, galinha.getContadorPassos(), cameraX, cameraY, 0);

        // Depois do primeiro golpe, a galinha do chão mostra a vida e fica brava.
        if (galinha.getVida() < VIDA_DA_GALINHA_NO_CHAO) {
            int x = (int) galinha.getCentroX() - cameraX;
            int topo = (int) galinha.getY() - cameraY - 14;
            if (barraDeVida != null) {
                int quadro = galinha.getVida() * 6 / VIDA_DA_GALINHA_NO_CHAO;
                g.drawImage(barraDeVida, x - 7, topo, x + 8, topo + 7, quadro * 15, 0, quadro * 15 + 15, 7, null);
            }
            if (balaoRaiva != null) {
                g.drawImage(balaoRaiva, x + 7, topo - 9, null);
            }
        }
    }

    private void desenharHeroi(Graphics2D g, Heroi heroi, int cameraX, int cameraY, int relogio) {
        if (!heroi.estaVivo()) {
            animacaoHeroiMorrendo.desenhar(g, heroi, heroi.getQuadrosDesdeAMorte(), cameraX, cameraY, 0);
            return;
        }
        if (heroi.estaAtacando()) {
            animacaoHeroiAtacando.desenhar(g, heroi, heroi.getQuadrosDesdeOInicioDoGolpe(), cameraX, cameraY, 0);
        } else if (heroi.estaInvencivel() && heroi.acabouDeLevarDano()) {
            animacaoHeroiComDano.desenhar(g, heroi, heroi.getQuadrosDesdeODano(), cameraX, cameraY, 0);
        } else {
            boolean invisivelNestePiscar = heroi.estaInvencivel() && heroi.getQuadrosInvencivel() % 8 < 4;
            if (!invisivelNestePiscar) {
                if (heroi.isAndando()) {
                    animacaoHeroiCorrendo.desenhar(g, heroi, heroi.getContadorPassos(), cameraX, cameraY, 0);
                } else {
                    animacaoHeroiParado.desenhar(g, heroi, relogio, cameraX, cameraY, 0);
                }
            }
        }

        // O sprite de ataque já desenha a espada; o retângulo só aparece se ele faltar.
        if (heroi.estaAtacando() && !animacaoHeroiAtacando.temImagem()) {
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

    /**
     * Balões em cima dos moradores: "!" no prefeito enquanto ele tem a missão,
     * e um balão de conversa (ou de coração) em quem está perto do herói.
     */
    private void desenharBaloes(Graphics2D g, int cameraX, int cameraY) {
        Dialogo dialogo = mundo.getDialogo();
        Heroi heroi = mundo.getHeroi();
        if (dialogo.isAtivo() || heroi == null) {
            return;
        }
        boolean missaoAceita = mundo.getPartida().isMissaoAceita();
        int pulo = (int) ((System.currentTimeMillis() / 300) % 2);

        Morador[] moradores = mundo.getMoradores();
        for (int i = 0; i < moradores.length; i++) {
            Morador morador = moradores[i];
            BufferedImage balao = null;
            if (morador.isDaMissao() && !missaoAceita) {
                balao = balaoAlerta;
            } else if (morador.estaPerto(heroi)) {
                balao = balaoConversa;
                if (morador.isDaMissao()) {
                    balao = balaoAmor;
                }
            }
            if (balao == null) {
                continue;
            }
            int x = (int) morador.getCentroX() - cameraX - balao.getWidth() / 2;
            int y = (int) (morador.getY() + morador.getAltura()) - cameraY - 26 - balao.getHeight() - pulo;
            g.drawImage(balao, x, y, null);
        }
    }

    // ---------- Tela inicial ----------

    private void desenharTelaInicial(Graphics2D g, Partida partida) {
        boolean temImagemPronta = imagemTelaInicial != null;
        if (temImagemPronta) {
            g.drawImage(imagemTelaInicial, 0, 0, LARGURA, ALTURA, null);
        } else {
            desenharVilaAoFundo(g);
            desenharPlacaDoTitulo(g);
        }

        desenharMenu(g, partida);

        g.setFont(Texto.NORMAL);
        if (piscando(500)) {
            Texto.escreverCentralizado(g, "Setas para escolher, Enter para confirmar", 222, Color.WHITE);
        }
        if (moeda != null) {
            g.drawImage(moeda, 5, ALTURA - 15, null);
        }
        Texto.escrever(g, "Recorde: " + partida.getRecorde(), 24, ALTURA - 6, COR_DAS_MOEDAS);
    }

    /**
     * A vila aparece ao fundo, com a câmera passeando devagar, coberta por um véu escuro.
     */
    private void desenharVilaAoFundo(Graphics2D g) {
        Mapa fundo = mundo.getMapaDoFundo();
        if (fundo == null) {
            g.setColor(COR_DO_FUNDO);
            g.fillRect(0, 0, LARGURA, ALTURA);
            return;
        }
        double tempo = System.currentTimeMillis() / 12000.0;
        int folgaX = Math.max(0, fundo.getLarguraEmPixels() - LARGURA);
        int folgaY = Math.max(0, fundo.getAlturaEmPixels() - ALTURA);
        int cameraX = (int) ((Math.sin(tempo) * 0.5 + 0.5) * folgaX);
        int cameraY = (int) ((Math.cos(tempo * 0.7) * 0.3 + 0.5) * folgaY);
        desenharCena(g, fundo, cameraX, cameraY, false);

        g.setColor(new Color(15, 20, 40, 120));
        g.fillRect(0, 0, LARGURA, ALTURA);
    }

    private void desenharPlacaDoTitulo(Graphics2D g) {
        int largura = 250;
        int x = (LARGURA - largura) / 2;
        int y = 26;
        caixaClara.desenhar(g, x, y, largura, 62);

        g.setFont(Texto.TITULO);
        String titulo = Janela.NOME_DO_JOGO;
        Texto.escreverComContorno(g, titulo, Texto.centralizar(g, titulo, LARGURA / 2), y + 33,
                COR_DO_TITULO, new Color(70, 35, 20), 1f);
        g.setFont(Texto.NORMAL);
        String subtitulo = "A missão é bem paga. As galinhas, nem tanto.";
        Texto.escreverSemSombra(g, subtitulo, Texto.centralizar(g, subtitulo, LARGURA / 2), y + 52, COR_DO_TEXTO_ESCURO);

        if (espada != null) {
            g.drawImage(espada, x + 8, y + 8, null);
            g.drawImage(espada, x + largura - 8, y + 8, x + largura - 8 - espada.getWidth(), y + 8 + espada.getHeight(),
                    0, 0, espada.getWidth(), espada.getHeight(), null);
        }
    }

    private void desenharMenu(Graphics2D g, Partida partida) {
        int largura = 96;
        int x = (LARGURA - largura) / 2;
        int y = 116;
        caixaEscura.desenhar(g, x, y, largura, 52);

        g.setFont(Texto.MEDIA);
        String[] opcoes = { "Jogar", "Sair" };
        for (int i = 0; i < opcoes.length; i++) {
            int linhaY = y + 21 + i * 18;
            Color cor = new Color(220, 200, 180);
            if (i == partida.getOpcaoDoMenu()) {
                cor = COR_DO_TITULO;
                int balanco = (int) ((System.currentTimeMillis() / 200) % 2);
                if (mao != null) {
                    g.drawImage(mao, x + 10 + balanco, linhaY - 10, null);
                } else {
                    Texto.escrever(g, ">", x + 12 + balanco, linhaY, cor);
                }
            }
            Texto.escrever(g, opcoes[i], x + 32, linhaY, cor);
        }
    }

    // ---------- Tela final ----------

    private void desenharTelaFinal(Graphics2D g, Partida partida) {
        Mapa mapa = mundo.getMapa();
        Heroi heroi = mundo.getHeroi();
        if (mapa != null && heroi != null) {
            int cameraX = calcularCamera(heroi.getCentroX(), mapa.getLarguraEmPixels(), LARGURA);
            int cameraY = calcularCamera(heroi.getCentroY(), mapa.getAlturaEmPixels(), ALTURA);
            desenharCena(g, mapa, cameraX, cameraY, true);
        } else {
            g.setColor(COR_DO_FUNDO);
            g.fillRect(0, 0, LARGURA, ALTURA);
        }
        g.setColor(new Color(10, 10, 25, 150));
        g.fillRect(0, 0, LARGURA, ALTURA);

        int largura = 210;
        int altura = 150;
        int x = (LARGURA - largura) / 2;
        int y = 30;
        caixaClara.desenhar(g, x, y, largura, altura);
        int centro = LARGURA / 2;

        g.setFont(Texto.GRANDE);
        String titulo = "Derrotado...";
        Color corDoTitulo = new Color(230, 70, 60);
        if (partida.isFugiu()) {
            titulo = "Você fugiu!";
            corDoTitulo = COR_DO_TITULO;
        }
        Texto.escreverComContorno(g, titulo, Texto.centralizar(g, titulo, centro), y + 24, corDoTitulo, new Color(70, 35, 20), 0.75f);

        g.setFont(Texto.MEDIA);
        String galinhas = "Galinhas derrotadas: " + partida.getGalinhasDerrotadas();
        Texto.escreverSemSombra(g, galinhas, Texto.centralizar(g, galinhas, centro), y + 52, COR_DO_TEXTO_ESCURO);

        String moedas = "Moedas: " + partida.getPontos();
        float moedasX = Texto.centralizar(g, moedas, centro + 10);
        if (moeda != null) {
            g.drawImage(moeda, (int) moedasX - 20, y + 62, null);
        }
        Texto.escreverSemSombra(g, moedas, moedasX, y + 72, COR_DO_TEXTO_ESCURO);

        String recorde = "Recorde: " + partida.getRecorde();
        Texto.escreverSemSombra(g, recorde, Texto.centralizar(g, recorde, centro), y + 92, new Color(120, 85, 60));

        if (partida.isNovoRecorde()) {
            g.setFont(Texto.GRANDE);
            String novo = "NOVO RECORDE!";
            float novoX = Texto.centralizar(g, novo, centro);
            if (piscando(300)) {
                Texto.escreverComContorno(g, novo, novoX, y + 116, new Color(255, 170, 40), new Color(90, 40, 10), 0.5f);
            }
            desenharBrilho(g, (int) novoX - 12, y + 106);
            desenharBrilho(g, (int) (novoX + Texto.largura(g, novo)) + 5, y + 106);
        }

        g.setFont(Texto.NORMAL);
        if (piscando(500)) {
            String voltar = "Enter para voltar";
            Texto.escreverSemSombra(g, voltar, Texto.centralizar(g, voltar, centro), y + altura - 10, COR_DO_TEXTO_ESCURO);
        }
    }

    private void desenharBrilho(Graphics2D g, int x, int y) {
        if (brilho == null) {
            return;
        }
        int quadro = (int) ((System.currentTimeMillis() / 90) % 6);
        g.drawImage(brilho, x, y, x + 7, y + 7, quadro * 7, 0, quadro * 7 + 7, 7, null);
    }
}
