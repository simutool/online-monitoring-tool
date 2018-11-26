package simutool.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import simutool.models.Panel;

@RestController
public class ExperimentDataRestController {

	@Autowired
	MainController ctrl;
	
	/**
	 * Returns JSON with panels data in current simulation
	 * @return info on selected panels
	 */
	@RequestMapping("/getExperimentData")
	public List<Panel> getExperimentData(Model m) {
		return MainController.pendingPanels;
	}

}
