package controle;

import modelo.Comando;
import modelo.Controles;

/**
 * Junta o teclado e o controle num estado só, por comando.
 * "Segurando" vale enquanto a tecla ou o botão estiver apertado (andar).
 * "Apertou" vale uma vez por toque: o toque chega a qualquer momento, entra no quadro seguinte
 * (novoQuadro) e, se ninguém consumir nesse quadro, é esquecido.
 * Na tela de controles, a entrada pode "capturar" a próxima tecla ou o próximo botão.
 * O teclado escreve aqui pela thread do Swing; o laço do jogo lê pela dele.
 */
public class Entrada {
    private static final int QUANTIDADE_DE_COMANDOS = Comando.values().length;

    private boolean[] segurandoPeloTeclado = new boolean[QUANTIDADE_DE_COMANDOS];
    private boolean[] segurandoPeloControle = new boolean[QUANTIDADE_DE_COMANDOS];
    private boolean[] toquesPendentes = new boolean[QUANTIDADE_DE_COMANDOS];
    private boolean[] toquesDoQuadro = new boolean[QUANTIDADE_DE_COMANDOS];

    // Teclas fixas, fora dos comandos: F11 ou Alt+Enter, - e +.
    private boolean telaCheiaPendente;
    private boolean maisPendente;
    private boolean menosPendente;
    private boolean telaCheiaDoQuadro;
    private boolean maisDoQuadro;
    private boolean menosDoQuadro;

    private boolean capturando;
    private int teclaCapturada = Controles.SEM_TECLA;
    private String botaoCapturado;

    public synchronized void apertarPeloTeclado(Comando comando) {
        int indice = comando.ordinal();
        if (!segurandoPeloTeclado[indice] && !segurandoPeloControle[indice]) {
            toquesPendentes[indice] = true;
        }
        segurandoPeloTeclado[indice] = true;
    }

    public synchronized void soltarPeloTeclado(Comando comando) {
        segurandoPeloTeclado[comando.ordinal()] = false;
    }

    /**
     * O controle é lido a cada quadro: diz se o comando está apertado agora.
     */
    public synchronized void definirPeloControle(Comando comando, boolean apertado) {
        int indice = comando.ordinal();
        if (apertado && !segurandoPeloControle[indice] && !segurandoPeloTeclado[indice]) {
            toquesPendentes[indice] = true;
        }
        segurandoPeloControle[indice] = apertado;
    }

    public synchronized void apertarTelaCheia() {
        telaCheiaPendente = true;
    }

    public synchronized void apertarMais() {
        maisPendente = true;
    }

    public synchronized void apertarMenos() {
        menosPendente = true;
    }

    /**
     * Chamado no começo de cada quadro: os toques que chegaram passam a valer neste quadro.
     */
    public synchronized void novoQuadro() {
        for (int i = 0; i < QUANTIDADE_DE_COMANDOS; i++) {
            toquesDoQuadro[i] = toquesPendentes[i];
            toquesPendentes[i] = false;
        }
        telaCheiaDoQuadro = telaCheiaPendente;
        maisDoQuadro = maisPendente;
        menosDoQuadro = menosPendente;
        telaCheiaPendente = false;
        maisPendente = false;
        menosPendente = false;
    }

    public synchronized boolean consumir(Comando comando) {
        boolean apertou = toquesDoQuadro[comando.ordinal()];
        toquesDoQuadro[comando.ordinal()] = false;
        return apertou;
    }

    public synchronized boolean isSegurando(Comando comando) {
        return segurandoPeloTeclado[comando.ordinal()] || segurandoPeloControle[comando.ordinal()];
    }

    /**
     * Esquece os toques, para um Enter de uma cena não valer na cena seguinte.
     */
    public synchronized void limparToques() {
        for (int i = 0; i < QUANTIDADE_DE_COMANDOS; i++) {
            toquesPendentes[i] = false;
            toquesDoQuadro[i] = false;
        }
    }

    /**
     * Depois de trocar uma tecla, o que estava apertado pode ter mudado de comando.
     */
    public synchronized void soltarTudo() {
        for (int i = 0; i < QUANTIDADE_DE_COMANDOS; i++) {
            segurandoPeloTeclado[i] = false;
            segurandoPeloControle[i] = false;
        }
        limparToques();
    }

    public synchronized boolean consumirTelaCheia() {
        boolean apertou = telaCheiaDoQuadro;
        telaCheiaDoQuadro = false;
        return apertou;
    }

    public synchronized boolean consumirMais() {
        boolean apertou = maisDoQuadro;
        maisDoQuadro = false;
        return apertou;
    }

    public synchronized boolean consumirMenos() {
        boolean apertou = menosDoQuadro;
        menosDoQuadro = false;
        return apertou;
    }

    // ---------- Captura (tela de controles) ----------

    public synchronized void comecarCaptura() {
        capturando = true;
        teclaCapturada = Controles.SEM_TECLA;
        botaoCapturado = null;
    }

    public synchronized void terminarCaptura() {
        capturando = false;
        teclaCapturada = Controles.SEM_TECLA;
        botaoCapturado = null;
    }

    public synchronized boolean isCapturando() {
        return capturando;
    }

    public synchronized void capturarTecla(int tecla) {
        teclaCapturada = tecla;
    }

    public synchronized void capturarBotao(String botao) {
        botaoCapturado = botao;
    }

    /**
     * Devolve a tecla apertada desde a última pergunta (ou SEM_TECLA) e a esquece.
     */
    public synchronized int pegarTeclaCapturada() {
        int tecla = teclaCapturada;
        teclaCapturada = Controles.SEM_TECLA;
        return tecla;
    }

    /**
     * Devolve o botão apertado desde a última pergunta (ou null) e o esquece.
     */
    public synchronized String pegarBotaoCapturado() {
        String botao = botaoCapturado;
        botaoCapturado = null;
        return botao;
    }
}
