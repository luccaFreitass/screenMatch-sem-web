package com.github.luccafreitass.screenmatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.luccafreitass.screenmatch.model.DadosSerie;
import com.github.luccafreitass.screenmatch.service.ConsumoApi;
import com.github.luccafreitass.screenmatch.service.ConverteDados;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ConsumoApi consumoApi = new ConsumoApi();
		String endereco = "https://www.omdbapi.com/?t=how+i+met+your+mother&apikey=ee5e4f03";
		String json = consumoApi.obterDados(endereco);
		System.out.println(json);

		ConverteDados conversor = new ConverteDados();
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);
	}

}
