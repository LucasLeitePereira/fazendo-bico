package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import modelo.Animal;
import modelo.Decoracao;
import modelo.Efeito;
import modelo.Mapa;

/**
 * Desenha o mapa do Tiled: as camadas de tiles, as decorações (algumas animadas),
 * os animais e os efeitos.
 *
 * As camadas de tiles não mudam, então são desenhadas uma vez só numa imagem grande
 * (uma para as camadas de baixo, outra para as de cima). Depois basta copiar o pedaço
 * que a câmera mostra.
 */
public class Cenario {
    private static final Color COR_DO_CHAO = new Color(120, 180, 90);
    private static final Color COR_DO_SOLIDO = new Color(40, 75, 50);
    private static final Color COR_DA_DECORACAO = new Color(110, 80, 50);
    private static final String[] NOMES_DOS_EFEITOS = { "poeira", "penas", "folhas", "brilho", "fumaca" };
    private static final int[] LARGURA_DO_QUADRO_DOS_EFEITOS = { 24, 64, 64, 7, 24 };

    private Imagens imagens;
    private Map<String, BufferedImage> tilesets = new HashMap<String, BufferedImage>();
    private Map<String, BufferedImage> objetos = new HashMap<String, BufferedImage>();
    private Map<String, BufferedImage> animados = new HashMap<String, BufferedImage>();
    private Map<Mapa, BufferedImage[]> camadasProntas = new HashMap<Mapa, BufferedImage[]>();
    private BufferedImage[] efeitos = new BufferedImage[NOMES_DOS_EFEITOS.length];

    public Cenario(Imagens imagens) {
        this.imagens = imagens;
        for (int i = 0; i < NOMES_DOS_EFEITOS.length; i++) {
            efeitos[i] = imagens.carregar("/efeitos/" + NOMES_DOS_EFEITOS[i] + ".png");
        }
    }

    // ---------- Tiles ----------

    public void desenharChao(Graphics2D g, Mapa mapa, int cameraX, int cameraY) {
        g.drawImage(camadasDo(mapa)[0], -cameraX, -cameraY, null);
    }

    public void desenharCima(Graphics2D g, Mapa mapa, int cameraX, int cameraY) {
        BufferedImage cima = camadasDo(mapa)[1];
        if (cima != null) {
            g.drawImage(cima, -cameraX, -cameraY, null);
        }
    }

    private BufferedImage[] camadasDo(Mapa mapa) {
        BufferedImage[] prontas = camadasProntas.get(mapa);
        if (prontas == null) {
            // Guarda poucos mapas: os antigos não voltam mais.
            if (camadasProntas.size() > 3) {
                camadasProntas.clear();
            }
            prontas = new BufferedImage[2];
            prontas[0] = desenharCamadas(mapa, true);
            if (!mapa.getCamadasDeCima().isEmpty()) {
                prontas[1] = desenharCamadas(mapa, false);
            }
            camadasProntas.put(mapa, prontas);
        }
        return prontas;
    }

    private BufferedImage desenharCamadas(Mapa mapa, boolean deBaixo) {
        int tipo = BufferedImage.TYPE_INT_ARGB;
        if (deBaixo) {
            tipo = BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage imagem = new BufferedImage(mapa.getLarguraEmPixels(), mapa.getAlturaEmPixels(), tipo);
        Graphics2D g = imagem.createGraphics();
        BufferedImage tileset = tilesetDo(mapa);

        if (tileset == null) {
            if (deBaixo) {
                desenharSemTileset(g, mapa);
            }
            g.dispose();
            return imagem;
        }

        java.util.List<int[]> camadas = mapa.getCamadasDeCima();
        if (deBaixo) {
            g.setColor(COR_DO_CHAO);
            g.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
            camadas = mapa.getCamadasDeBaixo();
        }
        for (int i = 0; i < camadas.size(); i++) {
            desenharCamada(g, mapa, tileset, camadas.get(i));
        }
        g.dispose();
        return imagem;
    }

    private void desenharCamada(Graphics2D g, Mapa mapa, BufferedImage tileset, int[] codigos) {
        int tamanho = Mapa.TAMANHO_TILE;
        int colunasDoTileset = mapa.getColunasDoTileset();
        int totalDeTiles = colunasDoTileset * (tileset.getHeight() / tamanho);

        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i] == 0) {
                continue;
            }
            // Os bits mais altos dizem se o tile foi espelhado no Tiled.
            boolean espelhadoX = (codigos[i] & 0x80000000) != 0;
            boolean espelhadoY = (codigos[i] & 0x40000000) != 0;
            int indice = (codigos[i] & 0x0FFFFFFF) - mapa.getPrimeiroCodigoDoTileset();
            if (indice < 0 || indice >= totalDeTiles) {
                continue;
            }
            int origemX = (indice % colunasDoTileset) * tamanho;
            int origemY = (indice / colunasDoTileset) * tamanho;
            int x = (i % mapa.getColunas()) * tamanho;
            int y = (i / mapa.getColunas()) * tamanho;

