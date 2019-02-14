import $ from 'jquery';
 import 'moment';
    declare var $: $;

	const springHost = 'http://localhost:8090';


 
			
	$(function(){
        $.ajax({
			method: 'GET',
			url: springHost + '/getExperimentData',
			headers: {
				'Access-Control-Allow-Origin': '*'
		},
		}).then(function successCallback(response) {
			console.log(response);
			console.log(window.location.host);
			
			appendCommentsCont(response);
			processExperimentData(response);
			let staticsLaunched = false;
			$($("title")[0]).text( response.name  );
			
			
			
			$($(".sendCommentBtn a")[0]).on("click", function(event){
				const comment = $(".commentBody")[0].value;
				const time = $(".customTimepicker")[0].value;
				
				console.log(comment + "---" + time);
				if(comment.length > 0){
					sendComment(comment, time);
				}
				console.log("submit comment was clicked");
			});
			
			$("#launchStatics").on("click", function(event){
				if(!$("#launchStatics").hasClass("disabled")){
					launchStatics();
				}
			});
			
			
			$("#saveDescriptionPreview").on("input", function(e, t){
				const name = e.target.name;
				const value = e.target.value;
				console.log("changing description: "+ value);
				$.ajax({
					method: 'POST',
					url: springHost + '/setSimulationData',
					data: JSON.stringify({ "description": value }),
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
			
			console.log($(".scroll-canvas"));
		$($(".scroll-canvas")[1]).prepend(`<h3 class="custom-header">Experiment ${response.name + (response.loaded ? " (SAVED)" : "")}</h3>`);
			$(".sidemenu, .navbar").addClass("hiddenSection");
		
			$(window).keyup(function(event){
					const key = event.keyCode; //find the key that was pressed
					if(key===68){ 
						if($(".sidemenu").hasClass("hiddenSection")){
							$(".sidemenu, .navbar").removeClass("hiddenSection");
							console.log("show developer tools");
						}else{
							$(".sidemenu, .navbar").addClass("hiddenSection");
							console.log("hide developer tools");
						}
					}
			});
			

			
		}, function errorCallback(response) {
				console.log(response);
		});
    });
	
	
	
		function saveExperiment(exit){
	    $.ajax({
				method: 'POST',
				url: springHost + '/savePanel',
				data: JSON.stringify({ "description": $("#saveDescriptionPreview")[0].value, "loaded": exit}),
				headers: {
					'Access-Control-Allow-Origin': '*',
					'Content-Type': 'application/json'
		},
		}).then(function successCallback(response) {
			
		
		}, function errorCallback(response) {
				console.log(response);
		});	
	}
	
	
	function sendComment(comment, time){
	    $.ajax({
				method: 'POST',
				url: springHost + '/sendComment',
				data: JSON.stringify({ "commentText": comment, "timeAsString": time }),
				headers: {
					'Access-Control-Allow-Origin': '*',
					'Content-Type': 'application/json'
		},
		}).then(function successCallback(response) {
			console.log(response);
			$(".commentBody")[0].value = "";

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
	
		function launchStatics(){
	    $.ajax({
				method: 'GET',
				url: springHost + '/launchStatics',
				headers: {
					'Access-Control-Allow-Origin': '*',
		},
		}).then(function successCallback(response) {
			console.log(response);
			
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
	
	function setTitle(response, timerId){
		//		$($(".panel-title-text")[counter]).text(name);
				let labelsFound = false;
				let fileNamesFound = false;
				let numOfFiles = 0;
				let fileNames = new Array();
				for(let panel of response.panelList){
					numOfFiles += panel.files.length;
					for(let file of panel.files){
						fileNames.push(file.name);
					}
				}
							console.log($(".panel-title-text"));
														console.log(response.panelList);

	if($(".panel-title-text").length == response.panelList.length){
			labelsFound = true;

		for(let i=0; i < $(".panel-title-text").length; i++){
				
			let title = $($(".panel-title-text")[i]);
			console.log($(".panel-title-text").length);
			if(response.loaded){
				$($(".panel-title-text")[i]).text( $.grep($(response.panelList), function(item) { return item.name.charAt(0) == i+1 })[0].name );				
			}else{
				if(title.text() == '0' || title.text() == '1' || title.text() == '2'){
					title.text( response.panelList[title.text()].name );
				}
			}
		
				
			//$.grep($(".graph-legend-alias pointer"), function(item) { return item.id === '5678' })[0].url;
			}
		}	

		if($(".graph-legend-alias ").length == numOfFiles){
			let counter = 0;
			fileNamesFound = true;
			for(let label of $(".graph-legend-alias ")){
				console.log(label.text);
				
				if(response.loaded){
			//		$(label).text( fileNames[counter] );
				}
				//let panel = $.grep($(response.panelList), function(item) { return item.id === '5678' })[0];
				
			//	$(label).text( $.grep($(response.panelList), function(item) { return item.id === '5678' })[0].url );
				counter++;
			}
		}
		
		if(labelsFound && fileNamesFound){
			clearInterval(timerId);
		}

	}
	
	function processExperimentData(response){
			let counter = 0;

			$("#resetButton").attr("href", springHost + "/reset");

			let staticDataFound = false;
			
			for(let panel of response.panelList){
				console.log(panel);
				console.log($(".panel-title-text"));
										console.log("panel: " + panel);

				for(let file of panel.files){

					if(file.type != "Sensor"){
											console.log("file.type" + file.type != "sensor");

						staticDataFound = true;
						break;
					}
				}
	
				counter++;
			}
			if(!staticDataFound){
					console.log();
					let myButton = $("#launchStatics");
					myButton.removeClass("btn-success");
					myButton.addClass("btn-warning");
					myButton.text("Save");
					myButton.removeAttr("onclick");
				//	myButton.attr("href", springHost + "/savePanel");
					myButton.attr("data-toggle", "modal"); 
					myButton.attr("data-target", "#savingModal");
			
				}
			let timerId = setInterval(() => setTitle(response, timerId), 500);


			if(response.staticsLoaded){
					let myButton = $("#launchStatics");
					myButton.removeClass("btn-success");
					myButton.addClass("btn-warning");
					myButton.text("Save");
					myButton.removeAttr("onclick");
			//		myButton.attr("href", springHost + "/savePanel");
					myButton.attr("data-toggle", "modal"); 
					myButton.attr("data-target", "#savingModal");
					console.log("statics were added");
			}
				
			
			const commentCont = $("#sortable");
			console.log(commentCont);
			commentCont.empty();
			
			if(response.loaded){
				console.log($(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody"));
					$(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody, #launchStatics").addClass("hiddenSection");
			}	

			for(let i=response.comments.length-1; i>=0; i--){

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
	
	function appendCommentsCont(response){
			const dashboardCont = $(".dashboard-container ");
			console.log($(".dashboard-container .react-grid-item"));
			dashboardCont.append(bigCommentCont);
			$("body").append(savingModal);
			$("#saveDescriptionPreview").text(response.description);
			$("#savingModalName").text(response.name);
			$("#savingModal").hide();
			
			$(function () {
				$('[data-toggle="tooltip"]').tooltip()
			})
			
						//Set initial time in time input
			$(".customTimepicker").val(new Date().toLocaleTimeString());

				//Set current time when time input is in focus
			$(".customTimepicker").on("focus", function(event){
					this.value = new Date().toLocaleTimeString();
			});
			
			$("#saveAndExit").attr("href", springHost + "/savePanel/true");
			$("#saveAndResume").attr("href", springHost + "/savePanel/false");

			
			//Set current time every time comment body is in focus
			$(".commentBody").on("focus", setCustomTime.bind(this));	
			function setCustomTime(){
					$(".customTimepicker").val(new Date().toLocaleTimeString());
			}
			
						
			//Connect comment sections with buttons that toggle them
			$(".saveAsCSV").attr("href", springHost + "/savePanel");

				
			$(".commentSectionToggle").on("click", toggleCommentBox.bind(this));
			function toggleCommentBox(){
				let cont = $(".commentsCont");
				console.log(cont.hasClass("hidden"));
				if(cont.hasClass("hidden")){
					cont.removeClass("hidden");
				}else{
					cont.addClass("hidden");
				}
			}
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
				<textarea  type="text"  rows="3" class="col-xs-6 form-control input-sm chat-input commentBody" placeholder="Write your message here..." ></textarea>
				
					
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
								<span aria-hidden="true">&times;</span>
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

					

					
					
					
					
					
					
					
					
					