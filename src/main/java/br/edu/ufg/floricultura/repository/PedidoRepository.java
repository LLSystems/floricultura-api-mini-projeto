package br.edu.ufg.floricultura.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.domain.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByStatusAndDataEntrega(StatusPedido status, LocalDate dataEntrega);

}
