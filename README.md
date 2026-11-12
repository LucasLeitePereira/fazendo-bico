# Raide das Galinhas

> Nome provisório. Para trocar, mude `NOME_DO_JOGO` em `src/view/Janela.java`.

Jogo 2D com visão de cima feito em Java Swing para o trabalho G2 de Programação Orientada a Objetos.

O herói acorda numa vila onde todo mundo odeia galinhas. O prefeito oferece um serviço muito bem pago: derrotar as galinhas do campo. Lá só há uma galinha, ciscando sozinha. Ao derrotá-la, começa a **raide**: ondas cada vez maiores de galinhas furiosas voam contra o herói. Cada galinha derrotada vale pontos. A partida termina quando o herói **foge** (volta pela estrada a oeste do campo, por onde chegou) ou é **derrotado** (perde todos os corações).

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
| F11 ou Alt+Enter | Liga e desliga a tela cheia |
| - e + | Volume da música, em qualquer tela (também no menu inicial, na opção "Música", com esquerda/direita). Começa em 50% |

A janela também pode ser esticada ou maximizada. A arte é desenhada em 320×240 e ampliada para caber na tela, com faixas pretas nas sobras. Para não pesar, a cena é montada numa escala inteira de até 3× e depois esticada. Se o PC estiver lento, essa escala cai sozinha.

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
| `modelo.Heroi`, `Galinha`, `Morador`, `Animal` | Herdam de `Entidade` e cada uma implementa `atualizar` do seu jeito |
| `modelo.Arma` | Dano, alcance e tempo de recarga do golpe |
| `modelo.Mapa` | Mapa do Tiled: camadas de tiles, o que é sólido, decorações, pontos marcados e a saída |
| `modelo.Decoracao` | Objeto parado do cenário (árvore, barril, moinho...), com colisores opcionais |
| `modelo.Efeito` | Efeito curto: poeira, penas, brilho, fumaça |
| `modelo.Partida` | Cena atual, pontos, recorde, missão, raide e onda |
| `modelo.Dialogo` | Fala atual de uma conversa |
| `modelo.Mundo` | Reúne mapa, herói, moradores, animais, galinhas, efeitos, partida e diálogo |
| `view.Janela` | `JFrame` do jogo |
| `view.TelaJogo` | `JPanel` que desenha a cena atual (`paintComponent`), na ordem da profundidade |
| `view.Cenario` | Desenha as camadas de tiles, as decorações (animadas ou não), os animais e os efeitos |
| `view.CaixaNoveFatias` | Desenha caixas de qualquer tamanho a partir de uma imagem 3×3 (9-slice) |
| `view.Animacao` | Recorta e desenha os quadros de uma folha de sprites |
| `view.Hud`, `CaixaDialogo`, `Texto` | Corações, moedas, onda, faixa da raide e caixa de conversa com retrato |
| `view.Imagens`, `Som` | Carregam imagens (PNG) e tocam sons (WAV) |
| `dao.DaoMapa`, `DaoRecorde` | Lê os mapas do Tiled (`.tmx`); lê e grava o recorde em `recorde.txt` |

### Como o jogo funciona
1. `ControleJogo.iniciar()` roda um laço que, a cada 1/60 de segundo, chama `atualizar()` e depois `janela.desenhar()`.
2. `atualizar()` olha a cena atual (`INICIAL`, `VILA`, `CAMPO` ou `FIM`) e aplica as regras dela.
3. `TelaJogo.paintComponent()` lê o `Mundo` e desenha a mesma cena. A câmera segue o herói.
4. Colisão: cada `Entidade` tem um retângulo (`getArea`). O movimento é desfeito se o retângulo encostar num tile sólido, num colisor (do mapa ou de uma decoração) ou em outra entidade, e o golpe acerta as galinhas cujo retângulo cruza a área do golpe.
5. Profundidade: decorações, animais, moradores, galinhas e herói entram numa fila ordenada pela altura do pé. Quem está mais acima é desenhado primeiro, então o herói passa atrás de uma árvore e na frente de outra.
6. Galinhas e efeitos ficam em vetores de tamanho fixo: os que saem de jogo ficam inativos e são reaproveitados.

## Mapas (editar no Tiled)

Os mapas são feitos no **Tiled**, um editor de mapas gratuito: <https://www.mapeditor.org>. Para mudar um mapa, instale o Tiled, abra `recursos/mapas/vila.tmx` ou `campo.tmx`, edite e salve. O jogo lê o arquivo direto da pasta `recursos` na próxima vez que rodar.

Arquivos:
- `vila.tmx`, `campo.tmx`: os mapas.
- `tileset.tsx`: o tileset do chão, `recursos/tiles/tileset.png` cortado em tiles de 16×16.
- `objetos.tsx`: coleção de imagens de decoração, uma por arquivo em `recursos/objetos/`.

**Camadas de tiles** (salve em CSV, que é o padrão do Tiled):

