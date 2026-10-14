package br.edu.ufg.floricultura.domain;

import java.util.Locale;

public enum Categoria {

	OURO(5),
	PRATA(3);

	private final int taxa;

	Categoria(int taxa) {
		this.taxa = taxa;
	}

	public int getTaxa() {
		return taxa;
	}

	/**
	 * Converte um texto na categoria correspondente, ignorando maiúsculas/minúsculas
	 * e espaços nas pontas.
	 *
	 * @param texto nome da categoria (por exemplo, {@code "Ouro"} ou {@code " prata "})
	 * @return a categoria correspondente
	 * @throws IllegalArgumentException se {@code texto} for nulo ou não corresponder a nenhuma categoria
	 */
	public static Categoria deTexto(String texto) {
		if (texto == null) {
			throw new IllegalArgumentException("Categoria não pode ser nula");
		}
		try {
			return valueOf(texto.strip().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Categoria desconhecida: \"" + texto + "\"", e);
		}
	}

}
