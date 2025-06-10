package ma.ensa.ebankingver1.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.DelegatingFilterProxy;

public class SecurityWebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        FilterRegistration.Dynamic securityFilter = servletContext.addFilter("springSecurityFilterChain",
                new DelegatingFilterProxy());

        securityFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
//Associe ce filtre à toutes les URL (/*).
