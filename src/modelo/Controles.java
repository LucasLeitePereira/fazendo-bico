package modelo;

import java.awt.event.KeyEvent;

/**
 * Quais teclas e quais botões do controle fazem cada comando.
 * Cada comando tem até duas teclas e até dois botões. Uma tecla só pode estar em um comando
 * (ao ir para um comando novo, sai do antigo). Um botão pode valer em mais de um comando:
 * o A, por exemplo, ataca no jogo e confirma no menu.
 * Botões são os nomes que a JInput dá a eles ("0", "1"...). As direções do controle vêm
 * sempre do direcional e do analógico esquerdo, por isso não têm botões.
 */
public class Controles {
    public static final int TECLAS_POR_COMANDO = 2;
    public static final int BOTOES_POR_COMANDO = 2;
    public static final int SEM_TECLA = KeyEvent.VK_UNDEFINED;
    public static final String SEM_BOTAO = "";

    // Nomes dos botões no modo X-input (o padrão dos controles 8BitDo e do Xbox no Windows).
    private static final String[] NOMES_NO_MODO_XINPUT = { "A", "B", "X", "Y", "LB", "RB", "Select", "Start", "L3", "R3" };

    private int[][] teclas = new int[Comando.values().length][TECLAS_POR_COMANDO];
    private String[][] botoes = new String[Comando.values().length][BOTOES_POR_COMANDO];
    // Nome do controle conectado, ou vazio se não houver nenhum.
    private String nomeDoControle = "";

    public Controles() {
        restaurarPadrao();
    }

    /**
     * Teclas de sempre (setas ou WASD, Espaço ou Z, Enter, Esc). No controle: A ou X atacam e falam,
     * Start ou A confirmam, Select volta.
     */
    public void restaurarPadrao() {
        definirPadrao(Comando.CIMA, KeyEvent.VK_UP, KeyEvent.VK_W, SEM_BOTAO, SEM_BOTAO);
        definirPadrao(Comando.BAIXO, KeyEvent.VK_DOWN, KeyEvent.VK_S, SEM_BOTAO, SEM_BOTAO);
        definirPadrao(Comando.ESQUERDA, KeyEvent.VK_LEFT, KeyEvent.VK_A, SEM_BOTAO, SEM_BOTAO);
        definirPadrao(Comando.DIREITA, KeyEvent.VK_RIGHT, KeyEvent.VK_D, SEM_BOTAO, SEM_BOTAO);
        definirPadrao(Comando.ACAO, KeyEvent.VK_SPACE, KeyEvent.VK_Z, "0", "2");
        definirPadrao(Comando.CONFIRMAR, KeyEvent.VK_ENTER, SEM_TECLA, "7", "0");
        definirPadrao(Comando.VOLTAR, KeyEvent.VK_ESCAPE, SEM_TECLA, "6", SEM_BOTAO);
    }

    private void definirPadrao(Comando comando, int tecla1, int tecla2, String botao1, String botao2) {
        teclas[comando.ordinal()][0] = tecla1;
        teclas[comando.ordinal()][1] = tecla2;
        botoes[comando.ordinal()][0] = botao1;
        botoes[comando.ordinal()][1] = botao2;
    }

    // ---------- Teclas ----------

    /**
     * Devolve o comando ligado à tecla, ou null se a tecla não faz nada.
     */
    public Comando comandoDaTecla(int tecla) {
        if (tecla == SEM_TECLA) {
            return null;
        }
        Comando[] comandos = Comando.values();
        for (int i = 0; i < comandos.length; i++) {
            for (int posicao = 0; posicao < TECLAS_POR_COMANDO; posicao++) {
                if (teclas[i][posicao] == tecla) {
                    return comandos[i];
                }
            }
        }
        return null;
    }

    public int getTecla(Comando comando, int posicao) {
        return teclas[comando.ordinal()][posicao];
    }

    /**
     * A primeira tecla do comando (para as dicas na tela).
     */
    public int getTeclaPrincipal(Comando comando) {
        return teclas[comando.ordinal()][0];
    }

    public int contarTeclas(Comando comando) {
        int quantidade = 0;
        for (int posicao = 0; posicao < TECLAS_POR_COMANDO; posicao++) {
            if (teclas[comando.ordinal()][posicao] != SEM_TECLA) {
                quantidade++;
            }
        }
        return quantidade;
    }

    /**
     * A tecla nova vira a primeira do comando e a antiga primeira vira a segunda.
     * Se a tecla era de outro comando, sai de lá. Devolve false (e não muda nada) se ela
     * era a única tecla do outro comando, para nenhum comando ficar sem teclado.
     */
    public boolean definirTecla(Comando comando, int tecla) {
        Comando dono = comandoDaTecla(tecla);
        if (dono != null && dono != comando && contarTeclas(dono) == 1) {
            return false;
        }
        if (dono != null) {
            tirarTecla(dono, tecla);
        }
        int[] doComando = teclas[comando.ordinal()];
        doComando[1] = doComando[0];
        doComando[0] = tecla;
        return true;
    }