| Camada | O que faz |
|---|---|
| qualquer nome (`mar`, `terra`, `caminhos`, `construcao`...) | Desenhada embaixo dos personagens, na ordem do arquivo |
| nome começando com `cima` | Desenhada por cima dos personagens (copas, telhados) |
| `colisao` | Qualquer tile pintado aqui vira parede. Não é desenhada no jogo; no Tiled ela aparece meio transparente |

**Camada de objetos `objetos`:**

| Objeto | O que faz |
|---|---|
| Objeto de imagem do `objetos.tsx` (ferramenta "Inserir tile") | Decoração. O nome do arquivo diz o tipo: `arvore1`, `poco`, `barril`... Pode ser espelhada na horizontal |
| `vaca`, `porco`, `ovelha`, `pato` | Viram animais vivos que passeiam pelo mapa |
| Ponto chamado `heroi` | Onde o herói começa |
| Pontos `morador1` a `morador4` | Onde ficam os NPCs 1 a 4 do `falas.txt` (só na vila). O NPC1 é o prefeito, que dá a missão |
| Ponto(s) `galinha` | Galinhas no chão ao entrar no campo. O mapa padrão tem uma só: as outras vêm com a raide |
| Retângulo chamado `saida` | Na vila, leva ao campo (depois da missão); no campo, é a fuga |

`sombra` fica sempre no chão; `moinho` e `fumaca1` a `fumaca5` ficam por cima de tudo.

### Colisores

Há três jeitos de criar parede, e todos são editados no Tiled:

| Onde | Tamanho | Bom para |
|---|---|---|
| Camada de tiles `colisao` | Tile inteiro (16×16) | Água, cercas, paredes de casas |
| Camada de objetos `colisores` | Retângulo livre, de qualquer tamanho | Cantos, meio-tile, fechar uma passagem |
| Colisão de cada imagem no `objetos.tsx` | Desenhada uma vez por imagem | Tronco das árvores, base de barris, poços... |

**Tile inteiro:** selecione a camada `colisao`. Pinte com o Carimbo (B) para criar parede e apague com a Borracha (E) para abrir passagem.

**Retângulo livre:**
1. Selecione a camada de objetos `colisores` (vermelha).
2. Use "Inserir Retângulo" (R) e arraste no mapa. Para ajustar, use "Selecionar Objetos" (S): arraste as alças ou digite X, Y, Largura e Altura no painel Propriedades.
3. Segure Ctrl ao arrastar para encaixar na grade. Polígonos e elipses valem pela caixa em volta deles.

**Colisão de uma decoração** (vale para todas as cópias dela, nos dois mapas):
1. No painel Tilesets, abra a aba `objetos` e clique no botão "Editar Tileset" (ícone de chave inglesa), que abre o `objetos.tsx`.
2. Clique na imagem (por exemplo `arvore1`) e abra o Editor de Colisão: **Exibir → Visualizações e Barras de Ferramentas → Editor de Colisão de Tile** (em inglês, *View → Views and Toolbars → Tile Collision Editor*).
3. Desenhe ou ajuste o retângulo vermelho. Pode ter mais de um. Para que a decoração possa ser atravessada, apague todos.
4. Salve o tileset com Ctrl+S.

Para ver os colisores das decorações no mapa, ligue **Exibir → Mostrar Formas de Colisão de Tile** (*View → Show Tile Collision Shapes*).

**Animações:** se existir `recursos/objetos/animados/<nome>.png`, o jogo usa essa tira (quadros lado a lado, cada um do tamanho da imagem parada) no lugar da imagem parada. É assim que as árvores balançam, o moinho gira e as chaminés soltam fumaça.

**Nova decoração:** coloque o PNG em `recursos/objetos/` e, no Tiled, adicione a imagem ao tileset `objetos` (botão "Adicionar tiles").

## Falas e nomes dos personagens

Todos os textos das conversas e os nomes dos NPCs ficam em **`recursos/falas.txt`**, no estilo de um arquivo `.env`:

```
NPC2_NOME=Dona Cida
NPC2_FALA_1=Galinhas? Nem me fale em galinhas.
NPC2_FALA_2=Uma delas ficou me encarando por três horas semana passada.
```

- `NPCn_NOME` é o nome que aparece na caixa de conversa. `NPCn_FALA_1`, `_2`, `_3`... são os balões, em ordem. Para acrescentar um balão, use o próximo número.
- `NPC1_DEPOIS_DA_MISSAO_n`: o que o prefeito diz depois de dar a missão. `ACORDAR_n`, `SAIDA_FECHADA_n` e `CHEGADA_NO_CAMPO_n`: a narração.
- Linhas com `#` são comentários. Aspas, acentos e `=` dentro do texto funcionam sem barra. Salve em UTF-8, que é o padrão do Bloco de Notas e do VS Code.
- Não precisa compilar: o arquivo é lido de novo a cada partida. Se faltar uma chave, o jogo mostra o nome dela entre colchetes, por exemplo `[NPC2_FALA_1]`.

