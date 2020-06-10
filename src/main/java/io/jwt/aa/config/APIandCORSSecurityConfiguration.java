package io.jwt.aa.config;

import io.jwt.aa.rest.UserRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration to enable web security, OAuth2, and method security for authorization,
 * see {@link UserRestController#getAuthorizedUserRoles(Authentication)}.
 */
@Configuration
@EnableWebSecurity
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class APIandCORSSecurityConfiguration extends ResourceServerConfigurerAdapter {

    /**
     * "security.oauth2.resource" properties.
     */
    private ResourceServerProperties resourceServerProperties;

    @Autowired
    private OPAAccessDecisionManager opaAccessDecisionManager;

    /**
     * Constructor.
     *
     * @param serverProperties easy access to "security.oauth2.resource.*" entries
     */
    public APIandCORSSecurityConfiguration(final ResourceServerProperties serverProperties) {
        this.resourceServerProperties = serverProperties;
    }

    /**
     * Configures the resource ID.
     *
     * @param configurer resource configurer for security.
     * @throws Exception
     */
    @Override
    public final void configure(final ResourceServerSecurityConfigurer configurer) throws Exception {
        configurer.resourceId(null);
    }

    /**
     * Configures HTTP security.
     *
     * @param httpSecurity http security settings
     * @throws Exception
     */
    @Override
    public final void configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // adds a CORS filter
            .cors()
            .configurationSource(corsConfigurationSource())

            .and()

            // prevent headers to be added
            .headers()
            .frameOptions()
            .disable()

            .and()

            // disable CSRF
            .csrf()
            .disable()

            // enable authorization
            .authorizeRequests()

            // enable authentication for all the paths starting with "/api/"
            .antMatchers("/api/**")
            .authenticated()

            // add voters
            .accessDecisionManager(opaAccessDecisionManager);
    }

    /**
     * Creates a CORS configuration for all paths.
     *
     * @return CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return configurationSource;
    }

    /**
     * Creates the rest template bean.
     *
     * @return Rest Template
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate client = new RestTemplate();
        MappingJackson2HttpMessageConverter jsonHttpMessageConverter = new MappingJackson2HttpMessageConverter();
        client.getMessageConverters().add(jsonHttpMessageConverter);
        return client;
    }

}
