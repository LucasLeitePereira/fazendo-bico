# Raide das Galinhas

> Nome provisório. Para trocar, mude `NOME_DO_JOGO` em `src/view/Janela.java`.

Jogo 2D com visão de cima feito em Java Swing para o trabalho G2 de Programação Orientada a Objetos.

O herói acorda numa vila onde todo mundo odeia galinhas. O prefeito oferece um serviço muito bem pago: derrotar as galinhas do campo. Lá, ao derrotar a primeira, começa a **raide**: ondas cada vez maiores de galinhas furiosas voam contra o herói. Cada galinha derrotada vale pontos. A partida termina quando o herói **foge** (sai do campo pela estrada) ou é **derrotado** (perde todos os corações).

## Como rodar

Precisa de um JDK (Java 6 ou mais novo) instalado.

```
compilar.bat
executar.bat
```

O código usa só recursos do Java 6. Para conferir isso, aponte a variável `JDK_JAVA6` para um JDK 6, 7 ou 8 antes de compilar, e o `compilar.bat` passa a compilar no modo Java 6.

Em uma IDE (Eclipse, IntelliJ, NetBeans), marque `src` **e** `recursos` como pastas de código-fonte e rode a classe `controle.Jogo`.

## Controles

| Tecla | Ação |
|---|---|
| Setas ou WASD | Andar (e escolher no menu) |
| Espaço ou Z | Conversar (vila), atacar (campo), avançar o diálogo |
| Enter | Confirmar |
| Esc | Voltar à tela inicial / sair |

## Estrutura (MVC)

O projeto segue a mesma divisão de pacotes das aulas:

```
src/
  modelo/    os dados e as regras de cada coisa do jogo
  view/      tudo o que aparece na tela ou toca no alto-falante
  controle/  o laço do jogo: lê o teclado, atualiza o modelo e pede para a tela redesenhar
  dao/       leitura e gravação de arquivos (mapas e recorde)
recursos/    mapas, imagens, sons e músicas
```

| Classe | O que faz |
|---|---|
| `controle.Jogo` | `main`: cria o `ControleJogo` e inicia o laço |
| `controle.ControleJogo` | Laço principal (60 quadros por segundo), troca de cenas, conversas, ataques, dano, fim de partida |
| `controle.ControleRaide` | Lança as galinhas da raide em ondas, cada vez mais rápidas |
| `controle.Teclado` | `KeyListener` que guarda quais teclas estão apertadas |
| `modelo.Entidade` | Classe abstrata: posição, tamanho, direção, movimento com colisão |
| `modelo.Heroi`, `Galinha`, `Morador` | Herdam de `Entidade` e cada uma implementa `atualizar` do seu jeito |
| `modelo.Arma` | Dano, alcance e tempo de recarga do golpe |
| `modelo.Mapa` | Grade de tiles lida de um `.txt`, com os tiles sólidos |
| `modelo.Partida` | Cena atual, pontos, recorde, missão, raide e onda |
| `modelo.Dialogo` | Fala atual de uma conversa |
| `modelo.Mundo` | Reúne mapa, herói, moradores, galinhas, partida e diálogo |
| `view.Janela` | `JFrame` do jogo |
| `view.TelaJogo` | `JPanel` que desenha a cena atual (`paintComponent`) |
| `view.Animacao` | Recorta e desenha os quadros de uma folha de sprites |
| `view.Hud`, `CaixaDialogo`, `Texto` | Corações, pontos, onda e caixa de conversa |
| `view.Imagens`, `Som` | Carregam imagens (PNG) e tocam sons (WAV) |
| `dao.DaoMapa`, `DaoRecorde` | Leem os mapas e leem/gravam o recorde em `recorde.txt` |

