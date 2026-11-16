package modelo;

/**
 * Onde está o cursor na tela de controles. As linhas são os comandos, depois
 * "Restaurar padrão" e "Voltar". As colunas são Teclado e Controle.
 */
public class EdicaoDeControles {
    public static final int COLUNA_TECLADO = 0;
    public static final int COLUNA_CONTROLE = 1;
    public static final int QUANTIDADE_DE_COLUNAS = 2;
    public static final int LINHA_RESTAURAR = Comando.values().length;
    public static final int LINHA_VOLTAR = LINHA_RESTAURAR + 1;
    public static final int QUANTIDADE_DE_LINHAS = LINHA_VOLTAR + 1;
    // Se ninguém apertar nada em 6 segundos, a troca é cancelada.
    public static final int QUADROS_ESPERANDO_NO_MAXIMO = 360;
    private static final int QUADROS_DO_AVISO = 150;

    private int linha;
    private int coluna;
    // Esperando o jogador apertar a nova tecla ou o novo botão.
    private boolean esperando;
    private int quadrosEsperando;
    // Aviso curto na parte de baixo da tela ("Tecla trocada" etc.).
    private String aviso = "";
    private int quadrosDoAviso;

    public void reiniciar() {
        linha = 0;
        coluna = COLUNA_TECLADO;
        esperando = false;
        aviso = "";
        quadrosDoAviso = 0;
    }

    public void subir() {
        linha = (linha + QUANTIDADE_DE_LINHAS - 1) % QUANTIDADE_DE_LINHAS;
    }

    public void descer() {
        linha = (linha + 1) % QUANTIDADE_DE_LINHAS;
    }

    public void trocarDeColuna() {
        coluna = (coluna + 1) % QUANTIDADE_DE_COLUNAS;
    }

    public boolean isNaTabela() {
        return linha < LINHA_RESTAURAR;
    }

    /**
     * O comando da linha atual. Só vale quando o cursor está na tabela.
     */
    public Comando getComando() {
        return Comando.values()[linha];
    }

    public void comecarAEsperar() {
        esperando = true;
        quadrosEsperando = 0;
        aviso = "";
    }

    public void pararDeEsperar() {
        esperando = false;
    }

    /**
     * Conta um quadro de espera. Devolve true quando o tempo acabou.
     */
    public boolean contarQuadroEsperando() {
        quadrosEsperando++;
        return quadrosEsperando >= QUADROS_ESPERANDO_NO_MAXIMO;
    }

    public void avisar(String texto) {
        aviso = texto;
        quadrosDoAviso = QUADROS_DO_AVISO;
    }

    public void contarQuadroDoAviso() {
        if (quadrosDoAviso > 0) {
            quadrosDoAviso--;
            if (quadrosDoAviso == 0) {
                aviso = "";
            }
        }
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public boolean isEsperando() {
        return esperando;
    }

    public String getAviso() {
        return aviso;
    }
}
