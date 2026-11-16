package controle;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import dao.DaoControles;
import dao.DaoFalas;
import dao.DaoMapa;
import dao.DaoRecorde;
import modelo.Animal;
import modelo.Cena;
import modelo.Comando;
import modelo.Configuracao;
import modelo.Controles;
import modelo.Decoracao;
import modelo.Dialogo;
import modelo.Direcao;
import modelo.EdicaoDeControles;
import modelo.Efeito;
import modelo.Entidade;
import modelo.Falas;
import modelo.Galinha;
import modelo.Heroi;
import modelo.Mapa;
import modelo.Morador;
import modelo.Mundo;
import modelo.Partida;
import view.Janela;
import view.Som;

/**
 * Laço principal e regras do jogo: lê o teclado e o controle, atualiza o modelo e pede para a janela redesenhar.
 */
public class ControleJogo {
    private static final int MILISSEGUNDOS_POR_QUADRO = 1000 / 60;
    private static final String SOM_DOS_PASSOS = "passos";
    // A galinha do campo cacareja de tempos em tempos: entre 3 e 6 segundos.
    private static final int PRIMEIRO_CACAREJO = 60;
    private static final int MENOR_PAUSA_ENTRE_CACAREJOS = 180;
    private static final int MAIOR_PAUSA_ENTRE_CACAREJOS = 360;
    // As galinhas da raide cacarejam no máximo uma vez por segundo.
    private static final int PAUSA_ENTRE_CACAREJOS_DA_RAIDE = 60;
    private static final int QUADROS_DE_TREMOR_DA_RAIDE = 90;
    private static final int QUADROS_DE_TREMOR_DO_DANO = 8;
    private static final float RECUO_NA_SAIDA_FECHADA = 6;
    private static final int PASSOS_ENTRE_POEIRAS = 14;
    // Chegada ao campo: o herói começa fora do mapa e anda devagar até o ponto marcado.
    private static final float INICIO_DA_ENTRADA_X = -14;
    private static final float VELOCIDADE_DA_ENTRADA = 0.6f;
    // Tempo da animação de morte (13 quadros) mais uma pausa antes da tela final.
    private static final int QUADROS_DA_MORTE = 13 * 6 + 60;
    // A tela final ignora os botões por meio segundo, para quem ainda estava atacando não pular o resultado.
    private static final int QUADROS_ANTES_DE_SAIR_DO_FIM = 30;
    private static final String MAPA_DA_VILA = "/mapas/vila.tmx";
    private static final String MAPA_DO_CAMPO = "/mapas/campo.tmx";

    // Todas as falas ficam em recursos/falas.txt (CHAVE=texto).
    private static final String ARQUIVO_DE_FALAS = "/falas.txt";
    private static final int QUANTIDADE_DE_MORADORES = 4;
    // O morador que oferece a missão (NPC1 no falas.txt, ponto morador1 no mapa).
    private static final int NUMERO_DO_PREFEITO = 1;

    private Mundo mundo = new Mundo();
    private Entrada entrada = new Entrada();
    private Teclado teclado = new Teclado(entrada, mundo.getConfiguracao().getControles());
    private Joystick joystick = new Joystick();
    private DaoControles daoControles = new DaoControles();
    private ControleRaide controleRaide = new ControleRaide();
    private DaoMapa daoMapa = new DaoMapa();
    private DaoRecorde daoRecorde = new DaoRecorde();
    private DaoFalas daoFalas = new DaoFalas();
    private Falas falas;
    private Som som = new Som();
    private Janela janela;
    private boolean jogando = true;
    private boolean missaoSendoOferecida = false;
    private float destinoDaEntradaX;
    // Posição do herói no quadro anterior: os passos só soam se ele saiu do lugar.
    private float ultimaPosicaoX;
    private float ultimaPosicaoY;
    private Random sorteio = new Random();
    private int quadrosAteCacarejar;
    private int quadrosDesdeOCacarejoDaRaide;
    // A música da raide espera o canto do galo terminar. Zero = nada esperando.
    private int quadrosAteAMusicaDaRaide;
    private int quadrosNaTelaFinal;

