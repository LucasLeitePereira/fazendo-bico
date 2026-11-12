package dao;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import modelo.Decoracao;
import modelo.Mapa;

/**
 * Lê mapas feitos no Tiled (https://www.mapeditor.org), no formato .tmx.
 *
 * O que o jogo entende:
 * - camadas de tiles em CSV (o padrão do Tiled). A camada "colisao" diz quais tiles são sólidos
 *   e não é desenhada; camadas com nome começando por "cima" ficam por cima dos personagens;
 * - a camada de objetos: objetos de imagem (decorações e animais, pelo nome do arquivo da imagem),
 *   pontos com nome (heroi, morador1...morador4, galinha) e o retângulo "saida";
 * - a camada de objetos "colisores": cada retângulo desenhado nela é sólido, de qualquer tamanho;
 * - no objetos.tsx, a colisão de cada imagem, feita no Editor de Colisão do Tiled
 *   (o tronco da árvore, a base do barril). Imagem sem colisão pode ser atravessada.
 */
public class DaoMapa {
    // Os três bits mais altos do código guardam se o tile foi espelhado no Tiled.
    private static final long ESPELHADO_NA_HORIZONTAL = 0x80000000L;
    private static final long SEM_ESPELHAMENTO = 0x0FFFFFFFL;

    private static final String CAMADA_DE_COLISORES = "colisores";

    private static final String[] ANIMAIS = { "vaca", "porco", "ovelha", "pato" };
    private static final String[] NO_CHAO = { "sombra", "sombra_moinho" };
    private static final String[] POR_CIMA = { "moinho", "fumaca1", "fumaca2", "fumaca3", "fumaca4", "fumaca5" };

    /**
     * Uma imagem do tileset de objetos: o nome do arquivo, o tamanho e as partes sólidas.
     */
    private static class ImagemDeObjeto {
        String nome;
        int largura;
        int altura;
        Rectangle[] colisores;
    }

    /**
     * Lê um mapa da pasta recursos. Exemplo de caminho: "/mapas/vila.tmx".
     */
    public Mapa carregar(String caminho) {
        Element raiz = lerXml(caminho).getDocumentElement();
        String pasta = caminho.substring(0, caminho.lastIndexOf('/') + 1);

        Mapa mapa = new Mapa(inteiro(raiz, "width"), inteiro(raiz, "height"));
        Map<Integer, ImagemDeObjeto> imagensDosObjetos = new HashMap<Integer, ImagemDeObjeto>();
        List<Decoracao> decoracoes = new ArrayList<Decoracao>();
        List<Decoracao> animais = new ArrayList<Decoracao>();
        List<Rectangle> colisores = new ArrayList<Rectangle>();

        NodeList filhos = raiz.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node filho = filhos.item(i);
            if (!(filho instanceof Element)) {
                continue;
            }
            Element elemento = (Element) filho;
            String tag = elemento.getTagName();
            if (tag.equals("tileset")) {
                lerTileset(elemento, pasta, mapa, imagensDosObjetos);
            } else if (tag.equals("layer")) {
                lerCamada(elemento, mapa);
            } else if (tag.equals("objectgroup") && elemento.getAttribute("name").equals(CAMADA_DE_COLISORES)) {
                lerColisores(elemento, colisores);
            } else if (tag.equals("objectgroup")) {
                lerObjetos(elemento, mapa, imagensDosObjetos, decoracoes, animais);
            }
        }

