package com.github.jackson.oauth2client.configures

import com.github.jackson.oauth2client.jwt.Jwt
import com.github.jackson.oauth2client.jwt.JwtAuthenticationTokenFilter
import com.github.jackson.oauth2client.oauth2.OAuth2AuthenticationSuccessHandler
import com.github.jackson.oauth2client.user.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextPersistenceFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfigure : WebSecurityConfigurerAdapter() {

    @Bean
    fun jwt(configure: JwtConfigure): Jwt = Jwt(configure.secretKey!!, configure.issuer, configure.expirySeconds)

    @Bean
    fun jwtAuthenticationTokenFilter(jwt: Jwt, configure: JwtConfigure): JwtAuthenticationTokenFilter =
        JwtAuthenticationTokenFilter(jwt, configure.headerKey)

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun oauth2AuthenticationSuccessHandler(jwt: Jwt, userService: UserService): OAuth2AuthenticationSuccessHandler =
        OAuth2AuthenticationSuccessHandler(jwt, userService)

    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
                .antMatchers("/api/user/me").hasAnyRole("USER")
                .anyRequest().permitAll()
            .and()
            .formLogin()
                .disable()
            .csrf()
                .disable()
            .headers()
                .disable()
            .httpBasic()
                .disable()
            .rememberMe()
                .disable()
            .logout()
                .disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .oauth2Login()
                .successHandler(applicationContext.getBean(OAuth2AuthenticationSuccessHandler::class.java))
                .and()
            http.addFilterAfter(
                applicationContext.getBean(JwtAuthenticationTokenFilter::class.java),
                SecurityContextPersistenceFilter::class.java
            )
    }

}