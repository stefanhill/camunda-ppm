ngDefine('cockpit.plugin.cockpit-plugin.admin-globals', function (module) {
    module.controller('adminGlobalsCtrl',
        ['$scope', '$rootScope', '$http', 'Uri', 'AdminService',
            function ($scope, $rootScope, $http, Uri, AdminService, ) {
                console.log("Hello from adminGlobalsCtrl.js!");

                let ctrl = $scope,
                    resource = $scope.resource,
                    resourceName = resource.name,
                    userName = $scope.$root.userFullName;
                AdminService.currentResourceName = resourceName;
                AdminService.userName = userName;

                ctrl.hide_ppmTabs = function () {
                    let tabs_bar = null
                    setTimeout( () => {
                        tabs_bar = document.querySelector(".nav-tabs")
                        if(tabs_bar === null){
                            return
                        }
                        let tabs = Array.prototype.slice.call( tabs_bar.children )
                        let creation_tab = tabs.find( tab => "PPM Creation".includes(tab.innerText))
                        let training_tab = tabs.find( tab => "PPM Training".includes(tab.innerText))
                        let runtime_tab = tabs.find( tab => "PPM Runtime".includes(tab.innerText))
                        tabs_bar.removeChild(creation_tab)
                        tabs_bar.removeChild(training_tab)
                        tabs_bar.removeChild(runtime_tab)}
                        , 42
                    )

                }

                if ((resourceName.endsWith(".bpmn"))) {
                    let resourceConfig = AdminService.resourceConfigs[resourceName]
                    if (!resourceConfig) {
                        AdminService.initResource(resource)
                    }
                } else {
                    setTimeout( () => {
                        ctrl.hide_ppmTabs()
                        }, 42
                    )

                }

            }]
    )
});