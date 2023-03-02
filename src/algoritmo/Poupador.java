package algoritmo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Poupador extends ProgramaPoupador {
	int [][] mapaExplorado = new int[31][31];
	
	// TODO explorar - pesos maiores pra quadrados explorados
	

	public int acao() {
		// acima = 7, abaixo = 16, esquerda = 11, direita = 12
		// acima = 1, abaixo = 2, esquerda = 4, direita = 3
		int[] direcoes = {0, 1, 2, 3, 4};
		// acima = 7, abaixo = 16, esquerda = 11, direita = 12
		int[] visao = {0, 7, 16, 12, 11};
		ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();
		
		// marcar posição como explorada
		this.mapaExplorado
				[sensor.getPosicao().x]
				[sensor.getPosicao().y] += 3;
		
		for(int i=0; i< direcoes.length; i++) {
			int direcao = direcoes[i];
			int chance = (int) 10/(this.lerQuadradoMapaExplorado(direcao)+1);
			
			for(int j=0; j<chance; j++) {
				direcoesComPeso.add(direcao);
			}
		}
		
		return this.escolherPasso(direcoesComPeso);
	}
	
	public int escolherPasso(ArrayList<Integer> direcoesComPeso) {
		int direcao = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
		return direcao;
	}
	
	// recebe um quadrado de acordo com o mapa da visão e pega seu valor no mapaExplorado
	public int lerQuadradoMapaExplorado(int quadradoMapaVisao) {
		// acima = 7, abaixo = 16, esquerda = 11, direita = 12
		Map<Integer, int[]> transform = new HashMap<>();
		transform.put(0, new int[]{0, 0});
		transform.put(1, new int[]{0, -1});
		transform.put(2, new int[]{-1, 0});
		transform.put(3, new int[]{1, 0});
		transform.put(4, new int[]{0, 1});
		int[] coordenadas = transform.get(quadradoMapaVisao);

		int[] coordenadasMapa = {sensor.getPosicao().x + coordenadas[0], sensor.getPosicao().y + coordenadas[1]};
		try {
			return mapaExplorado[coordenadasMapa[0]][coordenadasMapa[1]];
		}
		catch(Exception e) {
			return mapaExplorado[coordenadasMapa[0]+1][coordenadasMapa[1]+1];
		}
	}
	
}