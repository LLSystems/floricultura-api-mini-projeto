package br.edu.ufg.floricultura.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.ufg.floricultura.exception.PedidoJaEntregueException;

class PedidoTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 14);
	private static final LocalDate AMANHA = HOJE.plusDays(1);

	@ParameterizedTest(name = "{0} {1} anos, {2} -> desconto {3}%, final {4}")
	@CsvSource({
		"OURO,  5, 200.00, 25, 150.00",
		"PRATA, 3, 100.00,  9,  91.00",
		"OURO, 10, 100.00, 30,  70.00",  // teto
		// 33.33 * 95 / 100 = 31.6635 -> 31.66 (3ª casa é 3: não distingue modos de arredondamento)
		"OURO,  1,  33.33,  5,  31.66",
		// 10.30 * 95 / 100 = 9.785 exato -> HALF_UP 9.79 (HALF_EVEN/HALF_DOWN dariam 9.78)
		"OURO,  1,  10.30,  5,   9.79"
	})
	void criaPedidoComDescontoEValorFinal(Categoria categoria, int anos, BigDecimal valorBruto,
			int descontoEsperado, BigDecimal valorFinalEsperado) {
		Pedido pedido = Pedido.criar("Maria", categoria, anos, valorBruto, AMANHA, HOJE);

		assertEquals(descontoEsperado, pedido.getDescontoPercentual());
		assertEquals(valorFinalEsperado, pedido.getValorFinal());  // BigDecimal.equals exige escala 2
	}

	@Test
	void criaPedidoComDadosInformadosEStatusPendente() {
		Pedido pedido = Pedido.criar("Maria", Categoria.PRATA, 3, new BigDecimal("100.00"), AMANHA, HOJE);

		assertEquals("Maria", pedido.getCliente());
		assertEquals(Categoria.PRATA, pedido.getCategoria());
		assertEquals(3, pedido.getAnosFidelidade());
		assertEquals(new BigDecimal("100.00"), pedido.getValorBruto());
		assertEquals(AMANHA, pedido.getDataEntrega());
		assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
	}

	@Test
	void dataEntregaIgualAHojeEhValida() {
		Pedido pedido = Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), HOJE, HOJE);

		assertEquals(HOJE, pedido.getDataEntrega());
	}

	@ParameterizedTest(name = "data de entrega inválida: {0}")
	@NullSource
	@ValueSource(strings = {"2026-10-13"})  // ontem
	void dataEntregaNulaOuAnteriorAHojeLancaExcecao(LocalDate dataEntrega) {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), dataEntrega, HOJE));
	}

	@ParameterizedTest(name = "cliente inválido: \"{0}\"")
	@NullSource
	@ValueSource(strings = {"", "   "})
	void clienteNuloOuEmBrancoLancaExcecao(String cliente) {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar(cliente, Categoria.OURO, 5, new BigDecimal("200.00"), AMANHA, HOJE));
	}

	@ParameterizedTest(name = "valor bruto inválido: {0}")
	@NullSource
	@ValueSource(strings = {"0", "0.00", "-10.00"})
	void valorBrutoNuloZeroOuNegativoLancaExcecao(BigDecimal valorBruto) {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", Categoria.OURO, 5, valorBruto, AMANHA, HOJE));
	}

	@Test
	void marcarEntregueMudaStatusParaEntregue() {
		Pedido pedido = Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), AMANHA, HOJE);

		pedido.marcarEntregue();

		assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
	}

	@Test
	void marcarEntregueDuasVezesLancaExcecao() {
		Pedido pedido = Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), AMANHA, HOJE);
		pedido.marcarEntregue();

		assertThrows(PedidoJaEntregueException.class, pedido::marcarEntregue);
	}

	@Test
	void hojeNuloLancaExcecao() {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), AMANHA, null));
	}

	@Test
	void categoriaNulaLancaExcecao() {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", null, 5, new BigDecimal("200.00"), AMANHA, HOJE));
	}

	@Test
	void anosNegativosLancamExcecao() {
		assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", Categoria.OURO, -1, new BigDecimal("200.00"), AMANHA, HOJE));
	}

	@ParameterizedTest(name = "valor bruto {0} -> {1}")
	@CsvSource({
		"200,     200.00",
		"200.000, 200.00",  // zeros à direita não são casas significativas
		"10.30,   10.30"
	})
	void valorBrutoEhNormalizadoParaEscala2(BigDecimal valorBruto, BigDecimal esperado) {
		Pedido pedido = Pedido.criar("Maria", Categoria.OURO, 1, valorBruto, AMANHA, HOJE);

		assertEquals(esperado, pedido.getValorBruto());  // BigDecimal.equals compara a escala
	}

	@Test
	void valorBrutoComMaisDeDuasCasasSignificativasLancaExcecao() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> Pedido.criar("Maria", Categoria.OURO, 1, new BigDecimal("10.305"), AMANHA, HOJE));

		assertTrue(e.getMessage().contains("no máximo 2 casas decimais"), e.getMessage());
	}

}