    public ControleJogo() {
        mundo.getPartida().setRecorde(daoRecorde.ler());
        daoControles.ler(getControles());
        joystick.procurar(getControles());
        falas = daoFalas.carregar(ARQUIVO_DE_FALAS);
        mundo.setMapaDoFundo(daoMapa.carregar(MAPA_DA_VILA));
        janela = new Janela(mundo, teclado);
        janela.addFocusListener(teclado);
        som.definirVolumeDaMusica(mundo.getConfiguracao().getVolumeDaMusica());
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

    private Controles getControles() {
        return mundo.getConfiguracao().getControles();
    }

    private void atualizar() {
        joystick.ler(entrada, getControles());
        entrada.novoQuadro();
        if (entrada.consumirTelaCheia()) {
            janela.alternarTelaCheia();
            entrada.limparToques();
        }
        if (entrada.consumirMais()) {
            mudarVolumeDaMusica(Configuracao.PASSO_DO_VOLUME);
        }
        if (entrada.consumirMenos()) {
            mudarVolumeDaMusica(-Configuracao.PASSO_DO_VOLUME);
        }
        mundo.getConfiguracao().contarQuadroDoAviso();

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
            case CREDITOS:
                atualizarCreditos();
                break;
            case CONTROLES:
                atualizarTelaDeControles();
                break;
        }
        atualizarSomDosPassos();
    }

    private void mudarVolumeDaMusica(int diferenca) {
        Configuracao configuracao = mundo.getConfiguracao();
        configuracao.mudarVolumeDaMusica(diferenca);
        som.definirVolumeDaMusica(configuracao.getVolumeDaMusica());
        som.tocarEfeito("menu");
    }

    /**
     * Passos na grama em loop enquanto o herói anda de verdade (empurrar a parede não conta).
     */
    private void atualizarSomDosPassos() {
        Heroi heroi = mundo.getHeroi();
        Cena cena = mundo.getPartida().getCena();
        boolean andando = false;
        if (heroi != null && (cena == Cena.VILA || cena == Cena.CAMPO)) {
            boolean saiuDoLugar = heroi.getX() != ultimaPosicaoX || heroi.getY() != ultimaPosicaoY;
            andando = heroi.isAndando() && saiuDoLugar;
            ultimaPosicaoX = heroi.getX();
            ultimaPosicaoY = heroi.getY();
        }
        if (andando) {
            som.tocarEfeitoEmLoop(SOM_DOS_PASSOS);
        } else {
            som.pararEfeito(SOM_DOS_PASSOS);
        }
    }

    private void mudarCena(Cena cena) {
        mundo.getPartida().setCena(cena);
        entrada.limparToques();
    }

    // ---------- Tela inicial ----------

    private void atualizarTelaInicial() {
        Partida partida = mundo.getPartida();

        if (entrada.consumir(Comando.CIMA)) {
            partida.setOpcaoDoMenu((partida.getOpcaoDoMenu() + Partida.QUANTIDADE_DE_OPCOES - 1) % Partida.QUANTIDADE_DE_OPCOES);
            som.tocarEfeito("menu");
        }
        if (entrada.consumir(Comando.BAIXO)) {
            partida.setOpcaoDoMenu((partida.getOpcaoDoMenu() + 1) % Partida.QUANTIDADE_DE_OPCOES);
            som.tocarEfeito("menu");
        }

        // Na linha "Música", esquerda e direita mudam o volume.
        boolean apertouEsquerda = entrada.consumir(Comando.ESQUERDA);
        boolean apertouDireita = entrada.consumir(Comando.DIREITA);
        if (partida.getOpcaoDoMenu() == Partida.OPCAO_MUSICA) {
            if (apertouEsquerda) {
                mudarVolumeDaMusica(-Configuracao.PASSO_DO_VOLUME);
            }
            if (apertouDireita) {
                mudarVolumeDaMusica(Configuracao.PASSO_DO_VOLUME);
            }
        }

        if (entrada.consumir(Comando.CONFIRMAR)) {
            if (partida.getOpcaoDoMenu() == Partida.OPCAO_JOGAR) {
                comecarPartida();
            } else if (partida.getOpcaoDoMenu() == Partida.OPCAO_CONTROLES) {
                abrirTelaDeControles();
            } else if (partida.getOpcaoDoMenu() == Partida.OPCAO_CREDITOS) {
                mudarCena(Cena.CREDITOS);
                som.tocarEfeito("menu");
            } else if (partida.getOpcaoDoMenu() == Partida.OPCAO_SAIR) {
                jogando = false;
            }
        }

        if (entrada.consumir(Comando.VOLTAR)) {
            jogando = false;
        }
    }

