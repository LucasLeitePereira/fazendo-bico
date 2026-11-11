package controle;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Guarda o estado das teclas.
 * Setas: "segurando" (vale enquanto a tecla estiver apertada).
 * Enter, Espaço, Esc e as setas no menu: "apertou" (vale uma vez por toque e é consumido).
 */
public class Teclado implements KeyListener {
    private boolean cima;
    private boolean baixo;
    private boolean esquerda;
    private boolean direita;
    private boolean segurandoConfirmar;
    private boolean segurandoAcao;
    private boolean segurandoVoltar;

    private boolean apertouCima;
    private boolean apertouBaixo;
    private boolean apertouConfirmar;
    private boolean apertouAcao;
    private boolean apertouVoltar;

    @Override
    public void keyPressed(KeyEvent evento) {
        switch (evento.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (!cima) {
                    apertouCima = true;
                }
                cima = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (!baixo) {
                    apertouBaixo = true;
                }
                baixo = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                esquerda = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                direita = true;
                break;
            case KeyEvent.VK_ENTER:
                if (!segurandoConfirmar) {
                    apertouConfirmar = true;
                }
                segurandoConfirmar = true;
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_Z:
                if (!segurandoAcao) {
                    apertouAcao = true;
                }
                segurandoAcao = true;
                break;
            case KeyEvent.VK_ESCAPE:
                if (!segurandoVoltar) {
                    apertouVoltar = true;
                }
                segurandoVoltar = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent evento) {
        switch (evento.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                cima = false;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                baixo = false;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                esquerda = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                direita = false;
                break;
            case KeyEvent.VK_ENTER:
                segurandoConfirmar = false;
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_Z:
                segurandoAcao = false;
                break;
            case KeyEvent.VK_ESCAPE:
                segurandoVoltar = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent evento) {
    }

    /**
     * Esquece os toques pendentes, para um Enter de uma cena não valer na cena seguinte.
     */
    public void limparToques() {
        apertouCima = false;
        apertouBaixo = false;
        apertouConfirmar = false;
        apertouAcao = false;
        apertouVoltar = false;
    }

    public boolean consumirCima() {
        boolean apertou = apertouCima;
        apertouCima = false;
        return apertou;
    }

    public boolean consumirBaixo() {
        boolean apertou = apertouBaixo;
        apertouBaixo = false;
        return apertou;
    }

    public boolean consumirConfirmar() {
        boolean apertou = apertouConfirmar;
        apertouConfirmar = false;
        return apertou;
    }

    public boolean consumirAcao() {
        boolean apertou = apertouAcao;
        apertouAcao = false;
        return apertou;
    }

    public boolean consumirVoltar() {
        boolean apertou = apertouVoltar;
        apertouVoltar = false;
        return apertou;
    }

    public boolean isCima() {
        return cima;
    }

    public boolean isBaixo() {
        return baixo;
    }

    public boolean isEsquerda() {
        return esquerda;
    }

    public boolean isDireita() {
        return direita;
    }
}
