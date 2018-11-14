package simutool.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import simutool.DBpopulator.InfluxPopulator;


@ComponentScan(basePackages = "simutool")
@EnableAutoConfiguration
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
