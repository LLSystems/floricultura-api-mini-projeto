package br.edu.ufg.floricultura.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.ufg.floricultura.domain.Categoria;
import br.edu.ufg.floricultura.domain.Pedido;
import br.edu.ufg.floricultura.exception.PedidoJaEntregueException;
import br.edu.ufg.floricultura.exception.PedidoNaoEncontradoException;
import br.edu.ufg.floricultura.service.PedidoService;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 14);
	private static final LocalDate DATA_ENTREGA = LocalDate.of(2026, 10, 20);

	private static final String JSON_VALIDO = """
			{
			  "cliente": "Maria",
			  "categoria": "ouro",
			  "anosFidelidade": 5,
			  "valorBruto": 200.00,
			  "dataEntrega": "2026-10-20"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PedidoService service;

	private static Pedido pedidoComId(Long id) {
		Pedido pedido = Pedido.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), DATA_ENTREGA, HOJE);
		ReflectionTestUtils.setField(pedido, "id", id);
		return pedido;
	}

	// ---- POST /pedidos ----

	@Test
	void postValidoResponde201ComLocationECorpo() throws Exception {
		when(service.criar("Maria", Categoria.OURO, 5, new BigDecimal("200.00"), DATA_ENTREGA))
				.thenReturn(pedidoComId(1L));

		mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/pedidos/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.cliente").value("Maria"))
				.andExpect(jsonPath("$.categoria").value("OURO"))
				.andExpect(jsonPath("$.anosFidelidade").value(5))
				.andExpect(jsonPath("$.valorBruto").value(200.00))
				.andExpect(jsonPath("$.descontoPercentual").value(25))
				.andExpect(jsonPath("$.valorFinal").value(150.00))
				.andExpect(jsonPath("$.dataEntrega").value("2026-10-20"))
				.andExpect(jsonPath("$.status").value("PENDENTE"));
	}

	@Test
	void postComCamposAusentesResponde400ComErroPorCampo() throws Exception {
		mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.campos.cliente").exists())
				.andExpect(jsonPath("$.campos.categoria").exists())
				.andExpect(jsonPath("$.campos.anosFidelidade").exists())
				.andExpect(jsonPath("$.campos.valorBruto").exists())
				.andExpect(jsonPath("$.campos.dataEntrega").exists());

		verifyNoInteractions(service);
	}

	@Test
	void postComCategoriaInvalidaResponde400() throws Exception {
		String json = JSON_VALIDO.replace("\"ouro\"", "\"bronze\"");

		mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value(containsString("bronze")));

		verifyNoInteractions(service);
	}

	@Test
	void postQuandoServiceLancaIllegalArgumentResponde400() throws Exception {
		when(service.criar(any(), any(), anyInt(), any(), any()))
				.thenThrow(new IllegalArgumentException("Data de entrega não pode ser nula nem anterior a hoje: 2026-10-20"));

		mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("Data de entrega não pode ser nula nem anterior a hoje: 2026-10-20"));
	}

	@Test
	void postComJsonMalformadoResponde400() throws Exception {
		mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content("{\"cliente\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").exists());

		verifyNoInteractions(service);
	}

	// ---- GET /pedidos/entregas ----

	@Test
	void getSemDataChamaServiceComNull() throws Exception {
		when(service.listarEntregasPendentes(null)).thenReturn(List.of(pedidoComId(1L)));

		mockMvc.perform(get("/pedidos/entregas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1));

		verify(service).listarEntregasPendentes(null);
	}

	@Test
	void getComDataChamaServiceComADataInformada() throws Exception {
		when(service.listarEntregasPendentes(DATA_ENTREGA)).thenReturn(List.of(pedidoComId(1L), pedidoComId(2L)));

		mockMvc.perform(get("/pedidos/entregas").param("data", "2026-10-20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(service).listarEntregasPendentes(LocalDate.of(2026, 10, 20));
	}

	@Test
	void getComDataMalformadaResponde400() throws Exception {
		mockMvc.perform(get("/pedidos/entregas").param("data", "20-10-2026"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value(containsString("data")));

		verifyNoInteractions(service);
	}

	// ---- PATCH /pedidos/{id}/entregue ----

	@Test
	void patchValidoResponde200ComStatusEntregue() throws Exception {
		Pedido entregue = pedidoComId(1L);
		entregue.marcarEntregue();
		when(service.marcarEntregue(1L)).thenReturn(entregue);

		mockMvc.perform(patch("/pedidos/1/entregue"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("ENTREGUE"));
	}

	@Test
	void patchDePedidoInexistenteResponde404() throws Exception {
		when(service.marcarEntregue(99L)).thenThrow(new PedidoNaoEncontradoException("Pedido não encontrado: 99"));

		mockMvc.perform(patch("/pedidos/99/entregue"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Pedido não encontrado: 99"));
	}

	@Test
	void patchDePedidoJaEntregueResponde409() throws Exception {
		when(service.marcarEntregue(1L)).thenThrow(new PedidoJaEntregueException("Pedido já foi entregue: 1"));

		mockMvc.perform(patch("/pedidos/1/entregue"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("Pedido já foi entregue: 1"));
	}

}
