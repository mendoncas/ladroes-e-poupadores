# Ladrões e poupadores: implementando graus de autonomia em agentes racionais

## Características gerais do modelo do poupador
O agente implementado tem como objetivo coletar o máximo de moedas possível, guardando-as no banco quando for interessante.
O agente evita contato com ladrões no seu campo de visão e coleta moedas, que pode ser mais ou menos interessantes de acordo com seu objetivo atual.
Possui uma mecânica de exploração que prioriza quadrados não-explorados, bem como variáveis de medo e ambição que mudam de acordo com a experiência do agente e influenciam suas decisões no jogo.
Para decidir seu próximo passo (ficar parado, mover-se para cima, baixo, esquerda ou direita), o agente gera uma lista de possibilidades e escolhe uma componente aleatório da lista. 
A quantidade de vezes que uma determinada ação aparece na lista é determinada por uma série de fatores, de forma que ações mais interessantes possuem mais instâncias dentro da lista de possibilidades.

Implementação da lista de interesses:

```java
public int escolherDirecao() {
		ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();

        // ....

		for(int i=0; i< this.direcaoParaMovimento.length; i++) {
			int direcao = this.direcaoParaMovimento[i];
			int chance = 1 + avaliarQuadrado(direcao);
			
			for(int j=0; j<chance; j++) {
				direcoesComPeso.add(direcao);
			}
		}

        // ....
        int direcaoEscolhida = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
		
        // ....
		return direcaoEscolhida;
	}

```


## Agente reativo simples:
Um agente reativo simples tem seu comportamento determinado por uma série de regras simples no formato de **SE** isso acontece, **ENTÂO** faça isso. Em algumas situações, a melhor decisão é clara e o comportamento reativo é útil para o agente. Um exemplo na nossa implementação garante que o agente sempre vai guardar seu dinheiro no banco se estiver no seu alcance e se sua quantidade de moedas for maior que zero:

```java
// se a direção contém um banco e moedas > 0, tome a direção

public int escolherDirecao() {
    // ....
    if(sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 3 
          && sensor.getNumeroDeMoedas() > 0) {
		return direcao;
	}
}

```

## Agente baseado em modelo:
Um agente baseado em modelo retém informações sobre o ambiente, bem como regras que pode utilizar para calcular o próximo estado do ambiente. No caso, o agente poupador retém uma matriz correspondente ao tamanho do mapa, onde marca um quadrado sempre que passa por ele. A quantidade de vezes que o poupador passa num quadrado influencia o interesse do poupador nesse quadrado quando estiver explorando.
Dessa forma, quando o objetivo do poupador é explorar, quadrados não visitados são priorizados.

```java
public class Poupador extends ProgramaPoupador {
    int [][] mapaExplorado = new int[30][30];

    public int acao(){
    	// marcar posição como explorada
		this.mapaExplorado
				[sensor.getPosicao().x]
				[sensor.getPosicao().y] += 1;
    }
}
```

## Agente baseado em objetivo
Um agente baseado em objetivo decide o que fazer com base no estado atual do ambiente. Na nossa implementação, o agente tem objetivos como acumular, guardar, fugir e contornar. Uma vez definidos esses objetivos, o peso das possibilidades é alterado, favorecendo as que melhor atendem o objetivo no estado atual. Um exemplo é o objetivo de fugir: uma vez que o agente identifica um ladrão por perto, passa a tentar fugir e, para cada passo que toma, prioriza o que mais aumenta sua distância do ladrão.

```java
public int acao() {

	if(this.buscarLadrao()>=0) {
		this.objetivo = "fugir";
	}
		
	return this.escolherDirecao();
}

public int escolherDirecao(){
	ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();

    // ....

	for(int i=0; i< this.direcaoParaMovimento.length; i++) {
		int direcao = this.direcaoParaMovimento[i];
		int chance = 1 + avaliarQuadrado(direcao);
		
		for(int j=0; j<chance; j++) {
			direcoesComPeso.add(direcao);
		}
	}

	
	if (this.objetivo == "fugir") {
		for(int i=0; i<650; i++) {
			direcoesComPeso.add(this.direcaoMaisDistanteLadrao());
		}
	}

    // ....
    int direcaoEscolhida = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
	
    // ....
	return direcaoEscolhida;
}
```

## Agente baseado em utilidade
Um agente baseado em utilidade é capaz de calcular, dado suas prioridades e o estado do jogo, o quão "feliz" vai ser uma determinada ação. O agente possui um função internalizada de performance, sua função de utilidade. Objetivos determinam ações de maneira binária, como tristes ou felizes. Um agente baseado em utilidade quantifica a utilidade de uma ação com base na sua função de utilidade. 
Na nossa implementação, a função de utilidade leva em considração o objetivo do agente, bem como seu medo e ambição.

```java
	public int avaliarQuadrado(int direcao) {
		int peso = 1;
		System.out.println("meu olfato na direcao: " + direcao + "detecta " +(sensor.getAmbienteOlfatoPoupador() [this.direcaoParaOlfato[direcao]]));
		
		if(this.objetivo == "guardar") {
			peso = this.medo/(sensor.getAmbienteOlfatoPoupador() [this.direcaoParaOlfato[direcao]] + 2);
		}
		else if (this.objetivo == "acumular") {
			peso = this.ambicao*(this.lerQuadradoMapaExplorado(direcao))
				+ this.medo/(sensor.getAmbienteOlfatoPoupador() [this.direcaoParaOlfato[direcao]] + 2)
				- (int) this.ambicao*this.direcaoTemMoeda(direcao);
		}

		if(peso <= 0) peso = 1; 
		return (int) 100/(peso);
	}

```
 
Ao avaliar um quadrado, quando o objetivo é acumular, o poupador leva em conta se o quadrado ja foi explorado, se há algum ladrão por perto (olfato) e se a direcao possui alguma moeda. A relevância de cada um desses parâmetros é determinada de acordo com as variáveis medo e ambição, que evoluem ao longo do jogo.

Posteriormente, o resultado da função de utilidade é utilizado ao escolherDirecao() Para popular a lista de possibilidades:

```java
public int escolherDirecao() {
		ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();

        // ....

		for(int i=0; i< this.direcaoParaMovimento.length; i++) {
			int direcao = this.direcaoParaMovimento[i];
			int chance = 1 + avaliarQuadrado(direcao);
			
			for(int j=0; j<chance; j++) {
				direcoesComPeso.add(direcao);
			}
		}

        // ....
        int direcaoEscolhida = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
		
        // ....
		return direcaoEscolhida;
	}

```

