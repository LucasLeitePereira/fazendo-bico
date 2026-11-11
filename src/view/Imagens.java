package view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Imagens {

    /**
     * Lê uma imagem da pasta recursos. Exemplo de caminho: "/sprites/heroi.png".
     * Se o arquivo não existir, retorna null e quem chamou desenha um retângulo no lugar.
     */
    public BufferedImage carregar(String caminho) {
        URL endereco = Imagens.class.getResource(caminho);
        if (endereco == null) {
            return null;
        }
        try {
            return ImageIO.read(endereco);
        } catch (IOException erro) {
            System.out.println("Não foi possível ler a imagem " + caminho + ": " + erro.getMessage());
            return null;
        }
    }
}
