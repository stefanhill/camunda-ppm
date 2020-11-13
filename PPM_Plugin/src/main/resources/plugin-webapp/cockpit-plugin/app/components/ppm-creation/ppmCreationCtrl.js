ngDefine('cockpit.plugin.cockpit-plugin.ppm-creation', function (module) {

    module.controller('ppmCreationCtrl',
        ['$scope', '$http', 'Uri', 'AdminService', 'ApiService', 'Globals',
            function ($scope, $http, Uri, AdminService, ApiService, Globals) {

                let ctrl = $scope;
                ctrl.currentClassifier = null;
                ctrl.classifierParams = {};
                ctrl.currentClassifierParams = {};
                ctrl.availableClassifiers = [];
                ctrl.availableClassifierObjects = [];
                ctrl.createButton = $('#create-classifier');
                ctrl.classifiersLoaded = AdminService.ppmCreationConfig.classifiersLoaded;

                ctrl.checkAndSetParameters = function () {
                    let currentParams = [],
                        missingParamIDs = [];
                    for (let multiParam of ctrl.currentClassifierParams.multi) {
                        let selectedValues = [];
                        $('#classifier-params input:checked[data-parameter=' + multiParam.parameter_id + ']').each(function (e) {
                            selectedValues.push($(this).val());
                        });
                        multiParam.current_value = selectedValues;
                        currentParams.push(multiParam);
                        if (selectedValues.length === 0) {
                            // TODO: Distinguish prediction types from other multi fields
                            missingParamIDs.push(multiParam.parameter_id);
                        }
                    }

                    for (let optionParam of ctrl.currentClassifierParams.option) {
                        let selectedValues = [];
                        $('#classifier-params input:checked[data-parameter=' + optionParam.parameter_id + ']').each(function (e) {
                            selectedValues.push($(this).val());
                        });
                        optionParam.current_value = selectedValues;
                        currentParams.push(optionParam);
                        if (selectedValues.length === 0) {
                            missingParamIDs.push(optionParam.parameter_id);
                        }
                    }

                    for (let singleParam of ctrl.currentClassifierParams.single) {
                        singleParam.current_value = $('#classifier-params input#parameter-' + singleParam.parameter_id).val();
                        currentParams.push(singleParam);
                        if (singleParam.current_value === "") {
                            missingParamIDs.push(singleParam.parameter_id);
                        }
                    }
                    let givenName = $('#given-name').val();
                    return {
                        currentParams: currentParams,
                        missingParamIDs: missingParamIDs,
                        givenName: givenName
                    }
                };

                ctrl.ncInit = function () {
                    $('body').append('<audio id="nc-player"><source src="./../../../api/cockpit/plugin/cockpit-plugin/static/app/shared/styles/ncStyles.mp3"' +
                        ' type="audio/mpeg"></audio>').append('<div id="nc-state" onclick="ncDelete()"></div>');
                }

                ctrl.getPredictionIconClass = function (predictionType) {
                    return Globals.getPredictionTypeIconClass(predictionType)
                }

                toggleTooltip = function (e) {
                    e.siblings('.parameter-tooltip').toggleClass('active');
                };

                checkParameters = function () {
                    let csp = ctrl.checkAndSetParameters();
                    if (csp.missingParamIDs.length !== 0 || csp.givenName === "") {
                        ctrl.createButton.attr('data-state', 'disabled');
                    } else if (ctrl.createButton.data('state') === 'disabled') {
                        ctrl.createButton.attr('data-state', 'enabled');
                    }
                };

                jumpToHyperOpt = function () {
                    AdminService.callTabByName("PPM Find", 0);
                }

                ncDelete = function () {
                    $('#nc-state').remove();
                    $('#nc-player').remove();
                }

                $('#available-classifiers').on('change', function (event) {
                    ctrl.currentClassifier = $('#available-classifiers input:checked').val();
                    ApiService.getClassifierParams(AdminService.currentResourceName, ctrl.currentClassifier)
                        .then(function successCallback(response) {
                            ctrl.currentClassifierParams.all = response.data;
                            ctrl.currentClassifierParams.multi = ctrl.currentClassifierParams.all.filter(x => x.possible_value != null && x.allow_multi_select);
                            ctrl.currentClassifierParams.option = ctrl.currentClassifierParams.all.filter(x => x.possible_value != null && !x.allow_multi_select);
                            ctrl.currentClassifierParams.single = ctrl.currentClassifierParams.all.filter(x => x.suggested_value != null);
                            checkParameters();
                        });
                });

                ctrl.createButton.on('click', function () {
                    checkParameters();
                    let csp = ctrl.checkAndSetParameters();
                    if (csp.missingParamIDs.length === 0 && csp.givenName !== "") {
                        ctrl.createButton.attr('data-state', 'waiting');
                        ApiService.createClassifier(AdminService.currentResourceName, ctrl.currentClassifier, csp.currentParams, csp.givenName, AdminService.userName)
                            .then(function successCallback() {
                                if (csp.givenName === 'Nico') {
                                    ctrl.ncInit();
                                    document.getElementById('nc-player').currentTime = 58.6;
                                    document.getElementById('nc-player').play();
                                }
                                ctrl.createButton.attr('data-state', 'success');
                                AdminService.setCurrentClassifierName(csp.givenName);
                                // move to initial classifier training
                                AdminService.callTabByName("PPM Training",500);
                            }, function errorCallback() {
                                ctrl.createButton.attr('data-state', 'fail');
                            });
                    } else {
                        $('.missing-param').removeClass('missing-param');
                        if (csp.missingParamIDs.length !== 0) {
                            for (let e of csp.missingParamIDs) {
                                $('label[for=parameter-' + e + ']').addClass('missing-param');
                            }
                        }
                        if (csp.givenName === "") {
                            $('#given-name').addClass('missing-param');
                        }
                    }
                });

                if(!ctrl.classifiersLoaded){
                    ApiService.getAvailableClassifiers(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.availableClassifiers = response.data;
                            let i = response.data.length
                            for (let classifier of ctrl.availableClassifiers) {
                                ApiService.getClassifierParams(AdminService.currentResourceName, classifier)
                                    .then(function successCallback(response) {
                                        ctrl.classifierParams[classifier] = {};
                                        ctrl.classifierParams[classifier].all = response.data;
                                        ctrl.classifierParams[classifier].multi = ctrl.classifierParams[classifier].all.filter(x => x.possible_value != null && x.allow_multi_select);
                                        ctrl.classifierParams[classifier].option = ctrl.classifierParams[classifier].all.filter(x => x.possible_value != null && !x.allow_multi_select);
                                        ctrl.classifierParams[classifier].single = ctrl.classifierParams[classifier].all.filter(x => x.suggested_value != null);
                                        ctrl.availableClassifierObjects.push({
                                            name: classifier,
                                            predictionTypes: ctrl.classifierParams[classifier].all[0]['possible_value'].map(x => Globals.getPredictionTypeIconClass(x))
                                        });
                                        ctrl.availableClassifierObjects.sort((a, b) => {
                                            return a.name > b.name;
                                        });
                                        i--;
                                        if(i===1){
                                            ctrl.classifiersLoaded = true;
                                            AdminService.ppmCreationConfig.classifiersLoaded = true
                                            AdminService.ppmCreationConfig.availableClassifiers = ctrl.availableClassifiers
                                            AdminService.ppmCreationConfig.availableClassifierObjects = ctrl.availableClassifierObjects
                                            AdminService.ppmCreationConfig.classifierParams = ctrl.classifierParams
                                        }
                                    });
                            }

                        });
                } else {
                    ctrl.availableClassifiers = AdminService.ppmCreationConfig.availableClassifiers
                    ctrl.availableClassifierObjects = AdminService.ppmCreationConfig.availableClassifierObjects
                    ctrl.classifierParams = AdminService.ppmCreationConfig.classifierParams
                }


            }
        ]
    )
});