package com.ufrn.bookstore.controller;

import com.ufrn.bookstore.model.Book;
import com.ufrn.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    /**
     * 1. VITRINE DE LIVROS (INDEX)
     */
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        List<Book> listaLivros = bookRepository.findAll();
        model.addAttribute("livros", listaLivros);
        return "index";
    }

    /**
     * 2. EXIBIR FORMULÁRIO DE CADASTRO
     */
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("book", new Book());
        return "cadastro";
    }

    /**
     * 3. SALVAR LIVRO
     */
    @PostMapping("/salvar")
    public String salvarLivro(@ModelAttribute Book livro, RedirectAttributes redirectAttributes) {
        if (livro.getImgUrl() == null || livro.getImgUrl().trim().isEmpty()) {
            livro.setImgUrl("/capa.jpg");
        }

        bookRepository.save(livro);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Livro cadastrado com sucesso!");
        return "redirect:/index";
    }

    /**
     * 4. DETALHE DO LIVRO
     */
    @GetMapping("/detalhe/{id}")
    public String exibirDetalhes(@PathVariable("id") Long id, Model model) {
        Book livro = bookRepository.findById(id).orElse(null);

        if (livro == null) {
            return "redirect:/index";
        }

        model.addAttribute("livro", livro);
        return "detalhe";
    }

    /**
     * 5. PAINEL ADMINISTRATIVO (SOLUÇÃO DO PROBLEMA DE ACESSO)
     * 🛠️ ADICIONADO: Renderiza a listagem de gerenciamento exclusiva para o ADMIN.
     */
    @GetMapping("/admin")
    public String exibirPainelAdmin(Model model) {
        List<Book> listaLivros = bookRepository.findAll();
        model.addAttribute("livros", listaLivros);
        return "admin"; // 🚨 Vai buscar o arquivo src/main/resources/templates/admin.html
    }

    /**
     * 6. ADICIONAR AO CARRINHO
     */
    @GetMapping("/adicionarCarrinho")
    public String adicionarAoCarrinho(@RequestParam("id") Long id, HttpSession session) {
        Book livroSelecionado = bookRepository.findById(id).orElse(null);

        if (livroSelecionado != null) {
            List<Book> carrinho = (List<Book>) session.getAttribute("carrinho");

            if (carrinho == null) {
                carrinho = new ArrayList<>();
            }

            carrinho.add(livroSelecionado);
            session.setAttribute("carrinho", carrinho);
        }

        return "redirect:/index";
    }

    /**
     * 7. VISUALIZAR CARRINHO
     */
    @GetMapping("/verCarrinho")
    public String verCarrinho(HttpSession session, Model model) {
        List<Book> carrinho = (List<Book>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        double totalGeral = 0.0;
        for (Book item : carrinho) {
            if (item.getPrice() != null) {
                totalGeral += item.getPrice();
            }
        }

        model.addAttribute("itensCarrinho", carrinho);
        model.addAttribute("totalGeral", totalGeral);

        return "carrinho";
    }

    /**
     * 8. FINALIZAR COMPRA
     */
    @GetMapping("/finalizarCompra")
    public String finalizarCompra(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("carrinho");
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Compra finalizada com sucesso!");
        return "redirect:/index";
    }
}