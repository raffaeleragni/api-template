package app.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfiguration {

  @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      // .csrf().disable() //NOSONAR
      .authorizeRequests() //NOSONAR
      .antMatchers("/health/**").permitAll()
      .anyRequest().permitAll()
      .and()
      .addFilterBefore(new KongFilter(), AbstractPreAuthenticatedProcessingFilter.class)
      .build();
  }
}

class KongFilter extends GenericFilterBean {
  @Override
  public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    var request = (HttpServletRequest) req;
    try {
      var authentication = TokenParser.parse(request.getHeader("Authorization"));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    }
  }
}
