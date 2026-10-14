package br.edu.ufg.floricultura.domain;

public final class CalculadoraDesconto {

	private CalculadoraDesconto() {
	}

	public static int calcular(Categoria categoria, int anos) {
		if (categoria == null) {
			throw new IllegalArgumentException("Categoria não pode ser nula");
		}
		if (anos < 0) {
			throw new IllegalArgumentException("Anos não pode ser negativo: " + anos);
		}
		return (int) Math.min((long) anos * categoria.getTaxa(), 30);
	}

}
