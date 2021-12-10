package tourable;

import org.salespointframework.EnableSalespoint;
import org.salespointframework.SalespointSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableSalespoint
@EnableScheduling
public class Tourable {

	private static final String LOGIN_ROUTE = "/login";

	public static void main(String[] args) {
		SpringApplication.run(Tourable.class, args);
	}

	@Configuration
	static class TourableWebConfiguration implements WebMvcConfigurer {

		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			registry.addViewController(LOGIN_ROUTE).setViewName("login");
			registry.addViewController("/").setViewName("index");
		}
	}

	@Configuration
	static class WebSecurityConfiguration extends SalespointSecurityConfiguration {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.csrf().disable();

			http.authorizeRequests().antMatchers("/resources/**").permitAll().antMatchers("/login*").permitAll()
					.anyRequest().authenticated().and().formLogin().loginPage(LOGIN_ROUTE)
					.loginProcessingUrl(LOGIN_ROUTE).and().logout().logoutUrl("/logout").logoutSuccessUrl(LOGIN_ROUTE);
		}
	}
}
