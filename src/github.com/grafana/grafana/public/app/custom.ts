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
			appendCommentsCont();
			processExperimentData(response);
			let staticsLaunched = false;
			
		//	for(let panel of response){
			//	for(let file of panel.files){
				//	if(file.type == "Simulation" || file.type == "Curing cycle"){
					//	staticsLaunched = true;
						//console.log("is already launched!");
						//$("#launchStatics").addClass("disabled");
					//}
				//}
			//}
			
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

			
		}, function errorCallback(response) {
				console.log(response);
		});
    });
	
	
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
			$("#launchStatics").addClass("disabled");
		
		}, function errorCallback(response) {
				console.log(response);
		});	
	}
	
	function setTitle(names, timerId){
		//		$($(".panel-title-text")[counter]).text(name);
				let labelsFound = false;
		for(let i=0; i < $(".panel-title-text").length; i++){
				$($(".panel-title-text")[i]).text(names[i].name);
		labelsFound = true;
		}		
		if(labelsFound){
			clearInterval(timerId);
		}

	}
	
	function processExperimentData(response){
			var counter = 0;


			for(let panel of response){
				console.log(panel);
				console.log($(".panel-title-text"));

				//Set panel names
					//$($(".panel-title-text")[counter]).text(panel.name);
					let timerId = setInterval(() => setTitle(response, timerId), 500);

			
				counter++;
			}
			

			
			const commentCont = $("#sortable");
			console.log(commentCont);
			commentCont.empty();
			
			if(response[0].loaded){
				console.log($(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody"));
					$(".timepickerCont, .sendCommentBtn, .saveAsCSV, .commentBody, #launchStatics").addClass("hiddenSection");
			}	
			for(let i=response[0].comments.length-1; i>=0; i--){

				let commentHtml = `<div>
					<small class="pull-right text-muted">
					<span class="glyphicon glyphicon-time"></span>${response[0].comments[i].timeAsString}</small>
					</br>
					<li class="ui-state-default"> ${response[0].comments[i].commentText}</li>
					</br>
				</div>`;
				commentCont.append(commentHtml);
			}

			
	}
	
	function appendCommentsCont(){
			const dashboardCont = $(".dashboard-container ");
			console.log($(".dashboard-container .react-grid-item"));
			dashboardCont.append(bigCommentCont);
			
						//Set initial time in time input
			$(".customTimepicker").val(new Date().toLocaleTimeString());

				//Set current time when time input is in focus
			$(".customTimepicker").on("focus", function(event){
					this.value = new Date().toLocaleTimeString();
			});
			

			
			//Set current time every time comment body is in focus
			$(".commentBody").on("focus", setCustomTime.bind(this));	
			function setCustomTime(){
					$(".customTimepicker").val(new Date().toLocaleTimeString());
			}
			
						
			//Connect comment sections with buttons that toggle them
			$(".saveAsCSV").attr("href", springHost + "/savePanel");
				
			$(".commentSectionToggle").on("click", toggleCommentBox.bind(this));
			function toggleCommentBox(){
				var cont = $(".commentsCont");
				console.log(cont.hasClass("hidden"));
				if(cont.hasClass("hidden")){
					cont.removeClass("hidden");
				}else{
					cont.addClass("hidden");
				}
			}
	}
	
	const bigCommentCont = `	
	
	  <div class="row panelButtons">
	  	<a class="btn btn-success  " id="launchStatics"  role="button" >
			Run experiment
	</a>
	<a class="btn navbar-button  commentSectionToggle"  role="button" >
			Comments
	</a>
	<a class="btn navbar-button saveAsCSV"  href="#" role="button" >
			Save as CSV
	</a>
  </div>
  <div class="row commentsCont collapse" >
	
		
		<div class="col-sm-12 text-center commentsToHide">
			<div class="input-group">
				<textarea  type="text"  rows="3" class="col-xs-12 form-control input-sm chat-input commentBody" placeholder="Write your message here..." ></textarea>
				<div class="row">
					
					<div class=" col-xs-8 timepickerCont">
						<div class="row">
	
							<div class='col-sm-12'>
								<label for="timestamp" name="timestamp" >Set comment time manually</label>
								<input type='text' class="form-control customTimepicker" id='' />
							</div>
		
						</div>
					</div>	

					
				
					<span class="input-group-btn col-xs-4 sendCommentBtn" > 
						<a href="#" class="btn btn-secondary btn-sm"><span class="glyphicon glyphicon-comment"></span> Add Comment</a>
					</span>
				</div>
			</div>
			<hr data-brackets-id="12673">
			<ul data-brackets-id="12674" id="sortable" class="list-unstyled ui-sortable" style="height: 100px;overflow: auto">
				
			</ul>
		</div>
	</div>`;

					

					