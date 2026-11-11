package controle;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import dao.DaoMapa;
import dao.DaoRecorde;
import modelo.Cena;
import modelo.Dialogo;
import modelo.Galinha;
import modelo.Heroi;
import modelo.Mapa;
import modelo.Morador;
import modelo.Mundo;
import modelo.Partida;
import view.Janela;
import view.Som;

/**
 * Laço principal e regras do jogo: lê o teclado, atualiza o modelo e pede para a janela redesenhar.
 */
public class ControleJogo {
    private static final int MILISSEGUNDOS_POR_QUADRO = 1000 / 60;
    private static final int OPCAO_JOGAR = 0;
    private static final int OPCAO_SAIR = 1;
    private static final int QUADROS_DE_TREMOR_DA_RAIDE = 90;
    private static final int QUADROS_DE_TREMOR_DO_DANO = 8;
    private static final float RECUO_NA_SAIDA_FECHADA = 6;

    private static final String[] FALAS_AO_ACORDAR = {
        "Você acorda. Lá fora, alguém grita: \"GALINHA NÃO!\"",
        "Melhor ver o que está acontecendo na vila. (Setas para andar, Espaço para falar)"
    };
    private static final String[] FALAS_DO_PREFEITO_DEPOIS_DA_MISSAO = {
        "O campo fica ao sul, é só seguir a estrada.",
        "Boa sorte. Você vai precisar."
    };
    private static final String[] FALAS_DA_SAIDA_FECHADA = {
        "Melhor falar com o prefeito antes de sair da vila."
    };
    private static final String[] FALAS_AO_CHEGAR_NO_CAMPO = {
        "O campo. Algumas galinhas ciscam, tranquilas.",
        "Parece fácil. (Espaço para atacar, saia pela estrada para fugir)"
    };

    private Mundo mundo = new Mundo();
    private Teclado teclado = new Teclado();
    private ControleRaide controleRaide = new ControleRaide();
    private DaoMapa daoMapa = new DaoMapa();
    private DaoRecorde daoRecorde = new DaoRecorde();
    private Som som = new Som();
    private Janela janela;
    private boolean jogando = true;
    private boolean missaoSendoOferecida = false;

    public ControleJogo() {
        mundo.getPartida().setRecorde(daoRecorde.ler());
        janela = new Janela(mundo, teclado);
        som.tocarMusica("inicial");
    }

    public void iniciar() {
        long proximaAtualizacao = 0;

        while (jogando) {
            if (System.currentTimeMillis() >= proximaAtualizacao) {
                atualizar();
                janela.desenhar();
                proximaAtualizacao = System.currentTimeMillis() + MILISSEGUNDOS_POR_QUADRO;
            }
        }

        som.pararMusica();
        janela.dispose();
        System.exit(0);
    }

    private void atualizar() {
        switch (mundo.getPartida().getCena()) {
            case INICIAL:
                atualizarTelaInicial();
                break;
            case VILA:
                atualizarVila();
                break;
            case CAMPO:
                atualizarCampo();
                break;
            case FIM:
                atualizarTelaFinal();
                break;
        }
    }

    private void mudarCena(Cena cena) {
        mundo.getPartida().setCena(cena);
        teclado.limparToques();
    }

    // ---------- Tela inicial ----------

    private void atualizarTelaInicial() {
        Partida partida = mundo.getPartida();

        boolean apertouCima = teclado.consumirCima();
        boolean apertouBaixo = teclado.consumirBaixo();
        if (apertouCima || apertouBaixo) {
            if (partida.getOpcaoDoMenu() == OPCAO_JOGAR) {
                partida.setOpcaoDoMenu(OPCAO_SAIR);
            } else {
                partida.setOpcaoDoMenu(OPCAO_JOGAR);
            }
            som.tocarEfeito("menu");
        }

        if (teclado.consumirConfirmar()) {
            if (partida.getOpcaoDoMenu() == OPCAO_JOGAR) {
                comecarPartida();
            } else {
                jogando = false;
            }
        }

        if (teclado.consumirVoltar()) {
            jogando = false;
        }
    }

