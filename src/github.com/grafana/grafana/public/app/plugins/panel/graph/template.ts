const template = `
<div class="graph-panel" ng-class="{'graph-panel--legend-right': ctrl.panel.legend.rightSide}" >
  <div class="graph-panel__chart" grafana-graph ng-dblclick="ctrl.zoomOut()">
  </div>

  <div class="graph-legend">
    <div class="graph-legend-content" graph-legend></div>
  </div>
  
  <div class="row panelButtons">
	<a class="btn navbar-button  commentSectionToggle"  role="button" >
			Comments
	</a>
	<a class="btn navbar-button "  role="button" >
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
					
				
					<span class="input-group-btn col-xs-4" onclick="addComment()"> 
						<a href="#" class="btn btn-secondary btn-sm"><span class="glyphicon glyphicon-comment"></span> Add Comment</a>
					</span>
				</div>
			</div>
			<hr data-brackets-id="12673">
			<ul data-brackets-id="12674" id="sortable" class="list-unstyled ui-sortable" style="height: 100px;overflow: auto">
				<div>
					<strong class="pull-left primary-font">James</strong>
					<small class="pull-right text-muted">
					<span class="glyphicon glyphicon-time"></span>7 mins ago</small>
					</br>
					<li class="ui-state-default">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. </li>
					</br>
				</div>
				<div>
					<strong class="pull-left primary-font">Taylor</strong>
					<small class="pull-right text-muted">
					<span class="glyphicon glyphicon-time"></span>14 mins ago</small>
					</br>
					<li class="ui-state-default">Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</li>
				</div>
			</ul>
		</div>
	</div>
</div>
`;

export default template;
