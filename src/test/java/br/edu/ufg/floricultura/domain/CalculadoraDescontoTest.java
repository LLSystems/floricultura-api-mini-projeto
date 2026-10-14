package br.edu.ufg.floricultura.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CalculadoraDescontoTest {

	@ParameterizedTest(name = "{0} com {1} anos = {2}%")
	@CsvSource({
		"OURO,  5, 25",
		"OURO,  6, 30",  // limite exato do teto
		"OURO, 10, 30",
		"OURO,  0,  0",
		"PRATA, 3,  9",
		"PRATA, 10, 30",
		"PRATA, 11, 30"
	})
	void calculaDescontoComTetoDe30(Categoria categoria, int anos, int esperado) {
		assertEquals(esperado, CalculadoraDesconto.calcular(categoria, anos));
	}

	@ParameterizedTest(name = "{0} com anos negativos lança exceção")
	@EnumSource(Categoria.class)
	void anosNegativosLancamExcecao(Categoria categoria) {
		assertThrows(IllegalArgumentException.class,
				() -> CalculadoraDesconto.calcular(categoria, -1));
	}

	@ParameterizedTest(name = "\"{0}\" -> {1}")
	@CsvSource({
		"'Ouro',    OURO",
		"' prata ', PRATA"  // espaços dentro das aspas são preservados
	})
	void deTextoConverteIgnorandoCaixaEEspacos(String texto, Categoria esperado) {
		assertEquals(esperado, Categoria.deTexto(texto));
	}

	@ParameterizedTest(name = "texto inválido: {0}")
	@NullSource
	@ValueSource(strings = {"bronze"})
	void deTextoInvalidoLancaExcecao(String texto) {
		assertThrows(IllegalArgumentException.class, () -> Categoria.deTexto(texto));
	}

	@Test
	void categoriaNulaLancaExcecao() {
		assertThrows(IllegalArgumentException.class,
				() -> CalculadoraDesconto.calcular(null, 5));
	}

}
