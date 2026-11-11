package view;

import java.awt.event.KeyListener;

import javax.swing.JFrame;

import modelo.Mundo;

public class Janela extends JFrame {
    private static final long serialVersionUID = 1L;

    // Troque aqui quando a equipe decidir o nome do jogo.
    public static final String NOME_DO_JOGO = "Raide das Galinhas";

    private TelaJogo tela;

    public Janela(Mundo mundo, KeyListener teclado) {
        super(NOME_DO_JOGO);
        tela = new TelaJogo(mundo);
        getContentPane().add(tela);
        addKeyListener(teclado);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void desenhar() {
        tela.repaint();
    }
}
