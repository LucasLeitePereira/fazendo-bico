package view;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Toca músicas (em loop) e efeitos sonoros. Só aceita arquivos WAV.
 * Se um arquivo não existir, o jogo continua em silêncio.
 */
public class Som {
    // Os efeitos (galinhas, espada, galo, passos) tocam mais baixo que a música.
    private static final int VOLUME_DOS_EFEITOS = 30;

    private Clip musicaAtual;
    private int volumeDaMusica = 100;
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
            aplicarVolume(musicaAtual, volumeDaMusica);
            musicaAtual.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Volume da música de 0 a 100 (%). Vale para a música que está tocando e para as próximas.
     */
    public void definirVolumeDaMusica(int porcento) {
        volumeDaMusica = porcento;
        if (musicaAtual != null) {
            aplicarVolume(musicaAtual, porcento);
        }
    }

    /**
     * O Java mede o volume em decibéis: 50% da força do som é cerca de -6 dB, e 0% é o mínimo.
     * Alguns computadores não têm esse controle; aí o volume fica como está.
     */
    private void aplicarVolume(Clip clip, int porcento) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float decibeis = volume.getMinimum();
        if (porcento > 0) {
            decibeis = (float) (20 * Math.log10(porcento / 100.0));
        }
        volume.setValue(Math.max(volume.getMinimum(), Math.min(volume.getMaximum(), decibeis)));
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
        Clip efeito = buscarEfeito(nome);
        if (efeito == null) {
            return;
        }
        efeito.stop();
        efeito.setFramePosition(0);
        efeito.start();
    }

    /**
     * Toca um efeito repetindo sem parar (os passos do herói). Se já estiver tocando, continua.
     */
    public void tocarEfeitoEmLoop(String nome) {
        Clip efeito = buscarEfeito(nome);
        if (efeito == null || efeito.isRunning()) {
            return;
        }
        efeito.setFramePosition(0);
        efeito.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void pararEfeito(String nome) {
        Clip efeito = buscarEfeito(nome);
        if (efeito != null && efeito.isRunning()) {
            efeito.stop();
        }
    }

    /**
     * Quanto tempo o efeito dura. Se o arquivo não existir, devolve 0.
     */
    public int duracaoDoEfeitoEmMilissegundos(String nome) {
        Clip efeito = buscarEfeito(nome);
        if (efeito == null) {
            return 0;
        }
        return (int) (efeito.getMicrosecondLength() / 1000);
    }

    private Clip buscarEfeito(String nome) {
        if (!efeitos.containsKey(nome)) {
            Clip efeito = carregar("/sons/" + nome + ".wav");
            if (efeito != null) {
                aplicarVolume(efeito, VOLUME_DOS_EFEITOS);
            }
            efeitos.put(nome, efeito);
        }
        return efeitos.get(nome);
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
