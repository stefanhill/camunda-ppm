ngDefine('cockpit.plugin.cockpit-plugin.ppm-runtime', function (module) {

    module.controller('ppmRuntimeCtrl',
        ['$scope', '$http', 'Uri', 'AdminService', 'ApiService', 'Globals',
            function ($scope, $http, Uri, AdminService, ApiService, Globals) {

                let ctrl = $scope;
                ctrl.trainedClassifiers = [];
                ctrl.atomicClassifiers = [];
                ctrl.mergedClassifiers = [];
                ctrl.mergeButton = $('#merge-classifier');
                ctrl.classifierActionScope = null;
                ctrl.currentDefaultName = undefined

                ctrl.loadTrainedClassifiers = function () {
                    ApiService.getTrainedClassifiers(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.trainedClassifiers = [];
                            for (let elem in response.data) {
                                if (response.data.hasOwnProperty(elem)) {
                                    ctrl.trainedClassifiers.push({
                                        name: elem,
                                        predictionTypes: response.data[elem].map(x => Globals.getPredictionTypeIconClass(x))
                                    })
                                }
                            }
                            ctrl.trainedClassifiers.sort((a, b) => {
                                return a.name > b.name;
                            });
                        })
                };

                ctrl.loadAtomicClassifiers = function () {
                    ApiService.getAtomicClassifiers(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.atomicClassifiers = response.data.map(function (x) {
                                let isDefault = x.second;
                                if(isDefault){
                                    ctrl.currentDefaultName = x.first.given_name
                                }
                                return {
                                    name: x.first['given_name'],
                                    publicly: x.first['is_publicly_available'],
                                    version: x.first.version,
                                    def: x.second
                                }
                            });
                            ctrl.atomicClassifiers.sort((a, b) => {
                                return a.name > b.name;
                            });
                        });
                };

                ctrl.loadMergedClassifiers = function () {
                    ApiService.getMergedClassifiers(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.mergedClassifiers = response.data.map(function (x) {
                                let isDefault = x.second;
                                if(isDefault){
                                    ctrl.currentDefaultName = x.first.given_name
                                }
                                return {
                                    name: x.first.given_name,
                                    publicly: x.first.is_publicly_available,
                                    version: x.first.version,
                                    def: x.second
                                }
                            });
                            ctrl.mergedClassifiers.sort((a, b) => {
                                return a.name > b.name;
                            });
                        });
                };

                ctrl.loadMergedParams = function () {
                    let classifierList = [];
                    $('#trained-classifiers input:checked').each(function () {
                        classifierList.push($(this).val());
                    });
                    return {
                        classifierList: classifierList,
                        givenName: $('#merged-given-name').val()
                    }
                };

                ctrl.loadAll = function () {
                    ctrl.loadTrainedClassifiers();
                    ctrl.loadAtomicClassifiers();
                    ctrl.loadMergedClassifiers();
                };

                deleteMergedClassifier = function (e) {
                    ctrl.classifierActionScope = e.data('delete');
                    $('#confirmDeleteMergedModal').modal()
                };

                confirmDeleteMerged = function () {
                    ApiService.deleteClassifier(AdminService.currentResourceName, ctrl.classifierActionScope)
                        .then(function successCallback() {
                            ctrl.loadAll();
                        });
                }

                togglePubliclyClassifier = function (e) {
                    let classifierName = e.context.parentNode.innerText.trim()
                    if(ctrl.currentDefaultName === classifierName){
                        e[0].checked = true
                        return
                    }
                    ApiService.togglePubliclyClassifier(AdminService.currentResourceName, e.data('publicly'), e.is(':checked'))
                        .then(function successCallback() {
                            ctrl.loadAll();
                        });
                };

                checkRuntimeParameters = function () {
                    let csp = ctrl.loadMergedParams();

                    if (csp.classifierList.length <= 1 || csp.givenName === "") {
                        ctrl.mergeButton.attr('data-state', 'disabled');
                    } else if (ctrl.mergeButton.data('state') === 'disabled') {
                        ctrl.mergeButton.attr('data-state', 'enabled');
                    }
                };

                setDefaultClassifier = function (e) {
                    ApiService.setDefaultClassifier(AdminService.currentResourceName, e.data('def'))
                        .then(function successCallback() {
                            ctrl.loadAll();
                        });
                };

                ctrl.mergeButton.on('click', function () {

                    checkRuntimeParameters();
                    let paramData = ctrl.loadMergedParams();

                    if (paramData.classifierList.length >= 2 || paramData.givenName !== "") {
                        ctrl.mergeButton.attr('data-state', 'waiting');
                        ApiService.mergeClassifier(AdminService.currentResourceName, paramData.classifierList, paramData.givenName)
                            .then(function successCallback() {
                                ctrl.mergeButton.attr('data-state', 'success');
                                ctrl.loadAll();
                                setTimeout(function () {
                                    checkRuntimeParameters()
                                }, 4000)
                            });
                    }
                });


                ctrl.loadAll();

                /* bugfixes */
                $('.sub-modal').appendTo('body');

            }
        ]
    )
})
;