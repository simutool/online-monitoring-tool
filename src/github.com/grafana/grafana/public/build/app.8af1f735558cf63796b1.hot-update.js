webpackHotUpdate("app",{

/***/ "./public/app/plugins/panel/graph/template.ts":
/*!****************************************************!*\
  !*** ./public/app/plugins/panel/graph/template.ts ***!
  \****************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
var template = "\n<div class=\"graph-panel\" ng-class=\"{'graph-panel--legend-right': ctrl.panel.legend.rightSide}\">\n  <div class=\"graph-panel__chart\" grafana-graph ng-dblclick=\"ctrl.zoomOut()\">\n  </div>\n\n  <div class=\"graph-legend\">\n    <div class=\"graph-legend-content\" graph-legend></div>\n  </div>\n  <div class=\"row\">\n\t<div class=\"btn col-xs-5 offset-xs-1 btn-success\" ng-class=\"{'disabled' : ctrl.isRefreshing()}\" ng-click=\"ctrl.toggleRefresh(true)\">Start</div>\n\t<div class=\"btn col-xs-5 offset-xs-1 btn-warning\" ng-class=\"{'disabled' : !ctrl.isRefreshing()}\" ng-click=\"ctrl.toggleRefresh(false)\">Pause</div>\n  </div>\n</div>\n";
/* harmony default export */ __webpack_exports__["default"] = (template);


/***/ })

})
//# sourceMappingURL=app.8af1f735558cf63796b1.hot-update.js.map