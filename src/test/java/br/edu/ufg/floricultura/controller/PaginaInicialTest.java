package br.edu.ufg.floricultura.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class PaginaInicialTest {

	@Autowired
	private MockMvc mockMvc;

	// O Spring Boot serve static/index.html em "/" por forward interno, que o MockMvc não executa:
	// em GET / só dá para conferir o status e o destino do forward; o conteúdo é conferido em /index.html.
	@Test
	void raizEncaminhaParaAPaginaInicial() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("index.html"));
	}

	@Test
	void paginaInicialEhHtmlComOsElementosPrincipais() throws Exception {
		ResultActions resultado = mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

		for (String id : new String[] {
				"form-pedido", "cliente", "categoria", "anos", "valor", "data-entrega",
				"data-filtro", "tabela-entregas", "mensagens" }) {
			resultado.andExpect(content().string(containsString("id=\"" + id + "\"")));
		}
	}

}
