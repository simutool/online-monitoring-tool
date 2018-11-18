package simutool.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import simutool.DBpopulator.InfluxPopulator;


@ComponentScan(basePackages = "simutool")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, MultipartAutoConfiguration.class})
public class StartApp {
	
	@Autowired
	private InfluxPopulator influx;

	public static void main(String[] args) {
	//	Parser parser = new Parser();
	//	parser.parse();
	//	InfluxPopulator inf = new InfluxPopulator();
	//	inf.startInflux(parser);
	//	inf.addSimulationPoints();
	//	inf.simulateSensor(1000, 3, "datasens.csv");
	//	inf.simulateSensor(1000);
		SpringApplication.run(StartApp.class, args);
	}
	
	@PostConstruct
	public void influxStart() {
		influx.startInflux();
	}

}
