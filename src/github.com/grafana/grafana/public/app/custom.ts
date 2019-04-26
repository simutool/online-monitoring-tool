import $ from 'jquery';
import 'moment';
declare var $: $;

const springHost = 'http://localhost:8090';
export let experiment;

/**
   Basic function for modifying fronend assets
*/
$(function () {
	
	// Get JSON with experiment data from the server 
	$.ajax({
		method: 'GET',
		url: springHost + '/getExperimentData',
		headers: {
			'Access-Control-Allow-Origin': '*'
		},
	}).then(function successCallback(response) {
		console.log(response);
		experiment = response;
		console.log(window.location.host);

		// Render container for adding comments, buttons, panels titles
		appendCommentsCont(response);
		processExperimentData(response);
		const buttonAction = response.loaded ? `href=${springHost}` : `data-toggle="modal" data-target="#homepageModal"`;
		
				const customHeader = `<div>
		<a class="btn navbar-button "  ${buttonAction} style="position:absolute;top:10px;left:20px;">
			<i class="fa fa-home" ></i> Homepage
		</a>
		<h3 class="custom-header">Experiment ${response.name + (response.loaded ? " (SAVED)" : "")}</h3></div>`
		
		// Render header with experiment name
		$($(".scroll-canvas")[1]).prepend(customHeader);
		

		
		// Hide grafana navigation bars
		$(".sidemenu, .navbar").addClass("hiddenSection");

		
		// -------------- ADD EVENT LISTENERS ---------------------
		// Send comment t server if send button is clicked
		$($(".sendCommentBtn a")[0]).on("click", function (event) {
			const comment = $(".commentBody")[0].value;
			const time = $(".customTimepicker")[0].value;

			console.log(" time sent as ---" + time);
			if (comment.length > 0) {
				sendComment(comment, time);
			}
			console.log("submit comment was clicked");
		});
		
		// Show static datasets if "start button" is clicked
		$("#launchStatics").attr("href", springHost + "/launchStatics");

		// --- Saving experiment modal ---
		// When there is input change in the description field, send it to server
		$("#saveDescriptionPreview").on("input", function (e, t) {
			const name = e.target.name;
			const value = e.target.value;
			console.log("changing description: " + value);
			$.ajax({
				method: 'POST',
				url: springHost + '/setSimulationData',
				data: JSON.stringify({
					"description": value
				}),
				headers: {
					'Access-Control-Allow-Origin': '*',
					'Content-Type': 'application/json'
				},
			}).then(function successCallback(response) {
				console.log(response);
			}, function errorCallback(response) {
				console.log(response);
			});

		})

		// Show/hide developer tools when "d" key is pressed
		$(window).keyup(function (event) {
			const key = event.keyCode; //find the key that was pressed
			if (key === 68 && !$("#commentInput").is(":focus") && !$("#savingModal textarea").is(":focus")) {
				if ($(".sidemenu").hasClass("hiddenSection")) {
					$(".sidemenu, .navbar").removeClass("hiddenSection");
					console.log("show developer tools");
				}
				else {
					$(".sidemenu, .navbar").addClass("hiddenSection");
					console.log("hide developer tools");
				}
			}
			if (key === 13 && event.ctrlKey && ($("#commentInput").is(":focus") || $(".customTimepicker").is(":focus"))) {
				$($(".sendCommentBtn a")[0]).trigger("click");
				
			}
		});


	}, function errorCallback(response) {
		console.log(response);
	});
});


/** Sending comment
	Send text of the comment and timestamp
*/
function sendComment(comment, time) {
	$.ajax({
		method: 'POST',
		url: springHost + '/sendComment',
		data: JSON.stringify({
			"commentText": comment,
			"timeAsString": time
		}),
		headers: {
			'Access-Control-Allow-Origin': '*',
			'Content-Type': 'application/json'
		},
	}).then(function successCallback(response) {
		console.log(response);
		
		//Clear the textarea after sending
		$(".commentBody")[0].value = "";

		// Get updated experiment data 
		$.ajax({
			method: 'GET',
			url: springHost + '/getExperimentData',
			headers: {
				'Access-Control-Allow-Origin': '*'
			},
		}).then(function successCallback(response) {
			console.log(response);
			processExperimentData(response);

		}, function errorCallback(response) {
			console.log(response);
		});

	}, function errorCallback(response) {
		console.log(response);
	});
}

