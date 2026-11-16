package dao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import modelo.Comando;
import modelo.Controles;

/**
 * Guarda as teclas e os botões de cada comando em controles.txt, ao lado do jogo.
 * Formato: CIMA_TECLAS=38,87 (códigos do Java) e ACAO_BOTOES=0,2 (botões da JInput).
 * Se o arquivo não existir ou estiver estragado, ficam os controles padrão.
 */
public class DaoControles {
    private static final String ARQUIVO = "controles.txt";
    private static final String CODIFICACAO = "UTF-8";
    private static final String SUFIXO_DAS_TECLAS = "_TECLAS";
    private static final String SUFIXO_DOS_BOTOES = "_BOTOES";

    public void ler(Controles controles) {
        BufferedReader leitor = null;
        try {
            leitor = new BufferedReader(new InputStreamReader(new FileInputStream(ARQUIVO), CODIFICACAO));
            String linha = leitor.readLine();
            while (linha != null) {
                lerLinha(controles, linha.trim());
                linha = leitor.readLine();
            }
            if (!controles.isValido()) {
                System.out.println(ARQUIVO + " tem teclas repetidas ou faltando; usando os controles padrão.");
                controles.restaurarPadrao();
            }
        } catch (IOException erro) {
            controles.restaurarPadrao();
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

    private void lerLinha(Controles controles, String linha) {
        int igual = linha.indexOf('=');
        if (linha.length() == 0 || linha.startsWith("#") || igual < 0) {
            return;
        }
        String chave = linha.substring(0, igual).trim();
        String[] valores = separarValores(linha.substring(igual + 1));
        try {
            if (chave.endsWith(SUFIXO_DAS_TECLAS)) {
                Comando comando = Comando.valueOf(chave.substring(0, chave.length() - SUFIXO_DAS_TECLAS.length()));
                controles.carregarTeclas(comando, lerTecla(valores[0]), lerTecla(valores[1]));
            } else if (chave.endsWith(SUFIXO_DOS_BOTOES)) {
                Comando comando = Comando.valueOf(chave.substring(0, chave.length() - SUFIXO_DOS_BOTOES.length()));
                controles.carregarBotoes(comando, valores[0], valores[1]);
            }
        } catch (IllegalArgumentException erro) {
            System.out.println("Linha ignorada em " + ARQUIVO + ": " + linha);
        }
    }

    /**
     * Sempre devolve dois valores; o que faltar vem vazio.
     */
    private String[] separarValores(String texto) {
        String[] valores = { "", "" };
        String[] partes = texto.split(",");
        for (int i = 0; i < partes.length && i < valores.length; i++) {
            valores[i] = partes[i].trim();
        }
        return valores;
    }

    private int lerTecla(String texto) {
        if (texto.length() == 0) {
            return Controles.SEM_TECLA;
        }
        return Integer.parseInt(texto);
    }

    public void salvar(Controles controles) {
        Writer escritor = null;
        try {
            escritor = new OutputStreamWriter(new FileOutputStream(ARQUIVO), CODIFICACAO);
            escritor.write("# Controles do Fazendo Bico. Apague este arquivo para voltar ao padrão.\n");
            Comando[] comandos = Comando.values();
            for (int i = 0; i < comandos.length; i++) {
                Comando comando = comandos[i];
                escritor.write(comando.name() + SUFIXO_DAS_TECLAS + "=" + escreverTecla(controles.getTecla(comando, 0))
                        + "," + escreverTecla(controles.getTecla(comando, 1)) + "\n");
                escritor.write(comando.name() + SUFIXO_DOS_BOTOES + "=" + controles.getBotao(comando, 0)
                        + "," + controles.getBotao(comando, 1) + "\n");
            }
        } catch (IOException erro) {
            System.out.println("Não foi possível salvar os controles: " + erro.getMessage());
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

    private String escreverTecla(int tecla) {
        if (tecla == Controles.SEM_TECLA) {
            return "";
        }
        return String.valueOf(tecla);
    }
}
