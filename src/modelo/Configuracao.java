package modelo;

/**
 * Ajustes do jogador que valem para o jogo todo, fora da partida (por enquanto, o volume da música).
 */
public class Configuracao {
    public static final int VOLUME_INICIAL = 50;
    public static final int PASSO_DO_VOLUME = 10;
    // Por quanto tempo o aviso "Música: 60%" fica na tela depois de mudar o volume.
    public static final int QUADROS_DO_AVISO_DE_VOLUME = 90;

    private int volumeDaMusica = VOLUME_INICIAL;
    private int quadrosMostrandoVolume;

    /**
     * Soma (ou subtrai, se for negativo) ao volume, sem passar de 0% nem de 100%.
     */
    public void mudarVolumeDaMusica(int diferenca) {
        volumeDaMusica = Math.max(0, Math.min(100, volumeDaMusica + diferenca));
        quadrosMostrandoVolume = QUADROS_DO_AVISO_DE_VOLUME;
    }

    public void contarQuadroDoAviso() {
        if (quadrosMostrandoVolume > 0) {
            quadrosMostrandoVolume--;
        }
    }

    public int getVolumeDaMusica() {
        return volumeDaMusica;
    }

    public int getQuadrosMostrandoVolume() {
        return quadrosMostrandoVolume;
    }
}
