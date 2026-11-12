package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Os textos do jogo, lidos de recursos/falas.txt (CHAVE=texto).
 * Uma lista de balões usa chaves numeradas: ACORDAR_1, ACORDAR_2...
 */
public class Falas {
    private Map<String, String> textos = new HashMap<String, String>();

    public void guardar(String chave, String texto) {
        textos.put(chave, texto);
    }

    /**
     * Texto de uma chave. Se ela faltar no arquivo, aparece o nome da chave entre colchetes,
     * para ficar fácil de achar o que está faltando.
     */
    public String getTexto(String chave) {
        String texto = textos.get(chave);
        if (texto == null) {
            return "[" + chave + "]";
        }
        return texto;
    }

    /**
     * Balões em ordem: prefixo_1, prefixo_2... até o primeiro número que faltar.
     */
    public String[] getLista(String prefixo) {
        List<String> lista = new ArrayList<String>();
        int numero = 1;
        while (textos.containsKey(prefixo + "_" + numero)) {
            lista.add(textos.get(prefixo + "_" + numero));
            numero++;
        }
        if (lista.isEmpty()) {
            lista.add("[" + prefixo + "_1]");
        }
        return lista.toArray(new String[lista.size()]);
    }
}
