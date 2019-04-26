package simutool.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import simutool.CSVprocessor.ExperimentSaver;
import simutool.DBpopulator.InfluxPopulator;
import simutool.controllers.MainController;
import simutool.repos.SavedSimulationsRepo;

/**
 * Entry point. <br>
 * Launches the app and all needed servers
 *
 */
@ComponentScan(basePackages = "simutool")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, MultipartAutoConfiguration.class})
public class StartApp implements ApplicationRunner{

	/**
	 * Path to influxd.exe file
	 */
	@Value("${influxStarter}")
	private String influxStarter;
	
	@Autowired
	private SavedSimulationsRepo simRepo;
	
	/**
	 * Path to grafana-server.exe file
	 */
	@Value("${grafanaStarter}")
	private String grafanaStarter;

	@Autowired
	private ExperimentSaver saver;
	/**
	 * Path to the host where this Spring Boot app is running
	 */
	@Value("${server.port}")
	private String springPort;

	@Autowired
	private InfluxPopulator influx;

	public static void main(String[] args) {
		SpringApplication.run(StartApp.class, args);
	}

	
    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<String> zips = args.getOptionValues("l");
        if(zips != null) {
        	String zipPath = zips.get(0);
    		MainController.zipPath = zipPath;
        }
    }
	
	/**
	 * Launches grafana and influx servers after server has been started.
	 * Each server is running in a separate thread
	 */
	@PostConstruct
	public void serversStart() { 
	
		
		Process influxProcess = null; 
		final Process grafanaProcess; 
		try { 
			Thread infl = new Thread() {
				public void run() {
					try {

						ProcessBuilder builder = new ProcessBuilder(
								"cmd.exe", "/K", "cd \"" + influxStarter + "\" && influxd.exe");
						builder.redirectErrorStream(true);
						Process p = builder.start();
						BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line;
						while (true) {
							line = r.readLine();
							if (line == null) { break; }
						}

					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			infl.start();
			influx.startInflux();
		} 
		catch(Exception e){ 
			e.printStackTrace(); 
		} 
		try { 
			String command1 = "cmd /c start cmd.exe /K  cd \"" + grafanaStarter + "\" && grafana-server.exe " ;

			Thread grafana = new Thread() {
				public void run() {
					try {

						ProcessBuilder builder = new ProcessBuilder(
								"cmd.exe", "/K", "cd \"" + grafanaStarter  +"\" && grafana-server.exe");
						builder.redirectErrorStream(true);
						Process p = builder.start();
						BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line;
						while (true) {
							line = r.readLine();
							if (line == null) { break; }
						}

					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			grafana.start();
		} 
		catch(Exception e){ 
			e.printStackTrace(); 
		} 
	}
	



}



