package modelo;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Grade de tiles lida de um arquivo de texto. Cada caractere é um tile:
 * '.' grama, ',' flor, '=' caminho, 'S' saída, 'P' porta,
 * '#' árvore, '~' água, 'C' parede, 'T' telhado, 'F' cerca (esses cinco são sólidos).
 * Marcadores viram grama e guardam a posição inicial de alguém:
 * 'H' herói, '1' a '4' moradores, 'G' galinha.
 */
public class Mapa {
    public static final int TAMANHO_TILE = 16;
    public static final char GRAMA = '.';
    public static final char SAIDA = 'S';
    private static final String TILES_SOLIDOS = "#~CTF";
    private static final String MARCADORES = "H1234G";
    private static final char SEM_MARCADOR = ' ';

    private char[][] tiles;
    private char[][] marcadores;
    private int colunas;
    private int linhas;

    public Mapa(List<String> linhasDoArquivo) {
        linhas = linhasDoArquivo.size();
        colunas = linhasDoArquivo.get(0).length();
        tiles = new char[linhas][colunas];
        marcadores = new char[linhas][colunas];

        for (int linha = 0; linha < linhas; linha++) {
            String texto = linhasDoArquivo.get(linha);
            if (texto.length() != colunas) {
                throw new IllegalArgumentException("A linha " + (linha + 1) + " do mapa tem "
                        + texto.length() + " caracteres, mas deveria ter " + colunas);
            }
            for (int coluna = 0; coluna < colunas; coluna++) {
                char caractere = texto.charAt(coluna);
                if (MARCADORES.indexOf(caractere) >= 0) {
                    marcadores[linha][coluna] = caractere;
                    tiles[linha][coluna] = GRAMA;
                } else {
                    marcadores[linha][coluna] = SEM_MARCADOR;
                    tiles[linha][coluna] = caractere;
                }
            }
        }
    }

    /**
     * Retorna o canto superior esquerdo, em pixels, de cada tile que tem o marcador.
     */
    public List<Point> encontrarMarcadores(char marcador) {
        List<Point> posicoes = new ArrayList<Point>();
        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                if (marcadores[linha][coluna] == marcador) {
                    posicoes.add(new Point(coluna * TAMANHO_TILE, linha * TAMANHO_TILE));
                }
            }
        }
        return posicoes;
    }

    /**
     * Fora do mapa conta como sólido, assim ninguém sai andando pela borda.
     */
    public boolean colide(Rectangle area) {
        if (area.x < 0 || area.y < 0) {
            return true;
        }
        if (area.x + area.width > getLarguraEmPixels() || area.y + area.height > getAlturaEmPixels()) {
            return true;
        }

        int colunaInicial = area.x / TAMANHO_TILE;
        int colunaFinal = (area.x + area.width - 1) / TAMANHO_TILE;
        int linhaInicial = area.y / TAMANHO_TILE;
        int linhaFinal = (area.y + area.height - 1) / TAMANHO_TILE;

        for (int linha = linhaInicial; linha <= linhaFinal; linha++) {
            for (int coluna = colunaInicial; coluna <= colunaFinal; coluna++) {
                if (ehSolido(tiles[linha][coluna])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean ehSolido(char tile) {
        return TILES_SOLIDOS.indexOf(tile) >= 0;
    }

    public char getTileNaPosicao(float pixelX, float pixelY) {
        int coluna = (int) (pixelX / TAMANHO_TILE);
        int linha = (int) (pixelY / TAMANHO_TILE);
        if (coluna < 0 || linha < 0 || coluna >= colunas || linha >= linhas) {
            return GRAMA;
        }
        return tiles[linha][coluna];
    }

    public char getTile(int coluna, int linha) {
        return tiles[linha][coluna];
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
