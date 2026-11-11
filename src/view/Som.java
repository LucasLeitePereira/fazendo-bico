package view;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Toca músicas (em loop) e efeitos sonoros. Só aceita arquivos WAV.
 * Se um arquivo não existir, o jogo continua em silêncio.
 */
public class Som {
    private Clip musicaAtual;
    private String nomeDaMusicaAtual = "";
    private Map<String, Clip> efeitos = new HashMap<String, Clip>();

    /**
     * Exemplo: tocarMusica("vila") toca recursos/musicas/vila.wav.
     */
    public void tocarMusica(String nome) {
        if (nome.equals(nomeDaMusicaAtual)) {
            return;
        }
        pararMusica();
        nomeDaMusicaAtual = nome;
        musicaAtual = carregar("/musicas/" + nome + ".wav");
        if (musicaAtual != null) {
            musicaAtual.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void pararMusica() {
        if (musicaAtual != null) {
            musicaAtual.stop();
            musicaAtual.close();
            musicaAtual = null;
        }
        nomeDaMusicaAtual = "";
    }

    /**
     * Exemplo: tocarEfeito("golpe") toca recursos/sons/golpe.wav.
     */
    public void tocarEfeito(String nome) {
        if (!efeitos.containsKey(nome)) {
            efeitos.put(nome, carregar("/sons/" + nome + ".wav"));
        }
        Clip efeito = efeitos.get(nome);
        if (efeito == null) {
            return;
        }
        efeito.stop();
        efeito.setFramePosition(0);
        efeito.start();
    }

    private Clip carregar(String caminho) {
        URL endereco = Som.class.getResource(caminho);
        if (endereco == null) {
            return null;
        }
        try {
            AudioInputStream entrada = AudioSystem.getAudioInputStream(endereco);
            Clip clip = AudioSystem.getClip();
            clip.open(entrada);
            return clip;
        } catch (Exception erro) {
            System.out.println("Não foi possível tocar " + caminho + ": " + erro.getMessage());
            return null;
        }
    }
}
