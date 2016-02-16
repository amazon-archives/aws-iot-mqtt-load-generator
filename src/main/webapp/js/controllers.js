var configControllers = angular.module('configControllers', []);

configControllers.controller('ConfigListController', function ($scope, $http, $q, Config, $location) {
    var configs_list = Config.query(function (clist) {
        $scope.configs = [];
        $scope.$location = $location
        $scope.clist = clist;
        for (var i = 0; i < clist.length; i++) {
            var di = Config.get({configId: clist[i].id}, function (dm) {

                // Filter out my object with the running status in
                myconfig = $scope.clist.filter(function (obj) {
                    return obj.id == dm.id;
                });
                dm.running = myconfig[0].running;

                // Push the results as they come back to the model-attribute
                $scope.configs.push(dm);
            });
        }
    });

    $scope.startLoadConfiguration = function (config) {
        $http.post('webresources/config/' + config.id, '{}').success(function (response) {
            $location.path('webresources/series/' + config.id);
            var index = $scope.configs.indexOf(config);
            $scope.configs[index].running = true;
        });
    };

    $scope.deleteConfig = function (config) {
        $http.delete('webresources/config/' + config.id).success(function () {
            var index = $scope.configs.indexOf(config);
            $scope.configs.splice(index, 1);
        });
    };

    $scope.createConfig = function () {
        $scope.$location.url("/config/" + guid());
    };
});

configControllers.controller('ConfigDetailController', function ($scope, $http, $routeParams, Config, ngDialog) {

    $scope.config = Config.get({configId: $routeParams.configId}, function () {
    });

    $scope.editMode = function () {
        // $scope.editmode = !$scope.editmode;
        $scope.config.editmode = !$scope.config.editmode;
    };

    $scope.saveConfig = function () {

        $http.put('webresources/config/' + $routeParams.configId, $scope.config)
                .then(function () {
                    // success
                    $scope.config.editmode = false;
                    // $scope.editmode = false;
                },
                        function (response) {
                            alert('Error in processing the request')
                        });
    };

    $scope.cancelEdit = function (config) {
        //  $scope.editmode = false;
        $scope.config.editmode = false;
    };

    $scope.createFunction = function () {
        $scope.ms = {};

        ngDialog.openConfirm({
            template: 'partials/functiondetail.html',
            controller: 'FunctionController',
            scope: $scope,
            className: 'ngdialog-theme-default',
//            data: $scope.editmode,
            showClose: true
        }).then(function (data) {
            if (data != null) {
                $scope.config.functions.push(data);
            }
        });
    }

    $scope.editFunction = function (ms, index) {
        $scope.ms = ms;
        $scope.ms.index = index;

        ngDialog.openConfirm({
            template: 'partials/functiondetail.html',
            controller: 'FunctionController',
            scope: $scope,
            className: 'ngdialog-theme-default',
//            data: $scope.editmode            
        }).then(function (ms) {
            if (ms != null) {
                if (ms.index != null) {
                    $scope.config.functions[ms.index] = ms;
                }
                else {
                }
            }
            else {
            }
        });
    };

    $scope.deleteFunction = function (ms) {
        var index = $scope.config.functions.indexOf(ms);
        $scope.config.functions.splice(index, 1);
    };
});

configControllers.controller('FunctionController', function ($scope, $q, ngDialog) {
    if ($scope.ms.parameters == null) {
        $scope.ms.parameters = [];
    }

    $scope.saveFunction = function () {
        $scope.confirm($scope.ms);
    };

    $scope.deleteParameter = function (p) {
        var index = $scope.ms.parameters.indexOf(p);
        $scope.ms.parameters.splice(index, 1);
    };

    $scope.cancelFunctionEdit = function () {
        $scope.confirm();
    };

    $scope.createNewParameter = function () {
        var dialog = ngDialog.openConfirm({
            template: 'partials/parameterdetail.html',
            controller: 'ParameterController',
//            scope: $scope,
            className: 'ngdialog-theme-default'
        }).then(function (param) {
            if (param != null) {
                var p = {};
                p[param.name] = param.value;

                $scope.ms.parameters.push(p);
            }
            else {
            }
        });
    };
});

configControllers.controller('ParameterController', function ($scope) {
    $scope.parameter = {};

    $scope.saveParameter = function () {
        $scope.confirm($scope.parameter);
    };

    $scope.cancelEdit = function () {
        $scope.confirm();
    };
});

configControllers.controller('InplaceParameterController', function($scope) {
    
    $scope.updateParameter = function(data) {
      $scope.parameter.value = data;  
    };
    
});


var templateControllers = angular.module('templateControllers', []);

templateControllers.controller('TemplateListController', function ($scope, $http, $routeParams, $q) {
    $http.get('webresources/template').success(function (data) {
        var detailedRequests = [];
        var detData = [];

        for (var i = 0; i < data.length; i++) {
            detailedRequests.push($http.get('webresources/template/' + data[i]).success(function (ddata) {
                detData.push(ddata);
            }));
        }
        $q.all(detailedRequests).then(function () {
            $scope.templates = detData;
        });
    });
});

templateControllers.controller('TemplateDetailController', function ($scope, $http, $routeParams) {
    $http.get('webresources/template').success(function (data) {
        var detailedRequests = [];
        var detData = [];

        for (var i = 0; i < data.length; i++) {
            detailedRequests.push($http.get('webresources/template/' + data[i].id).success(function (ddata) {
                detData.push(ddata);
            }));
        }
        $q.all(detailedRequests).then(function () {
            $scope.template = detData;
        });
    });
});

var metricsSeriesContollers = angular.module('metricsSeriesContollers', []);


metricsSeriesContollers.controller('MetricsSeriesListController', function ($scope, $http, $q, $routeParams, Series) {

    $http.get('webresources/series').success(function (data) {
        var detailedRequests = [];
        var detData = [];
        for (var j = 0; j < data.length; j++) {
            detailedRequests.push($http.get('webresources/series/' + data[j].id).success(function (ddata) {
                ddata.runtime = new Date().getTime() - ddata.start;
                detData.push(ddata);
            }));
        }
        $q.all(detailedRequests).then(function () {
            $scope.series = detData;
        });
    });

    $scope.stopSeries = function (s) {
        Series.delete({seriesId: s.id}, function () {
            var index = $scope.series.indexOf(s);
            $scope.series.splice(index, 1);
        });
    };
});


function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
}