package controle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import modelo.Comando;
import modelo.Controles;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.DirectInputEnvironmentPlugin;

/**
 * Lê um controle (8BitDo, Xbox, genérico) pela biblioteca JInput, via DirectInput do Windows.
 * Direções: direcional e analógico esquerdo. Botões: os que estiverem nos Controles.
 * Sem controle, ou fora do Windows, o jogo segue só no teclado.
 */
public class Joystick {
    private static final float ZONA_MORTA = 0.5f;
    private static final String PASTA_DAS_BIBLIOTECAS = "fazendo-bico-jinput-2.0.7";
    private static final String[] BIBLIOTECAS = { "jinput-dx8.dll", "jinput-dx8_64.dll" };
    // A JInput avisa no console de cada aparelho que não conseguiu abrir (touchpad etc.). Guardado aqui
    // para o Java não descartar o ajuste do nível.
    private static final Logger REGISTRO_DA_JINPUT = Logger.getLogger("net.java.games.input");

    private Controller controle;
    private Component[] botoes = new Component[0];
    private boolean[] botaoApertado = new boolean[0];
    private Component direcional;
    private Component eixoX;
    private Component eixoY;
    private boolean[] comandosApertados = new boolean[Comando.values().length];
    private boolean bibliotecaPronta;

    /**
     * Procura um controle conectado. Pode demorar um pouco: chamar só ao abrir o jogo
     * e ao entrar na tela de controles.
     */
    public void procurar(Controles controles) {
        esquecerControle(controles);
        if (!prepararBiblioteca()) {
            return;
        }
        REGISTRO_DA_JINPUT.setLevel(Level.WARNING);
        try {
            Controller[] encontrados = new DirectInputEnvironmentPlugin().getControllers();
            for (int i = 0; i < encontrados.length; i++) {
                Controller.Type tipo = encontrados[i].getType();
                if (tipo == Controller.Type.GAMEPAD || tipo == Controller.Type.STICK) {
                    usarControle(encontrados[i], controles);
                    return;
                }
            }
        } catch (Throwable erro) {
            System.out.println("Não foi possível procurar controles: " + erro);
        }
    }

    private void usarControle(Controller encontrado, Controles controles) {
        Component[] componentes = encontrado.getComponents();
        int quantidadeDeBotoes = 0;
        for (int i = 0; i < componentes.length; i++) {
            if (componentes[i].getIdentifier() instanceof Component.Identifier.Button) {
                quantidadeDeBotoes++;
            }
        }
        botoes = new Component[quantidadeDeBotoes];
        botaoApertado = new boolean[quantidadeDeBotoes];
        int proximo = 0;
        for (int i = 0; i < componentes.length; i++) {
            Component.Identifier identificador = componentes[i].getIdentifier();
            if (identificador instanceof Component.Identifier.Button) {
                botoes[proximo] = componentes[i];
                proximo++;
            } else if (identificador == Component.Identifier.Axis.POV) {
                direcional = componentes[i];
            } else if (identificador == Component.Identifier.Axis.X) {
                eixoX = componentes[i];
            } else if (identificador == Component.Identifier.Axis.Y) {
                eixoY = componentes[i];
            }
        }
        controle = encontrado;
        controles.setNomeDoControle(encontrado.getName().trim());
        System.out.println("Controle conectado: " + encontrado.getName() + " (" + quantidadeDeBotoes + " botões)");
    }

    private void esquecerControle(Controles controles) {
        controle = null;
        botoes = new Component[0];
        botaoApertado = new boolean[0];
        direcional = null;
        eixoX = null;
        eixoY = null;
        controles.setNomeDoControle("");
    }

    /**
     * Lê o controle e passa para a Entrada o que está apertado. Chamado uma vez por quadro.
     */
    public void ler(Entrada entrada, Controles controles) {
        if (controle == null) {
            return;
        }
        if (!controle.poll()) {
            System.out.println("Controle desconectado.");
            esquecerControle(controles);
            soltarTudo(entrada);
            return;
        }

        boolean capturando = entrada.isCapturando();
        Comando[] comandos = Comando.values();
        for (int i = 0; i < comandosApertados.length; i++) {
            comandosApertados[i] = false;
        }

        for (int i = 0; i < botoes.length; i++) {
            boolean apertado = botoes[i].getPollData() > ZONA_MORTA;
            String nome = botoes[i].getIdentifier().getName();
            if (apertado && !botaoApertado[i] && capturando) {
                entrada.capturarBotao(nome);
            }
            botaoApertado[i] = apertado;
            if (apertado) {
                for (int c = 0; c < comandos.length; c++) {
                    if (controles.temBotao(comandos[c], nome)) {
                        comandosApertados[c] = true;
                    }
                }
            }
        }
        lerDirecoes();

        for (int c = 0; c < comandos.length; c++) {
            entrada.definirPeloControle(comandos[c], comandosApertados[c] && !capturando);
        }
    }

