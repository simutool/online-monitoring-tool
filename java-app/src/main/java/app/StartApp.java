package app;

import CSVprocessor.Parser;
import DBpopulator.InfluxPopulator;

public class StartApp {

	public static void main(String[] args) {
		Parser parser = new Parser();
		parser.parse();
		InfluxPopulator inf = new InfluxPopulator();
		inf.startInflux(parser);
		inf.addSimulationPoints(2, "sensor-simulation-4.csv");
		inf.simulateSensor(1000, 3, "datasens.csv");
	}

}
