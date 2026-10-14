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
