package com.github.luccafreitass.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

		System.out.println("\n Top 5 episódios");
		dadosEpisodios.stream().filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
				.sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()).limit(5)
				.forEach(System.out::println);

		List<Episodio> episodios = temporadas.stream()
				.flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numero(), d)))
				.collect(Collectors.toList());

		episodios.forEach(System.out::println);

		System.out.println("A partir de qual ano voce deseja ver os eps? ");
		var ano = in.nextInt();
		in.nextLine();

		LocalDate dataBusca = LocalDate.of(ano, 1, 1);

		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
				.forEach(e -> System.out.println("Temporada: " + e.getTemporada() + "\nEpisodio: " + e.getTitulo()
						+ "\nData de lancamento: " + e.getDataLancamento().format(formatador)));
	}

}