        mapa.setDecoracoes(decoracoes.toArray(new Decoracao[decoracoes.size()]));
        mapa.setAnimais(animais.toArray(new Decoracao[animais.size()]));
        mapa.setColisores(colisores.toArray(new Rectangle[colisores.size()]));
        return mapa;
    }

    // ---------- Tilesets ----------

    /**
     * Um tileset pode ser uma imagem grande cortada em tiles (o chão) ou
     * uma coleção de imagens soltas (as decorações). Os dois ficam em arquivos .tsx.
     */
    private void lerTileset(Element tileset, String pasta, Mapa mapa, Map<Integer, ImagemDeObjeto> imagensDosObjetos) {
        int primeiroCodigo = inteiro(tileset, "firstgid");
        String pastaDoTileset = pasta;
        Element dados = tileset;
        String arquivo = tileset.getAttribute("source");
        if (arquivo.length() > 0) {
            String caminho = juntarCaminho(pasta, arquivo);
            pastaDoTileset = caminho.substring(0, caminho.lastIndexOf('/') + 1);
            dados = lerXml(caminho).getDocumentElement();
        }

        Element imagem = primeiroFilho(dados, "image");
        if (imagem != null) {
            String caminhoDaImagem = juntarCaminho(pastaDoTileset, imagem.getAttribute("source"));
            mapa.definirTileset(caminhoDaImagem, primeiroCodigo, inteiro(dados, "columns"));
            return;
        }

        NodeList tiles = dados.getElementsByTagName("tile");
        for (int i = 0; i < tiles.getLength(); i++) {
            Element tile = (Element) tiles.item(i);
            Element imagemDoTile = primeiroFilho(tile, "image");
            if (imagemDoTile != null) {
                ImagemDeObjeto imagemDeObjeto = new ImagemDeObjeto();
                imagemDeObjeto.nome = nomeDoArquivo(imagemDoTile.getAttribute("source"));
                imagemDeObjeto.largura = inteiro(imagemDoTile, "width");
                imagemDeObjeto.altura = inteiro(imagemDoTile, "height");
                // O Editor de Colisão do Tiled grava as formas num <objectgroup> dentro do tile.
                List<Rectangle> formas = new ArrayList<Rectangle>();
                Element colisao = primeiroFilho(tile, "objectgroup");
                if (colisao != null) {
                    lerColisores(colisao, formas);
                }
                imagemDeObjeto.colisores = formas.toArray(new Rectangle[formas.size()]);
                imagensDosObjetos.put(primeiroCodigo + inteiro(tile, "id"), imagemDeObjeto);
            }
        }
    }

    // ---------- Camadas de tiles ----------

    private void lerCamada(Element camada, Mapa mapa) {
        String nome = camada.getAttribute("name");
        Element dados = primeiroFilho(camada, "data");
        if (dados == null) {
            return;
        }
        if (!dados.getAttribute("encoding").equals("csv")) {
            throw new IllegalStateException("A camada \"" + nome + "\" precisa estar salva em CSV (opção do Tiled).");
        }

        String[] valores = dados.getTextContent().trim().split("[,\\s]+");
        int[] codigos = new int[mapa.getColunas() * mapa.getLinhas()];
        for (int i = 0; i < codigos.length && i < valores.length; i++) {
            codigos[i] = (int) Long.parseLong(valores[i]);
        }

        if (nome.equals("colisao")) {
            for (int i = 0; i < codigos.length; i++) {
                if (codigos[i] != 0) {
                    mapa.marcarSolido(i % mapa.getColunas(), i / mapa.getColunas());
                }
            }
            return;
        }
        if (camada.getAttribute("visible").equals("0")) {
            return;
        }
        mapa.adicionarCamada(codigos, nome.startsWith("cima"));
    }

    // ---------- Objetos ----------

    private void lerObjetos(Element grupo, Mapa mapa, Map<Integer, ImagemDeObjeto> imagensDosObjetos,
            List<Decoracao> decoracoes, List<Decoracao> animais) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            String nome = objeto.getAttribute("name");
            int x = (int) Math.round(decimal(objeto, "x"));
            int y = (int) Math.round(decimal(objeto, "y"));
            int largura = (int) Math.round(decimal(objeto, "width"));
            int altura = (int) Math.round(decimal(objeto, "height"));

            if (objeto.getAttribute("gid").length() > 0) {
                long codigoCompleto = Long.parseLong(objeto.getAttribute("gid"));
                ImagemDeObjeto imagem = imagensDosObjetos.get((int) (codigoCompleto & SEM_ESPELHAMENTO));
                if (imagem == null) {
                    continue;
                }
                String tipo = imagem.nome;
                boolean espelhada = (codigoCompleto & ESPELHADO_NA_HORIZONTAL) != 0;
                // No Tiled, objetos de imagem têm a posição no canto de baixo, à esquerda.
                Decoracao decoracao = new Decoracao(tipo, x, y - altura, largura, altura, espelhada, camadaDo(tipo));
                if (contem(ANIMAIS, tipo)) {
                    animais.add(decoracao);
                } else {
                    if (imagem.largura > 0 && imagem.altura > 0) {
                        decoracao.definirColisores(imagem.colisores, imagem.largura, imagem.altura);
                    }
                    decoracoes.add(decoracao);
                }
            } else if (nome.equals("saida")) {
                mapa.setSaida(new Rectangle(x, y, largura, altura));
            } else if (nome.length() > 0) {
                mapa.adicionarMarcador(nome, x + largura / 2, y + altura / 2);
            }
        }
    }

    private int camadaDo(String tipo) {
        if (contem(NO_CHAO, tipo)) {
            return Decoracao.NO_CHAO;
        }
        if (contem(POR_CIMA, tipo)) {
            return Decoracao.POR_CIMA;
        }
        return Decoracao.EM_PE;
    }

    /**
     * Lê as formas de um grupo de objetos como retângulos sólidos. Retângulo e elipse valem
     * pela caixa deles; polígono vale pelo retângulo que o envolve; pontos são ignorados.
     */
    private void lerColisores(Element grupo, List<Rectangle> colisores) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            double x = decimal(objeto, "x");
            double y = decimal(objeto, "y");
            double largura = decimal(objeto, "width");
            double altura = decimal(objeto, "height");

            Element poligono = primeiroFilho(objeto, "polygon");
            if (poligono != null) {
                double menorX = Double.MAX_VALUE;
                double menorY = Double.MAX_VALUE;
                double maiorX = -Double.MAX_VALUE;
                double maiorY = -Double.MAX_VALUE;
                String[] pontos = poligono.getAttribute("points").trim().split("\\s+");
                for (int j = 0; j < pontos.length; j++) {
                    String[] coordenadas = pontos[j].split(",");
                    double pontoX = Double.parseDouble(coordenadas[0]);
                    double pontoY = Double.parseDouble(coordenadas[1]);
                    menorX = Math.min(menorX, pontoX);
                    menorY = Math.min(menorY, pontoY);
                    maiorX = Math.max(maiorX, pontoX);
                    maiorY = Math.max(maiorY, pontoY);
                }
                x = x + menorX;
                y = y + menorY;
                largura = maiorX - menorX;
                altura = maiorY - menorY;
            }

            int larguraInteira = (int) Math.round(largura);
            int alturaInteira = (int) Math.round(altura);
            if (larguraInteira <= 0 || alturaInteira <= 0) {
                continue;
            }
            int xInteiro = (int) Math.round(x);
            int yInteiro = (int) Math.round(y);
            colisores.add(new Rectangle(xInteiro, yInteiro, larguraInteira, alturaInteira));
        }
    }

    // ---------- Ajudantes ----------

    private Document lerXml(String caminho) {
        InputStream entrada = DaoMapa.class.getResourceAsStream(caminho);
        if (entrada == null) {
            throw new IllegalStateException("Arquivo não encontrado: " + caminho);
        }
        try {
            DocumentBuilder leitor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return leitor.parse(entrada);
        } catch (Exception erro) {
            throw new IllegalStateException("Erro ao ler " + caminho + ": " + erro.getMessage(), erro);
        } finally {
            fechar(entrada);
        }
    }

    /**
     * Resolve um caminho relativo, como o Tiled grava: juntarCaminho("/mapas/", "../tiles/a.png") dá "/tiles/a.png".
     */
    private String juntarCaminho(String pasta, String relativo) {
        String resultado = pasta + relativo;
        int volta = resultado.indexOf("/../");
        while (volta >= 0) {
            int anterior = resultado.lastIndexOf('/', volta - 1);
            resultado = resultado.substring(0, anterior) + resultado.substring(volta + 3);
            volta = resultado.indexOf("/../");
        }
        return resultado.replace("/./", "/");
    }

    private String nomeDoArquivo(String caminho) {
        String nome = caminho.substring(caminho.lastIndexOf('/') + 1);
        int ponto = nome.lastIndexOf('.');
        if (ponto > 0) {
            nome = nome.substring(0, ponto);
        }
        return nome;
    }

    private Element primeiroFilho(Element pai, String tag) {
        NodeList filhos = pai.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node filho = filhos.item(i);
            if (filho instanceof Element && ((Element) filho).getTagName().equals(tag)) {
                return (Element) filho;
            }
        }
        return null;
    }

    private boolean contem(String[] lista, String valor) {
        for (int i = 0; i < lista.length; i++) {
            if (lista[i].equals(valor)) {
                return true;
            }
        }
        return false;
    }

    private int inteiro(Element elemento, String atributo) {
        String valor = elemento.getAttribute(atributo);
        if (valor.length() == 0) {
            return 0;
        }
        return Integer.parseInt(valor);
    }

    private double decimal(Element elemento, String atributo) {
        String valor = elemento.getAttribute(atributo);
        if (valor.length() == 0) {
            return 0;
        }
        return Double.parseDouble(valor);
    }

    private void fechar(InputStream entrada) {
        try {
            entrada.close();
        } catch (IOException erro) {
            System.out.println("Não foi possível fechar o arquivo do mapa.");
        }
    }
}