    // ---------- Créditos ----------

    private void atualizarCreditos() {
        boolean apertouConfirmar = entrada.consumir(Comando.CONFIRMAR);
        boolean apertouVoltar = entrada.consumir(Comando.VOLTAR);
        boolean apertouAcao = entrada.consumir(Comando.ACAO);
        if (apertouConfirmar || apertouVoltar || apertouAcao) {
            mudarCena(Cena.INICIAL);
            som.tocarEfeito("menu");
        }
    }

    // ---------- Controles ----------

    private void abrirTelaDeControles() {
        mundo.getEdicaoDeControles().reiniciar();
        if (!getControles().isControleConectado()) {
            joystick.procurar(getControles());
        }
        mudarCena(Cena.CONTROLES);
        som.tocarEfeito("menu");
    }

    private void atualizarTelaDeControles() {
        EdicaoDeControles edicao = mundo.getEdicaoDeControles();
        edicao.contarQuadroDoAviso();
        if (edicao.isEsperando()) {
            atualizarTrocaDeControle(edicao);
            return;
        }

        if (entrada.consumir(Comando.CIMA)) {
            edicao.subir();
            som.tocarEfeito("menu");
        }
        if (entrada.consumir(Comando.BAIXO)) {
            edicao.descer();
            som.tocarEfeito("menu");
        }
        boolean apertouEsquerda = entrada.consumir(Comando.ESQUERDA);
        boolean apertouDireita = entrada.consumir(Comando.DIREITA);
        if ((apertouEsquerda || apertouDireita) && edicao.isNaTabela()) {
            edicao.trocarDeColuna();
            som.tocarEfeito("menu");
        }

        if (entrada.consumir(Comando.VOLTAR)) {
            sairDaTelaDeControles();
            return;
        }
        boolean apertouConfirmar = entrada.consumir(Comando.CONFIRMAR);
        boolean apertouAcao = entrada.consumir(Comando.ACAO);
        if (!apertouConfirmar && !apertouAcao) {
            return;
        }

        if (edicao.getLinha() == EdicaoDeControles.LINHA_VOLTAR) {
            sairDaTelaDeControles();
        } else if (edicao.getLinha() == EdicaoDeControles.LINHA_RESTAURAR) {
            getControles().restaurarPadrao();
            entrada.soltarTudo();
            daoControles.salvar(getControles());
            edicao.avisar("Controles padrão de volta");
            som.tocarEfeito("missao");
        } else if (edicao.getColuna() == EdicaoDeControles.COLUNA_CONTROLE) {
            comecarTrocaDeBotao(edicao);
        } else {
            edicao.comecarAEsperar();
            entrada.comecarCaptura();
            som.tocarEfeito("menu");
        }
    }

    private void comecarTrocaDeBotao(EdicaoDeControles edicao) {
        if (edicao.getComando().isDirecao()) {
            edicao.avisar("No controle, ande com o direcional ou o analógico");
            return;
        }
        if (!getControles().isControleConectado()) {
            joystick.procurar(getControles());
        }
        if (!getControles().isControleConectado()) {
            edicao.avisar("Nenhum controle encontrado");
            som.tocarEfeito("erro");
            return;
        }
        edicao.comecarAEsperar();
        entrada.comecarCaptura();
        som.tocarEfeito("menu");
    }

    /**
     * Esperando a tecla ou o botão novo. Esc cancela; Backspace apaga
     * (no teclado, a segunda tecla; no controle, os botões do comando).
     */
    private void atualizarTrocaDeControle(EdicaoDeControles edicao) {
        if (edicao.contarQuadroEsperando()) {
            terminarTrocaDeControle(edicao, "Tempo esgotado, nada mudou");
            return;
        }
        Comando comando = edicao.getComando();
        boolean naColunaDoControle = edicao.getColuna() == EdicaoDeControles.COLUNA_CONTROLE;
        int tecla = entrada.pegarTeclaCapturada();
        String botao = entrada.pegarBotaoCapturado();

        if (tecla == KeyEvent.VK_ESCAPE) {
            terminarTrocaDeControle(edicao, "Nada mudou");
        } else if (tecla == KeyEvent.VK_BACK_SPACE || tecla == KeyEvent.VK_DELETE) {
            if (naColunaDoControle) {
                getControles().apagarBotoes(comando);
                terminarTrocaDeControle(edicao, comando.getNome() + ": sem botões no controle");
            } else {
                getControles().apagarSegundaTecla(comando);
                terminarTrocaDeControle(edicao, comando.getNome() + ": segunda tecla apagada");
            }
        } else if (tecla != Controles.SEM_TECLA && naColunaDoControle) {
            edicao.avisar("Aperte um botão do controle");
        } else if (tecla != Controles.SEM_TECLA) {
            trocarTecla(edicao, comando, tecla);
        } else if (botao != null && !naColunaDoControle) {
            edicao.avisar("Aperte uma tecla do teclado");
        } else if (botao != null) {
            getControles().definirBotao(comando, botao);
            terminarTrocaDeControle(edicao, comando.getNome() + ": " + getControles().nomeDoBotao(botao));
        }
    }

