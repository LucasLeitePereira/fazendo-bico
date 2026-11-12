package modelo;

public class Partida {
    public static final int PONTOS_POR_GALINHA = 10;
    // Opções do menu inicial, de cima para baixo.
    public static final int OPCAO_JOGAR = 0;
    public static final int OPCAO_MUSICA = 1;
    public static final int OPCAO_SAIR = 2;
    public static final int QUANTIDADE_DE_OPCOES = 3;

    private Cena cena = Cena.INICIAL;
    private int opcaoDoMenu;
    private int pontos;
    private int galinhasDerrotadas;
    private int recorde;
    private boolean novoRecorde;
    private boolean missaoAceita;
    private boolean raideAtiva;
    private int onda;
    private int quadrosTremendo;
    private boolean fugiu;
    private int quadrosDaEntrada;

    /**
     * Zera tudo para uma nova partida, menos o recorde.
     */
    public void reiniciar() {
        pontos = 0;
        galinhasDerrotadas = 0;
        novoRecorde = false;
        missaoAceita = false;
        raideAtiva = false;
        onda = 0;
        quadrosTremendo = 0;
        fugiu = false;
    }

    public void registrarGalinhaDerrotada() {
        galinhasDerrotadas++;
        pontos = pontos + PONTOS_POR_GALINHA;
    }

    /**
     * Retorna true se os pontos desta partida bateram o recorde.
     */
    public boolean atualizarRecorde() {
        if (pontos > recorde) {
            recorde = pontos;
            novoRecorde = true;
        } else {
            novoRecorde = false;
        }
        return novoRecorde;
    }

    public void tremer(int quadros) {
        if (quadros > quadrosTremendo) {
            quadrosTremendo = quadros;
        }
    }

    public void diminuirTremor() {
        if (quadrosTremendo > 0) {
            quadrosTremendo--;
        }
    }

    public Cena getCena() {
        return cena;
    }

    public void setCena(Cena cena) {
        this.cena = cena;
    }

    public int getOpcaoDoMenu() {
        return opcaoDoMenu;
    }

    public void setOpcaoDoMenu(int opcaoDoMenu) {
        this.opcaoDoMenu = opcaoDoMenu;
    }

    public int getPontos() {
        return pontos;
    }

    public int getGalinhasDerrotadas() {
        return galinhasDerrotadas;
    }

    public int getRecorde() {
        return recorde;
    }

    public void setRecorde(int recorde) {
        this.recorde = recorde;
    }

    public boolean isNovoRecorde() {
        return novoRecorde;
    }

    public boolean isMissaoAceita() {
        return missaoAceita;
    }

    public void setMissaoAceita(boolean missaoAceita) {
        this.missaoAceita = missaoAceita;
    }

    public boolean isRaideAtiva() {
        return raideAtiva;
    }

    public void setRaideAtiva(boolean raideAtiva) {
        this.raideAtiva = raideAtiva;
    }

    public int getOnda() {
        return onda;
    }

    public void setOnda(int onda) {
        this.onda = onda;
    }

    public int getQuadrosTremendo() {
        return quadrosTremendo;
    }

    /**
     * Quadros desde que o herói chegou ao campo. A tela usa para clarear aos poucos.
     */
    public int getQuadrosDaEntrada() {
        return quadrosDaEntrada;
    }

    public void comecarEntrada() {
        quadrosDaEntrada = 0;
    }

    public void contarQuadroDaEntrada() {
        quadrosDaEntrada++;
    }

    public boolean isFugiu() {
        return fugiu;
    }

    public void setFugiu(boolean fugiu) {
        this.fugiu = fugiu;
    }
}
