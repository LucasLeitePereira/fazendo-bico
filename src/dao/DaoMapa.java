package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import modelo.Mapa;

public class DaoMapa {

    /**
     * Lê um mapa da pasta recursos. Exemplo de caminho: "/mapas/vila.txt".
     */
    public Mapa carregar(String caminho) {
        InputStream entrada = DaoMapa.class.getResourceAsStream(caminho);
        if (entrada == null) {
            throw new IllegalStateException("Mapa não encontrado: " + caminho);
        }

        List<String> linhas = new ArrayList<String>();
        BufferedReader leitor = null;
        try {
            leitor = new BufferedReader(new InputStreamReader(entrada, "UTF-8"));
            String linha = leitor.readLine();
            while (linha != null) {
                if (linha.length() > 0) {
                    linhas.add(linha);
                }
                linha = leitor.readLine();
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Erro ao ler o mapa " + caminho, erro);
        } finally {
            fechar(leitor);
        }

        return new Mapa(linhas);
    }

    private void fechar(BufferedReader leitor) {
        if (leitor == null) {
            return;
        }
        try {
            leitor.close();
        } catch (IOException erro) {
            System.out.println("Não foi possível fechar o arquivo do mapa.");
        }
    }
}
