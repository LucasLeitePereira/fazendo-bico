package view;

import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import modelo.Mundo;

public class Janela extends JFrame {
    private static final long serialVersionUID = 1L;

    // Troque aqui quando a equipe decidir o nome do jogo.
    public static final String NOME_DO_JOGO = "Fazendo Bico";

    private TelaJogo tela;
    private boolean telaCheia = false;
    private Rectangle tamanhoEmJanela;
    private Cursor cursorInvisivel;

    public Janela(Mundo mundo, KeyListener teclado) {
        super(NOME_DO_JOGO);
        tela = new TelaJogo(mundo);
        getContentPane().add(tela);
        addKeyListener(teclado);
        // Sem isso, o F11 e o Tab podem ser engolidos pelo Swing antes de chegar ao teclado do jogo.
        setFocusTraversalKeysEnabled(false);

        BufferedImage vazia = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        cursorInvisivel = Toolkit.getDefaultToolkit().createCustomCursor(vazia, new Point(0, 0), "invisivel");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // A janela pode ser esticada ou maximizada: o jogo se ajusta ao tamanho.
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void desenhar() {
        tela.repaint();
    }

    /**
     * Tela cheia sem borda, do tamanho do monitor. Chamado de novo, volta para a janela.
     */
    public void alternarTelaCheia() {
        // O laço do jogo roda fora da thread do Swing; a troca é feita na vez dela.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                trocarModo();
            }
        });
    }

    private void trocarModo() {
        telaCheia = !telaCheia;
        // Para tirar ou pôr a borda, a janela precisa ser fechada e aberta de novo.
        dispose();
        if (telaCheia) {
            tamanhoEmJanela = getBounds();
            setUndecorated(true);
            Rectangle monitor = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            setBounds(monitor);
            setCursor(cursorInvisivel);
        } else {
            setUndecorated(false);
            setBounds(tamanhoEmJanela);
            setCursor(Cursor.getDefaultCursor());
        }
        setVisible(true);
        toFront();
        requestFocus();
    }

    public boolean isTelaCheia() {
        return telaCheia;
    }
}
