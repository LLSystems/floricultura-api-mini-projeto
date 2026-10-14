package br.edu.ufg.floricultura.domain;

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
		throw new UnsupportedOperationException("Não implementado");
	}

}
