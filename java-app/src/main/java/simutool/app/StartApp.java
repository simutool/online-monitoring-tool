package simutool.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = "simutool")
@EnableAutoConfiguration
public class StartApp {
	

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

}
