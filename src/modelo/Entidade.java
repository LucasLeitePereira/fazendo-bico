package modelo;

import java.awt.Rectangle;

/**
 * Tudo o que existe no mapa e ocupa espaço: herói, galinhas e moradores.
 */
public abstract class Entidade {
    private static final Entidade[] SEM_OBSTACULOS = new Entidade[0];

    protected float x;
    protected float y;
    protected int largura;
    protected int altura;
    protected float velocidade;
    protected Direcao direcao = Direcao.BAIXO;
    protected int contadorPassos;

    public Entidade(float x, float y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public abstract void atualizar(Mapa mapa);

    public boolean mover(float distanciaX, float distanciaY, Mapa mapa) {
        return mover(distanciaX, distanciaY, mapa, SEM_OBSTACULOS);
    }

    /**
     * Move um eixo de cada vez para a entidade conseguir deslizar encostada numa parede.
     * Retorna false se algum dos eixos foi bloqueado.
     */
    public boolean mover(float distanciaX, float distanciaY, Mapa mapa, Entidade[] obstaculos) {
        boolean moveuTudo = true;

        float xAntigo = x;
        x = x + distanciaX;
        if (estaBloqueada(mapa, obstaculos)) {
            x = xAntigo;
            moveuTudo = false;
        }

        float yAntigo = y;
        y = y + distanciaY;
        if (estaBloqueada(mapa, obstaculos)) {
            y = yAntigo;
            moveuTudo = false;
        }

        return moveuTudo;
    }

    private boolean estaBloqueada(Mapa mapa, Entidade[] obstaculos) {
        if (mapa.colide(getArea())) {
            return true;
        }
        for (int i = 0; i < obstaculos.length; i++) {
            if (obstaculos[i] != this && colide(obstaculos[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean colide(Entidade outra) {
        return getArea().intersects(outra.getArea());
    }

    public Rectangle getArea() {
        return new Rectangle((int) x, (int) y, largura, altura);
    }

    public float distanciaAte(Entidade outra) {
        float distanciaX = outra.getCentroX() - getCentroX();
        float distanciaY = outra.getCentroY() - getCentroY();
        return (float) Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);
    }

    public float getCentroX() {
        return x + largura / 2.0f;
    }

    public float getCentroY() {
        return y + altura / 2.0f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public float getVelocidade() {
        return velocidade;
    }

    public Direcao getDirecao() {
        return direcao;
    }

    public void setDirecao(Direcao direcao) {
        this.direcao = direcao;
    }

    public int getContadorPassos() {
        return contadorPassos;
    }
}
