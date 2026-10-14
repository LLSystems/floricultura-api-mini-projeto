package br.edu.ufg.floricultura.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.domain.StatusPedido;

public record PedidoResponse(
		Long id,
		String cliente,
		Categoria categoria,
		int anosFidelidade,
		BigDecimal valorBruto,
		int descontoPercentual,
		BigDecimal valorFinal,
		LocalDate dataEntrega,
		StatusPedido status) {

	public static PedidoResponse from(Pedido pedido) {
		return new PedidoResponse(
				pedido.getId(),
				pedido.getCliente(),
				pedido.getCategoria(),
				pedido.getAnosFidelidade(),
				pedido.getValorBruto(),
				pedido.getDescontoPercentual(),
				pedido.getValorFinal(),
				pedido.getDataEntrega(),
				pedido.getStatus());
	}

}