/** Displaying static datasets when "start" button is triggered
*/
function launchStatics() {
	
	// Tell server to push static data to the database
	$.ajax({
		method: 'GET',
		url: springHost + '/launchStatics',
		headers: {
			'Access-Control-Allow-Origin': '*',
		},
	}).then(function successCallback(response) {
		console.log(response);

		/* Get and render updated expriment data
		$.ajax({
			method: 'GET',
			url: springHost + '/getExperimentData',
			headers: {
				'Access-Control-Allow-Origin': '*'
			},
		}).then(function successCallback(response) {
			console.log(response);
			processExperimentData(response);

		}, function errorCallback(response) {
			console.log(response);
		}); */

	}, function errorCallback(response) {
		console.log(response);
	});
}

/** Overwrite default grafana panel names (is triggered multiple times with setInterval function)
	
*/
function setTitle(response, timerId) {
	let labelsFound = false;
	let fileNamesFound = false;
	let numOfFiles = 0;
	let fileNames = new Array();
	
	// Put all custom panel names
	for (let panel of response.panelList) {

		for (let file of panel.files) {
			if( file.type.toLowerCase() != "sensor" && !response.staticsLoaded && !response.loaded){
				console.log(file.type != "Sensor");
								console.log(!response.staticsLoaded);
				console.log(file.type != "Sensor" && !response.staticsLoaded );
			
			}else{
								console.log("here2");

				numOfFiles = numOfFiles+1;
			}
			//fileNames.push({"id": "db.P" + file.panelNumber + "_" + file.type.toLowerCase() + "_" + file.internalNumber, "name":  });
			fileNames.push(file);
		}
		console.log("numOfFiles: " + numOfFiles);
	}

	// Check if number of default labels is the same as number of panels - it means, all labels are loaded now
	if ($(".panel-title-text").length == response.panelList.length) {
		labelsFound = true;

		for (let i = 0; i < $(".panel-title-text").length; i++) {

			let title = $($(".panel-title-text")[i]);
			console.log($(".panel-title-text").length);
			if (response.loaded) {
				$($(".panel-title-text")[i]).text($.grep($(response.panelList), function (item) {
					return item.name.charAt(0) == i + 1
				})[0].name);
				$("#resetButton").text("Reset view");
			}
			else {
				if (title.text() == '0' || title.text() == '1' || title.text() == '2') {
					title.text(response.panelList[title.text()].name);
				}
			}
		}
	}

	/** ---- Not implemented yet ------
		is supposed to overwrite dataset names in the legend (e.g. "db.P1_simulation_1" to "Left sensor")
	*/
	if ($(".graph-legend-alias ").length == numOfFiles) {
		fileNamesFound = true;
		for (let label of $(".graph-legend-alias ")) {
			console.log(label.text);
			
			let oldLabel = $.grep($(fileNames), function(file) { return "db.P" + file.panelNumber + "_" + file.type.toLowerCase().replace(' ','_') + "_" + file.internalNumber === label.text })[0];
			if(oldLabel){
				console.log("Match found:");
				console.log("db.P" + oldLabel.panelNumber + "_" + oldLabel.type.toLowerCase().replace(' ','_') + "_" + oldLabel.internalNumber);
				console.log(label.text);
								console.log("Text set to: " + oldLabel.name);

				console.log("-----");

				$(label).text( oldLabel.name );
			}
		}
	
	}

	// If all default labels were found and changed - stop the timer
	// otherwise it will be triggered again in 500ms
	if (labelsFound && fileNamesFound) {
		clearInterval(timerId);
	}

}

