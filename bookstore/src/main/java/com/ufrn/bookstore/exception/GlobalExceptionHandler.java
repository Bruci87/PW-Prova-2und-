package com.ufrn.bookstore.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // Avisa ao Spring que esta classe cuida dos erros do site inteiro
public class GlobalExceptionHandler {

    // Captura o erro da Questão 9 quando um livro não é encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {

        // Pega a mensagem de erro ("Livro com o ID X não foi encontrado.")
        String mensagemErro = ex.getMessage();

        // Passa a mensagem para a tela HTML, escondendo as linhas de erro do servidor (Stack Trace)
        model.addAttribute("mensagem", mensagemErro);

        // Manda o usuário para a tela erro.html
        return "erro";
    }
}