const template = `
<div class="graph-panel" ng-class="{'graph-panel--legend-right': ctrl.panel.legend.rightSide}">
  <div class="graph-panel__chart" grafana-graph ng-dblclick="ctrl.zoomOut()">
  </div>

  <div class="graph-legend">
    <div class="graph-legend-content" graph-legend></div>
  </div>
  <div class="row" style="margin-top: 30px; margin-bottom: 30px;">
	<div class="btn col-xs-4 offset-xs-1 btn-success" ng-class="{'disabled' : ctrl.isRefreshing()}" ng-click="ctrl.toggleRefresh(true)">Start</div>
	<div class="btn col-xs-4 offset-xs-2 btn-warning" ng-class="{'disabled' : !ctrl.isRefreshing()}" ng-click="ctrl.toggleRefresh(false)">Pause</div>
  </div>
</div>
`;

export default template;