    private void trocarTecla(EdicaoDeControles edicao, Comando comando, int tecla) {
        String nome = Controles.nomeDaTecla(tecla);
        if (Teclado.isTeclaDoVolume(tecla)) {
            edicao.avisar(nome + " fica com o volume da música");
            return;
        }
        Comando dono = getControles().comandoDaTecla(tecla);
        if (!getControles().definirTecla(comando, tecla)) {
            edicao.avisar(nome + " é a única tecla de " + dono.getNome());
            return;
        }
        String mensagem = comando.getNome() + ": " + nome;
        if (dono != null && dono != comando) {
            mensagem = mensagem + " (saiu de " + dono.getNome() + ")";
        }
        terminarTrocaDeControle(edicao, mensagem);
    }

    private void terminarTrocaDeControle(EdicaoDeControles edicao, String aviso) {
        edicao.pararDeEsperar();
        edicao.avisar(aviso);
        entrada.terminarCaptura();
        entrada.soltarTudo();
        daoControles.salvar(getControles());
        som.tocarEfeito("menu");
    }

    private void sairDaTelaDeControles() {
        mudarCena(Cena.INICIAL);
        som.tocarEfeito("menu");
    }

    private void comecarPartida() {
        mundo.getPartida().reiniciar();
        mundo.recolherTodasAsGalinhas();
        mundo.apagarEfeitos();
        missaoSendoOferecida = false;
        // Lido de novo a cada partida: dá para mudar uma fala sem fechar o jogo.
        falas = daoFalas.carregar(ARQUIVO_DE_FALAS);
        carregarVila();
        mundo.getDialogo().abrir("", falas.getLista("ACORDAR"));
    }

    private void voltarParaTelaInicial() {
        mundo.getDialogo().fechar();
        mundo.recolherTodasAsGalinhas();
        mundo.getPartida().setRaideAtiva(false);
        quadrosAteAMusicaDaRaide = 0;
        mudarCena(Cena.INICIAL);
        som.tocarMusica("inicial");
    }

    // ---------- Vila ----------

    private void carregarVila() {
        Mapa mapa = daoMapa.carregar(MAPA_DA_VILA);
        mundo.setMapa(mapa);
        mundo.setHeroi(criarHeroi(mapa));
        mundo.setMoradores(criarMoradores(mapa));
        mundo.setAnimais(criarAnimais(mapa));
        mudarCena(Cena.VILA);
        som.tocarMusica("vila");
    }

    private Heroi criarHeroi(Mapa mapa) {
        Point inicio = mapa.encontrarMarcador("heroi");
        return new Heroi(inicio.x - 6, inicio.y - 6);
    }

    /**
     * Cada animal desenhado no Tiled vira um animal de verdade, que passeia.
     */
    private Animal[] criarAnimais(Mapa mapa) {
        Decoracao[] lugares = mapa.getAnimais();
        Animal[] animais = new Animal[lugares.length];
        for (int i = 0; i < lugares.length; i++) {
            Decoracao lugar = lugares[i];
            String tipo = lugar.getTipo();
            float peX = lugar.getX() + lugar.getLargura() / 2.0f;
            float peY = lugar.getPeY() - Animal.margemDaImagem(tipo);
            animais[i] = new Animal(tipo, peX, peY);
            if (lugar.isEspelhada()) {
                animais[i].setDirecao(Direcao.ESQUERDA);
            }
        }
        return animais;
    }

