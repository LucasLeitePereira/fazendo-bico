package modelo;

import java.util.Random;

/**
 * Vaca, porco, ovelha ou pato. Passeia devagar pelo mapa, para bastante para comer
 * e não pode ser atacado. O herói não atravessa os animais.
 */
public class Animal extends Entidade {
    private static final Random sorteio = new Random();
    private static final Entidade[] SEM_OBSTACULOS = new Entidade[0];

    private String tipo;
    private boolean parado = true;
    private int quadrosAteMudarDePasseio;
    private float xAntesDoPasso;
    private float yAntesDoPasso;

    /**
     * O ponto (peX, peY) é onde ficam os pés do animal no mapa.
     */
    public Animal(String tipo, float peX, float peY) {
        super(0, 0, larguraDo(tipo), alturaDo(tipo));
        this.tipo = tipo;
        setPosicao(peX - largura / 2.0f, peY - altura);
        xAntesDoPasso = x;
        yAntesDoPasso = y;
        velocidade = 0.25f;
        quadrosAteMudarDePasseio = 60 + sorteio.nextInt(240);
        if (sorteio.nextBoolean()) {
            setDirecao(Direcao.ESQUERDA);
        }
    }

    /**
     * Quantos pixels vazios a imagem do animal tem embaixo dos pés.
     * O pé do animal fica nessa distância acima da borda de baixo da imagem.
     */
    public static int margemDaImagem(String tipo) {
        if (tipo.equals("vaca")) {
            return 4;
        }
        if (tipo.equals("porco") || tipo.equals("ovelha")) {
            return 6;
        }
        return 1;
    }

    private static int larguraDo(String tipo) {
        if (tipo.equals("vaca")) {
            return 18;
        }
        if (tipo.equals("pato")) {
            return 8;
        }
        return 14;
    }

    private static int alturaDo(String tipo) {
        if (tipo.equals("pato")) {
            return 5;
        }
        return 7;
    }

    @Override
    public void atualizar(Mapa mapa) {
        atualizar(mapa, SEM_OBSTACULOS);
    }

    public void atualizar(Mapa mapa, Entidade[] obstaculos) {
        quadrosAteMudarDePasseio--;
        if (quadrosAteMudarDePasseio <= 0) {
            escolherNovoPasseio();
        }
        if (parado) {
            return;
        }
        xAntesDoPasso = x;
        yAntesDoPasso = y;
        float passoX = direcao.getPassoX() * velocidade;
        float passoY = direcao.getPassoY() * velocidade;
        boolean conseguiuAndar = mover(passoX, passoY, mapa, obstaculos);
        if (conseguiuAndar) {
            contadorPassos++;
        } else {
            parado = true;
        }
    }

    /**
     * Na maior parte do tempo o animal fica parado, comendo.
     */
    private void escolherNovoPasseio() {
        int escolha = sorteio.nextInt(8);
        if (escolha >= 4) {
            parado = true;
            quadrosAteMudarDePasseio = 120 + sorteio.nextInt(240);
        } else {
            parado = false;
            setDirecao(Direcao.values()[escolha]);
            quadrosAteMudarDePasseio = 40 + sorteio.nextInt(80);
        }
    }

    /**
     * Volta para onde estava antes do último passo e para de andar.
     */
    public void desfazerUltimoPasso() {
        setPosicao(xAntesDoPasso, yAntesDoPasso);
        parado = true;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isParado() {
        return parado;
    }
}
