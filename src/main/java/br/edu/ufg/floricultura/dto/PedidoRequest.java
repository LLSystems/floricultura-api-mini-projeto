package br.edu.ufg.floricultura.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoRequest(
		String cliente,
		String categoria,
		Integer anosFidelidade,
		BigDecimal valorBruto,
		LocalDate dataEntrega) {
}
