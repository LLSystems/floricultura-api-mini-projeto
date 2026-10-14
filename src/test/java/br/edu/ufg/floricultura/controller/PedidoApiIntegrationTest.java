package br.edu.ufg.floricultura.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Clock clock;

	private static void mostrar(String etapa, MvcResult resultado) throws Exception {
		System.out.printf("[INTEGRACAO] %s -> %d %s%n", etapa,
				resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
	}

	@Test
	void fluxoCompletoCriarListarEntregarListarEEntregarDeNovo() throws Exception {
		// mesmo Clock que o serviço usa, para "amanhã" ficar alinhado com o "hoje" da aplicação
		String amanha = LocalDate.now(clock).plusDays(1).toString();
		String json = """
				{
				  "cliente": "Floricultura Integração",
				  "categoria": " Ouro ",
				  "anosFidelidade": 5,
				  "valorBruto": 200.00,
				  "dataEntrega": "%s"
				}
				""".formatted(amanha);

		// 1. cria
		MvcResult criado = mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.categoria").value("OURO"))
				.andExpect(jsonPath("$.descontoPercentual").value(25))
				.andExpect(jsonPath("$.valorFinal").value(150.00))
				.andExpect(jsonPath("$.dataEntrega").value(amanha))
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andReturn();
		mostrar("POST /pedidos", criado);
		Integer id = JsonPath.read(criado.getResponse().getContentAsString(), "$.id");
		assertEquals("/pedidos/" + id, criado.getResponse().getHeader("Location"));

		// 2. lista as entregas do dia
		MvcResult lista = mockMvc.perform(get("/pedidos/entregas").param("data", amanha))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[*].id", hasItem(id)))
				.andReturn();
		mostrar("GET /pedidos/entregas?data=" + amanha, lista);

		// 3. marca como entregue
		MvcResult entregue = mockMvc.perform(patch("/pedidos/{id}/entregue", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ENTREGUE"))
				.andReturn();
		mostrar("PATCH /pedidos/" + id + "/entregue", entregue);

		// 4. lista de novo: vazia
		MvcResult listaVazia = mockMvc.perform(get("/pedidos/entregas").param("data", amanha))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0))
				.andReturn();
		mostrar("GET /pedidos/entregas?data=" + amanha, listaVazia);

		// 5. marca de novo: 409
		MvcResult conflito = mockMvc.perform(patch("/pedidos/{id}/entregue", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andReturn();
		mostrar("PATCH /pedidos/" + id + "/entregue (de novo)", conflito);
	}

}
