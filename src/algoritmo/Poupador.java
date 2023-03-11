package algoritmo;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Poupador extends ProgramaPoupador {
	int [][] mapaExplorado = new int[30][30];
	int [][] mapaInteresse = new int[32][32];
	// 0: parado; 1: acima; 2: abaixo; 3: direita; 4: esquerda;
	int [] direcaoParaVisao = {0, 7, 16, 12, 11};
	int [] direcaoParaOlfato = {0, 1, 6, 4, 3};
	int [] direcaoParaMovimento = {0, 1, 2, 3, 4};
	int [] coordenadasBanco = {8, 8};
	String objetivo = "acumular"; //acumular, guardar ou fugir
	boolean roubado = false;
	int ultimoNumeroMoedasBanco = 0;
	int saldoTotal = 0;
	int ultimoSaldo = 0;
	int medo = 10;
	int ambicao = 1;
	boolean contornando = false;
	int[] direcaoContorno = {0, 0};
	
	// DONE explorar - prioridades maiores pra quadrados inexplorados
	// Estratégia ideal: pôr uma moeda no banco e desistir de pegar moedas
	// TODO refatorar comportamento com base no saldo da partida
	// TODO Função de chance para explorar considera mais pesos (cheiro de ladroes, presença de moedas, etc)
	// TODO mecânica de isca pra um dos poupadores 

	public int acao() {
		System.out.println(sensor.getPosicao().x + " " + sensor.getPosicao().y);
		// calcula o saldo geral do poupador
		this.avaliarSaldo();

		// popular matriz com o conteúdo da visão atual
		this.mapearInteresses();
		
		// marcar posição como explorada
		this.mapaExplorado
				[sensor.getPosicao().x]
				[sensor.getPosicao().y] += 1;
		
//		if (chanceAcumular>50)
//			this.objetivo = "guardar";
		this.decidirObjetivo();

		if(this.buscarLadrao()>=0) {
			this.objetivo = "fugir";
			this.contornando = false;
			this.medo += 1;
			System.out.println("PQP UM LADRAO");
		}
		
		return this.escolherDirecaoAleatoria();
	}
	
	public void decidirObjetivo() {
		if(this.saldoTotal > 0)
			this.objetivo = "guardar";
		else
			this.objetivo = "acumular";
		int ignorarPrejuizo = (int)(Math.random() *100 * this.ambicao);
		if (this.medo > ignorarPrejuizo)
			this.saldoTotal = 0;
	}
	public void mapearInteresses() {
		for(int i=0; i<sensor.getVisaoIdentificacao().length; i++) {
			int[] coordenadas = this.getCoordenadasVisao(i);
			try {
				this.mapaInteresse[coordenadas[0]][coordenadas[1]] = sensor.getVisaoIdentificacao()[i];
			}
			catch (Exception e){
//				System.out.println(e);
				
			}
		}
	}
	
	public int buscarLadrao() {
		for(int i=0; i<sensor.getVisaoIdentificacao().length; i++) {
			if(sensor.getVisaoIdentificacao()[i]>=200)
				return i;
		}
		return -1;
	}
	
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
//				- this.calcularDistancia(this.getCoorenadasDirecao(direcao), coordenadasBanco);
		}

		if(peso <= 0) peso = 1; 
		return (int) 100/(peso);
	}

	public int escolherDirecaoAleatoria() {
		ArrayList<Integer> direcoesComPeso = new ArrayList<Integer>();
		System.out.println("estou na posição " + sensor.getPosicao().x + "," + sensor.getPosicao().y 
				+ " tenho " + sensor.getNumeroDeMoedas() + " meu saldo eh " + this.saldoTotal+ " e meu objetivo é " + this.objetivo );
		

		for(int i=0; i< this.direcaoParaMovimento.length; i++) {
			int direcao = this.direcaoParaMovimento[i];

			if(sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 3 && sensor.getNumeroDeMoedas() > 0) {
				return direcao;
			}

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

		if(this.objetivo == "guardar") {
			int direcaoMaisProxima = this.direcaoParaMovimento[1];
			double distanciaMaisProxima =  this.calcularDistancia(
					this.getCoorenadasDirecao(direcaoMaisProxima), this.coordenadasBanco);

			for(int i=0; i< this.direcaoParaMovimento.length; i++) {
				int direcao = this.direcaoParaMovimento[i];
				int[] coordenadasDirecao = this.getCoorenadasDirecao(direcao);


				int distancia = this.calcularDistancia(coordenadasDirecao, this.coordenadasBanco);


//				System.out.println("a direcao " + i +" nas coordenadas" + 
//						coordenadasDirecao[0] +"," + coordenadasDirecao[1]+ " esta a " + distancia);
				if(distancia < distanciaMaisProxima) {
					direcaoMaisProxima = direcao;
					distanciaMaisProxima= distancia;
				}
			}
			for(int i=0; i<50; i++) {
				direcoesComPeso.add(direcaoMaisProxima);
			}
		}
		
		System.out.println(direcoesComPeso.toString());
		int direcao = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));

		if(this.contornando) {
			System.out.println("estou tentando contornar pra realizar o movimento" +this.direcaoContorno[1] +" !");
			if(this.direcaoValida(this.direcaoContorno[1])) {
				this.contornando = false;
				direcao = direcaoContorno[1];
			}
			else
				direcao = this.direcaoContorno[0];
		}
	
		if(!this.contornando && sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 1){
			this.direcaoContorno[1] = direcao;
			this.contornando = true;
			if(direcao == 1 || direcao == 2) {
				int[] opcoesContorno = {3, 4};
				direcao = opcoesContorno[(int) (Math.random() * opcoesContorno.length)];
				this.direcaoContorno[0] = direcao;
			}
			else {
				int[] opcoesContorno = {1, 2};
				direcao = opcoesContorno[(int) (Math.random() * opcoesContorno.length)];
				this.direcaoContorno[0] = direcao;
			}
		}

		// evita jogadas inválidas(correr para o ladrao, sair do mapa ou se jogar na parede)
		System.out.println("escolhi a direcao " + direcao);
		while(!this.direcaoValida(direcao)) {
			System.out.println("não posso ir nessa direção!");
			direcao = direcoesComPeso.get((int) (Math.random() * direcoesComPeso.size()));
			this.contornando = false;
		}

		return direcao;
	}
	
	public int direcaoMaisDistanteLadrao() {
			int direcaoMaisDistante = this.direcaoParaMovimento[1];
			int [] coordenadasLadrao = this.getCoordenadasVisao(this.buscarLadrao());
			double distanciaMaisDistante =  this.calcularDistancia(
					this.getCoorenadasDirecao(direcaoMaisDistante), coordenadasLadrao);

			System.out.println("o ladrao esta na posicao " + coordenadasLadrao[0] + "," + coordenadasLadrao[1]);
			for(int i=0; i< this.direcaoParaMovimento.length; i++) {
				int direcao = this.direcaoParaMovimento[i];
				int[] coordenadasDirecao = this.getCoorenadasDirecao(direcao);


				int distancia = this.calcularDistancia(coordenadasDirecao, coordenadasLadrao);

//				System.out.println("a direcao " + i +" nas coordenadas" + 
//				coordenadasDirecao[0] +"," + coordenadasDirecao[1]+ " esta a " + distancia);

				if(distancia > distanciaMaisDistante) {
					direcaoMaisDistante = direcao;
					distanciaMaisDistante= distancia;
				}
			}
			System.out.println("a direcao mais distante é: " + direcaoMaisDistante);
		return direcaoMaisDistante;
	}
	
	public boolean direcaoValida(int direcao) {
		return 
			!(sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] >= 100 ||
			sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] < 0 ||
			(sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 3 && sensor.getNumeroDeMoedas() == 0) ||
			(sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 5 && sensor.getNumeroDeMoedas() < 5) ||
			sensor.getVisaoIdentificacao()[this.direcaoParaVisao[direcao]] == 1);
	}
	
	public int direcaoTemMoeda(int direcao) {
		int[] coordenadas = this.getCoorenadasDirecao(direcao);
		try {
			if(this.mapaInteresse[coordenadas[0]][coordenadas[1]] == 4)
				return 1;
		}
		catch(Exception e) {
			return 0;
		}
		return 0;
	}
	
	public int calcularDistancia(int[] primeiroPonto, int[] segundoPonto) {
		return (int)
				Math.pow( Point2D.distance(
				primeiroPonto[0], primeiroPonto[1], 
				segundoPonto[0], segundoPonto[1]), 3);
	}
	
	public void avaliarSaldo() {
		if(sensor.getNumeroDeMoedas() < this.ultimoSaldo) {
			if((sensor.getNumeroDeMoedasBanco() == this.ultimoNumeroMoedasBanco)) {
				System.out.println("FUI ROUBADOOO");
				if(saldoTotal > 0)
					this.saldoTotal -= this.ultimoSaldo;
				this.saldoTotal -= this.ultimoSaldo;
				this.medo += 5;			
			}
			else {
				this.saldoTotal = 0;
			}
		}
		else if(sensor.getNumeroDeMoedas() > this.ultimoSaldo) {
			this.saldoTotal += 1;
			this.ambicao = 10;
		}
		else {
			this.ambicao  += 1;
		}
		
		
		this.ultimoSaldo = sensor.getNumeroDeMoedas();
	}
	
	
	// recebe um quadrado no alcance do agente (0, 1, 2, 3, 4) e pega seu valor no mapaExplorado
	public int lerQuadradoMapaExplorado(int quadradoMapaVisao) {
		int[] coordenadasMapa = this.getCoorenadasDirecao(quadradoMapaVisao);
		try {
			return mapaExplorado[coordenadasMapa[0]][coordenadasMapa[1]];
		}
		catch(Exception e) {
//			System.out.println(e);
			return 333;
		}
	}
	
	// recebe um quadrado no alcance do agente (0, 1, 2, 3, 4) e retorna suas coordenadas
	public int[] getCoorenadasDirecao(int direcao) {
		int[][] transform = {{0,0}, {0,-1}, {0,1}, {1,0}, {-1,0}};
		int[] coordenadas = transform[direcao];
		
		int[] coordenadasMapa = {sensor.getPosicao().x + coordenadas[0], sensor.getPosicao().y + coordenadas[1]};
		return coordenadasMapa;
	}
	
	public int[] getCoordenadasVisao(int quadradoVisao) {
		int[][] transform = {
				{-2, 2},  {-1, 2},  {0, 2},  {1, 2},  {2, 2},
				{-2, 1},  {-1, 1},  {0, 1},  {1, 1},  {2, 1},
				{-2, 0},  {-1, 0},  {0, 0},  {1, 0},  {2, 0},
				{-2, -1}, {-1, -1}, {-1, 0}, {1, -1}, {2, -1},
				{-2, -2}, {-1, -2}, {-2, 0}, {1, -2}, {2, -2},
		};
		int[] poupador = {sensor.getPosicao().x, sensor.getPosicao().y};
		int[] diferenca = transform[quadradoVisao];

		return new int[] {poupador[0]+diferenca[0], poupador[1]+diferenca[1]};
	}
}