package modelo;

/**
 * Um efeito visual curto: poeira dos passos, penas voando, brilho... Fica ativo
 * durante alguns quadros e depois some. Os efeitos ficam num vetor fixo no Mundo
 * e são reaproveitados.
 */
public class Efeito {
    public static final int POEIRA = 0;
    public static final int PENAS = 1;
    public static final int FOLHAS = 2;
    public static final int BRILHO = 3;
    public static final int FUMACA = 4;

    private static final int[] DURACAO = { 32, 40, 40, 30, 32 };

    private int tipo;
    private float x;
    private float y;
    private int idade;
    private boolean espelhado;
    private boolean ativo;

    /**
     * O ponto (x, y) é o centro do efeito no mapa.
     */
    public void iniciar(int tipo, float x, float y, boolean espelhado) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.espelhado = espelhado;
        idade = 0;
        ativo = true;
    }

    public void atualizar() {
        if (!ativo) {
            return;
        }
        idade++;
        if (idade >= getDuracao()) {
            ativo = false;
        }
    }

    public void desativar() {
        ativo = false;
    }

    public int getDuracao() {
        return DURACAO[tipo];
    }

    /**
     * Quanto do efeito já passou, de 0 (acabou de começar) até perto de 1 (vai sumir).
     */
    public float getProgresso() {
        return idade / (float) getDuracao();
    }

    public int getTipo() {
        return tipo;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isEspelhado() {
        return espelhado;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
