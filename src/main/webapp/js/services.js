
var applicationServices = angular.module('applicationServices',[ 'ngResource']);

applicationServices.factory('Config', function($resource) {
    return $resource('webresources/config/:configId', {}, {
        'update' : { method: 'PUT' }
    });
});

applicationServices.factory('Series', function($resource){
    return $resource('webresources/series/:seriesId', {}, {});    
} );