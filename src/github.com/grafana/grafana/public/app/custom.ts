import $ from 'jquery';
 import  '../../node_modules/eonasdan-bootstrap-datetimepicker/build/js/bootstrap-datetimepicker.min';
 import 'moment';
    declare var $: $;



 $(window).on('load',function(){

            });
			
	$(window).on('load',function(){
        $.ajax({
			method: 'GET',
			url: 'http://localhost:8090/getExperimentData',
			headers: {
				'Access-Control-Allow-Origin': '*'
		},
		}).then(function successCallback(response) {
			console.log(response);
			var counter = 0;
			for(let panel of response){
				$($(".panel-title-text")[counter]).text(panel.name);
				console.log(panel.name);
			
			
			console.log($(".customTimepicker")[counter]);

			$($(".customTimepicker")[counter]).val(new Date().toLocaleTimeString());

			
			$($(".customTimepicker")[counter]).on("focus", function(event){
				this.value = new Date().toLocaleTimeString();
				console.log(event.type);
            });
			
				
			$($(".commentBody")[counter]).on("focus", setCustomTime.bind(this, counter));
			
			function setCustomTime(counter){
						$($(".customTimepicker")[counter]).val(new Date().toLocaleTimeString());
				console.log(counter);
				console.log("comment body on focus");
			}
			
			counter++;
			}	
		}, function errorCallback(response) {
				console.log(response);
		});
    });
	


	