    private void comecarPartida() {
        mundo.getPartida().reiniciar();
        mundo.recolherTodasAsGalinhas();
        missaoSendoOferecida = false;
        carregarVila();
        mundo.getDialogo().abrir("", FALAS_AO_ACORDAR);
    }

    private void voltarParaTelaInicial() {
        mundo.getDialogo().fechar();
        mundo.recolherTodasAsGalinhas();
        mundo.getPartida().setRaideAtiva(false);
        mudarCena(Cena.INICIAL);
        som.tocarMusica("inicial");
    }

    // ---------- Vila ----------

    private void carregarVila() {
        Mapa mapa = daoMapa.carregar("/mapas/vila.txt");
        mundo.setMapa(mapa);
        mundo.setHeroi(criarHeroi(mapa));
        mundo.setMoradores(criarMoradores(mapa));
        mudarCena(Cena.VILA);
        som.tocarMusica("vila");
    }

    private Heroi criarHeroi(Mapa mapa) {
        Point inicio = mapa.encontrarMarcadores('H').get(0);
        return new Heroi(inicio.x + 2, inicio.y + 2);
    }

    /**
     * Cada morador fica no lugar do seu número no mapa (1 a 4).
     */
    private Morador[] criarMoradores(Mapa mapa) {
        Morador[] moradores = new Morador[4];

        moradores[0] = criarMorador(mapa, 1, "Prefeito Bento", new String[] {
            "Ah, finalmente acordou! Tenho um serviço pra você.",
            "Tem galinhas no campo ao sul. Derrote quantas puder.",
            "Pago 10 moedas por galinha!",
            "...Por que tanto? Ah, você vai descobrir. Digo, não é nada!"
        }, true);

        moradores[1] = criarMorador(mapa, 2, "Dona Cida", new String[] {
            "Galinhas? Nem me fale em galinhas.",
            "Uma delas ficou me encarando por três horas semana passada."
        }, false);

        moradores[2] = criarMorador(mapa, 3, "Seu Juca", new String[] {
            "Já reparou que ninguém nesta vila come ovo?",
            "Pois é. Melhor não perguntar."
        }, false);

        moradores[3] = criarMorador(mapa, 4, "Tico", new String[] {
            "Esse galinheiro aqui tá vazio desde o Incidente.",
            "Meu pai diz que galinha não anda em bando. Anda em EXÉRCITO."
        }, false);

        return moradores;
    }

    private Morador criarMorador(Mapa mapa, int numero, String nome, String[] falas, boolean daMissao) {
        char marcador = Character.forDigit(numero, 10);
        Point posicao = mapa.encontrarMarcadores(marcador).get(0);
        return new Morador(posicao.x + 2, posicao.y + 2, numero, nome, falas, daMissao);
    }

    private void atualizarVila() {
        if (teclado.consumirVoltar()) {
            voltarParaTelaInicial();
            return;
        }
        if (mundo.getDialogo().isAtivo()) {
            atualizarDialogo();
            return;
        }

        Heroi heroi = mundo.getHeroi();
        heroi.atualizar(mundo.getMapa());
        moverHeroiPeloTeclado();

        if (teclado.consumirAcao()) {
            conversarComMoradorProximo();
        }

        if (heroiEstaNaSaida()) {
            if (mundo.getPartida().isMissaoAceita()) {
                carregarCampo();
            } else {
                recuarHeroi();
                mundo.getDialogo().abrir("", FALAS_DA_SAIDA_FECHADA);
            }
        }
    }

    private void conversarComMoradorProximo() {
        Heroi heroi = mundo.getHeroi();
        Morador[] moradores = mundo.getMoradores();

        for (int i = 0; i < moradores.length; i++) {
            Morador morador = moradores[i];
            if (morador.estaPerto(heroi)) {
                morador.olharPara(heroi);
                heroi.setAndando(false);
                abrirConversa(morador);
                som.tocarEfeito("dialogo");
                return;
            }
        }
    }