### Como o jogo funciona
1. `ControleJogo.iniciar()` roda um laço que, a cada 1/60 de segundo, chama `atualizar()` e depois `janela.desenhar()`.
2. `atualizar()` olha a cena atual (`INICIAL`, `VILA`, `CAMPO` ou `FIM`) e aplica as regras dela.
3. `TelaJogo.paintComponent()` lê o `Mundo` e desenha a mesma cena. A câmera segue o herói.
4. Colisão: cada `Entidade` tem um retângulo (`getArea`). O movimento é desfeito se o retângulo encostar num tile sólido ou em outra entidade, e o golpe acerta as galinhas cujo retângulo cruza a área do golpe.
5. As galinhas ficam num vetor de tamanho fixo: as que saem de jogo ficam inativas e são reaproveitadas na raide.

## Mapas

Os mapas ficam em `recursos/mapas/` e cada caractere é um tile de 16×16 pixels. Todas as linhas precisam ter o mesmo tamanho.

| Caractere | Tile | Sólido? |
|---|---|---|
| `.` | grama | não |
| `,` | flor | não |
| `=` | caminho | não |
| `S` | saída | não |
| `P` | porta | não |
| `#` | árvore | sim |
| `~` | água | sim |
| `C` | parede de casa | sim |
| `T` | telhado | sim |
| `F` | cerca | sim |
| `H` | onde o herói começa | — |
| `1` a `4` | onde cada morador fica (só na vila) | — |
| `G` | galinha solta (só no campo) | — |

## Arte e som

Todos os arquivos são opcionais. Sem uma imagem, o jogo desenha um retângulo colorido no lugar; sem um som, segue em silêncio. Basta colocar o arquivo com o nome certo na pasta certa.

**Imagens (PNG com fundo transparente)**

| Arquivo | O que é |
|---|---|
| `recursos/telas/inicial.png` | Fundo da tela inicial (320×240 ou 640×480). Se tiver o nome do jogo desenhado, ótimo: o texto não é escrito por cima |
| `recursos/sprites/heroi.png` | Herói andando |
| `recursos/sprites/heroi_ataque.png` | Herói atacando (opcional; sem ele usa o sprite de andar) |
| `recursos/sprites/galinha.png` | Galinha |
| `recursos/sprites/morador1.png` a `morador4.png` | Prefeito, Dona Cida, Seu Juca e Tico |
| `recursos/sprites/golpe.png` | Efeito do golpe (esticado na área do golpe) |
| `recursos/sprites/coracao.png`, `coracao_vazio.png` | Vida no HUD |
| `recursos/tiles/grama.png`, `flor.png`, `caminho.png`, `saida.png`, `porta.png`, `arvore.png`, `agua.png`, `parede.png`, `telhado.png`, `cerca.png` | Tiles de 16×16 |

**Folhas de sprites:** cada quadro tem 16×16 pixels; cada linha é uma direção, nesta ordem: baixo, esquerda, direita, cima; cada coluna é um quadro da animação. Uma folha com uma linha só vale para todas as direções. Para sprites de outro tamanho, troque os números `16, 16` nas linhas de `new Animacao(...)` em `src/view/TelaJogo.java`.

**Sons (WAV PCM de 16 bits; MP3 e OGG não funcionam)**

| Arquivo | Quando toca |
|---|---|
| `recursos/musicas/inicial.wav` | Tela inicial |
| `recursos/musicas/vila.wav` | Vila |
| `recursos/musicas/campo.wav` | Campo, antes da raide |
| `recursos/musicas/raide.wav` | Durante a raide |
| `recursos/musicas/fuga.wav`, `derrota.wav` | Tela final |
| `recursos/sons/menu.wav` | Mudar de opção no menu |
| `recursos/sons/dialogo.wav` | Abrir ou avançar uma conversa |
| `recursos/sons/missao.wav` | Aceitar a missão |
| `recursos/sons/golpe.wav` | Herói ataca |
| `recursos/sons/acerto.wav` | Golpe acerta sem matar |
| `recursos/sons/galinha.wav` | Galinha derrotada |
| `recursos/sons/raide.wav` | Começo da raide |
| `recursos/sons/dano.wav` | Herói leva dano |

As músicas ficam inteiras na memória: prefira 22 kHz ou 44 kHz e alguns minutos no máximo.

## Créditos

Liste aqui a origem e a licença de cada sprite, som e música usados.
