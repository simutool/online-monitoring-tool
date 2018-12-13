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
	<a class="btn navbar-button saveAsCSV"  href="#" role="button" >
			Save as CSV
	</a>
  </div>
  

</div>
`;

export default template;
