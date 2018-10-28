webpackHotUpdate("app",{

/***/ "./public/app/plugins/panel/graph/module.ts":
/*!**************************************************!*\
  !*** ./public/app/plugins/panel/graph/module.ts ***!
  \**************************************************/
/*! exports provided: GraphCtrl, PanelCtrl */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "GraphCtrl", function() { return GraphCtrl; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PanelCtrl", function() { return GraphCtrl; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _graph__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./graph */ "./public/app/plugins/panel/graph/graph.ts");
/* harmony import */ var _legend__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./legend */ "./public/app/plugins/panel/graph/legend.ts");
/* harmony import */ var _series_overrides_ctrl__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./series_overrides_ctrl */ "./public/app/plugins/panel/graph/series_overrides_ctrl.ts");
/* harmony import */ var _thresholds_form__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./thresholds_form */ "./public/app/plugins/panel/graph/thresholds_form.ts");
/* harmony import */ var _template__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./template */ "./public/app/plugins/panel/graph/template.ts");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! lodash */ "./node_modules/lodash/lodash.js");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_6___default = /*#__PURE__*/__webpack_require__.n(lodash__WEBPACK_IMPORTED_MODULE_6__);
/* harmony import */ var app_core_config__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! app/core/config */ "./public/app/core/config.ts");
/* harmony import */ var app_plugins_sdk__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! app/plugins/sdk */ "./public/app/plugins/sdk.ts");
/* harmony import */ var _data_processor__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ./data_processor */ "./public/app/plugins/panel/graph/data_processor.ts");
/* harmony import */ var _axes_editor__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ./axes_editor */ "./public/app/plugins/panel/graph/axes_editor.ts");