            int x1 = x;
            int x2 = x + tamanho;
            if (espelhadoX) {
                x1 = x + tamanho;
                x2 = x;
            }
            int y1 = y;
            int y2 = y + tamanho;
            if (espelhadoY) {
                y1 = y + tamanho;
                y2 = y;
            }
            g.drawImage(tileset, x1, y1, x2, y2, origemX, origemY, origemX + tamanho, origemY + tamanho, null);
        }
    }

    /**
     * Sem a imagem do tileset: chão verde e quadrados escuros onde é sólido.
     */
    private void desenharSemTileset(Graphics2D g, Mapa mapa) {
        int tamanho = Mapa.TAMANHO_TILE;
        g.setColor(COR_DO_CHAO);
        g.fillRect(0, 0, mapa.getLarguraEmPixels(), mapa.getAlturaEmPixels());
        g.setColor(COR_DO_SOLIDO);
        for (int linha = 0; linha < mapa.getLinhas(); linha++) {
            for (int coluna = 0; coluna < mapa.getColunas(); coluna++) {
                if (mapa.isSolido(coluna, linha)) {
                    g.fillRect(coluna * tamanho, linha * tamanho, tamanho, tamanho);
                }
            }
        }
    }

    private BufferedImage tilesetDo(Mapa mapa) {
        String caminho = mapa.getImagemDoTileset();
        if (caminho == null) {
            return null;
        }
        if (!tilesets.containsKey(caminho)) {
            tilesets.put(caminho, imagens.carregar(caminho));
        }
        return tilesets.get(caminho);
    }

    // ---------- Decorações ----------

    /**
     * Imagem parada de um objeto (a mesma que aparece no Tiled), ou null se ela não existir.
     */
    public BufferedImage imagemDoObjeto(String tipo) {
        if (!objetos.containsKey(tipo)) {
            objetos.put(tipo, imagens.carregar("/objetos/" + tipo + ".png"));
        }
        return objetos.get(tipo);
    }

    /**
     * Tira com os quadros da animação do objeto, se ele for animado.
     */
    private BufferedImage animacaoDoObjeto(String tipo) {
        if (!animados.containsKey(tipo)) {
            animados.put(tipo, imagens.carregar("/objetos/animados/" + tipo + ".png"));
        }
        return animados.get(tipo);
    }

    private int velocidadeDaAnimacao(String tipo) {
        if (tipo.startsWith("arvore")) {
            return 14;
        }
        if (tipo.startsWith("fumaca")) {
            return 9;
        }
        if (tipo.startsWith("moinho") || tipo.startsWith("sombra_moinho")) {
            return 7;
        }
        return 10;
    }

    public boolean estaNaTela(Decoracao decoracao, int cameraX, int cameraY) {
        return decoracao.getX() + decoracao.getLargura() >= cameraX && decoracao.getX() <= cameraX + TelaJogo.LARGURA
                && decoracao.getY() + decoracao.getAltura() >= cameraY && decoracao.getY() <= cameraY + TelaJogo.ALTURA;
    }

    public void desenharDecoracao(Graphics2D g, Decoracao decoracao, int cameraX, int cameraY, int relogio) {
        int x = decoracao.getX() - cameraX;
        int y = decoracao.getY() - cameraY;
        String tipo = decoracao.getTipo();
        // Cada objeto começa a animação num quadro diferente, para as árvores não balançarem juntas.
        int defasagem = decoracao.getX() * 7 + decoracao.getY() * 3;
        boolean desenhou = desenharObjeto(g, tipo, x, y, decoracao.getLargura(), decoracao.getAltura(),
                decoracao.isEspelhada(), relogio / velocidadeDaAnimacao(tipo) + defasagem);

        if (!desenhou) {
            g.setColor(COR_DA_DECORACAO);
            g.fillRect(x + 2, y + decoracao.getAltura() / 2, decoracao.getLargura() - 4, decoracao.getAltura() / 2);
        }
    }

    /**
     * Desenha o quadro de um objeto (animado ou não) no retângulo indicado da tela.
     * Retorna false se não houver imagem.
     */
    private boolean desenharObjeto(Graphics2D g, String tipo, int x, int y, int largura, int altura,
            boolean espelhado, int quadro) {
        BufferedImage parada = imagemDoObjeto(tipo);
        if (parada == null) {
            return false;
        }
        BufferedImage fonte = parada;
        int origemX = 0;
        int larguraDoQuadro = parada.getWidth();
        BufferedImage tira = animacaoDoObjeto(tipo);
        if (tira != null) {
            int totalDeQuadros = tira.getWidth() / larguraDoQuadro;
            fonte = tira;
            origemX = (quadro % totalDeQuadros) * larguraDoQuadro;
        }

        int x1 = x;
        int x2 = x + largura;
        if (espelhado) {
            x1 = x + largura;
            x2 = x;
        }
        g.drawImage(fonte, x1, y, x2, y + altura, origemX, 0, origemX + larguraDoQuadro, parada.getHeight(), null);
        return true;
    }

    // ---------- Animais ----------

    public void desenharAnimal(Graphics2D g, Animal animal, int cameraX, int cameraY, int relogio) {
        String tipo = animal.getTipo();
        BufferedImage imagem = imagemDoObjeto(tipo);
        int peX = (int) animal.getCentroX() - cameraX;
        int peY = (int) (animal.getY() + animal.getAltura()) - cameraY;
        if (imagem == null) {
            g.setColor(Color.WHITE);
            g.fillRect((int) animal.getX() - cameraX, (int) animal.getY() - cameraY - 6, animal.getLargura(), animal.getAltura() + 6);
            return;
        }
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int x = peX - largura / 2;
        int y = peY + Animal.margemDaImagem(tipo) - altura;
        // Andando, a animação corre mais rápido; parado, o bicho só mexe a cabeça devagar.
        int quadro = relogio / 16 + (int) animal.getX();
        if (!animal.isParado()) {
            quadro = animal.getContadorPassos() / 8;
        }
        desenharObjeto(g, tipo, x, y, largura, altura, animal.isOlhandoParaEsquerda(), quadro);
    }

    // ---------- Efeitos ----------

    public void desenharEfeito(Graphics2D g, Efeito efeito, int cameraX, int cameraY) {
        BufferedImage tira = efeitos[efeito.getTipo()];
        int x = (int) efeito.getX() - cameraX;
        int y = (int) efeito.getY() - cameraY;
        if (tira == null) {
            if (efeito.getTipo() == Efeito.PENAS) {
                g.setColor(Color.WHITE);
                int espalhar = (int) (efeito.getProgresso() * 10);
                g.fillRect(x - espalhar, y - espalhar, 2, 2);
                g.fillRect(x + espalhar, y - espalhar / 2, 2, 2);
            }
            return;
        }
        int larguraDoQuadro = LARGURA_DO_QUADRO_DOS_EFEITOS[efeito.getTipo()];
        int totalDeQuadros = tira.getWidth() / larguraDoQuadro;
        int quadro = (int) (efeito.getProgresso() * totalDeQuadros);
        if (quadro >= totalDeQuadros) {
            quadro = totalDeQuadros - 1;
        }
        int altura = tira.getHeight();
        int origemX = quadro * larguraDoQuadro;
        int esquerda = x - larguraDoQuadro / 2;
        int direita = esquerda + larguraDoQuadro;
        if (efeito.isEspelhado()) {
            int troca = esquerda;
            esquerda = direita;
            direita = troca;
        }
        g.drawImage(tira, esquerda, y - altura / 2, direita, y - altura / 2 + altura,
                origemX, 0, origemX + larguraDoQuadro, altura, null);
    }
}
