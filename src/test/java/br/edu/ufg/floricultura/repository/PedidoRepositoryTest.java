package br.edu.ufg.floricultura.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.domain.StatusPedido;

@DataJpaTest
class PedidoRepositoryTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 14);
	private static final LocalDate DIA = LocalDate.of(2026, 10, 20);

	@Autowired
	private PedidoRepository repository;

	private static Pedido pedido(String cliente, LocalDate dataEntrega) {
		return Pedido.criar(cliente, Categoria.PRATA, 3, new BigDecimal("100.00"), dataEntrega, HOJE);
	}

	@Test
	void findByStatusAndDataEntregaDevolveSoPendentesDoDia() {
		Pedido pendenteNoDia = repository.save(pedido("Ana", DIA));
		Pedido entregueNoDia = pedido("Bruno", DIA);
		entregueNoDia.marcarEntregue();
		repository.save(entregueNoDia);
		repository.save(pedido("Carla", DIA.plusDays(1)));
		repository.flush();

		List<Pedido> resultado = repository.findByStatusAndDataEntrega(StatusPedido.PENDENTE, DIA);

		assertEquals(1, resultado.size());
		assertEquals(pendenteNoDia.getId(), resultado.get(0).getId());
		assertEquals("Ana", resultado.get(0).getCliente());
	}

}