var GraphCtrl = /** @class */ (function (_super) {
    tslib__WEBPACK_IMPORTED_MODULE_0__["__extends"](GraphCtrl, _super);
    /** @ngInject */
    function GraphCtrl($scope, $injector, annotationsSrv, timeSrv) {
        var _this = _super.call(this, $scope, $injector) || this;
        _this.annotationsSrv = annotationsSrv;
        _this.hiddenSeries = {};
        _this.seriesList = [];
        _this.dataList = [];
        _this.annotations = [];
        _this.colors = [];
        _this.panelDefaults = {
            // datasource name, null = default datasource
            datasource: null,
            // sets client side (flot) or native graphite png renderer (png)
            renderer: 'flot',
            yaxes: [
                {
                    label: null,
                    show: true,
                    logBase: 1,
                    min: null,
                    max: null,
                    format: 'short',
                },
                {
                    label: null,
                    show: true,
                    logBase: 1,
                    min: null,
                    max: null,
                    format: 'short',
                },
            ],
            xaxis: {
                show: true,
                mode: 'time',
                name: null,
                values: [],
                buckets: null,
            },
            yaxis: {
                align: false,
                alignLevel: null,
            },
            // show/hide lines
            lines: true,
            // fill factor
            fill: 1,
            // line width in pixels
            linewidth: 1,
            // show/hide dashed line
            dashes: false,
            // length of a dash
            dashLength: 10,
            // length of space between two dashes
            spaceLength: 10,
            // show hide points
            points: false,
            // point radius in pixels
            pointradius: 5,
            // show hide bars
            bars: false,
            // enable/disable stacking
            stack: false,
            // stack percentage mode
            percentage: false,
            // legend options
            legend: {
                show: true,
                values: false,
                min: false,
                max: false,
                current: false,
                total: false,
                avg: false,
            },
            // how null points should be handled
            nullPointMode: 'null',
            // staircase line mode
            steppedLine: false,
            // tooltip options
            tooltip: {
                value_type: 'individual',
                shared: true,
                sort: 0,
            },
            // time overrides
            timeFrom: null,
            timeShift: null,
            // metric queries
            targets: [{}],
            // series color overrides
            aliasColors: {},
            // other style overrides
            seriesOverrides: [],
            thresholds: [],
        };
        lodash__WEBPACK_IMPORTED_MODULE_6___default.a.defaults(_this.panel, _this.panelDefaults);
        lodash__WEBPACK_IMPORTED_MODULE_6___default.a.defaults(_this.panel.tooltip, _this.panelDefaults.tooltip);
        lodash__WEBPACK_IMPORTED_MODULE_6___default.a.defaults(_this.panel.legend, _this.panelDefaults.legend);
        lodash__WEBPACK_IMPORTED_MODULE_6___default.a.defaults(_this.panel.xaxis, _this.panelDefaults.xaxis);
        _this.processor = new _data_processor__WEBPACK_IMPORTED_MODULE_9__["DataProcessor"](_this.panel);
        _this.events.on('render', _this.onRender.bind(_this));
        _this.events.on('data-received', _this.onDataReceived.bind(_this));
        _this.events.on('data-error', _this.onDataError.bind(_this));
        _this.events.on('data-snapshot-load', _this.onDataSnapshotLoad.bind(_this));
        _this.events.on('init-edit-mode', _this.onInitEditMode.bind(_this));
        _this.events.on('init-panel-actions', _this.onInitPanelActions.bind(_this));
        return _this;
    }
    GraphCtrl.prototype.onInitEditMode = function () {
        this.addEditorTab('Display', 'public/app/plugins/panel/graph/tab_display.html', 4);
        this.addEditorTab('Axes', _axes_editor__WEBPACK_IMPORTED_MODULE_10__["axesEditorComponent"], 2);
        this.addEditorTab('Legend', 'public/app/plugins/panel/graph/tab_legend.html', 3);
        if (app_core_config__WEBPACK_IMPORTED_MODULE_7__["default"].alertingEnabled) {
            this.addEditorTab('Alert', app_plugins_sdk__WEBPACK_IMPORTED_MODULE_8__["alertTab"], 5);
        }
        this.subTabIndex = 0;
    };
    GraphCtrl.prototype.onInitPanelActions = function (actions) {
        actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
        actions.push({ text: 'Toggle legend', click: 'ctrl.toggleLegend()' });
    };
    GraphCtrl.prototype.issueQueries = function (datasource) {
        var _this = this;
        this.annotationsPromise = this.annotationsSrv.getAnnotations({
            dashboard: this.dashboard,
            panel: this.panel,
            range: this.range,
        });
        /* Wait for annotationSrv requests to get datasources to
         * resolve before issuing queries. This allows the annotations
         * service to fire annotations queries before graph queries
         * (but not wait for completion). This resolves
         * issue 11806.
         */
        return this.annotationsSrv.datasourcePromises.then(function (r) {
            return _super.prototype.issueQueries.call(_this, datasource);
        });
    };
    GraphCtrl.prototype.zoomOut = function (evt) {
        this.publishAppEvent('zoom-out', 2);
    };
    GraphCtrl.prototype.toggleRefresh = function (mode) {
        if (mode) {
            this.timeSrv.setAutoRefresh('1s');
            console.log(this.timeSrv.dashboard.refresh);
        }
        else {
            this.timeSrv.setAutoRefresh(undefined);
            console.log(this.timeSrv.dashboard.refresh);
        }
    };
    GraphCtrl.prototype.isRefreshing = function () {
        return this.dashboard.refresh !== undefined;
    };
    GraphCtrl.prototype.onDataSnapshotLoad = function (snapshotData) {
        this.annotationsPromise = this.annotationsSrv.getAnnotations({
            dashboard: this.dashboard,
            panel: this.panel,
            range: this.range,
        });
        this.onDataReceived(snapshotData);
    };
    GraphCtrl.prototype.onDataError = function (err) {
        this.seriesList = [];
        this.annotations = [];
        this.render([]);
    };
    GraphCtrl.prototype.onDataReceived = function (dataList) {
        var _this = this;
        this.dataList = dataList;
        this.seriesList = this.processor.getSeriesList({
            dataList: dataList,
            range: this.range,
        });
        this.dataWarning = null;
        var datapointsCount = this.seriesList.reduce(function (prev, series) {
            return prev + series.datapoints.length;
        }, 0);
        if (datapointsCount === 0) {
            this.dataWarning = {
                title: 'No data points',
                tip: 'No datapoints returned from data query',
            };
        }
        else {
            for (var _i = 0, _a = this.seriesList; _i < _a.length; _i++) {
                var series = _a[_i];
                if (series.isOutsideRange) {
                    this.dataWarning = {
                        title: 'Data points outside time range',
                        tip: 'Can be caused by timezone mismatch or missing time filter in query',
                    };
                    break;
                }
            }
        }
        this.annotationsPromise.then(function (result) {
            _this.loading = false;
            _this.alertState = result.alertState;
            _this.annotations = result.annotations;
            _this.render(_this.seriesList);
        }, function () {
            _this.loading = false;
            _this.render(_this.seriesList);
        });
    };
    GraphCtrl.prototype.onRender = function () {
        if (!this.seriesList) {
            return;
        }
        for (var _i = 0, _a = this.seriesList; _i < _a.length; _i++) {
            var series = _a[_i];
            series.applySeriesOverrides(this.panel.seriesOverrides);
            if (series.unit) {
                this.panel.yaxes[series.yaxis - 1].format = series.unit;
            }
        }
    };
    GraphCtrl.prototype.changeSeriesColor = function (series, color) {
        series.setColor(color);
        this.panel.aliasColors[series.alias] = series.color;
        this.render();
    };
    GraphCtrl.prototype.toggleSeries = function (serie, event) {
        if (event.ctrlKey || event.metaKey || event.shiftKey) {
            if (this.hiddenSeries[serie.alias]) {
                delete this.hiddenSeries[serie.alias];
            }
            else {
                this.hiddenSeries[serie.alias] = true;
            }
        }
        else {
            this.toggleSeriesExclusiveMode(serie);
        }
        this.render();
    };
    GraphCtrl.prototype.toggleSeriesExclusiveMode = function (serie) {
        var _this = this;
        var hidden = this.hiddenSeries;
        if (hidden[serie.alias]) {
            delete hidden[serie.alias];
        }
        // check if every other series is hidden
        var alreadyExclusive = lodash__WEBPACK_IMPORTED_MODULE_6___default.a.every(this.seriesList, function (value) {
            if (value.alias === serie.alias) {
                return true;
            }
            return hidden[value.alias];
        });
        if (alreadyExclusive) {
            // remove all hidden series
            lodash__WEBPACK_IMPORTED_MODULE_6___default.a.each(this.seriesList, function (value) {
                delete _this.hiddenSeries[value.alias];
            });
        }
        else {
            // hide all but this serie
            lodash__WEBPACK_IMPORTED_MODULE_6___default.a.each(this.seriesList, function (value) {
                if (value.alias === serie.alias) {
                    return;
                }
                _this.hiddenSeries[value.alias] = true;
            });
        }
    };
    GraphCtrl.prototype.toggleAxis = function (info) {
        var override = lodash__WEBPACK_IMPORTED_MODULE_6___default.a.find(this.panel.seriesOverrides, { alias: info.alias });
        if (!override) {
            override = { alias: info.alias };
            this.panel.seriesOverrides.push(override);
        }
        info.yaxis = override.yaxis = info.yaxis === 2 ? 1 : 2;
        this.render();
    };
    GraphCtrl.prototype.addSeriesOverride = function (override) {
        this.panel.seriesOverrides.push(override || {});
    };
    GraphCtrl.prototype.removeSeriesOverride = function (override) {
        this.panel.seriesOverrides = lodash__WEBPACK_IMPORTED_MODULE_6___default.a.without(this.panel.seriesOverrides, override);
        this.render();
    };
    GraphCtrl.prototype.toggleLegend = function () {
        this.panel.legend.show = !this.panel.legend.show;
        this.refresh();
    };
    GraphCtrl.prototype.legendValuesOptionChanged = function () {
        var legend = this.panel.legend;
        legend.values = legend.min || legend.max || legend.avg || legend.current || legend.total;
        this.render();
    };
    GraphCtrl.prototype.exportCsv = function () {
        var scope = this.$scope.$new(true);
        scope.seriesList = this.seriesList;
        this.publishAppEvent('show-modal', {
            templateHtml: '<export-data-modal data="seriesList"></export-data-modal>',
            scope: scope,
            modalClass: 'modal--narrow',
        });
    };
    GraphCtrl.template = _template__WEBPACK_IMPORTED_MODULE_5__["default"];
    return GraphCtrl;
}(app_plugins_sdk__WEBPACK_IMPORTED_MODULE_8__["MetricsPanelCtrl"]));



/***/ })

})
//# sourceMappingURL=app.b4f271df46ab92d8af6b.hot-update.js.map