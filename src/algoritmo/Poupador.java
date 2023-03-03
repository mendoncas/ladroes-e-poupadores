package algoritmo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Poupador extends ProgramaPoupador {
	int [][] mapaExplorado = new int[31][31];
	// 0: parado; 1: acima; 2: abaixo; 3: direita; 4: esquerda;
	int [] direcaoParaVisao = {0, 7, 16, 12, 11};
	int [] direcaoParaOlfato = {0, 1, 6, 4, 3};
	// Fila contendo os últimos nós visitados
	Queue<int []> ultimosVisitados = new LinkedList<>();
	int [] ultimoVisitado;
	
	// DONE explorar - prioridades maiores pra quadrados inexplorados
	// TODO refatorar função lerQuadradoMapaExplorado para utilizar um vetor
	// TODO Função de chance para explorar considera mais pesos (cheiro de ladroes, presença de moedas, etc)
	// TODO Buscar o banco: poupadores buscam caminho para o banco
	// TODO Isca: um poupador com 0 moedas, ao final do jogo, distrai ladroes
	// TODO Medo: um poupador se sente mais seguro quanto mais próximo do banco
	

	public int acao() {
		// acima = 1, abaixo = 2, esquerda = 4, direita = 3
		int[] direcoes = {0, 1, 2, 3, 4};
		ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();
		
		// marcar posição como explorada
		this.mapaExplorado
				[sensor.getPosicao().x]
				[sensor.getPosicao().y] += 2;
		
		for(int i=0; i< direcoes.length; i++) {
			int direcao = direcoes[i];
			int chance = 1 + avaliarQuadrado(direcao);
			
			for(int j=0; j<chance; j++) {
				direcoesComPeso.add(direcao);
			}
		}
		
		return this.escolherDirecaoAleatoria(direcoesComPeso);
	}
	
	public int avaliarQuadrado(int direcao) {
		
		System.out.println("olfato: " + sensor.getAmbienteOlfatoLadrao() [this.direcaoParaOlfato[direcao]]);
		int peso = (this.lerQuadradoMapaExplorado(direcao))
				+ 50/(sensor.getAmbienteOlfatoLadrao() [this.direcaoParaOlfato[direcao]] + 1);
		return (int) 100/(peso + 1);
	}

	public int escolherDirecaoAleatoria(ArrayList<Integer> direcoesComPeso) {
		System.out.println(direcoesComPeso.toString());

		int direcao = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));

		while(
				sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] < 0 ||
				sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 1) {

			direcao = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
		}

		this.ultimoVisitado = this.getCoordenadasDoQuadrado(direcao);
		return direcao;
	}
	
	// recebe um quadrado no alcance do agente (0, 1, 2, 3, 4) e pega seu valor no mapaExplorado
	public int lerQuadradoMapaExplorado(int quadradoMapaVisao) {
		int[] coordenadasMapa = this.getCoordenadasDoQuadrado(quadradoMapaVisao);
		try {
			return mapaExplorado[coordenadasMapa[0]][coordenadasMapa[1]];
		}
		catch(Exception e) {
			return 300;
//			return mapaExplorado[coordenadasMapa[0]+1][coordenadasMapa[1]+1];
		}
	}
	
	// recebe um quadrado no alcance do agente (0, 1, 2, 3, 4) e retorna suas coordenadas
	public int[] getCoordenadasDoQuadrado(int direcao) {
		int[][] transform = {{0,0}, {0,-1}, {-1,0}, {1,0}, {0,1}};
		int[] coordenadas = transform[direcao];
		
		int[] coordenadasMapa = {sensor.getPosicao().x + coordenadas[0], sensor.getPosicao().y + coordenadas[1]};
		return coordenadasMapa;
	}
}