package modelo;

public class Morador extends Entidade {
    private static final int DISTANCIA_PARA_CONVERSAR = 10;

    private int numero;
    private String nome;
    private String[] falas;
    private boolean daMissao;

    public Morador(float x, float y, int numero, String nome, String[] falas, boolean daMissao) {
        super(x, y, 12, 12);
        this.numero = numero;
        this.nome = nome;
        this.falas = falas;
        this.daMissao = daMissao;
    }

    @Override
    public void atualizar(Mapa mapa) {
        // Os moradores ficam parados no lugar.
    }

    public boolean estaPerto(Entidade outra) {
        return distanciaAte(outra) <= largura + DISTANCIA_PARA_CONVERSAR;
    }

    public void olharPara(Entidade outra) {
        float distanciaX = outra.getCentroX() - getCentroX();
        float distanciaY = outra.getCentroY() - getCentroY();
        if (Math.abs(distanciaX) > Math.abs(distanciaY)) {
            if (distanciaX < 0) {
                direcao = Direcao.ESQUERDA;
            } else {
                direcao = Direcao.DIREITA;
            }
        } else {
            if (distanciaY < 0) {
                direcao = Direcao.CIMA;
            } else {
                direcao = Direcao.BAIXO;
            }
        }
    }

    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public String[] getFalas() {
        return falas;
    }

    public boolean isDaMissao() {
        return daMissao;
    }
}
