package br.edu.ufg.floricultura.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.domain.StatusPedido;
import br.edu.ufg.floricultura.exception.PedidoNaoEncontradoException;
import br.edu.ufg.floricultura.repository.PedidoRepository;

@Service
public class PedidoService {

	private final PedidoRepository repository;
	private final Clock clock;

	public PedidoService(PedidoRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	/**
	 * Cria e salva um pedido, usando a data atual do {@link Clock} como "hoje".
	 *
	 * @return o pedido salvo
	 * @throws IllegalArgumentException se algum dado for inválido (ver {@link Pedido#criar})
	 */
	public Pedido criar(String cliente, Categoria categoria, int anos,
			BigDecimal valorBruto, LocalDate dataEntrega) {
		Pedido pedido = Pedido.criar(cliente, categoria, anos, valorBruto, dataEntrega, LocalDate.now(clock));
		return repository.save(pedido);
	}

	/**
	 * Lista os pedidos {@link StatusPedido#PENDENTE} com entrega na data informada.
	 *
	 * @param data data de entrega; se nula, usa a data atual do {@link Clock}
	 * @return pedidos pendentes na data (lista vazia se não houver)
	 */
	public List<Pedido> listarEntregasPendentes(LocalDate data) {
		LocalDate dia = (data != null) ? data : LocalDate.now(clock);
		return repository.findByStatusAndDataEntrega(StatusPedido.PENDENTE, dia);
	}

	/**
	 * Marca o pedido como entregue e o salva.
	 *
	 * @param id identificador do pedido
	 * @return o pedido salvo, com status {@link StatusPedido#ENTREGUE}
	 * @throws PedidoNaoEncontradoException se não existir pedido com o id
	 * @throws br.edu.ufg.floricultura.exception.PedidoJaEntregueException se o pedido já estiver entregue
	 */
	@Transactional
	public Pedido marcarEntregue(Long id) {
		Pedido pedido = repository.findById(id)
				.orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));
		pedido.marcarEntregue();
		return repository.save(pedido);
	}

}
