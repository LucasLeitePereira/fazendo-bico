package modelo;

public class Dialogo {
    private String nome = "";
    private String[] falas = new String[0];
    private int indice;
    private boolean ativo;

    public void abrir(String nome, String[] falas) {
        this.nome = nome;
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

    public String getFalaAtual() {
        if (!ativo) {
            return "";
        }
        return falas[indice];
    }
}
