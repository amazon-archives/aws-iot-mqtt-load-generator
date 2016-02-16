var mqttApp = angular.module('mqttApp', ['ngRoute', 'configControllers', 'templateControllers', 'metricsSeriesContollers', 'applicationServices', 'ngDialog']);

mqttApp.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/config', {
            templateUrl: 'partials/configlist.html',
            controller: 'ConfigListController'
        }).when('/config/:configId', {
            templateUrl: 'partials/configdetail.html',
            controller: 'ConfigDetailController'
        }).when('config/:configId/series/:seriesId', {
            templateUrl: '',
            controller: 'FunctionController'
        }).when('/template', {
            templateUrl: 'partials/templatelist.html',
            controller: 'TemplateListController'
        }).when('/template/:templateId', {
            templateUrl: 'partials/templateDetails.html',
            controller: 'TemplateDetailController'
        }).when('/series', {
            templateUrl: 'partials/serieslist.html',
            controller: 'MetricsSeriesListController'
        }).when('/series/:seriesId', {
            templateUrl: 'partials/seriesdetail.html',
            controller: 'MetricsSeriesDetailController'
        }).otherwise({redirectTo: '/config'});
    }]);

mqttApp.directive("contenteditable", function() {
  return {
    require: "ngModel",
    link: function(scope, element, attrs, ngModel) {

      function read() {
        ngModel.$setViewValue(element.html());
      }

      ngModel.$render = function() {
        element.html(ngModel.$viewValue || "");
      };

      element.bind("blur keyup change", function() {
        scope.$apply(read);
      });
    }
  };
});