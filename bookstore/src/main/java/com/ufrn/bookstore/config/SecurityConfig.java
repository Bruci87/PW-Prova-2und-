package com.ufrn.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN") // Spring Security adiciona o prefixo "ROLE_" automaticamente aqui ("ROLE_ADMIN")
                .build();

        UserDetails visitante = User.builder()
                .username("visitante")
                .password(encoder.encode("visite123"))
                .roles("VISITANTE")
                .build();

        return new InMemoryUserDetailsManager(admin, visitante);
    }

    // =========================================================================
    // QUESTÃO 15: Filtros de Acesso (RBAC) e FormLogin
    // =========================================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuração das regras de autorização de requisições (Túnel de Filtros)
                .authorizeHttpRequests(authorize -> authorize
                        // Libera arquivos estáticos (CSS, JS, Imagens das capas) para que a tela de login não fique feia/desconfigurada
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/vendor/**").permitAll()

                        // Exigência da Q15: Rotas de cadastro, salvamento, edição, exclusão e restauração exclusivas do ADMIN
                        .requestMatchers("/admin", "/cadastro", "/salvar", "/editar", "/deletar", "/restaurar").hasRole("ADMIN")

                        // Exigência da Q15: Todas as outras rotas do sistema requerem apenas que o usuário esteja autenticado
                        .anyRequest().authenticated()
                )
                // 2. Exigência da Q15: Habilita o formulário de login padrão do Spring Security
                .formLogin(form -> form
                        .defaultSuccessUrl("/index", true) // Após logar com sucesso, joga o usuário direto para a vitrine
                        .permitAll()
                )
                // Habilita o logout padrão para o usuário conseguir deslogar se necessário
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}