package com.ufrn.bookstore.repository;

// O SEU ERRO ESTÁ AQUI: Certifique-se de que o import aponta para com.ufrn.bookstore
import com.ufrn.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByIsDeletedIsNull();
}