    private void atualizarAnimais() {
        Animal[] animais = mundo.getAnimais();
        Entidade[] obstaculos = mundo.getObstaculos();
        Heroi heroi = mundo.getHeroi();
        for (int i = 0; i < animais.length; i++) {
            animais[i].atualizar(mundo.getMapa(), obstaculos);
            // Se o animal encostou no herói, volta para não prendê-lo.
            if (animais[i].colide(heroi)) {
                animais[i].desfazerUltimoPasso();
            }
        }
    }

    /**
     * Cada morador fica no ponto "morador1" a "morador4" do mapa.
     */
    private Morador[] criarMoradores(Mapa mapa) {
        Morador[] moradores = new Morador[QUANTIDADE_DE_MORADORES];
        for (int i = 0; i < moradores.length; i++) {
            int numero = i + 1;
            String nome = falas.getTexto("NPC" + numero + "_NOME");
            String[] falasDoMorador = falas.getLista("NPC" + numero + "_FALA");
            boolean daMissao = numero == NUMERO_DO_PREFEITO;
            moradores[i] = criarMorador(mapa, numero, nome, falasDoMorador, daMissao);
        }
        return moradores;
    }

    private Morador criarMorador(Mapa mapa, int numero, String nome, String[] falas, boolean daMissao) {
        Point posicao = mapa.encontrarMarcador("morador" + numero);
        return new Morador(posicao.x - 6, posicao.y - 6, numero, nome, falas, daMissao);
    }