    private void lerDirecoes() {
        float pov = Component.POV.OFF;
        if (direcional != null) {
            pov = direcional.getPollData();
        }
        float x = 0;
        float y = 0;
        if (eixoX != null) {
            x = eixoX.getPollData();
        }
        if (eixoY != null) {
            y = eixoY.getPollData();
        }
        boolean cima = pov == Component.POV.UP || pov == Component.POV.UP_LEFT || pov == Component.POV.UP_RIGHT;
        boolean baixo = pov == Component.POV.DOWN || pov == Component.POV.DOWN_LEFT || pov == Component.POV.DOWN_RIGHT;
        boolean esquerda = pov == Component.POV.LEFT || pov == Component.POV.UP_LEFT || pov == Component.POV.DOWN_LEFT;
        boolean direita = pov == Component.POV.RIGHT || pov == Component.POV.UP_RIGHT || pov == Component.POV.DOWN_RIGHT;
        comandosApertados[Comando.CIMA.ordinal()] = cima || y < -ZONA_MORTA;
        comandosApertados[Comando.BAIXO.ordinal()] = baixo || y > ZONA_MORTA;
        comandosApertados[Comando.ESQUERDA.ordinal()] = esquerda || x < -ZONA_MORTA;
        comandosApertados[Comando.DIREITA.ordinal()] = direita || x > ZONA_MORTA;
    }

    private void soltarTudo(Entrada entrada) {
        Comando[] comandos = Comando.values();
        for (int c = 0; c < comandos.length; c++) {
            entrada.definirPeloControle(comandos[c], false);
        }
    }

    /**
     * As DLLs da JInput vão dentro do jogo (recursos/nativos). O Windows só carrega DLL de arquivo,
     * então elas são copiadas para a pasta temporária e a JInput é avisada de onde estão.
     */
    private boolean prepararBiblioteca() {
        if (bibliotecaPronta) {
            return true;
        }
        if (System.getProperty("os.name", "").indexOf("Windows") < 0) {
            return false;
        }
        File pasta = new File(System.getProperty("java.io.tmpdir"), PASTA_DAS_BIBLIOTECAS);
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            System.out.println("Não foi possível criar " + pasta);
            return false;
        }
        for (int i = 0; i < BIBLIOTECAS.length; i++) {
            if (!copiarBiblioteca(BIBLIOTECAS[i], new File(pasta, BIBLIOTECAS[i]))) {
                return false;
            }
        }
        System.setProperty("net.java.games.input.librarypath", pasta.getAbsolutePath());
        bibliotecaPronta = true;
        return true;
    }

    private boolean copiarBiblioteca(String nome, File destino) {
        InputStream origem = Joystick.class.getResourceAsStream("/nativos/" + nome);
        if (origem == null) {
            System.out.println("Biblioteca do controle não encontrada: " + nome);
            return false;
        }
        OutputStream saida = null;
        try {
            // Se outra cópia do jogo estiver aberta, a DLL está em uso; a que já está lá serve.
            if (destino.isFile() && destino.length() > 0) {
                return true;
            }
            saida = new FileOutputStream(destino);
            byte[] pedaco = new byte[8192];
            int lidos = origem.read(pedaco);
            while (lidos > 0) {
                saida.write(pedaco, 0, lidos);
                lidos = origem.read(pedaco);
            }
            return true;
        } catch (IOException erro) {
            System.out.println("Não foi possível copiar " + nome + ": " + erro.getMessage());
            return false;
        } finally {
            fechar(origem);
            if (saida != null) {
                try {
                    saida.close();
                } catch (IOException erro) {
                    System.out.println("Não foi possível fechar " + destino);
                }
            }
        }
    }

    private void fechar(InputStream origem) {
        try {
            origem.close();
        } catch (IOException erro) {
            System.out.println("Não foi possível fechar a biblioteca do controle.");
        }
    }
}
