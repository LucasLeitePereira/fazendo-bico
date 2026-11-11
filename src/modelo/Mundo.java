package modelo;

/**
 * Reúne tudo o que a tela precisa desenhar e o controle precisa atualizar.
 * As galinhas ficam num vetor de tamanho fixo: as que não estão em jogo ficam inativas
 * e são reaproveitadas. Assim a lista nunca muda de tamanho enquanto a tela desenha.
 */
public class Mundo {
    public static final int MAXIMO_DE_GALINHAS = 60;

    private Partida partida = new Partida();
    private Dialogo dialogo = new Dialogo();
    private Mapa mapa;
    private Heroi heroi;
    private Morador[] moradores = new Morador[0];
    private Galinha[] galinhas = new Galinha[MAXIMO_DE_GALINHAS];

    public Mundo() {
        for (int i = 0; i < galinhas.length; i++) {
            galinhas[i] = new Galinha();
        }
    }

    /**
     * Retorna uma galinha inativa para ser usada, ou null se todas estiverem em jogo.
     */
    public Galinha pegarGalinhaLivre() {
        for (int i = 0; i < galinhas.length; i++) {
            if (!galinhas[i].isAtiva()) {
                return galinhas[i];
            }
        }
        return null;
    }

    public void recolherTodasAsGalinhas() {
        for (int i = 0; i < galinhas.length; i++) {
            galinhas[i].desativar();
        }
    }

    public Partida getPartida() {
        return partida;
    }

    public Dialogo getDialogo() {
        return dialogo;
    }

    public Mapa getMapa() {
        return mapa;
    }

    public void setMapa(Mapa mapa) {
        this.mapa = mapa;
    }

    public Heroi getHeroi() {
        return heroi;
    }

    public void setHeroi(Heroi heroi) {
        this.heroi = heroi;
    }

    public Morador[] getMoradores() {
        return moradores;
    }

    public void setMoradores(Morador[] moradores) {
        this.moradores = moradores;
    }

    public Galinha[] getGalinhas() {
        return galinhas;
    }
}
