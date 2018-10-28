package postgres;

import java.util.ArrayList;

/**
 * @deprecated
 * Query result on postgres - set of Series objects
 *
 */
public class SeriesSet {
	
	private ArrayList<Series> series;

	public SeriesSet() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public SeriesSet(ArrayList<Series> series) {
		super();
		this.series = series;
	}



	public ArrayList<Series> getSeries() {
		return series;
	}

	public void setSeries(ArrayList<Series> series) {
		this.series = series;
	}

	
	
	
}
