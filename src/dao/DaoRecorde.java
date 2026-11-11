package dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Guarda o recorde num arquivo de texto ao lado do jogo.
 */
public class DaoRecorde {
    private static final String ARQUIVO = "recorde.txt";

    public int ler() {
        BufferedReader leitor = null;
        try {
            leitor = new BufferedReader(new FileReader(ARQUIVO));
            String linha = leitor.readLine();
            if (linha == null) {
                return 0;
            }
            return Integer.parseInt(linha.trim());
        } catch (IOException erro) {
            return 0;
        } catch (NumberFormatException erro) {
            return 0;
        } finally {
            if (leitor != null) {
                try {
                    leitor.close();
                } catch (IOException erro) {
                    System.out.println("Não foi possível fechar " + ARQUIVO);
                }
            }
        }
    }

    public void salvar(int recorde) {
        FileWriter escritor = null;
        try {
            escritor = new FileWriter(ARQUIVO);
            escritor.write(String.valueOf(recorde));
        } catch (IOException erro) {
            System.out.println("Não foi possível salvar o recorde: " + erro.getMessage());
        } finally {
            if (escritor != null) {
                try {
                    escritor.close();
                } catch (IOException erro) {
                    System.out.println("Não foi possível fechar " + ARQUIVO);
                }
            }
        }
    }
}
