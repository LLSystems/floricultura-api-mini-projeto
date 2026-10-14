package br.edu.ufg.floricultura.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail tratarArgumentoInvalido(IllegalArgumentException e) {
		return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail tratarValidacao(MethodArgumentNotValidException e) {
		Map<String, String> campos = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors()
				.forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
				"Um ou mais campos obrigatórios estão ausentes ou em branco");
		problema.setProperty("campos", campos);
		return problema;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail tratarCorpoIlegivel(HttpMessageNotReadableException e) {
		return problema(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
				"JSON malformado ou com valor em formato inválido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail tratarParametroInvalido(MethodArgumentTypeMismatchException e) {
		return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
				"Valor inválido para o parâmetro '" + e.getName() + "': " + e.getValue()
						+ (e.getName().equals("data") ? " (formato esperado: AAAA-MM-DD)" : ""));
	}

	@ExceptionHandler(PedidoNaoEncontradoException.class)
	public ProblemDetail tratarNaoEncontrado(PedidoNaoEncontradoException e) {
		return problema(HttpStatus.NOT_FOUND, "Pedido não encontrado", e.getMessage());
	}

	@ExceptionHandler(PedidoJaEntregueException.class)
	public ProblemDetail tratarJaEntregue(PedidoJaEntregueException e) {
		return problema(HttpStatus.CONFLICT, "Pedido já entregue", e.getMessage());
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setTitle(titulo);
		return problema;
	}

}