## Arte e som

Todos os arquivos são opcionais, menos os mapas. Sem uma imagem, o jogo desenha um retângulo colorido no lugar (sem o tileset, o chão fica verde e as paredes escuras); sem um som, segue em silêncio. Basta colocar o arquivo com o nome certo na pasta certa.

**Imagens (PNG com fundo transparente)**

| Arquivo | O que é |
|---|---|
| `recursos/telas/inicial.png` | Fundo pronto da tela inicial (320×240 ou 640×480), opcional. Sem ele, a tela inicial mostra a vila ao fundo e o título numa placa |
| `recursos/sprites/heroi_parado.png`, `heroi_correndo.png`, `heroi_ataque.png`, `heroi_dano.png` | Herói (tiras do Sunnyside World: idle, run, sword, hurt) |
| `recursos/sprites/galinha.png` | Galinha (os 4 quadros de `spr_deco_chicken_01` lado a lado) |
| `recursos/sprites/morador1.png` a `morador4.png` | Prefeito, Dona Cida, Seu Juca e Tico (corpo base do pacote com cabelo e camisa diferentes). O primeiro quadro vira o retrato no diálogo |
| `recursos/sprites/heroi_morte.png` | Animação de morte do herói |
| `recursos/sprites/golpe.png` | Efeito do golpe, só usado se faltar `heroi_ataque.png` |
| `recursos/tiles/tileset.png` | Tileset do Sunnyside World (64 colunas de tiles 16×16), usado pelos mapas |
| `recursos/objetos/*.png`, `objetos/animados/*.png` | Decorações e animais (veja "Mapas") |
| `recursos/interface/` | Caixas 9-slice (`caixa_clara`, `caixa_escura`, `caixa_branca`), `coracao.png` e `coracao_vazio.png`, moedas, balões (`balao_alerta`, `balao_conversa`, `balao_amor`, `balao_raiva`), etiqueta de nome, indicador, mão do menu, espada, barra de vida da galinha |
| `recursos/efeitos/` | `poeira.png` e `fumaca.png` (quadros 24×9), `penas.png` e `folhas.png` (64×32), `brilho.png` (7×7) |

**Tiras de sprites:** os quadros da animação ficam lado a lado numa linha só. Os personagens têm quadros de 96×64, com os pés no ponto (48, 39), e olham para a direita (o jogo espelha para a esquerda). A galinha tem quadros de 32×32, com os pés em (15, 24), e olha para a esquerda. Esses números ficam em `criarAnimacaoDePessoa` e `criarAnimacaoDeGalinha`, em `src/view/TelaJogo.java`.

**Sons (WAV PCM de 16 bits; MP3 e OGG não funcionam)**

Para converter uma música MP3/M4A: abra no Audacity e use "Arquivo > Exportar > Exportar como WAV" (PCM de 16 bits). Hoje só existem `vila.wav` e `passos.wav`; os outros nomes ficam em silêncio até alguém colocar o arquivo. A música de luta entra como `raide.wav`.

| Arquivo | Quando toca |
|---|---|
| `recursos/musicas/inicial.wav` | Tela inicial (hoje em silêncio) |
| `recursos/musicas/vila.wav` | Vila (tem: convertida de `entrada/soundtrack/musica_vila.mp3`) |
| `recursos/musicas/campo.wav` | Campo, antes da raide (hoje em silêncio, de propósito) |
| `recursos/musicas/raide.wav` | Durante a raide (música de luta, a definir) |
| `recursos/musicas/fuga.wav`, `derrota.wav` | Tela final |
| `recursos/sons/menu.wav` | Mudar de opção no menu |
| `recursos/sons/dialogo.wav` | Abrir ou avançar uma conversa |
| `recursos/sons/missao.wav` | Aceitar a missão |
| `recursos/sons/golpe.wav` | Herói ataca |
| `recursos/sons/acerto.wav` | Golpe acerta sem matar |
| `recursos/sons/galinha.wav` | Galinha derrotada |
| `recursos/sons/raide.wav` | Começo da raide |
| `recursos/sons/dano.wav` | Herói leva dano |
| `recursos/sons/passos.wav` | Passos na grama, em loop enquanto o herói anda (tem: `entrada/soundtrack/caminhando_grama.wav`) |

As músicas ficam inteiras na memória: prefira 22 kHz ou 44 kHz e alguns minutos no máximo.

## Créditos

- Tileset, personagens, animais, decorações, efeitos e interface: **Sunnyside World**, de Daniel Diggle (itch.io). Confirmar os termos da licença na página do pacote.
- Mapas editados com o **Tiled** (<https://www.mapeditor.org>). A vila foi recortada da cena de exemplo que vem no pacote.

Liste aqui a origem e a licença de cada outro sprite, som e música usados.
