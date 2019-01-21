package simutool.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import simutool.DBpopulator.InfluxPopulator;

/**
 * Entry point
 *
 */
@ComponentScan(basePackages = "simutool")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, MultipartAutoConfiguration.class})
public class StartApp {
	
	@Value("${influxStarter}")
	private String influxStarter;
	
	@Value("${grafanaStarter}")
	private String grafanaStarter;
	
	@Autowired
	private InfluxPopulator influx;

	public static void main(String[] args) {
		SpringApplication.run(StartApp.class, args);
	}
	
	/**
	 * Launches influx database after server has been started
	 */
	@PostConstruct
	public void serversStart() { 
	    Process influxProcess = null; 
	    Process grafanaProcess = null; 
	    try 
	    { 
	      influxProcess = Runtime.getRuntime().exec(influxStarter); 
		  influx.startInflux();
	    } 
	    catch(Exception e){ 
	      System.out.println("error==="+e.getMessage()); 
	      e.printStackTrace(); 
	    } 
	    try 
	    { 
	      grafanaProcess = Runtime.getRuntime().exec(grafanaStarter); 
	      System.out.println(influxProcess);
	      System.out.println(grafanaProcess);
	    } 
	    catch(Exception e){ 
	      System.out.println("error==="+e.getMessage()); 
	      e.printStackTrace(); 
	    } 
	}

}
