package com.ufrn.bookstore.controller;

import com.ufrn.bookstore.dto.BookFormDTO;
import com.ufrn.bookstore.exception.ResourceNotFoundException;
import com.ufrn.bookstore.model.Book;
import com.ufrn.bookstore.repository.BookRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class BookController {

    // Lista de imagens pré-existentes para o sorteio no cadastro de novos livros (Q8)
    private static final String[] IMAGENS_PRE_EXISTENTES = {
            "/img/book1.png",
            "/img/book2.png",
            "/img/book3.png",
            "/img/book4.png"
    };

    @Autowired
    private BookRepository bookRepository;

    // =========================================================================
    // QUESTÃO 4 & 11: Vitrine de Livros Ativos e Contador do Carrinho
    // =========================================================================
    @GetMapping("/index")
    public String index(Model model, HttpSession session) {
        // Filtra apenas os livros que não foram excluídos logicamente
        List<Book> livrosAtivos = bookRepository.findByIsDeletedIsNull();
        model.addAttribute("livros", livrosAtivos);

        // Recupera ou inicializa o contador do carrinho na barra de navegação
        List<Book> carrinho = (List<Book>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }
        model.addAttribute("qtdCarrinho", carrinho.size());

        return "index";
    }

    // =========================================================================
    // QUESTÃO 5: Rota do Painel Administrativo
    // =========================================================================
    @GetMapping("/admin")
    public String admin(Model model) {
        // O administrador visualiza todos os livros (inclusive os excluídos logicamente)
        List<Book> todosOsLivros = bookRepository.findAll();
        model.addAttribute("livros", todosOsLivros);

        return "admin";
    }

    // =========================================================================
    // QUESTÃO 6: Exibir Formulário de Cadastro
    // =========================================================================
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("bookFormDTO", new BookFormDTO());
        return "cadastro";
    }

    // =========================================================================
    // QUESTÃO 7: Exibir Formulário de Edição (Preenchido)
    // =========================================================================
    @GetMapping("/editar")
    public String editar(@RequestParam Long id, Model model) {
        Book livro = bookRepository.findById(id).orElse(null);

        if (livro == null) {
            return "redirect:/admin";
        }

        // Transfere os dados da Entidade para o DTO que vai preencher o formulário
        BookFormDTO dto = new BookFormDTO();
        dto.setId(livro.getId());
        dto.setTitle(livro.getTitle());
        dto.setAuthor(livro.getAuthor());
        dto.setIsbn(livro.getIsbn());
        dto.setPrice(livro.getPrice());
        dto.setCategory(livro.getCategory());

        model.addAttribute("bookFormDTO", dto);
        return "cadastro";
    }

    // =========================================================================
    // QUESTÃO 8: Rota Unificada para Salvar (Cadastro e Edição)
    // =========================================================================
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("bookFormDTO") BookFormDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        // Se houver erros de validação (ex: campos em branco), retorna à tela de cadastro
        if (bindingResult.hasErrors()) {
            return "cadastro";
        }

        Book livro;

        if (dto.getId() != null) {
            // Modo Edição: busca o livro existente para manter a mesma imagem/histórico
            livro = bookRepository.findById(dto.getId()).orElse(new Book());
            if (livro.getId() == null) {
                return "redirect:/admin";
            }
        } else {
            // Modo Cadastro Novo: instancia um novo objeto e sorteia uma capa aleatória
            livro = new Book();

            Random random = new Random();
            int indexAleatorio = random.nextInt(IMAGENS_PRE_EXISTENTES.length);
            livro.setImgUrl(IMAGENS_PRE_EXISTENTES[indexAleatorio]);
        }

        // Atualiza os atributos da entidade com os dados validados do DTO
        livro.setTitle(dto.getTitle());
        livro.setAuthor(dto.getAuthor());
        livro.setIsbn(dto.getIsbn());
        livro.setCategory(dto.getCategory());
        livro.setPrice(dto.getPrice());

        bookRepository.save(livro);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Livro salvo com sucesso!");
        return "redirect:/admin";
    }

    // =========================================================================
    // QUESTÃO 10: Rota de Remoção Lógica (Soft Delete)
    // =========================================================================
    @GetMapping("/deletar")
    public String deletar(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Book livro = bookRepository.findById(id).orElse(null);

        if (livro != null) {
            // Grava o timestamp atual (Long) para indicar que foi removido logicamente
            livro.setIsDeleted(System.currentTimeMillis());
            bookRepository.save(livro);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Livro removido logicamente com sucesso!");
        }

        // Redireciona para a vitrine (/index) conforme exigido no enunciado da Q10
        return "redirect:/index";
    }

    // =========================================================================
    // QUESTÃO 10: Rota de Restauração do Item Excluído
    // =========================================================================
    @GetMapping("/restaurar")
    public String restaurar(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Book livro = bookRepository.findById(id).orElse(null);

        if (livro != null) {
            // Define isDeleted como null para que volte a aparecer na vitrine ativa
            livro.setIsDeleted(null);
            bookRepository.save(livro);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Livro restaurado com sucesso!");
        }

        return "redirect:/admin";
    }

    // =========================================================================
    // QUESTÃO 9: Rota de Detalhes Dinâmicos com PathVariable
    // =========================================================================
    @GetMapping("/detalhe/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        // Se o ID não for encontrado, estoura a exceção de negócio customizada (HTTP 404)
        Book livro = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro com o ID " + id + " não foi encontrado."));

        model.addAttribute("livro", livro);
        return "detalhe";
    }

    // =========================================================================
    // QUESTÃO 11: Adicionar Item ao Carrinho de Compras (Session)
    // =========================================================================
    @GetMapping("/adicionarCarrinho")
    public String adicionarCarrinho(@RequestParam Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Book livro = bookRepository.findById(id).orElse(null);

        if (livro != null) {
            List<Book> carrinho = (List<Book>) session.getAttribute("carrinho");

            // Inicializa a lista na sessão se for a primeira inclusão do usuário
            if (carrinho == null) {
                carrinho = new ArrayList<>();
            }

            carrinho.add(livro);
            session.setAttribute("carrinho", carrinho);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "O livro '" + livro.getTitle() + "' foi adicionado ao carrinho!");
        }

        return "redirect:/index";
    }

    // =========================================================================
    // QUESTÃO 12: Visualizar Itens do Carrinho de Compras
    // =========================================================================
    @GetMapping("/verCarrinho")
    public String verCarrinho(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<Book> carrinho = (List<Book>) session.getAttribute("carrinho");

        // Se o carrinho estiver nulo ou vazio, barra o acesso e redireciona para a vitrine
        if (carrinho == null || carrinho.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Seu carrinho está vazio! Adicione algum produto antes de visualizá-lo.");
            return "redirect:/index";
        }

        // Calcula dinamicamente o valor total acumulado dos livros na sessão
        double totalGeral = carrinho.stream().mapToDouble(Book::getPrice).sum();

        model.addAttribute("itensCarrinho", carrinho);
        model.addAttribute("totalGeral", totalGeral);

        return "carrinho";
    }

    // =========================================================================
    // QUESTÃO 13: Finalizar Compra (Invalida a Sessão HTTP)
    // =========================================================================
    @GetMapping("/finalizarCompra")
    public String finalizarCompra(HttpSession session, RedirectAttributes redirectAttributes) {
        // Destrói a sessão atual, limpando completamente os dados em memória (esvazia o carrinho)
        session.invalidate();

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Compra finalizada com sucesso! Seu pedido foi processado.");
        return "redirect:/index";
    }
}