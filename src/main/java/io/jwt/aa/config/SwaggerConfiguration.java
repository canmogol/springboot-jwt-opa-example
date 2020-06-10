package io.jwt.aa.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

/**
 * Swagger configuration.
 */
@Configuration
@EnableSwagger2
@Import(springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class)
public class SwaggerConfiguration {


    @Value("${api.path}")
    private String api;
    @Value("${swagger.title}")
    private String title;
    @Value("${swagger.description}")
    private String description;
    @Value("${swagger.version}")
    private String version;
    @Value("${swagger.termsUrl}")
    private String termsOfServiceUrl;
    @Value("${swagger.contact.name}")
    private String contactName;
    @Value("${swagger.contact.url}")
    private String contactUrl;
    @Value("${swagger.contact.email}")
    private String contactEmail;
    @Value("${swagger.license.name}")
    private String license;
    @Value("${swagger.license.url}")
    private String licenseUrl;

    public static final String JWT = "JWT";
    public static final String HEADER = "header";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String GLOBAL = "global";
    public static final String ACCESS_EVERYTHING = "accessEverything";
    public static final String API_PATH = "/%s/.*";


    /**
     * Creates the Docket bean for configuration.
     *
     * @return Docket
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            // other then the "Basic Error Controller", basic-error-controller
            .paths(Predicates.not(PathSelectors.regex("/error.*")))
            .build()
            .apiInfo(apiInfo())
            .securityContexts(Collections.singletonList(securityContext()))
            .securitySchemes(Collections.singletonList(apiKey()))
            .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            title,
            description,
            version,
            termsOfServiceUrl,
            new Contact(contactName, contactUrl, contactEmail),
            license, licenseUrl, Collections.emptyList());
    }

    private ApiKey apiKey() {
        return new ApiKey(JWT, AUTHORIZATION_HEADER, HEADER);
    }

    private SecurityContext securityContext() {
        String pathRegex = String.format(API_PATH, api);
        return SecurityContext.builder()
            .securityReferences(securityReferences())
            .forPaths(PathSelectors.regex(pathRegex))
            .build();
    }

    private List<SecurityReference> securityReferences() {
        AuthorizationScope authorizationScope = new AuthorizationScope(GLOBAL, ACCESS_EVERYTHING);
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return Collections.singletonList(new SecurityReference(JWT, authorizationScopes));
    }
}
