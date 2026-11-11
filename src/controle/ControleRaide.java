package controle;

import java.util.Random;

import modelo.Galinha;
import modelo.Heroi;
import modelo.Mundo;
import modelo.Partida;

/**
 * Cuida da raide: depois de uma pausa dramática, lança galinhas contra o herói
 * a partir de pontos fora da tela. A cada onda elas chegam mais rápido e em maior número.
 */
public class ControleRaide {
    private static final int QUADROS_DE_PREPARACAO = 90;
    private static final int QUADROS_POR_ONDA = 600;
    private static final int INTERVALO_INICIAL = 60;
    private static final int INTERVALO_MINIMO = 12;
    private static final int REDUCAO_DO_INTERVALO_POR_ONDA = 8;
    private static final float VELOCIDADE_INICIAL = 1.8f;
    private static final float AUMENTO_DE_VELOCIDADE_POR_ONDA = 0.15f;
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

    public void atualizar(Mundo mundo) {
        Partida partida = mundo.getPartida();
        if (!partida.isRaideAtiva()) {
            return;
        }
        if (quadrosDePreparacao > 0) {
            quadrosDePreparacao--;
            return;
        }

        quadrosNaOnda++;
        if (quadrosNaOnda >= QUADROS_POR_ONDA) {
            passarDeOnda(partida);
        }

        quadrosAteProximaGalinha--;
        if (quadrosAteProximaGalinha <= 0) {
            int quantidade = 1 + partida.getOnda() / 3;
            for (int i = 0; i < quantidade; i++) {
                lancarGalinha(mundo);
            }
            quadrosAteProximaGalinha = intervalo;
        }

        recolherGalinhasDistantes(mundo);
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
    private void lancarGalinha(Mundo mundo) {
        Galinha galinha = mundo.pegarGalinhaLivre();
        if (galinha == null) {
            return;
        }
        Heroi heroi = mundo.getHeroi();

        double angulo = sorteio.nextDouble() * 2 * Math.PI;
        float inicioX = heroi.getCentroX() + (float) Math.cos(angulo) * DISTANCIA_DE_SURGIMENTO;
        float inicioY = heroi.getCentroY() + (float) Math.sin(angulo) * DISTANCIA_DE_SURGIMENTO;
        float alvoX = heroi.getX() + sorteio.nextInt(ERRO_DE_MIRA * 2 + 1) - ERRO_DE_MIRA;
        float alvoY = heroi.getY() + sorteio.nextInt(ERRO_DE_MIRA * 2 + 1) - ERRO_DE_MIRA;
        float velocidadeDoVoo = velocidade + sorteio.nextFloat() * 0.6f;

        galinha.lancarContra(inicioX, inicioY, alvoX, alvoY, velocidadeDoVoo);
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