    private void abrirConversa(Morador morador) {
        Dialogo dialogo = mundo.getDialogo();
        if (morador.isDaMissao() && mundo.getPartida().isMissaoAceita()) {
            dialogo.abrir(morador.getNome(), FALAS_DO_PREFEITO_DEPOIS_DA_MISSAO);
        } else {
            dialogo.abrir(morador.getNome(), morador.getFalas());
            if (morador.isDaMissao()) {
                missaoSendoOferecida = true;
            }
        }
    }

    /**
     * Empurra o herói um pouco para trás, para ele não ficar parado em cima da saída.
     */
    private void recuarHeroi() {
        Heroi heroi = mundo.getHeroi();
        float recuoX = -heroi.getDirecao().getPassoX() * RECUO_NA_SAIDA_FECHADA;
        float recuoY = -heroi.getDirecao().getPassoY() * RECUO_NA_SAIDA_FECHADA;
        heroi.mover(recuoX, recuoY, mundo.getMapa(), mundo.getMoradores());
        heroi.setAndando(false);
    }

    // ---------- Diálogo ----------

    private void atualizarDialogo() {
        boolean apertouAcao = teclado.consumirAcao();
        boolean apertouConfirmar = teclado.consumirConfirmar();
        if (!apertouAcao && !apertouConfirmar) {
            return;
        }

        Dialogo dialogo = mundo.getDialogo();
        dialogo.avancar();
        som.tocarEfeito("dialogo");

        if (!dialogo.isAtivo() && missaoSendoOferecida) {
            missaoSendoOferecida = false;
            mundo.getPartida().setMissaoAceita(true);
            som.tocarEfeito("missao");
        }
    }

    // ---------- Campo ----------

    private void carregarCampo() {
        Mapa mapa = daoMapa.carregar("/mapas/campo.txt");
        mundo.setMapa(mapa);
        mundo.setHeroi(criarHeroi(mapa));
        mundo.setMoradores(new Morador[0]);
        mundo.recolherTodasAsGalinhas();

        List<Point> posicoesDasGalinhas = mapa.encontrarMarcadores('G');
        for (int i = 0; i < posicoesDasGalinhas.size(); i++) {
            Point posicao = posicoesDasGalinhas.get(i);
            Galinha galinha = mundo.pegarGalinhaLivre();
            galinha.soltarNoChao(posicao.x + 2, posicao.y + 2);
        }

        mudarCena(Cena.CAMPO);
        som.tocarMusica("campo");
        mundo.getDialogo().abrir("", FALAS_AO_CHEGAR_NO_CAMPO);
    }

    private void atualizarCampo() {
        if (teclado.consumirVoltar()) {
            voltarParaTelaInicial();
            return;
        }
        if (mundo.getDialogo().isAtivo()) {
            atualizarDialogo();
            return;
        }

        Partida partida = mundo.getPartida();
        Heroi heroi = mundo.getHeroi();
        heroi.atualizar(mundo.getMapa());

        if (heroi.estaAtacando()) {
            heroi.setAndando(false);
        } else {
            moverHeroiPeloTeclado();
        }

        if (teclado.consumirAcao() && heroi.podeAtacar()) {
            heroi.atacar();
            heroi.setAndando(false);
            som.tocarEfeito("golpe");
        }
        if (heroi.golpeAcertaAgora()) {
            aplicarGolpe();
        }

        atualizarGalinhas();
        controleRaide.atualizar(mundo);
        verificarGalinhasAcertandoOHeroi();
        partida.diminuirTremor();

        if (!heroi.estaVivo()) {
            terminarPartida(false);
        } else if (heroiEstaNaSaida()) {
            terminarPartida(true);
        }
    }

