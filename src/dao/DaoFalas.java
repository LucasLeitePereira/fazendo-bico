package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import modelo.Falas;

/**
 * Lê o arquivo de falas, no estilo de um .env: uma linha por texto, CHAVE=texto.
 * Linhas vazias e linhas que começam com # são ignoradas. O arquivo é lido em UTF-8 (acentos).
 */
public class DaoFalas {

    /**
     * Lê um arquivo da pasta recursos. Exemplo de caminho: "/falas.txt".
     */
    public Falas carregar(String caminho) {
        InputStream entrada = DaoFalas.class.getResourceAsStream(caminho);
        if (entrada == null) {
            throw new IllegalStateException("Arquivo não encontrado: " + caminho);
        }

        Falas falas = new Falas();
        BufferedReader leitor = null;
        try {
            leitor = new BufferedReader(new InputStreamReader(entrada, "UTF-8"));
            String linha = leitor.readLine();
            while (linha != null) {
                lerLinha(linha, falas);
                linha = leitor.readLine();
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Erro ao ler " + caminho + ": " + erro.getMessage(), erro);
        } finally {
            fechar(entrada);
        }
        return falas;
    }

    private void lerLinha(String linha, Falas falas) {
        // O Bloco de Notas às vezes grava uma marca invisível (BOM) no começo do arquivo.
        if (linha.length() > 0 && linha.charAt(0) == '﻿') {
            linha = linha.substring(1);
        }
        linha = linha.trim();
        if (linha.length() == 0 || linha.startsWith("#")) {
            return;
        }
        // Só o primeiro "=" separa a chave do texto; o texto pode ter outros.
        int igual = linha.indexOf('=');
        if (igual <= 0) {
            System.out.println("Linha ignorada em falas.txt (falta o =): " + linha);
            return;
        }
        String chave = linha.substring(0, igual).trim();
        String texto = linha.substring(igual + 1).trim();
        falas.guardar(chave, texto);
    }

    private void fechar(InputStream entrada) {
        try {
            entrada.close();
        } catch (IOException erro) {
            System.out.println("Não foi possível fechar o arquivo de falas.");
        }
    }
}
