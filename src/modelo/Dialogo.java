package modelo;

public class Dialogo {
    private String nome = "";
    private String[] falas = new String[0];
    private int indice;
    private boolean ativo;
    private int retrato;

    public void abrir(String nome, String[] falas) {
        abrir(nome, falas, 0);
    }

    /**
     * O retrato é o número do morador que está falando (0 quando não há ninguém, só narração).
     */
    public void abrir(String nome, String[] falas, int retrato) {
        this.nome = nome;
        this.retrato = retrato;
        this.falas = falas;
        indice = 0;
        ativo = falas.length > 0;
    }

    public void avancar() {
        indice++;
        if (indice >= falas.length) {
            ativo = false;
        }
    }

    public void fechar() {
        ativo = false;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getNome() {
        return nome;
    }

    public int getRetrato() {
        return retrato;
    }

    public String getFalaAtual() {
        if (!ativo) {
            return "";
        }
        return falas[indice];
    }
}
