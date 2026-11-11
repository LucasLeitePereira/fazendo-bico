package modelo;

import java.util.Random;

/**
 * Uma galinha pode estar passeando pelo chão (antes da raide)
 * ou voando em linha reta contra o herói (durante a raide).
 */
public class Galinha extends Entidade {
    private static final int VIDA_NO_CHAO = 3;
    private static final int VIDA_VOANDO = 1;
    private static final int QUADROS_PISCANDO = 12;
    private static final float DISTANCIA_DO_EMPURRAO = 10;
    private static final Random sorteio = new Random();

    private boolean ativa;
    private boolean voando;
    private boolean parada;
    private int vida;
    private float velocidadeX;
    private float velocidadeY;
    private int quadrosAteMudarDePasseio;
    private int quadrosPiscando;

    public Galinha() {
        super(0, 0, 12, 12);
    }

    public void soltarNoChao(float x, float y) {
        setPosicao(x, y);
        ativa = true;
        voando = false;
        vida = VIDA_NO_CHAO;
        velocidade = 0.4f;
        quadrosPiscando = 0;
        contadorPassos = 0;
        escolherNovoPasseio();
    }

    public void lancarContra(float inicioX, float inicioY, float alvoX, float alvoY, float velocidadeDoVoo) {
        setPosicao(inicioX, inicioY);
        ativa = true;
        voando = true;
        vida = VIDA_VOANDO;
        quadrosPiscando = 0;
        contadorPassos = 0;

        float distanciaX = alvoX - inicioX;
        float distanciaY = alvoY - inicioY;
        float distancia = (float) Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);
        if (distancia == 0) {
            distancia = 1;
        }
        velocidadeX = distanciaX / distancia * velocidadeDoVoo;
        velocidadeY = distanciaY / distancia * velocidadeDoVoo;

        if (velocidadeX < 0) {
            setDirecao(Direcao.ESQUERDA);
        } else {
            setDirecao(Direcao.DIREITA);
        }
    }

    @Override
    public void atualizar(Mapa mapa) {
        if (!ativa) {
            return;
        }
        if (quadrosPiscando > 0) {
            quadrosPiscando--;
        }
        if (voando) {
            x = x + velocidadeX;
            y = y + velocidadeY;
            contadorPassos++;
        } else {
            passear(mapa);
        }
    }

    private void passear(Mapa mapa) {
        quadrosAteMudarDePasseio--;
        if (quadrosAteMudarDePasseio <= 0) {
            escolherNovoPasseio();
        }
        if (parada) {
            return;
        }
        float distanciaX = direcao.getPassoX() * velocidade;
        float distanciaY = direcao.getPassoY() * velocidade;
        boolean conseguiuAndar = mover(distanciaX, distanciaY, mapa);
        if (conseguiuAndar) {
            contadorPassos++;
        } else {
            parada = true;
            contadorPassos = 0;
        }
    }

    private void escolherNovoPasseio() {
        int escolha = sorteio.nextInt(5);
        if (escolha == 4) {
            parada = true;
            contadorPassos = 0;
        } else {
            parada = false;
            setDirecao(Direcao.values()[escolha]);
        }
        quadrosAteMudarDePasseio = 40 + sorteio.nextInt(80);
    }

    /**
     * Retorna true se a galinha morreu com o golpe.
     */
    public boolean receberGolpe(int dano, Direcao direcaoDoGolpe, Mapa mapa) {
        vida = vida - dano;
        if (vida <= 0) {
            desativar();
            return true;
        }
        quadrosPiscando = QUADROS_PISCANDO;
        float empurraoX = direcaoDoGolpe.getPassoX() * DISTANCIA_DO_EMPURRAO;
        float empurraoY = direcaoDoGolpe.getPassoY() * DISTANCIA_DO_EMPURRAO;
        mover(empurraoX, empurraoY, mapa);
        return false;
    }

    public void desativar() {
        ativa = false;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public int getVida() {
        return vida;
    }

    public boolean isVoando() {
        return voando;
    }

    public boolean estaPiscando() {
        return quadrosPiscando > 0;
    }

    public int getQuadrosPiscando() {
        return quadrosPiscando;
    }
}
