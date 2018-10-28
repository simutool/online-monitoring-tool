webpackHotUpdate("app",{

/***/ "./public/app/plugins/panel/graph/template.ts":
/*!****************************************************!*\
  !*** ./public/app/plugins/panel/graph/template.ts ***!
  \****************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
var template = "\n<div class=\"graph-panel\" ng-class=\"{'graph-panel--legend-right': ctrl.panel.legend.rightSide}\">\n  <div class=\"graph-panel__chart\" grafana-graph ng-dblclick=\"ctrl.zoomOut()\">\n  </div>\n\n  <div class=\"graph-legend\">\n    <div class=\"graph-legend-content\" graph-legend></div>\n  </div>\n  <div class=\"btn col-6 btn-success\" [ngClass]=\"this.timeSrv.refresh == undefined ? '' : 'disabled'\" ng-click=\"ctrl.toggleRefresh(true)\">Start</div>\n  <div class=\"btn col-6 btn-warning\" ng-click=\"ctrl.toggleRefresh(false)\">Pause</div>\n</div>\n";
/* harmony default export */ __webpack_exports__["default"] = (template);


/***/ })

})
//# sourceMappingURL=app.c5e9322c2fc7e6f4ed9d.hot-update.js.map