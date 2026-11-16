package controle;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import modelo.Comando;
import modelo.Controles;

/**
 * Traduz as teclas em comandos, de acordo com os Controles, e avisa a Entrada.
 * Fixas (não dá para trocar): F11 ou Alt+Enter para a tela cheia, - e + para o volume da música.
 */
public class Teclado implements KeyListener, FocusListener {
    private Entrada entrada;
    private Controles controles;
    // Teclas seguradas agora. Segurar uma tecla faz o Windows repetir o keyPressed; a repetição é ignorada.
    private Set<Integer> teclasSeguradas = new HashSet<Integer>();

    public Teclado(Entrada entrada, Controles controles) {
        this.entrada = entrada;
        this.controles = controles;
    }

    public static boolean isTeclaDoVolume(int tecla) {
        return tecla == KeyEvent.VK_MINUS || tecla == KeyEvent.VK_SUBTRACT || tecla == KeyEvent.VK_EQUALS
                || tecla == KeyEvent.VK_PLUS || tecla == KeyEvent.VK_ADD;
    }

    @Override
    public void keyPressed(KeyEvent evento) {
        int tecla = evento.getKeyCode();
        boolean repetida = !teclasSeguradas.add(Integer.valueOf(tecla));
        if (repetida) {
            return;
        }

        boolean telaCheia = tecla == KeyEvent.VK_F11 || (tecla == KeyEvent.VK_ENTER && evento.isAltDown());
        if (telaCheia) {
            entrada.apertarTelaCheia();
            return;
        }
        if (entrada.isCapturando()) {
            entrada.capturarTecla(tecla);
            return;
        }

        Comando comando = controles.comandoDaTecla(tecla);
        if (comando != null) {
            entrada.apertarPeloTeclado(comando);
        } else if (tecla == KeyEvent.VK_MINUS || tecla == KeyEvent.VK_SUBTRACT) {
            entrada.apertarMenos();
        } else if (isTeclaDoVolume(tecla)) {
            entrada.apertarMais();
        }
    }

    @Override
    public void keyReleased(KeyEvent evento) {
        int tecla = evento.getKeyCode();
        teclasSeguradas.remove(Integer.valueOf(tecla));
        Comando comando = controles.comandoDaTecla(tecla);
        if (comando != null) {
            entrada.soltarPeloTeclado(comando);
        }
    }

    @Override
    public void keyTyped(KeyEvent evento) {
    }

    @Override
    public void focusGained(FocusEvent evento) {
    }

    /**
     * Sem foco, o keyReleased não chega: solta tudo para o herói não sair andando sozinho.
     */
    @Override
    public void focusLost(FocusEvent evento) {
        teclasSeguradas.clear();
        Comando[] comandos = Comando.values();
        for (int i = 0; i < comandos.length; i++) {
            entrada.soltarPeloTeclado(comandos[i]);
        }
    }
}
