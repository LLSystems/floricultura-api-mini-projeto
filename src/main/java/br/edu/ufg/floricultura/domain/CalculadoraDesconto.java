package br.edu.ufg.floricultura.domain;

public final class CalculadoraDesconto {

	private CalculadoraDesconto() {
	}

	public static int calcular(Categoria categoria, int anos) {
		if (anos < 0) {
			throw new IllegalArgumentException("Anos não pode ser negativo: " + anos);
		}
		return Math.min(anos * categoria.getTaxa(), 30);
	}

}
