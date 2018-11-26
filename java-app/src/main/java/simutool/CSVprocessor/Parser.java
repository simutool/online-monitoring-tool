package simutool.CSVprocessor;

import java.io.InvalidObjectException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import simutool.models.Panel;

/**
 * This class parses .csv files and creates FileDTO Objects.
 *
 */
@Service
public class Parser {

	CsvParserSettings settings = new CsvParserSettings();
	CsvParser libParser;
	List<FileDTO> sensorFiles;
	List<FileDTO> simulationFiles;


	public Parser(){
		settings.detectFormatAutomatically();
		libParser = new CsvParser(settings);
	}



	/**
	 * Reads .csv file in the passed FileReader and casts FileDTO object.
	 * @param type sensor or simulation
	 * @return FileDTO object
	 */
	public FileDTO parseFilesForPanels(String type, Reader r) {
		FileDTO file = new FileDTO();	 

		List<String[]> rows = null;	 
		
			rows = libParser.parseAll(r);
		
			rows = rows.subList(1, rows.size())
					.stream().map(item -> Arrays.copyOfRange(item, 0, 3)).collect(Collectors.toList());
	

		//	Map<String, List<String[]>> grouped = rows.stream()
		//            .collect(Collectors.groupingBy(list -> list[2]));

		//	List<List<String[]>> list = new ArrayList<List<String[]>>(grouped.values());

		file.setRows(rows);
		file.setType(type);
		file.setDatasource_id(Integer.parseInt(rows.get(0)[2]));

		return file;
	}


	public List<FileDTO> getSensorFiles() {
		return sensorFiles;
	}


	public void setSensorFiles(List<FileDTO> sensorFiles) {
		this.sensorFiles = sensorFiles;
	}


	public List<FileDTO> getSimulationFiles() {
		return simulationFiles;
	}


	public void setSimulationFiles(List<FileDTO> simulationFiles) {
		this.simulationFiles = simulationFiles;
	}


	/** Returns data file with requested datasource, file name and data type.
	 * 
	 * @param type Data type: simulation, sensor.
	 * @param datasource Datasource id
	 * @param fileName name of the file
	 * @throws IllegalArgumantException If type is a not supported value 
	 * @return requested fileDTO object
	 */
	public FileDTO getFile(String type, int datasource, String fileName) {
		List<FileDTO> list;
		FileDTO file = null;

		if(type.equals("sensor"))
			list = sensorFiles;
		else if(type.equals("simulation"))
			list = simulationFiles;
		else
			throw new IllegalArgumentException("unsupported type");

		for(FileDTO entry : list) {
			if(entry.getDatasource_id() == datasource && entry.getName().equals(fileName)) {
				file = entry;
				break;			
			}

		}
		return file;
	}


	public static void main(String[] args) {
		Parser p = new Parser();
		//	p.parse();
	}


}
