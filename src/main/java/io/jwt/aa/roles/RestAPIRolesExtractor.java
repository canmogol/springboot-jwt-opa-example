package io.jwt.aa.roles;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.jwt.aa.roles.ams.AMSClient;
import io.jwt.aa.roles.ams.models.ControllerRoles;
import io.jwt.aa.roles.ams.models.MethodRoles;
import io.jwt.aa.roles.ams.models.ServiceRoles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extracts roles from REST API.
 */
@Slf4j
@Component
public class RestAPIRolesExtractor implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private static final String EMPTY_STRING = "";
    private static final String[] EMPTY_STRINGS = new String[]{};
    private static final String HAS_ANY_AUTHORITY = "hasAnyAuthority(";
    private static final String HAS_AUTHORITY = "hasAuthority(";
    private static final String SUFFIX = ")";

    private ApplicationContext applicationContext;

    @Value("#{'${service.roles}'.split(',')}")
    private List<String> serviceRoleList;

    @Value("${service.name}")
    private String serviceName;

    @Value("${service.description}")
    private String serviceDescription;

    @Value("${ams.service.url}")
    private String amsServiceURL;


    /**
     * Handles context refresh events.
     *
     * @param event context refreshed event
     */
    @Override
    public final void onApplicationEvent(final ContextRefreshedEvent event) {
        registerRoles();
    }

    /**
     * Registers all the REST API and related Roles to OAuth2 backend.
     */
    private void registerRoles() {
        ServiceRoles serviceRoles = new ServiceRoles();
        setServiceRoles(serviceRoles);
        setControllerRoles(serviceRoles);
        registerRolesToAMS(serviceRoles);
    }

    private void registerRolesToAMS(final ServiceRoles serviceRoles) {
        AMSClient amsClient = Feign.builder()
            .client(new OkHttpClient())
            .encoder(new GsonEncoder())
            .decoder(new GsonDecoder())
            .logger(new Slf4jLogger(AMSClient.class))
            .logLevel(Logger.Level.BASIC)
            .target(AMSClient.class, amsServiceURL);
        try {
            amsClient.register(serviceRoles);
        } catch (Exception e) {
            log.error(String.format("Got error while sending Register Request to AMS, error: %s", e.getMessage()));
        }
    }

    private void setServiceRoles(final ServiceRoles serviceRoles) {
        serviceRoles.setServiceName(serviceName);
        serviceRoles.setServiceDescription(serviceDescription);
        serviceRoles.setRoleList(serviceRoleList);
    }

    private void setControllerRoles(final ServiceRoles serviceRoles) {
        List<ControllerRoles> controllerRoles = applicationContext.getBeansWithAnnotation(RestController.class)
            .entrySet()
            .stream()
            .map(pair -> getRolesForController(pair.getKey(), pair.getValue()))
            .collect(Collectors.toList());
        serviceRoles.setControllerRoles(controllerRoles);
    }

    /**
     * Registers roles for a single Controller.
     *
     * @param controllerBeanName contoller's name
     * @param controllerBean     contoller object
     */
    private ControllerRoles getRolesForController(final String controllerBeanName, final Object controllerBean) {
        ControllerRoles controllerRoles = new ControllerRoles();
        controllerRoles.setControllerName(controllerBeanName);
        controllerRoles.setControllerDescription(getControllerDescription(controllerBean));
        controllerRoles.setRoles(getControllerRoles(controllerBean));
        controllerRoles.setMethodRoles(getMethodRoles(controllerBean));
        return controllerRoles;
    }

    private List<MethodRoles> getMethodRoles(final Object controllerBean) {
        Method[] methods = Optional.ofNullable(((Advised) controllerBean).getTargetSource().getTargetClass())
            .map(Class::getMethods)
            .orElse(new Method[]{});
        return Stream.of(methods)
            .filter(m -> m.getAnnotation(PreAuthorize.class) != null)
            .map(this::getMethodRoles)
            .collect(Collectors.toList());
    }

    private MethodRoles getMethodRoles(final Method m) {
        String name = m.getName();
        String description = Optional.ofNullable(m.getAnnotation(ApiOperation.class))
            .map(ApiOperation::value)
            .orElse(EMPTY_STRING);
        String value = Optional.ofNullable(m.getAnnotation(PreAuthorize.class))
            .map(PreAuthorize::value)
            .orElse(EMPTY_STRING);
        List<String> roles = getRoles(value);
        return new MethodRoles(name, description, roles);
    }

    private List<String> getControllerRoles(final Object controllerBean) {
        String value = Optional.ofNullable(controllerBean.getClass().getAnnotation(PreAuthorize.class))
            .map(PreAuthorize::value)
            .orElse(EMPTY_STRING);
        return getRoles(value);
    }

    private List<String> getRoles(final String value) {
        String authorities = EMPTY_STRING;
        if (value.startsWith(HAS_ANY_AUTHORITY) && value.endsWith(SUFFIX)) {
            authorities = value.substring(HAS_ANY_AUTHORITY.length(), value.length() - SUFFIX.length());
        } else if (value.startsWith(HAS_AUTHORITY) && value.endsWith(SUFFIX)) {
            authorities = value.substring(HAS_AUTHORITY.length(), value.length() - SUFFIX.length());
        }
        return Stream.of(authorities)
            .map(s -> s.replaceAll("'", ""))
            .flatMap(s -> Stream.of(s.split(",")))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private String getControllerDescription(final Object controllerBean) {
        String[] tags = Optional.ofNullable(controllerBean.getClass().getAnnotation(Api.class))
            .map(Api::tags)
            .orElse(EMPTY_STRINGS);
        return String.join(", ", tags);
    }

    /**
     * Sets the application context.
     *
     * @param context spring application context
     */
    @Override
    public final void setApplicationContext(final ApplicationContext context) {
        this.applicationContext = context;
    }

}
