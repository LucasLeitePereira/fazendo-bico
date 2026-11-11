package modelo;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapa lido de um arquivo do Tiled (.tmx). Guarda as camadas de tiles, quais tiles
 * são sólidos, as decorações, os pontos marcados (herói, moradores, galinha) e a saída.
 */
public class Mapa {
    public static final int TAMANHO_TILE = 16;

    private int colunas;
    private int linhas;
    private List<int[]> camadasDeBaixo = new ArrayList<int[]>();
    private List<int[]> camadasDeCima = new ArrayList<int[]>();
    private boolean[] solido;

    private String imagemDoTileset;
    private int primeiroCodigoDoTileset = 1;
    private int colunasDoTileset = 1;

    private Decoracao[] decoracoes = new Decoracao[0];
    private Decoracao[] animais = new Decoracao[0];
    private Map<String, List<Point>> marcadores = new HashMap<String, List<Point>>();
    private Rectangle saida;

    public Mapa(int colunas, int linhas) {
        this.colunas = colunas;
        this.linhas = linhas;
        solido = new boolean[colunas * linhas];
    }

    /**
     * Cada camada é um vetor com um código de tile por posição (linha * colunas + coluna).
     * Zero quer dizer posição vazia.
     */
    public void adicionarCamada(int[] codigos, boolean desenharPorCima) {
        if (desenharPorCima) {
            camadasDeCima.add(codigos);
        } else {
            camadasDeBaixo.add(codigos);
        }
    }

    public void definirTileset(String imagem, int primeiroCodigo, int colunasDaImagem) {
        imagemDoTileset = imagem;
        primeiroCodigoDoTileset = primeiroCodigo;
        colunasDoTileset = colunasDaImagem;
    }

    public void marcarSolido(int coluna, int linha) {
        solido[linha * colunas + coluna] = true;
    }

    public void adicionarMarcador(String nome, int x, int y) {
        List<Point> pontos = marcadores.get(nome);
        if (pontos == null) {
            pontos = new ArrayList<Point>();
            marcadores.put(nome, pontos);
        }
        pontos.add(new Point(x, y));
    }

    /**
     * Pontos marcados com este nome no Tiled. A lista vem vazia se não houver nenhum.
     */
    public List<Point> encontrarMarcadores(String nome) {
        List<Point> pontos = marcadores.get(nome);
        if (pontos == null) {
            return new ArrayList<Point>();
        }
        return pontos;
    }

    public Point encontrarMarcador(String nome) {
        List<Point> pontos = encontrarMarcadores(nome);
        if (pontos.isEmpty()) {
            throw new IllegalStateException("O mapa não tem o ponto \"" + nome + "\".");
        }
        return pontos.get(0);
    }

    /**
     * Fora do mapa conta como sólido, para ninguém sair pela borda.
     */
    public boolean colide(Rectangle area) {
        if (area.x < 0 || area.y < 0 || area.x + area.width > getLarguraEmPixels()
                || area.y + area.height > getAlturaEmPixels()) {
            return true;
        }

        int primeiraColuna = area.x / TAMANHO_TILE;
        int ultimaColuna = (area.x + area.width - 1) / TAMANHO_TILE;
        int primeiraLinha = area.y / TAMANHO_TILE;
        int ultimaLinha = (area.y + area.height - 1) / TAMANHO_TILE;
        for (int linha = primeiraLinha; linha <= ultimaLinha; linha++) {
            for (int coluna = primeiraColuna; coluna <= ultimaColuna; coluna++) {
                if (isSolido(coluna, linha)) {
                    return true;
                }
            }
        }

        for (int i = 0; i < decoracoes.length; i++) {
            if (decoracoes[i].bloqueia(area)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSolido(int coluna, int linha) {
        if (coluna < 0 || linha < 0 || coluna >= colunas || linha >= linhas) {
            return true;
        }
        return solido[linha * colunas + coluna];
    }

    public boolean estaNaSaida(float x, float y) {
        return saida != null && saida.contains(x, y);
    }

    public List<int[]> getCamadasDeBaixo() {
        return camadasDeBaixo;
    }

    public List<int[]> getCamadasDeCima() {
        return camadasDeCima;
    }

    public String getImagemDoTileset() {
        return imagemDoTileset;
    }

    public int getPrimeiroCodigoDoTileset() {
        return primeiroCodigoDoTileset;
    }

    public int getColunasDoTileset() {
        return colunasDoTileset;
    }

    public Decoracao[] getDecoracoes() {
        return decoracoes;
    }

    public void setDecoracoes(Decoracao[] decoracoes) {
        this.decoracoes = decoracoes;
    }

    /**
     * Animais desenhados no Tiled. O jogo cria um animal vivo no lugar de cada um.
     */
    public Decoracao[] getAnimais() {
        return animais;
    }

    public void setAnimais(Decoracao[] animais) {
        this.animais = animais;
    }

    public Rectangle getSaida() {
        return saida;
    }

    public void setSaida(Rectangle saida) {
        this.saida = saida;
    }

    public int getColunas() {
        return colunas;
    }

    public int getLinhas() {
        return linhas;
    }

    public int getLarguraEmPixels() {
        return colunas * TAMANHO_TILE;
    }

    public int getAlturaEmPixels() {
        return linhas * TAMANHO_TILE;
    }
}