    private void aplicarGolpe() {
        Heroi heroi = mundo.getHeroi();
        Rectangle areaDoGolpe = heroi.getAreaDoGolpe();
        Galinha[] galinhas = mundo.getGalinhas();

        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (galinha.isAtiva() && areaDoGolpe.intersects(galinha.getArea())) {
                boolean morreu = galinha.receberGolpe(heroi.getArma().getDano(), heroi.getDirecaoDoOlhar(), mundo.getMapa());
                if (morreu) {
                    galinhaDerrotada();
                } else {
                    som.tocarEfeito("acerto");
                }
            }
        }
    }

    private void galinhaDerrotada() {
        Partida partida = mundo.getPartida();
        partida.registrarGalinhaDerrotada();
        som.tocarEfeito("galinha");

        if (!partida.isRaideAtiva()) {
            controleRaide.iniciar(partida);
            partida.tremer(QUADROS_DE_TREMOR_DA_RAIDE);
            som.tocarEfeito("raide");
            som.tocarMusica("raide");
        }
    }

    private void atualizarGalinhas() {
        Galinha[] galinhas = mundo.getGalinhas();
        for (int i = 0; i < galinhas.length; i++) {
            galinhas[i].atualizar(mundo.getMapa());
        }
    }

    /**
     * Só as galinhas da raide machucam. As que estão ciscando no chão são inofensivas.
     */
    private void verificarGalinhasAcertandoOHeroi() {
        Heroi heroi = mundo.getHeroi();
        Galinha[] galinhas = mundo.getGalinhas();

        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (galinha.isAtiva() && galinha.isVoando() && !heroi.estaInvencivel() && galinha.colide(heroi)) {
                heroi.receberDano(1);
                mundo.getPartida().tremer(QUADROS_DE_TREMOR_DO_DANO);
                som.tocarEfeito("dano");
            }
        }
    }

    private void terminarPartida(boolean fugiu) {
        Partida partida = mundo.getPartida();
        partida.setFugiu(fugiu);
        partida.setRaideAtiva(false);
        mundo.recolherTodasAsGalinhas();

        if (partida.atualizarRecorde()) {
            daoRecorde.salvar(partida.getRecorde());
        }

        mudarCena(Cena.FIM);
        if (fugiu) {
            som.tocarMusica("fuga");
        } else {
            som.tocarMusica("derrota");
        }
    }

    // ---------- Tela final ----------

    private void atualizarTelaFinal() {
        boolean apertouConfirmar = teclado.consumirConfirmar();
        boolean apertouVoltar = teclado.consumirVoltar();
        if (apertouConfirmar || apertouVoltar) {
            mudarCena(Cena.INICIAL);
            som.tocarMusica("inicial");
        }
    }

    // ---------- Movimento do herói ----------

    private void moverHeroiPeloTeclado() {
        Heroi heroi = mundo.getHeroi();
        float passoX = 0;
        float passoY = 0;

        if (teclado.isEsquerda()) {
            passoX = -1;
        } else if (teclado.isDireita()) {
            passoX = 1;
        }
        if (teclado.isCima()) {
            passoY = -1;
        } else if (teclado.isBaixo()) {
            passoY = 1;
        }

        if (passoX == 0 && passoY == 0) {
            heroi.setAndando(false);
            return;
        }

        float velocidade = heroi.getVelocidade();
        boolean naDiagonal = passoX != 0 && passoY != 0;
        if (naDiagonal) {
            velocidade = velocidade * 0.7f;
        }

        heroi.virarPara(passoX, passoY);
        heroi.mover(passoX * velocidade, passoY * velocidade, mundo.getMapa(), mundo.getMoradores());
        heroi.setAndando(true);
    }

    private boolean heroiEstaNaSaida() {
        Heroi heroi = mundo.getHeroi();
        char tile = mundo.getMapa().getTileNaPosicao(heroi.getCentroX(), heroi.getCentroY());
        return tile == Mapa.SAIDA;
    }
}
