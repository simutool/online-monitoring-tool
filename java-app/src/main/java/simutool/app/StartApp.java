package simutool.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

	@Value("${grafanaParam}")
	private String grafanaParam;

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
		try { 
			influxProcess = Runtime.getRuntime().exec(influxStarter); 
			influx.startInflux();
		} 
		catch(Exception e){ 
			System.out.println("error==="+e.getMessage()); 
			e.printStackTrace(); 
		} 
		try { 
			String command1 = "cmd /c start cmd.exe /K  cd \"../src/github.com/grafana/grafana/bin\" && grafana-server.exe " ;
			System.out.println(command1);

			Thread grafana = new Thread() {
				public void run() {
					try {

						ProcessBuilder builder = new ProcessBuilder(
								"cmd.exe", "/K", "cd \"..//src//github.com//grafana//grafana//bin\" && grafana-server.exe");
						builder.redirectErrorStream(true);
						Process p = builder.start();
						BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line;
						while (true) {
							line = r.readLine();
							if (line == null) { break; }
							System.out.println(line);
						}

					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			grafana.start();
		} 
		catch(Exception e){ 
			System.out.println("error==="+e.getMessage()); 
			e.printStackTrace(); 
		} 
	}

}