    public void apagarSegundaTecla(Comando comando) {
        teclas[comando.ordinal()][1] = SEM_TECLA;
    }

    private void tirarTecla(Comando comando, int tecla) {
        int[] doComando = teclas[comando.ordinal()];
        if (doComando[0] == tecla) {
            doComando[0] = doComando[1];
        }
        doComando[1] = SEM_TECLA;
    }

    // ---------- Botões ----------

    public String getBotao(Comando comando, int posicao) {
        return botoes[comando.ordinal()][posicao];
    }

    public boolean temBotao(Comando comando, String botao) {
        String[] doComando = botoes[comando.ordinal()];
        for (int posicao = 0; posicao < BOTOES_POR_COMANDO; posicao++) {
            if (!doComando[posicao].equals(SEM_BOTAO) && doComando[posicao].equals(botao)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mesma ideia das teclas: o botão novo vira o primeiro e o antigo primeiro vira o segundo.
     */
    public void definirBotao(Comando comando, String botao) {
        String[] doComando = botoes[comando.ordinal()];
        if (doComando[0].equals(botao)) {
            return;
        }
        doComando[1] = doComando[0];
        doComando[0] = botao;
    }

    public void apagarBotoes(Comando comando) {
        botoes[comando.ordinal()][0] = SEM_BOTAO;
        botoes[comando.ordinal()][1] = SEM_BOTAO;
    }

    // ---------- Carregar do arquivo ----------

    /**
     * Usado pelo DAO ao ler o arquivo: grava sem as regras de troca.
     */
    public void carregarTeclas(Comando comando, int tecla1, int tecla2) {
        teclas[comando.ordinal()][0] = tecla1;
        teclas[comando.ordinal()][1] = tecla2;
    }

    public void carregarBotoes(Comando comando, String botao1, String botao2) {
        botoes[comando.ordinal()][0] = botao1;
        botoes[comando.ordinal()][1] = botao2;
    }

    /**
     * Todo comando tem pelo menos uma tecla e nenhuma tecla se repete.
     */
    public boolean isValido() {
        Comando[] comandos = Comando.values();
        for (int i = 0; i < comandos.length; i++) {
            if (teclas[i][0] == SEM_TECLA) {
                return false;
            }
            for (int posicao = 0; posicao < TECLAS_POR_COMANDO; posicao++) {
                int tecla = teclas[i][posicao];
                if (tecla != SEM_TECLA && comandoDaTecla(tecla) != comandos[i]) {
                    return false;
                }
            }
            if (teclas[i][0] == teclas[i][1]) {
                return false;
            }
        }
        return true;
    }

    // ---------- Controle conectado ----------

    public String getNomeDoControle() {
        return nomeDoControle;
    }

    public void setNomeDoControle(String nomeDoControle) {
        this.nomeDoControle = nomeDoControle;
    }

    public boolean isControleConectado() {
        return nomeDoControle.length() > 0;
    }

    // ---------- Nomes para mostrar na tela ----------

    public static String nomeDaTecla(int tecla) {
        switch (tecla) {
            case SEM_TECLA:
                return "-";
            case KeyEvent.VK_UP:
                return "Seta cima";
            case KeyEvent.VK_DOWN:
                return "Seta baixo";
            case KeyEvent.VK_LEFT:
                return "Seta esq.";
            case KeyEvent.VK_RIGHT:
                return "Seta dir.";
            case KeyEvent.VK_SPACE:
                return "Espaço";
            case KeyEvent.VK_ENTER:
                return "Enter";
            case KeyEvent.VK_ESCAPE:
                return "Esc";
            case KeyEvent.VK_SHIFT:
                return "Shift";
            case KeyEvent.VK_CONTROL:
                return "Ctrl";
            case KeyEvent.VK_ALT:
                return "Alt";
            case KeyEvent.VK_TAB:
                return "Tab";
            case KeyEvent.VK_BACK_SPACE:
                return "Backspace";
            default:
                return KeyEvent.getKeyText(tecla);
        }
    }

    /**
     * No modo X-input (ou sem controle, já que o padrão pensa nele) os botões têm os nomes do Xbox.
     * Nos outros modos a numeração muda de controle para controle, então aparece o número.
     */
    public String nomeDoBotao(String botao) {
        if (botao.equals(SEM_BOTAO)) {
            return "-";
        }
        int numero = -1;
        try {
            numero = Integer.parseInt(botao);
        } catch (NumberFormatException erro) {
            return botao;
        }
        if (isModoXInput() && numero >= 0 && numero < NOMES_NO_MODO_XINPUT.length) {
            return NOMES_NO_MODO_XINPUT[numero];
        }
        return "Botão " + numero;
    }

    private boolean isModoXInput() {
        if (!isControleConectado()) {
            return true;
        }
        String nome = nomeDoControle.toLowerCase();
        return nome.indexOf("xbox") >= 0 || nome.indexOf("xinput") >= 0 || nome.indexOf("x-input") >= 0;
    }
}
