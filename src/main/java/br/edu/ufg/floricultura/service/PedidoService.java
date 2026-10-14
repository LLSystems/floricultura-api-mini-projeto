package br.edu.ufg.floricultura.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.repository.PedidoRepository;

@Service
public class PedidoService {

	private final PedidoRepository repository;
	private final Clock clock;

	public PedidoService(PedidoRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public Pedido criar(String cliente, Categoria categoria, int anos,
			BigDecimal valorBruto, LocalDate dataEntrega) {
		throw new UnsupportedOperationException("Não implementado");
	}

	public List<Pedido> listarEntregasPendentes(LocalDate data) {
		throw new UnsupportedOperationException("Não implementado");
	}

	@Transactional
	public Pedido marcarEntregue(Long id) {
		throw new UnsupportedOperationException("Não implementado");
	}

}
