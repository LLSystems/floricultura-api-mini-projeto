package br.edu.ufg.floricultura.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para criar um pedido. Valida apenas a presença dos campos;
 * as regras de negócio (valor, data, anos, categoria) ficam no domínio.
 */
public record PedidoRequest(
		@NotBlank String cliente,
		@NotBlank String categoria,
		@NotNull Integer anosFidelidade,
		@NotNull BigDecimal valorBruto,
		@NotNull LocalDate dataEntrega) {
}