    private void atualizarVila() {
        if (entrada.consumir(Comando.VOLTAR)) {
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
        atualizarAnimais();
        mundo.atualizarEfeitos();

        if (entrada.consumir(Comando.ACAO)) {
            conversarComMoradorProximo();
        }

        if (heroiEstaNaSaida()) {
            if (mundo.getPartida().isMissaoAceita()) {
                carregarCampo();
            } else {
                recuarHeroi();
                mundo.getDialogo().abrir("", falas.getLista("SAIDA_FECHADA"));
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
            dialogo.abrir(morador.getNome(), falas.getLista("NPC" + morador.getNumero() + "_DEPOIS_DA_MISSAO"),
                    morador.getNumero());
        } else {
            dialogo.abrir(morador.getNome(), morador.getFalas(), morador.getNumero());
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
        heroi.mover(recuoX, recuoY, mundo.getMapa(), mundo.getObstaculos());
        heroi.setAndando(false);
    }

    // ---------- Diálogo ----------

    private void atualizarDialogo() {
        boolean apertouAcao = entrada.consumir(Comando.ACAO);
        boolean apertouConfirmar = entrada.consumir(Comando.CONFIRMAR);
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
        Mapa mapa = daoMapa.carregar(MAPA_DO_CAMPO);
        mundo.setMapa(mapa);
        mundo.setHeroi(criarHeroi(mapa));
        mundo.setMoradores(new Morador[0]);
        mundo.setAnimais(criarAnimais(mapa));
        mundo.recolherTodasAsGalinhas();
        mundo.apagarEfeitos();

        // O campo começa só com as galinhas marcadas no mapa (no mapa padrão, uma).
        // As outras chegam com a raide, depois que a primeira cai.
        List<Point> posicoesDasGalinhas = mapa.encontrarMarcadores("galinha");
        for (int i = 0; i < posicoesDasGalinhas.size(); i++) {
            Point posicao = posicoesDasGalinhas.get(i);
            Galinha galinha = mundo.pegarGalinhaLivre();
            galinha.soltarNoChao(posicao.x - 6, posicao.y - 6);
        }

        // O herói chega andando devagar, vindo de fora do mapa pela esquerda,
        // até o ponto marcado no mapa. Só então aparece a fala da chegada.
        Heroi heroi = mundo.getHeroi();
        destinoDaEntradaX = heroi.getX();
        heroi.setPosicao(INICIO_DA_ENTRADA_X, heroi.getY());
        heroi.setDirecao(Direcao.DIREITA);
        heroi.setCaminhando(true);
        heroi.setAndando(true);
        mundo.getPartida().comecarEntrada();
        quadrosAteCacarejar = PRIMEIRO_CACAREJO;
        quadrosDesdeOCacarejoDaRaide = PAUSA_ENTRE_CACAREJOS_DA_RAIDE;
        quadrosAteAMusicaDaRaide = 0;

        mudarCena(Cena.CAMPO);
        som.tocarMusica("campo");
    }

    private void atualizarEntradaNoCampo() {
        Heroi heroi = mundo.getHeroi();
        atualizarAnimais();
        atualizarGalinhas();
        mundo.atualizarEfeitos();

        // Os passos andam na metade do ritmo normal, para combinar com o andar lento.
        if (mundo.getPartida().getQuadrosDaEntrada() % 2 == 0) {
            heroi.atualizar(mundo.getMapa());
        }

        float novoX = heroi.getX() + VELOCIDADE_DA_ENTRADA;
        if (novoX < destinoDaEntradaX) {
            heroi.setPosicao(novoX, heroi.getY());
            return;
        }
        heroi.setPosicao(destinoDaEntradaX, heroi.getY());
        heroi.setCaminhando(false);
        heroi.setAndando(false);
        entrada.limparToques();
        mundo.getDialogo().abrir("", falas.getLista("CHEGADA_NO_CAMPO"));
    }

    private void atualizarCampo() {
        if (entrada.consumir(Comando.VOLTAR)) {
            voltarParaTelaInicial();
            return;
        }
        mundo.getPartida().contarQuadroDaEntrada();
        atualizarCacarejoDaGalinhaDoCampo();
        if (mundo.getHeroi().isCaminhando()) {
            atualizarEntradaNoCampo();
            return;
        }
        if (mundo.getDialogo().isAtivo()) {
            atualizarDialogo();
            return;
        }

        Partida partida = mundo.getPartida();
        Heroi heroi = mundo.getHeroi();
        heroi.atualizar(mundo.getMapa());
        atualizarAnimais();
        mundo.atualizarEfeitos();

        if (!heroi.estaVivo()) {
            atualizarMorte();
            return;
        }

        if (heroi.estaAtacando()) {
            heroi.setAndando(false);
        } else {
            moverHeroiPeloTeclado();
        }

        if (entrada.consumir(Comando.ACAO) && heroi.podeAtacar()) {
            heroi.atacar();
            heroi.setAndando(false);
        }
        if (heroi.golpeAcertaAgora()) {
            boolean acertou = aplicarGolpe();
            if (acertou) {
                som.tocarEfeito("acerto");
            } else {
                som.tocarEfeito("erro");
            }
        }

        atualizarGalinhas();
        boolean lancouGalinhas = controleRaide.atualizar(mundo);
        atualizarCacarejoDaRaide(lancouGalinhas);
        atualizarMusicaDaRaide();
        verificarGalinhasAcertandoOHeroi();
        partida.diminuirTremor();

        if (!heroi.estaVivo()) {
            heroi.setAndando(false);
            quadrosAteAMusicaDaRaide = 0;
            som.pararMusica();
        } else if (heroiEstaNaSaida()) {
            terminarPartida(true);
        }
    }

    /**
     * O herói caiu: as galinhas que já estavam no ar terminam o voo, a animação
     * de morte roda e só depois aparece a tela final.
     */
    private void atualizarMorte() {
        atualizarGalinhas();
        mundo.getPartida().diminuirTremor();
        if (mundo.getHeroi().getQuadrosDesdeAMorte() >= QUADROS_DA_MORTE) {
            terminarPartida(false);
        }
    }

    /**
     * Cacarejo da galinha que cisca no campo, de tempos em tempos, até a raide começar.
     */
    private void atualizarCacarejoDaGalinhaDoCampo() {
        if (mundo.getPartida().isRaideAtiva() || !mundo.temGalinhaCiscando()) {
            return;
        }
        quadrosAteCacarejar--;
        if (quadrosAteCacarejar <= 0) {
            som.tocarEfeito("galinha_ciscando");
            int variacao = MAIOR_PAUSA_ENTRE_CACAREJOS - MENOR_PAUSA_ENTRE_CACAREJOS + 1;
            quadrosAteCacarejar = MENOR_PAUSA_ENTRE_CACAREJOS + sorteio.nextInt(variacao);
        }
    }

    private void atualizarCacarejoDaRaide(boolean lancouGalinhas) {
        quadrosDesdeOCacarejoDaRaide++;
        if (lancouGalinhas && quadrosDesdeOCacarejoDaRaide >= PAUSA_ENTRE_CACAREJOS_DA_RAIDE) {
            som.tocarEfeito("galinha_raide");
            quadrosDesdeOCacarejoDaRaide = 0;
        }
    }

    /**
     * Conta o tempo do canto do galo; quando ele acaba, entra a música da raide.
     */
    private void atualizarMusicaDaRaide() {
        if (quadrosAteAMusicaDaRaide <= 0) {
            return;
        }
        quadrosAteAMusicaDaRaide--;
        if (quadrosAteAMusicaDaRaide == 0) {
            som.tocarMusica("raide");
        }
    }

    /**
     * Devolve true se o golpe pegou pelo menos uma galinha.
     */
    private boolean aplicarGolpe() {
        Heroi heroi = mundo.getHeroi();
        Rectangle areaDoGolpe = heroi.getAreaDoGolpe();
        Galinha[] galinhas = mundo.getGalinhas();
        boolean acertou = false;

        for (int i = 0; i < galinhas.length; i++) {
            Galinha galinha = galinhas[i];
            if (galinha.isAtiva() && areaDoGolpe.intersects(galinha.getArea())) {
                acertou = true;
                float centroX = galinha.getCentroX();
                float centroY = galinha.getCentroY() - 6;
                boolean morreu = galinha.receberGolpe(heroi.getArma().getDano(), heroi.getDirecaoDoOlhar(), mundo.getMapa());
                mundo.criarEfeito(Efeito.PENAS, centroX, centroY, heroi.isOlhandoParaEsquerda());
                if (morreu) {
                    mundo.criarEfeito(Efeito.FUMACA, centroX, centroY + 4, false);
                    mundo.criarEfeito(Efeito.BRILHO, centroX, centroY - 10, false);
                    galinhaDerrotada();
                }
            }
        }
        return acertou;
    }

    private void galinhaDerrotada() {
        Partida partida = mundo.getPartida();
        partida.registrarGalinhaDerrotada();
        som.tocarEfeito("galinha");

        if (!partida.isRaideAtiva()) {
            controleRaide.iniciar(partida);
            partida.tremer(QUADROS_DE_TREMOR_DA_RAIDE);
            // Primeiro o galo canta; a música da luta entra quando ele termina.
            som.pararMusica();
            som.tocarEfeito("raide");
            quadrosAteAMusicaDaRaide = som.duracaoDoEfeitoEmMilissegundos("raide") / MILISSEGUNDOS_POR_QUADRO + 1;
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
        quadrosAteAMusicaDaRaide = 0;
        mundo.recolherTodasAsGalinhas();

        if (partida.atualizarRecorde()) {
            daoRecorde.salvar(partida.getRecorde());
        }

        mudarCena(Cena.FIM);
        quadrosNaTelaFinal = 0;
        if (fugiu) {
            som.tocarMusica("fuga");
        } else {
            som.tocarMusica("derrota");
        }
    }

    // ---------- Tela final ----------

    private void atualizarTelaFinal() {
        quadrosNaTelaFinal++;
        if (quadrosNaTelaFinal < QUADROS_ANTES_DE_SAIR_DO_FIM) {
            return;
        }
        boolean apertouConfirmar = entrada.consumir(Comando.CONFIRMAR);
        boolean apertouVoltar = entrada.consumir(Comando.VOLTAR);
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

        if (entrada.isSegurando(Comando.ESQUERDA)) {
            passoX = -1;
        } else if (entrada.isSegurando(Comando.DIREITA)) {
            passoX = 1;
        }
        if (entrada.isSegurando(Comando.CIMA)) {
            passoY = -1;
        } else if (entrada.isSegurando(Comando.BAIXO)) {
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
        heroi.mover(passoX * velocidade, passoY * velocidade, mundo.getMapa(), mundo.getObstaculos());
        heroi.setAndando(true);

        // Um pouco de poeira nos pés enquanto corre.
        if (heroi.getContadorPassos() % PASSOS_ENTRE_POEIRAS == 1) {
            float peY = heroi.getY() + heroi.getAltura() - 3;
            mundo.criarEfeito(Efeito.POEIRA, heroi.getCentroX(), peY, heroi.isOlhandoParaEsquerda());
        }
    }

    private boolean heroiEstaNaSaida() {
        Heroi heroi = mundo.getHeroi();
        return mundo.getMapa().estaNaSaida(heroi.getCentroX(), heroi.getCentroY());
    }
}
