package modelo;

import java.awt.Rectangle;

public class Heroi extends Entidade {
    public static final int VIDA_MAXIMA = 5;
    private static final int QUADROS_INVENCIVEL = 60;
    private static final int QUADROS_DO_GOLPE = 12;
    private static final int LARGURA_DO_GOLPE = 14;

    private int vida = VIDA_MAXIMA;
    private Arma arma;
    private boolean andando;
    private int quadrosInvencivel;
    private int quadrosDoGolpe;
    private int quadrosDeRecarga;

    public Heroi(float x, float y) {
        super(x, y, 12, 12);
        velocidade = 1.5f;
        arma = new Arma("Graveto", 1, 14, 20);
    }

    @Override
    public void atualizar(Mapa mapa) {
        if (quadrosInvencivel > 0) {
            quadrosInvencivel--;
        }
        if (quadrosDoGolpe > 0) {
            quadrosDoGolpe--;
        }
        if (quadrosDeRecarga > 0) {
            quadrosDeRecarga--;
        }
        if (andando) {
            contadorPassos++;
        }
    }

    /**
     * Na diagonal o herói continua olhando para o lado, como nos jogos de visão de cima.
     */
    public void virarPara(float passoX, float passoY) {
        if (passoX < 0) {
            direcao = Direcao.ESQUERDA;
        } else if (passoX > 0) {
            direcao = Direcao.DIREITA;
        } else if (passoY < 0) {
            direcao = Direcao.CIMA;
        } else if (passoY > 0) {
            direcao = Direcao.BAIXO;
        }
    }

    public boolean podeAtacar() {
        return quadrosDeRecarga == 0;
    }

    public void atacar() {
        quadrosDoGolpe = QUADROS_DO_GOLPE;
        quadrosDeRecarga = arma.getRecarga();
    }

    public boolean estaAtacando() {
        return quadrosDoGolpe > 0;
    }

    public int getQuadrosDesdeOInicioDoGolpe() {
        return QUADROS_DO_GOLPE - quadrosDoGolpe;
    }

    /**
     * Retângulo à frente do herói onde o golpe acerta.
     */
    public Rectangle getAreaDoGolpe() {
        int alcance = arma.getAlcance();
        int centroX = (int) getCentroX();
        int centroY = (int) getCentroY();
        int metadeDaLargura = LARGURA_DO_GOLPE / 2;

        switch (direcao) {
            case CIMA:
                return new Rectangle(centroX - metadeDaLargura, (int) y - alcance, LARGURA_DO_GOLPE, alcance);
            case BAIXO:
                return new Rectangle(centroX - metadeDaLargura, (int) y + altura, LARGURA_DO_GOLPE, alcance);
            case ESQUERDA:
                return new Rectangle((int) x - alcance, centroY - metadeDaLargura, alcance, LARGURA_DO_GOLPE);
            default:
                return new Rectangle((int) x + largura, centroY - metadeDaLargura, alcance, LARGURA_DO_GOLPE);
        }
    }

    public void receberDano(int dano) {
        if (estaInvencivel()) {
            return;
        }
        vida = vida - dano;
        if (vida < 0) {
            vida = 0;
        }
        quadrosInvencivel = QUADROS_INVENCIVEL;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public boolean estaInvencivel() {
        return quadrosInvencivel > 0;
    }

    public int getQuadrosInvencivel() {
        return quadrosInvencivel;
    }

    public void setAndando(boolean andando) {
        this.andando = andando;
        if (!andando) {
            contadorPassos = 0;
        }
    }

    public int getVida() {
        return vida;
    }

    public Arma getArma() {
        return arma;
    }
}
