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
 * - camadas de tiles em CSV (o padrão do Tiled). A camada "colisao" diz o que é sólido
 *   e não é desenhada; camadas com nome começando por "cima" ficam por cima dos personagens;
 * - a camada de objetos: objetos de imagem (decorações e animais, pelo nome do arquivo da imagem),
 *   pontos com nome (heroi, morador1...morador4, galinha) e o retângulo "saida".
 */
public class DaoMapa {
    // Os três bits mais altos do código guardam se o tile foi espelhado no Tiled.
    private static final long ESPELHADO_NA_HORIZONTAL = 0x80000000L;
    private static final long SEM_ESPELHAMENTO = 0x0FFFFFFFL;

    private static final String[] ANIMAIS = { "vaca", "porco", "ovelha", "pato" };
    private static final String[] NO_CHAO = { "sombra", "sombra_moinho" };
    private static final String[] POR_CIMA = { "moinho", "fumaca1", "fumaca2", "fumaca3", "fumaca4", "fumaca5" };

    /**
     * Decorações que o herói não atravessa: "nome:largura:altura" da base sólida, no pé da imagem.
     */
    private static final String[] BASES_SOLIDAS = {
        "arvore1:8:5", "arvore2:8:5", "poco:16:8", "poco_coberto:16:8", "barril:11:6", "barril_aberto:11:6",
        "barril_agua:11:6", "barril_espadas:11:6", "caixote1:14:7", "caixote2:14:7", "bau:14:7", "bau_aberto:14:7",
        "bigorna:18:6", "cocho:26:7", "fogueira:18:8", "bote_terra:28:10", "pedra:8:5", "minerio_carvao:8:5",
        "minerio_ouro:8:5", "chamine_cozinha:23:8"
    };

    /**
     * Lê um mapa da pasta recursos. Exemplo de caminho: "/mapas/vila.tmx".
     */
    public Mapa carregar(String caminho) {
        Element raiz = lerXml(caminho).getDocumentElement();
        String pasta = caminho.substring(0, caminho.lastIndexOf('/') + 1);

        Mapa mapa = new Mapa(inteiro(raiz, "width"), inteiro(raiz, "height"));
        Map<Integer, String> nomesDosObjetos = new HashMap<Integer, String>();
        List<Decoracao> decoracoes = new ArrayList<Decoracao>();
        List<Decoracao> animais = new ArrayList<Decoracao>();

        NodeList filhos = raiz.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node filho = filhos.item(i);
            if (!(filho instanceof Element)) {
                continue;
            }
            Element elemento = (Element) filho;
            String tag = elemento.getTagName();
            if (tag.equals("tileset")) {
                lerTileset(elemento, pasta, mapa, nomesDosObjetos);
            } else if (tag.equals("layer")) {
                lerCamada(elemento, mapa);
            } else if (tag.equals("objectgroup")) {
                lerObjetos(elemento, mapa, nomesDosObjetos, decoracoes, animais);
            }
        }

        mapa.setDecoracoes(decoracoes.toArray(new Decoracao[decoracoes.size()]));
        mapa.setAnimais(animais.toArray(new Decoracao[animais.size()]));
        return mapa;
    }

    // ---------- Tilesets ----------

    /**
     * Um tileset pode ser uma imagem grande cortada em tiles (o chão) ou
     * uma coleção de imagens soltas (as decorações). Os dois ficam em arquivos .tsx.
     */
    private void lerTileset(Element tileset, String pasta, Mapa mapa, Map<Integer, String> nomesDosObjetos) {
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
                int codigo = primeiroCodigo + inteiro(tile, "id");
                nomesDosObjetos.put(codigo, nomeDoArquivo(imagemDoTile.getAttribute("source")));
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

    private void lerObjetos(Element grupo, Mapa mapa, Map<Integer, String> nomesDosObjetos,
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
                String tipo = nomesDosObjetos.get((int) (codigoCompleto & SEM_ESPELHAMENTO));
                if (tipo == null) {
                    continue;
                }
                boolean espelhada = (codigoCompleto & ESPELHADO_NA_HORIZONTAL) != 0;
                // No Tiled, objetos de imagem têm a posição no canto de baixo, à esquerda.
                Decoracao decoracao = new Decoracao(tipo, x, y - altura, largura, altura, espelhada, camadaDo(tipo));
                if (contem(ANIMAIS, tipo)) {
                    animais.add(decoracao);
                } else {
                    definirBase(decoracao);
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

    private void definirBase(Decoracao decoracao) {
        for (int i = 0; i < BASES_SOLIDAS.length; i++) {
            String[] partes = BASES_SOLIDAS[i].split(":");
            if (partes[0].equals(decoracao.getTipo())) {
                decoracao.definirBaseSolida(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]));
                return;
            }
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
