package com.spgg.backend.security;

import com.spgg.backend.common.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Binds one EntityManager to the request thread (mirroring what Open-Session-In-View
 * does) and, if {@link TenantContext} has a tenant id (set by {@link JwtAuthenticationFilter},
 * which MUST run before this filter), enables Hibernate's "tenantFilter" for the
 * whole request so every {@code TenantAwareEntity} query is transparently scoped.
 *
 * <p>Implemented as a request-scoped filter instead of relying on Spring Boot's
 * {@code spring.jpa.open-in-view} auto-configuration so the ordering relative to
 * JWT auth is explicit and guaranteed, rather than dependent on interceptor
 * registration order.
 */
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(entityManager));
        try {
            Long tenantId = TenantContext.getTenantId();
            Session session = entityManager.unwrap(Session.class);
            // The "tenantFilter" Hibernate filter only exists once some entity actually
            // extends TenantAwareEntity (none do yet — Product/Customer/Order etc. will).
            // Guard against UnknownFilterException until then.
            boolean filterDefined = session.getSessionFactory().getDefinedFilterNames().contains("tenantFilter");
            if (tenantId != null && filterDefined) {
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TransactionSynchronizationManager.unbindResource(entityManagerFactory);
            EntityManagerFactoryUtils.closeEntityManager(entityManager);
        }
    }
}
