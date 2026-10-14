package br.edu.ufg.floricultura.controller;

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

import br.edu.ufg.floricultura.dto.PedidoRequest;
import br.edu.ufg.floricultura.dto.PedidoResponse;
import br.edu.ufg.floricultura.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService service;

	public PedidoController(PedidoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PedidoResponse> criar(@RequestBody PedidoRequest request) {
		throw new UnsupportedOperationException("Não implementado");
	}

	@GetMapping("/entregas")
	public List<PedidoResponse> listarEntregasPendentes(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
		throw new UnsupportedOperationException("Não implementado");
	}

	@PatchMapping("/{id}/entregue")
	public PedidoResponse marcarEntregue(@PathVariable Long id) {
		throw new UnsupportedOperationException("Não implementado");
	}

}
