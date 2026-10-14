package br.edu.ufg.floricultura.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.dto.PedidoRequest;
import br.edu.ufg.floricultura.dto.PedidoResponse;
import br.edu.ufg.floricultura.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService service;

	public PedidoController(PedidoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
		Pedido pedido = service.criar(
				request.cliente(),
				Categoria.deTexto(request.categoria()),
				request.anosFidelidade(),
				request.valorBruto(),
				request.dataEntrega());
		return ResponseEntity.created(URI.create("/pedidos/" + pedido.getId()))
				.body(PedidoResponse.from(pedido));
	}

	@GetMapping("/entregas")
	public List<PedidoResponse> listarEntregasPendentes(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
		return service.listarEntregasPendentes(data).stream()
				.map(PedidoResponse::from)
				.toList();
	}

	@PatchMapping("/{id}/entregue")
	public PedidoResponse marcarEntregue(@PathVariable Long id) {
		return PedidoResponse.from(service.marcarEntregue(id));
	}

}
