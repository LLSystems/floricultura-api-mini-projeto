package br.edu.ufg.floricultura.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

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
		throw new UnsupportedOperationException("Não implementado");
	}

	public void marcarEntregue() {
		throw new UnsupportedOperationException("Não implementado");
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
