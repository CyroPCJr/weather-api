// package com.weatherworld.security
//
// import org.springframework.context.annotation.Bean
// import org.springframework.context.annotation.Configuration
// import org.springframework.security.config.Customizer
// import org.springframework.security.config.annotation.web.builders.HttpSecurity
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
// import org.springframework.security.web.SecurityFilterChain
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher
//
// @Configuration
// @EnableWebSecurity
// class SecurityConfig {
//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .authorizeHttpRequests { auth ->
//                auth
//                    .requestMatchers(AntPathRequestMatcher("/api/weather/**"))
//                    .permitAll()
//                    .anyRequest()
//                    .authenticated()
//            }.csrf { it.disable() } // Forma correta no Spring Security 6+
//            .httpBasic(Customizer.withDefaults()) // Pode remover se não quiser basic auth
//
//        return http.build()
//    }
// }
