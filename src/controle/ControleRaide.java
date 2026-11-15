package controle;

import java.util.Random;

import modelo.Galinha;
import modelo.Heroi;
import modelo.Mundo;
import modelo.Partida;

/**
 * Cuida da raide: depois de uma pausa dramática, lança galinhas contra o herói
 * a partir de pontos fora da tela. A cada onda elas chegam um pouco mais rápido e em maior número.
 */
public class ControleRaide {
    private static final int QUADROS_DE_PREPARACAO = 90;
    private static final int QUADROS_POR_ONDA = 600;
    private static final int INTERVALO_INICIAL = 60;
    private static final int INTERVALO_MINIMO = 12;
    private static final int REDUCAO_DO_INTERVALO_POR_ONDA = 8;
    private static final float VELOCIDADE_INICIAL = 1.0f;
    private static final float AUMENTO_DE_VELOCIDADE_POR_ONDA = 0.08f;
    // Abaixo da velocidade do herói, para sempre dar para escapar correndo.
    private static final float VELOCIDADE_MAXIMA = 1.6f;
    private static final float VARIACAO_DE_VELOCIDADE = 0.3f;
    private static final float DISTANCIA_DE_SURGIMENTO = 220;
    private static final float DISTANCIA_PARA_SUMIR = 400;
    private static final int ERRO_DE_MIRA = 16;

    private Random sorteio = new Random();
    private int quadrosDePreparacao;
    private int quadrosNaOnda;
    private int quadrosAteProximaGalinha;
    private int intervalo;
    private float velocidade;

    public void iniciar(Partida partida) {
        partida.setRaideAtiva(true);
        partida.setOnda(1);
        quadrosDePreparacao = QUADROS_DE_PREPARACAO;
        quadrosNaOnda = 0;
        quadrosAteProximaGalinha = 0;
        intervalo = INTERVALO_INICIAL;
        velocidade = VELOCIDADE_INICIAL;
    }

    /**
     * Devolve true se lançou alguma galinha neste quadro (para o controle tocar o cacarejo).
     */
    public boolean atualizar(Mundo mundo) {
        Partida partida = mundo.getPartida();
        if (!partida.isRaideAtiva()) {
            return false;
        }
        if (quadrosDePreparacao > 0) {
            quadrosDePreparacao--;
            return false;
        }

        quadrosNaOnda++;
        if (quadrosNaOnda >= QUADROS_POR_ONDA) {
            passarDeOnda(partida);
        }

        boolean lancou = false;
        quadrosAteProximaGalinha--;
        if (quadrosAteProximaGalinha <= 0) {
            int quantidade = 1 + partida.getOnda() / 3;
            for (int i = 0; i < quantidade; i++) {
                if (lancarGalinha(mundo)) {
                    lancou = true;
                }
            }
            quadrosAteProximaGalinha = intervalo;
        }

        recolherGalinhasDistantes(mundo);
        return lancou;
    }

    private void passarDeOnda(Partida partida) {
        partida.setOnda(partida.getOnda() + 1);
        quadrosNaOnda = 0;
        intervalo = intervalo - REDUCAO_DO_INTERVALO_POR_ONDA;
        if (intervalo < INTERVALO_MINIMO) {
            intervalo = INTERVALO_MINIMO;
        }
        velocidade = velocidade + AUMENTO_DE_VELOCIDADE_POR_ONDA;
    }

    /**
     * Sorteia um ponto num círculo em volta do herói (fora da tela) e lança a galinha na direção dele.
     */
    private boolean lancarGalinha(Mundo mundo) {
        Galinha galinha = mundo.pegarGalinhaLivre();
        if (galinha == null) {
            return false;
        }
        Heroi heroi = mundo.getHeroi();

        double angulo = sorteio.nextDouble() * 2 * Math.PI;
        float inicioX = heroi.getCentroX() + (float) Math.cos(angulo) * DISTANCIA_DE_SURGIMENTO;
        float inicioY = heroi.getCentroY() + (float) Math.sin(angulo) * DISTANCIA_DE_SURGIMENTO;
        float alvoX = heroi.getX() + sorteio.nextInt(ERRO_DE_MIRA * 2 + 1) - ERRO_DE_MIRA;
        float alvoY = heroi.getY() + sorteio.nextInt(ERRO_DE_MIRA * 2 + 1) - ERRO_DE_MIRA;
        float velocidadeDoVoo = velocidade + sorteio.nextFloat() * VARIACAO_DE_VELOCIDADE;
        if (velocidadeDoVoo > VELOCIDADE_MAXIMA) {
            velocidadeDoVoo = VELOCIDADE_MAXIMA;
        }

        galinha.lancarContra(inicioX, inicioY, alvoX, alvoY, velocidadeDoVoo);
        return true;
    }

    /**
     * Galinhas que passaram direto e ficaram muito longe voltam para o estoque.
     */
    private void recolherGalinhasDistantes(Mundo mundo) {
        Galinha[] galinhas = mundo.getGalinhas();
        Heroi heroi = mundo.getHeroi();
        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (galinha.isAtiva() && galinha.isVoando() && galinha.distanciaAte(heroi) > DISTANCIA_PARA_SUMIR) {
                galinha.desativar();
            }
        }
    }
}
