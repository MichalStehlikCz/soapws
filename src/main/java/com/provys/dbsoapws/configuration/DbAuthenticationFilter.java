package com.provys.dbsoapws.configuration;


import com.provys.auth.api.AuthProviderLookup;
import com.provys.auth.none.NoneAuthenticationConverter;
import com.provys.auth.none.NoneAuthenticationToken;
import com.provys.common.exception.InternalException;
import com.provys.dbsoapws.model.EndpointDefinition;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

public class DbAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOG = LogManager.getLogger(DbAuthenticationFilter.class);

  private final Map<String, AuthenticationExecutor> executorsByPath;

  private static Map<String, AuthenticationExecutor> buildExecutorMap(
      ServiceDefinition serviceDefinition,
      AuthProviderLookup authProviderLookup) {
    return serviceDefinition.getEndpoints().stream()
        .collect(Collectors.toUnmodifiableMap(
            EndpointDefinition::getPath,
            endpoint -> new AuthenticationExecutor(endpoint.getName(), endpoint.getAuthProvider(),
                authProviderLookup)));
  }

  DbAuthenticationFilter(ServiceDefinition serviceDefinition,
      AuthProviderLookup authProviderLookup) {
    executorsByPath = buildExecutorMap(serviceDefinition, authProviderLookup);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    // retrieve authentication converter and provider for given uri
    var path = request.getServletPath()
        + (request.getPathInfo() == null ? "" : request.getPathInfo());
    if (!("GET".equals(request.getMethod()) && (path.endsWith(".wsdl") || path.endsWith(".xsd")))) {
      var executor = executorsByPath.get(path);
      if (executor != null) {
        if (!executor.authenticate(request, response)) {
          return;
        }
      } else {
        LOG.info("Authentication configuration not found for path {}", path);
      }
    }
    chain.doFilter(request, response);
  }

  /**
   * Does actual authentication.
   */
  private static class AuthenticationExecutor {

    private final String name;
    private final AuthenticationConverter converter;
    private final AuthenticationProvider provider;
    private final @Nullable AuthenticationEntryPoint entryPoint;

    AuthenticationExecutor(
        String name,
        AuthenticationConverter converter,
        AuthenticationProvider provider,
        @Nullable AuthenticationEntryPoint entryPoint) {
      this.name = name;
      this.converter = converter;
      this.provider = provider;
      this.entryPoint = entryPoint;
    }

    AuthenticationExecutor(String name, String authProvider,
        AuthProviderLookup authProviderLookup) {
      this.name = name;
      this.provider = authProviderLookup.getAuthProvider(authProvider);
      if (provider.supports(NoneAuthenticationToken.class)) {
        // doesn't need any specific information
        this.converter = NoneAuthenticationConverter.getInstance();
        this.entryPoint = null;
      } else if (provider.supports(UsernamePasswordAuthenticationToken.class)) {
        this.converter = new BasicAuthenticationConverter();
        this.entryPoint = new BasicAuthenticationEntryPoint();
        ((BasicAuthenticationEntryPoint) this.entryPoint).setRealmName("Realm");
      } else {
        throw new InternalException("Cannot set up endpoint " + name + "with provider " + provider
            + "; no supported converter can extract credentials for configured provider");
      }
    }

    @SuppressWarnings("nullness") // null is ok - library not annotated
    private boolean onFailure(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
      if (entryPoint != null) {
        entryPoint.commence(request, response, null);
      } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }
      return false;
    }

    /**
     * Do authentication based on settings of this executor.
     *
     * @param request  is request to be authenticated
     * @param response is response, that can be used to indicate authentication failure
     * @return true if authentication was successful and processing should continue using next
     *     filter, false otherwise
     */
    boolean authenticate(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
      Authentication authResult;
      // try to get authentication
      try {
        var authSource = converter.convert(request);
        if (authSource == null) {
          /*
           Credentials were not retrieved - we cannot authenticate, thus continue with request
           processing
           */
          LOG.debug("No credentials found, endpoint {}, converter {}", name, converter);
          return onFailure(request, response);
        }
        authResult = provider.authenticate(authSource);
        if (authResult == null) {
          LOG.info("Empty authentication result, endpoint {}, provider {}", name, provider);
          return onFailure(request, response);
        }
      } catch (AuthenticationException e) {
        LOG.info("Authentication failed, endpoint {}, provider {}", name, provider);
        return onFailure(request, response);
      }
      // and set authentication to security context
      try {
        SecurityContextHolder.getContext().setAuthentication(authResult);
      } catch (AccessDeniedException e) {
        LOG.info("Failed to set authentication to security context, endpoint {}", name);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "authenticationParameters{"
          + " name=" + name
          + ", converter=" + converter
          + ", provider=" + provider
          + ", entryPoint=" + entryPoint
          + '}';
    }

  }

  @Override
  public String toString() {
    return "DbAuthenticationFilter{"
        + "executorsByPath=" + executorsByPath
        + '}';
  }
}
