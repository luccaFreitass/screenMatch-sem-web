package com.github.luccafreitass.screenmatch.service;

public interface IConverteDados {
	
	<T> T obterDados(String json, Class <T> classe);

}
