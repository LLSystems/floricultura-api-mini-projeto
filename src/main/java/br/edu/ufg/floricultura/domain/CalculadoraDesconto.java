package br.edu.ufg.floricultura.domain;

public final class CalculadoraDesconto {

	private static final int DESCONTO_MAXIMO_PERCENTUAL = 30;

	private CalculadoraDesconto() {
	}

	/**
	 * Calcula o desconto de fidelidade como {@code anos * taxa} da categoria,
	 * limitado ao teto de {@value #DESCONTO_MAXIMO_PERCENTUAL}%.
	 *
	 * @param categoria categoria de fidelidade do cliente
	 * @param anos      anos de fidelidade (zero ou positivo)
	 * @return desconto percentual, entre 0 e {@value #DESCONTO_MAXIMO_PERCENTUAL}
	 * @throws IllegalArgumentException se {@code categoria} for nula ou {@code anos} for negativo
	 */
	public static int calcular(Categoria categoria, int anos) {
		if (categoria == null) {
			throw new IllegalArgumentException("Categoria não pode ser nula");
		}
		if (anos < 0) {
			throw new IllegalArgumentException("Anos não pode ser negativo: " + anos);
		}
		return (int) Math.min((long) anos * categoria.getTaxa(), DESCONTO_MAXIMO_PERCENTUAL);
	}

}
