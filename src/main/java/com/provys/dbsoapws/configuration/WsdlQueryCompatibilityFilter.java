package com.provys.dbsoapws.configuration;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class WsdlQueryCompatibilityFilter implements Filter {

  @Override
  public void init(final FilterConfig filterConfig) {
    // do nothing
  }

  private static class WsdlQueryRequestWrapper extends HttpServletRequestWrapper {

    WsdlQueryRequestWrapper(HttpServletRequest httpServletRequest) {
      super(httpServletRequest);
    }

    @Override
    @SuppressWarnings("override.return.invalid") // base library not annotated
    public @Nullable String getQueryString() {
      return null;
    }

    @Override
    public String getRequestURI() {
      return super.getRequestURI() + ".wsdl";
    }
  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse response,
      final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    if ("GET".equals(request.getMethod())
        && "wsdl".equalsIgnoreCase(request.getQueryString())) {
      var requestWrapper = new WsdlQueryRequestWrapper(request);
      request.getSession().getServletContext()
          .getRequestDispatcher(request.getServletPath()
              + (request.getPathInfo() == null ? "" : request.getPathInfo()) + ".wsdl")
          .forward(requestWrapper, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }
}