/** Overwrites buttons depending on whether statics are present, adds rendered comments 
*/
function processExperimentData(response) {
	let counter = 0;

	// Set href for reset button
	$("#resetButton").attr("href", springHost + "/reset");

	//Detect if there are any static datasets 
	let staticDataFound = false;
	for (let panel of response.panelList) {
		console.log(panel);
		console.log($(".panel-title-text"));
		console.log("panel: " + panel);

		for (let file of panel.files) {
			if (file.type != "Sensor") {
				console.log("file.type" + file.type != "sensor");
				staticDataFound = true;
				break;
			}
		}
		counter++;
	}
	
	// If no static data is present, change "Start" button to "Save" (skipping the whole "starting" stage as there is no data for comparison)
	//(only sensors are displayed by default) 
	if (!staticDataFound) {
		console.log();
		let myButton = $("#launchStatics");
		myButton.removeClass("btn-success");
		myButton.addClass("btn-warning");
		myButton.text("Save");
		myButton.removeAttr("href");
		myButton.attr("data-toggle", "modal");
		myButton.attr("data-target", "#savingModal");
	}
	
	// Set timer to overwrite default panel names 
	// (implemented as timer task, since it is unpredictable whether existing fronend components or this script is loaded first)
	let timerId = setInterval(() => setTitle(response, timerId), 500);

	// After statics were added, change "Start" button to "Save"
	if (response.staticsLoaded) {
		let myButton = $("#launchStatics");
		myButton.removeClass("btn-success");
		myButton.addClass("btn-warning");
		myButton.text("Save");
		myButton.removeAttr("href");
		myButton.attr("data-toggle", "modal");
		myButton.attr("data-target", "#savingModal");
		console.log("statics were added");
	}

	// Empty comments container
	const commentCont = $("#sortable");
	console.log(commentCont);
	commentCont.empty();

	// If experiment is a saved simulation, hide all the stuff that has to do with adding new comments, start and save buttons
	if (response.loaded) {
		console.log($(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody"));
		$(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody, #launchStatics").addClass("hiddenSection");
	}

	// Render every comment
	for (let i = response.comments.length - 1; i >= 0; i--) {
		let commentHtml = `<div>
					<small class="pull-right text-muted">
					<span class="glyphicon glyphicon-time"></span>${response.comments[i].timeAsString}</small>
					</br>
					<li class="ui-state-default"> ${response.comments[i].commentText.replace(/"/g, "")}</li>
					</br>
				</div>`;
		commentCont.append(commentHtml);
	}


}

/** Renders comments container
*/
function appendCommentsCont(response) {
	// Append comments container
	const dashboardCont = $(".dashboard-container ");
	dashboardCont.append(bigCommentCont);
	
	// Append saving modal
	$("body").append(savingModal);
	$("#saveDescriptionPreview").text(response.description);
	$("#savingModalName").text(response.name);
	$("#savingModal").hide();
	
	$("body").append(homepageModal);
	$("#homepageModal").hide();


	//  Activate tooltips with hints
	$(function () {
		$('[data-toggle="tooltip"]').tooltip()
	})

	//Set initial time in time input
	$(".customTimepicker").val(getTimeNoLocale());

	//Set current time when time input is in focus
	$(".customTimepicker").on("focus", function (event) {
		this.value = getTimeNoLocale();
		console.log("time changed to: " + this.value);
	});
	
	function getTimeNoLocale(){
		var date = new Date();
		var dateToString = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
		return dateToString;
	}

	// Set links for "save & exit" and "save & resume" buttons
	$("#saveAndExit").attr("href", springHost + "/savePanel/true");
	$("#saveAndResume").attr("href", springHost + "/savePanel/false");


	//Set current time every time comment body is in focus
	$(".commentBody").on("focus", setCustomTime.bind(this));

	// Function for setting timepicker value from system time
	function setCustomTime() {
		$(".customTimepicker").val(getTimeNoLocale());
	}

	//Connect comment sections with buttons that toggle them
	$(".saveAsCSV").attr("href", springHost + "/savePanel");

}


const bigCommentCont = `	
	
	  <div class="row panelButtons" style="justify-content: space-around;">
	  	<a class="btn btn-success btn-lg col-xs-4 " id="launchStatics"  title="" role="button" >
			Start
		</a>
		<a class="btn btn-secondary btn-lg col-xs-4 " id="resetButton"  href="" title="" role="button" >
			Resume
		</a>
	   </div>
  
  <div class="row commentsCont collapse" >
		<div class="col-sm-12 text-center commentsToHide">
			<div class="input-group">
				<div class="row" >
				<textarea  id="commentInput" type="text"  rows="3" class="col-xs-6 form-control input-sm chat-input commentBody" placeholder="Write your message here..." ></textarea>
				
					
					<div class=" col-xs-3 timepickerCont" >
						<div class="row" style="vertical-align: middle">
	
							<div class='col-sm-12'>
								<label for="timestamp" name="timestamp" >Set comment time manually</label>
								<input type='text' class="form-control customTimepicker" id='' />
							</div>
		
						</div>
					</div>	

					
				
					<span class="input-group-btn col-xs-3 sendCommentBtn" > 
						<a href="#" class="btn btn-secondary btn-sm"><span class="glyphicon glyphicon-comment"></span> Add Comment</a>
					</span>
				</div>
			</div>
			<hr data-brackets-id="12673">
			<ul data-brackets-id="12674" id="sortable" class="list-unstyled ui-sortable" style="overflow: auto">
				
			</ul>
		</div>
	</div>`;

const savingModal = `
        <div id="savingModal" class="modal"  tabindex="-1" role="dialog">
            <div class="modal-dialog" role="document">
				<form class="form-group col-xs-12"  action="${springHost} + '/savePanel'" method="post" enctype="multipart/form-data">
					<div class="modal-content">
						<div class=" ">
							<button type="button" style="margin-right:-1rem;color:#fff;margin-top:-1rem;opacity:0.5" class="close" data-dismiss="modal" aria-label="Close">
								<span aria-hidden="true">×</span>
							</button>
							<h1 id="savingModalName" class="modal-title"style="text-align: center;"></h1>
	
						</div>
						
						<div class="modal-body col-xs-12">
						
				

						    <p style="margin-bottom: 10px; text-align: center">Description: 
							<textarea  type="text"  rows="3" class="form-control col-xs-12 " id="saveDescriptionPreview" type="text" value="${name}" ></textarea>
							
							<div class="row panelButtons" style="justify-content: space-around;">
								<a class="btn btn-secondary btn-lg col-xs-4 "  href="#" id="saveAndResume"  title="" >
									Save & Resume
								</a>
								<a class="btn btn-success btn-lg col-xs-4 "  href="#" title="" id="saveAndExit" >
									Save & Exit
								</a>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	`;
	
	
	const homepageModal = `
        <div id="homepageModal" class="modal"  tabindex="-1" role="dialog">
            <div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class=" ">
							<button type="button" style="margin-right:-1rem;color:#fff;margin-top:-1rem;opacity:0.5" class="close" data-dismiss="modal" aria-label="Close">
								<span aria-hidden="true">×</span>
							</button>
							<h1 id="savingModalName" class="modal-title"style="text-align: center;"></h1>
	
						</div>
						
						<div class="modal-body col-xs-12">
							<p>Do you want do go back to the start page? All experiment data will be lost.</p>
				
							<div class="row panelButtons" style="justify-content: space-around;">
								<a class="btn btn-secondary btn-lg col-xs-4 "  href="${springHost}" id="saveAndResume"  title="" >
									Yes
								</a>
								<a class="btn btn-success btn-lg col-xs-4 "  href="#" type="button" data-dismiss="modal" id="saveAndExit" >
									No
								</a>
							</div>
						</div>
					</div>
			</div>
		</div>
	`;
	
	