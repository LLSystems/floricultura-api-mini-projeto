package br.edu.ufg.floricultura.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import br.edu.ufg.floricultura.exception.PedidoJaEntregueException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String cliente;

	@Enumerated(EnumType.STRING)
	private Categoria categoria;

	private int anosFidelidade;

	private BigDecimal valorBruto;

	private int descontoPercentual;

	private BigDecimal valorFinal;

	private LocalDate dataEntrega;

	@Enumerated(EnumType.STRING)
	private StatusPedido status;

	protected Pedido() {
	}

	public static Pedido criar(String cliente, Categoria categoria, int anos,
			BigDecimal valorBruto, LocalDate dataEntrega, LocalDate hoje) {
		if (cliente == null || cliente.isBlank()) {
			throw new IllegalArgumentException("Cliente não pode ser nulo ou em branco");
		}
		if (valorBruto == null || valorBruto.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Valor bruto deve ser maior que zero: " + valorBruto);
		}
		if (dataEntrega == null || dataEntrega.isBefore(hoje)) {
			throw new IllegalArgumentException("Data de entrega não pode ser anterior a hoje: " + dataEntrega);
		}
		int desconto = CalculadoraDesconto.calcular(categoria, anos);

		Pedido pedido = new Pedido();
		pedido.cliente = cliente;
		pedido.categoria = categoria;
		pedido.anosFidelidade = anos;
		pedido.valorBruto = valorBruto;
		pedido.descontoPercentual = desconto;
		pedido.valorFinal = valorBruto.multiply(BigDecimal.valueOf(100 - desconto))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		pedido.dataEntrega = dataEntrega;
		pedido.status = StatusPedido.PENDENTE;
		return pedido;
	}

	public void marcarEntregue() {
		if (status == StatusPedido.ENTREGUE) {
			throw new PedidoJaEntregueException("Pedido já foi entregue: " + id);
		}
		status = StatusPedido.ENTREGUE;
	}

	public Long getId() {
		return id;
	}

	public String getCliente() {
		return cliente;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public int getAnosFidelidade() {
		return anosFidelidade;
	}

	public BigDecimal getValorBruto() {
		return valorBruto;
	}

	public int getDescontoPercentual() {
		return descontoPercentual;
	}

	public BigDecimal getValorFinal() {
		return valorFinal;
	}

	public LocalDate getDataEntrega() {
		return dataEntrega;
	}

	public StatusPedido getStatus() {
		return status;
	}

}
