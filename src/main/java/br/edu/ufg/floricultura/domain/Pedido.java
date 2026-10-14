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

	private static final int CEM_POR_CENTO = 100;
	private static final int ESCALA_MONETARIA = 2;

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

	/**
	 * Cria um pedido pendente, aplicando o desconto de fidelidade da categoria.
	 * O valor final é {@code valorBruto * (100 - desconto) / 100}, com escala 2 e
	 * arredondamento {@link RoundingMode#HALF_UP}.
	 *
	 * @param cliente     nome do cliente (não nulo nem em branco)
	 * @param categoria   categoria de fidelidade
	 * @param anos        anos de fidelidade (zero ou positivo)
	 * @param valorBruto  valor antes do desconto (maior que zero, com no máximo 2 casas decimais
	 *                    significativas); é normalizado para escala 2 sem arredondamento
	 *                    (ex.: {@code 200} e {@code 200.000} viram {@code 200.00}; {@code 10.305} é rejeitado)
	 * @param dataEntrega data de entrega (igual ou posterior a {@code hoje})
	 * @param hoje        data de referência (não nula), injetada para tornar o cálculo determinístico
	 * @return o pedido criado, com status {@link StatusPedido#PENDENTE}
	 * @throws IllegalArgumentException se algum dos dados for inválido
	 * @see CalculadoraDesconto#calcular(Categoria, int)
	 */
	public static Pedido criar(String cliente, Categoria categoria, int anos,
			BigDecimal valorBruto, LocalDate dataEntrega, LocalDate hoje) {
		if (cliente == null || cliente.isBlank()) {
			throw new IllegalArgumentException("Cliente não pode ser nulo ou em branco");
		}
		if (valorBruto == null || valorBruto.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Valor bruto deve ser maior que zero: " + valorBruto);
		}
		if (valorBruto.stripTrailingZeros().scale() > ESCALA_MONETARIA) {
			throw new IllegalArgumentException("Valor bruto aceita no máximo 2 casas decimais: " + valorBruto);
		}
		valorBruto = valorBruto.setScale(ESCALA_MONETARIA);  // sem arredondamento: não há casa significativa a descartar
		if (hoje == null) {
			throw new IllegalArgumentException("Data de referência (hoje) não pode ser nula");
		}
		if (dataEntrega == null || dataEntrega.isBefore(hoje)) {
			throw new IllegalArgumentException("Data de entrega não pode ser nula nem anterior a hoje: " + dataEntrega);
		}
		int desconto = CalculadoraDesconto.calcular(categoria, anos);

		Pedido pedido = new Pedido();
		pedido.cliente = cliente;
		pedido.categoria = categoria;
		pedido.anosFidelidade = anos;
		pedido.valorBruto = valorBruto;
		pedido.descontoPercentual = desconto;
		pedido.valorFinal = aplicarDesconto(valorBruto, desconto);
		pedido.dataEntrega = dataEntrega;
		pedido.status = StatusPedido.PENDENTE;
		return pedido;
	}

	private static BigDecimal aplicarDesconto(BigDecimal valorBruto, int descontoPercentual) {
		return valorBruto.multiply(BigDecimal.valueOf(CEM_POR_CENTO - descontoPercentual))
				.divide(BigDecimal.valueOf(CEM_POR_CENTO), ESCALA_MONETARIA, RoundingMode.HALF_UP);
	}

	/**
	 * Marca o pedido como entregue.
	 *
	 * @throws PedidoJaEntregueException se o pedido já estiver {@link StatusPedido#ENTREGUE}
	 */
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
