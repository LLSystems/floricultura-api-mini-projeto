package br.edu.ufg.floricultura.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.domain.StatusPedido;
import br.edu.ufg.floricultura.exception.PedidoJaEntregueException;
import br.edu.ufg.floricultura.exception.PedidoNaoEncontradoException;
import br.edu.ufg.floricultura.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 14);

	// 12:00 UTC = 09:00 em São Paulo, ainda 2026-10-14
	private static final Clock CLOCK =
			Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

	@Mock
	private PedidoRepository repository;

	private PedidoService service;

	@BeforeEach
	void setUp() {
		service = new PedidoService(repository, CLOCK);
	}

	private static Pedido pedidoPendente() {
		return Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), HOJE, HOJE);
	}

	@Test
	void criarSalvaEDevolvePedidoComValoresCorretos() {
		when(repository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

		Pedido resultado = service.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), HOJE.plusDays(1));

		ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
		verify(repository).save(captor.capture());
		Pedido salvo = captor.getValue();
		assertEquals("Maria", salvo.getCliente());
		assertEquals(Categoria.OURO, salvo.getCategoria());
		assertEquals(5, salvo.getAnosFidelidade());
		assertEquals(new BigDecimal("200.00"), salvo.getValorBruto());
		assertEquals(25, salvo.getDescontoPercentual());
		assertEquals(new BigDecimal("150.00"), salvo.getValorFinal());
		assertEquals(HOJE.plusDays(1), salvo.getDataEntrega());
		assertEquals(StatusPedido.PENDENTE, salvo.getStatus());
		assertSame(salvo, resultado);
	}

	@Test
	void criarComDataNoPassadoLancaExcecaoENaoSalva() {
		assertThrows(IllegalArgumentException.class,
				() -> service.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), HOJE.minusDays(1)));

		verify(repository, never()).save(any());
	}

	@Test
	void listarComDataNulaUsaHoje() {
		Pedido pedido = pedidoPendente();
		when(repository.findByStatusAndDataEntrega(StatusPedido.PENDENTE, HOJE)).thenReturn(List.of(pedido));

		List<Pedido> resultado = service.listarEntregasPendentes(null);

		assertEquals(List.of(pedido), resultado);
		verify(repository).findByStatusAndDataEntrega(StatusPedido.PENDENTE, LocalDate.of(2026, 10, 14));
	}

	@Test
	void listarComDataInformada() {
		LocalDate data = LocalDate.of(2026, 10, 20);
		Pedido pedido = pedidoPendente();
		when(repository.findByStatusAndDataEntrega(StatusPedido.PENDENTE, data)).thenReturn(List.of(pedido));

		List<Pedido> resultado = service.listarEntregasPendentes(data);

		assertEquals(List.of(pedido), resultado);
		verify(repository).findByStatusAndDataEntrega(StatusPedido.PENDENTE, data);
	}

	@Test
	void marcarEntregueDeIdInexistenteLancaExcecao() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(PedidoNaoEncontradoException.class, () -> service.marcarEntregue(99L));

		verify(repository, never()).save(any());
	}

	@Test
	void marcarEntregueDePedidoJaEntregueLancaExcecaoENaoSalva() {
		Pedido pedido = pedidoPendente();
		pedido.marcarEntregue();
		when(repository.findById(1L)).thenReturn(Optional.of(pedido));

		assertThrows(PedidoJaEntregueException.class, () -> service.marcarEntregue(1L));

		verify(repository, never()).save(any());
	}

	@Test
	void marcarEntregueValidoSalvaComStatusEntregue() {
		Pedido pedido = pedidoPendente();
		when(repository.findById(1L)).thenReturn(Optional.of(pedido));
		when(repository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

		Pedido resultado = service.marcarEntregue(1L);

		ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
		verify(repository).save(captor.capture());
		assertEquals(StatusPedido.ENTREGUE, captor.getValue().getStatus());
		assertSame(captor.getValue(), resultado);
	}

	// Data distante do relógio real: garante que "hoje" vem do Clock injetado, não de LocalDate.now()
	private static final LocalDate HOJE_FUTURO = LocalDate.of(2030, 6, 15);
	private static final Clock CLOCK_FUTURO =
			Clock.fixed(Instant.parse("2030-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

	@Test
	void listarComDataNulaUsaDataDoClockInjetado() {
		PedidoService serviceFuturo = new PedidoService(repository, CLOCK_FUTURO);

		serviceFuturo.listarEntregasPendentes(null);

		verify(repository).findByStatusAndDataEntrega(StatusPedido.PENDENTE, HOJE_FUTURO);
	}

	@Test
	void criarValidaDataDeEntregaContraHojeDoClockInjetado() {
		PedidoService serviceFuturo = new PedidoService(repository, CLOCK_FUTURO);

		assertThrows(IllegalArgumentException.class,
				() -> serviceFuturo.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), HOJE_FUTURO.minusDays(1)));

		verify(repository, never()).save(any());
	}

}
