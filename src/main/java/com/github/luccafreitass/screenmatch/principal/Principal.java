package com.github.luccafreitass.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.github.luccafreitass.screenmatch.model.DadosEpisodio;
import com.github.luccafreitass.screenmatch.model.DadosSerie;
import com.github.luccafreitass.screenmatch.model.DadosTemporada;
import com.github.luccafreitass.screenmatch.model.Episodio;
import com.github.luccafreitass.screenmatch.service.ConsumoApi;
import com.github.luccafreitass.screenmatch.service.ConverteDados;

public class Principal {

	private Scanner in = new Scanner(System.in);

	private ConsumoApi consumoApi = new ConsumoApi();
	private ConverteDados conversor = new ConverteDados();

	private final String ENDERECO = "https://www.omdbapi.com/?t=";
	private final String API_KEY = "&apikey=ee5e4f03";

	public void exibeMenu() {
		System.out.println("Digite o nome da serie para a busca: ");
		String nomeSerie = in.nextLine();
		String json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);

		List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

		temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

		List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream())
				.collect(Collectors.toList());

//		top 10 eps 
		
		System.out.println("\n Top 10 episódios");
		dadosEpisodios.stream().filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
				.peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
				.sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
				.peek(e -> System.out.println("Ordenacao " + e)).limit(10).peek(e -> System.out.println("Limite " + e))
				.map(e -> e.titulo().toUpperCase()).peek(e -> System.out.println("Mapeamento " + e))
				.forEach(System.out::println);

		List<Episodio> episodios = temporadas.stream()
				.flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numero(), d)))
				.collect(Collectors.toList());

		episodios.forEach(System.out::println);
		
//		buscando a temporada do ep buscado 
		
		System.out.println("Digite um trecho do título do episódio");
		var trechoTitulo = in.nextLine();
		Optional<Episodio> episodioBuscado = episodios.stream()
		        .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
		        .findFirst();
		if(episodioBuscado.isPresent()) {
			System.out.println("Episodio encontrado");
			System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
		} else {
			System.out.println("Episodio nao encontrado");
		}


		System.out.println("A partir de qual ano voce deseja ver os eps? ");
		var ano = in.nextInt();
		in.nextLine();

		LocalDate dataBusca = LocalDate.of(ano, 1, 1);

		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
				.forEach(e -> System.out.println("Temporada: " + e.getTemporada() + "\nEpisodio: " + e.getTitulo()
						+ "\nData de lancamento: " + e.getDataLancamento().format(formatador)));
		
		
//		avaliacao da temporada
		
		Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
				.filter(e -> e.getAvaliacao() > 0.0)
		        .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
		System.out.println(avaliacoesPorTemporada);
		
		DoubleSummaryStatistics est = episodios.stream()
				.filter(e -> e.getAvaliacao() > 0.0)
				.collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
		
		System.out.println("Media: " + est.getAverage());
		System.out.println("Melhor ep: " + est.getMax());
		System.out.println("Pior ep: " + est.getMin());
	}

}
