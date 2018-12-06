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
			
			var counter = 0;
							console.log(response);

			for(let panel of response){
				console.log(panel);
				//Set panel names
				$($(".panel-title-text")[counter]).text(panel.name);

				//Set initial time in time input
				$($(".customTimepicker")[counter]).val(new Date().toLocaleTimeString());

				//Set current time when time input is in focus
				$($(".customTimepicker")[counter]).on("focus", function(event){
					this.value = new Date().toLocaleTimeString();
				});
				 
			
				//Set current time every time comment body is in focus
				$($(".commentBody")[counter]).on("focus", setCustomTime.bind(this, counter));	
				function setCustomTime(counter){
						$($(".customTimepicker")[counter]).val(new Date().toLocaleTimeString());
				}
								console.log($($(".commentsCont")[counter]));

				//Connect comment sections with buttons that toggle them
				$($(".saveAsCSV")[counter]).attr("href", springHost + "/savePanel?id=" + counter);
				
				$($(".commentSectionToggle")[counter]).on("click", toggleCommentBox.bind(this, counter));
				function toggleCommentBox(counter){
					
						var cont = $($(".commentsCont")[counter]);
						console.log(cont.hasClass("hidden"));
						if(cont.hasClass("hidden")){
							cont.removeClass("hidden");
						}else{
							cont.addClass("hidden");
						}
				}


				counter++;
			}	
		}, function errorCallback(response) {
				console.log(response);
		});
    });
	


	