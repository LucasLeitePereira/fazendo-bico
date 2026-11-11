package modelo;

/**
 * Reúne tudo o que a tela precisa desenhar e o controle precisa atualizar.
 * Galinhas e efeitos ficam em vetores de tamanho fixo: os que não estão em uso ficam
 * inativos e são reaproveitados. Assim os vetores nunca mudam de tamanho enquanto a tela desenha.
 */
public class Mundo {
    public static final int MAXIMO_DE_GALINHAS = 60;
    public static final int MAXIMO_DE_EFEITOS = 48;

    private Partida partida = new Partida();
    private Dialogo dialogo = new Dialogo();
    private Mapa mapa;
    private Mapa mapaDoFundo;
    private Heroi heroi;
    private Morador[] moradores = new Morador[0];
    private Animal[] animais = new Animal[0];
    private Entidade[] obstaculos = new Entidade[0];
    private Galinha[] galinhas = new Galinha[MAXIMO_DE_GALINHAS];
    private Efeito[] efeitos = new Efeito[MAXIMO_DE_EFEITOS];

    public Mundo() {
        for (int i = 0; i < galinhas.length; i++) {
            galinhas[i] = new Galinha();
        }
        for (int i = 0; i < efeitos.length; i++) {
            efeitos[i] = new Efeito();
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

    public int contarGalinhasAtivas() {
        int total = 0;
        for (int i = 0; i < galinhas.length; i++) {
            if (galinhas[i].isAtiva()) {
                total++;
            }
        }
        return total;
    }

    /**
     * Começa um efeito num lugar do mapa. Se todos estiverem em uso, o efeito é ignorado.
     */
    public void criarEfeito(int tipo, float x, float y, boolean espelhado) {
        for (int i = 0; i < efeitos.length; i++) {
            if (!efeitos[i].isAtivo()) {
                efeitos[i].iniciar(tipo, x, y, espelhado);
                return;
            }
        }
    }

    public void atualizarEfeitos() {
        for (int i = 0; i < efeitos.length; i++) {
            efeitos[i].atualizar();
        }
    }

    public void apagarEfeitos() {
        for (int i = 0; i < efeitos.length; i++) {
            efeitos[i].desativar();
        }
    }

    /**
     * Junta moradores e animais num vetor só: é o que o herói não pode atravessar.
     */
    private void juntarObstaculos() {
        Entidade[] juntos = new Entidade[moradores.length + animais.length];
        for (int i = 0; i < moradores.length; i++) {
            juntos[i] = moradores[i];
        }
        for (int i = 0; i < animais.length; i++) {
            juntos[moradores.length + i] = animais[i];
        }
        obstaculos = juntos;
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

    /**
     * Mapa que aparece atrás do menu da tela inicial.
     */
    public Mapa getMapaDoFundo() {
        return mapaDoFundo;
    }

    public void setMapaDoFundo(Mapa mapaDoFundo) {
        this.mapaDoFundo = mapaDoFundo;
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
        juntarObstaculos();
    }

    public Animal[] getAnimais() {
        return animais;
    }

    public void setAnimais(Animal[] animais) {
        this.animais = animais;
        juntarObstaculos();
    }

    public Entidade[] getObstaculos() {
        return obstaculos;
    }

    public Galinha[] getGalinhas() {
        return galinhas;
    }

    public Efeito[] getEfeitos() {
        return efeitos;
    }
}
