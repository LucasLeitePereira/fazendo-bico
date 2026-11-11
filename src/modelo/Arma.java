package modelo;

public class Arma {
    private String nome;
    private int dano;
    private int alcance;
    private int recarga;

    public Arma(String nome, int dano, int alcance, int recarga) {
        this.nome = nome;
        this.dano = dano;
        this.alcance = alcance;
        this.recarga = recarga;
    }

    public String getNome() {
        return nome;
    }

    public int getDano() {
        return dano;
    }

    public int getAlcance() {
        return alcance;
    }

    /**
     * Quantidade de quadros que o herói espera entre um golpe e outro.
     */
    public int getRecarga() {
        return recarga;
    }
}
