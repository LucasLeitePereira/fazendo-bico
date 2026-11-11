package modelo;

import java.awt.Rectangle;

public class Heroi extends Entidade {
    public static final int VIDA_MAXIMA = 5;
    public static final int QUADROS_DO_GOLPE = 20;
    public static final int QUADROS_DA_REACAO_AO_DANO = 16;
    private static final int QUADROS_INVENCIVEL = 60;
    private static final int MOMENTO_DO_ACERTO = 10;
    private static final int ALTURA_DO_GOLPE = 24;

    private int vida = VIDA_MAXIMA;
    private Arma arma;
    private boolean andando;
    private int quadrosInvencivel;
    private int quadrosDoGolpe;
    private int quadrosDeRecarga;

    public Heroi(float x, float y) {
        super(x, y, 12, 12);
        velocidade = 1.5f;
        arma = new Arma("Espada", 1, 18, 24);
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

    public void virarPara(float passoX, float passoY) {
        if (passoX < 0) {
            setDirecao(Direcao.ESQUERDA);
        } else if (passoX > 0) {
            setDirecao(Direcao.DIREITA);
        } else if (passoY < 0) {
            setDirecao(Direcao.CIMA);
        } else if (passoY > 0) {
            setDirecao(Direcao.BAIXO);
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
     * O golpe só acerta num quadro da animação: quando a espada está esticada.
     */
    public boolean golpeAcertaAgora() {
        return estaAtacando() && getQuadrosDesdeOInicioDoGolpe() == MOMENTO_DO_ACERTO;
    }

    /**
     * Retângulo à frente do herói, do lado para onde ele olha, onde o golpe acerta.
     */
    public Rectangle getAreaDoGolpe() {
        int alcance = arma.getAlcance();
        int topo = (int) getCentroY() - ALTURA_DO_GOLPE / 2;
        if (olhandoParaEsquerda) {
            return new Rectangle((int) x - alcance, topo, alcance, ALTURA_DO_GOLPE);
        }
        return new Rectangle((int) x + largura, topo, alcance, ALTURA_DO_GOLPE);
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

    public boolean acabouDeLevarDano() {
        return getQuadrosDesdeODano() < QUADROS_DA_REACAO_AO_DANO;
    }

    public int getQuadrosDesdeODano() {
        return QUADROS_INVENCIVEL - quadrosInvencivel;
    }

    public void setAndando(boolean andando) {
        this.andando = andando;
        if (!andando) {
            contadorPassos = 0;
        }
    }

    public boolean isAndando() {
        return andando;
    }

    public int getVida() {
        return vida;
    }

    public Arma getArma() {
        return arma;
    }
}
