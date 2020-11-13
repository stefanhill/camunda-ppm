ngDefine('cockpit.plugin.cockpit-plugin.ppm-training', function (module) {

    module.controller('ppmTrainingCtrl',
        ['$scope', '$rootScope', '$http', 'Uri', 'AdminService', 'ApiService',
            function ($scope, $rootScope, $http, Uri, AdminService, ApiService) {

                console.log("Hello from ppmTrainingCtrl.js!");
                let ctrl = $scope;
                ctrl.AdminService = AdminService;
                ctrl.resourceName = AdminService.currentResourceName;
                ctrl.trainableClassifiers = undefined;
                ctrl.configs = undefined;
                ctrl.currentConfig = undefined;
                ctrl.currentClassifierMeta = undefined;
                ctrl.currentClassifierParams = undefined;
                ctrl.currentClassifierName = undefined;
                ctrl.activeClassifierElem = undefined;
                ctrl.currentClassifierVersionRange = undefined;
                ctrl.targetVersion = null;


                ctrl.initialize = function () {
                    ctrl.load();
                };

                ctrl.load = function () {
                    ApiService.getTrainableClassifiers(ctrl.resourceName)
                        .then(function successCallback(response) {
                            let resourceName = ctrl.resourceName;
                            ctrl.trainableClassifiers = [];
                            for (let elem of response.data) {
                                ctrl.trainableClassifiers.push({
                                    name: elem.first['given_name'],
                                    version: elem.first.version
                                });
                            }
                            ctrl.trainableClassifiers.sort((a, b) => {
                                return a.name > b.name;
                            });
                            // initialize configs for all available classifiers
                            AdminService.updatePpmTrainingConfigs(resourceName, ctrl.trainableClassifiers.map(x => x.name));
                            // fetch classifier configs to local scope
                            ctrl.getConfigs();
                            ctrl.updateCurrentClassifier();
                        });
                };

                ctrl.updateTrainableClassifiers = function () {
                    ApiService.getTrainableClassifiers(ctrl.resourceName)
                        .then(function successCallback(response) {
                            ctrl.trainableClassifiers = [];
                            for (let elem of response.data) {
                                ctrl.trainableClassifiers.push({
                                    name: elem.first['given_name'],
                                    version: elem.first.version
                                });
                            }
                            ctrl.trainableClassifiers.sort((a, b) => {
                                return a.name > b.name;
                            });
                        });
                }

                ctrl.getConfigs = function () {
                    let resourceConfig = AdminService.resourceConfigs[ctrl.resourceName];
                    ctrl.currentClassifierName = resourceConfig.ppmTrainingConfig.currentClassifierName;
                    ctrl.configs = resourceConfig.ppmTrainingConfig.classifierConfigs;
                    ctrl.currentConfig = ctrl.configs[ctrl.currentClassifierName];
                };

                ctrl.saveConfigs = function () {
                    ctrl.configs[ctrl.currentClassifierName] = ctrl.currentConfig;
                    let resourceConfig = AdminService.resourceConfigs[ctrl.resourceName];
                    resourceConfig.ppmTrainingConfig.currentClassifierName = ctrl.currentClassifierName;
                    resourceConfig.ppmTrainingConfig.classifierConfigs = ctrl.configs
                };

                ctrl.clearCurrentConfig = function() {
                    ctrl.saveConfigs()
                    ctrl.currentConfig = undefined;
                    ctrl.currentClassifierMeta = undefined;
                    ctrl.currentClassifierParams = undefined;
                    ctrl.currentClassifierName = undefined;
                    ctrl.activeClassifierElem = undefined;
                    ctrl.currentClassifierVersionRange = undefined;
                    ctrl.targetVersion = null;
                }

                ctrl.$on("$destroy", function () {
                    ctrl.saveConfigs();
                    clearInterval(ctrl.trainingDurationListener)
                });

                ctrl.setTrainingDuration = function(duration){
                    let trainingDurationElement = document.getElementById("training-duration")
                    if(!trainingDurationElement){
                        return
                    }
                    trainingDurationElement.innerHTML = duration ? duration : "00:00:000"

                }

                ctrl.setAcurracy = function(acurracy){
                    let acurracyElement = document.getElementById("acurracy")
                    if(!acurracyElement){
                        return
                    }
                    acurracyElement.innerHTML = acurracy  ? acurracy : "0 %"
                }

                ctrl.manageTrainingDurationListener = function () {
                    if (ctrl.currentConfig === undefined){
                        return
                    }
                    let trainingDurationElement = document.getElementById("training-duration")
                    if (ctrl.trainingDurationListener) {
                        clearInterval(ctrl.trainingDurationListener)
                    }
                    if(!ctrl.currentConfig.trainingState == "waiting") {
                        ctrl.setTrainingDuration(ctrl.currentConfig.trainingDuration);
                        return
                    }
                    ctrl.trainingDurationListener = setInterval(function () {
                        ctrl.setTrainingDuration(ctrl.currentConfig.trainingDuration)
                        ctrl.setAcurracy(ctrl.currentConfig.acurracy)
                    }, 50);
                };


                ctrl.updateCurrentClassifier = function () {
                    ctrl.manageTrainingDurationListener();
                    let givenName = ctrl.currentClassifierName;
                    if (!givenName) {
                        return;
                    }
                    ApiService.getClassifierMeta(ctrl.resourceName, givenName)
                        .then(function successCallback(metaResponse) {
                            let acurracy = Math.floor(metaResponse.data.acurracy*10000)/100 + " %"
                            ctrl.currentConfig.acurracy = acurracy
                            ctrl.currentClassifierMeta = metaResponse.data
                            ctrl.updateVersionRange();
                            updateTrainTestRatio();
                            ApiService.getClassifierInstanceParams(ctrl.resourceName, givenName)
                                .then(function successCallback(paramsResponse) {
                                    ctrl.currentClassifierParams = paramsResponse.data;
                                    ctrl.$digest()
                                });
                        });
                };

                ctrl.updateVersionRange = function () {
                    if (!ctrl.currentClassifierMeta) {
                        console.log("failed updateVersionRange, currentClassifierMeta was empty");
                    }
                    let version = ctrl.currentClassifierMeta.version;
                    ctrl.currentClassifierVersionRange = [...Array(version).keys()].slice(1); // get versions 1.. version-1
                };

                ctrl.getCurrentClassifierCreationTimeString = function () {
                    if (!(ctrl.currentClassifierMeta && ctrl.currentClassifierMeta.creationTime)) {
                        return ""
                    }
                    let dateString = ctrl.currentClassifierMeta.creationTime;
                    let date = new Date(dateString);
                    return date.toLocaleString();
                };


                ctrl.getCurrentClassifierLastModifiedString = function () {
                    if (!(ctrl.currentClassifierMeta && ctrl.currentClassifierMeta.lastModified)) {
                        return ""
                    }
                    let dateString = ctrl.currentClassifierMeta.lastModified;
                    let date = new Date(dateString);
                    return date.toLocaleString();
                };

                ctrl.getTrainingDataButtonClasses = function (source) {
                    let classes = "admin-button";
                    if (ctrl.currentConfig && !ctrl.isTrainingRunning) {
                        if (source === "history" &&
                            (ctrl.currentConfig.trainWithHistoryData
                                || ctrl.currentConfig.trainWithHistoryDataAndTimeFrame)) {
                            classes += " active-button"
                        } else if (source === "xes"
                            && ctrl.currentConfig.trainWithXESFile) {
                            classes += " active-button"
                        }
                    }
                    return classes
                };

                ctrl.startCountingTrainingDuration = function (config) {
                    let start = new Date().getTime();
                    config.trainingStopwatch
                        = setInterval(function () {
                        let now = new Date().getTime(),
                            distance = now - start,
                            minutes = Math.floor(distance / 60000),
                            seconds = Math.floor((distance % 60000) / 1000),
                            miliseconds = distance % 1000;

                        minutes = minutes < 10 ? "0" + minutes : minutes;
                        seconds = seconds < 10 ? "0" + seconds : seconds;
                        for (let i = 0; i < 3-miliseconds.toString().length; i++){
                            miliseconds = "0" + miliseconds;
                        }
                        let trainingDuration = minutes + ":" + seconds + ":" + miliseconds;
                        config.trainingDuration = trainingDuration
                        /*if(config.classifierName === ctrl.currentClassifierName){
                            document.getElementById("training-duration").innerHTML = trainingDuration
                        }*/


                        // Update the timer every 50 milliseconds
                    }, 50);
                };

                ctrl.stopCountingTrainingDuration = function (config) {
                    clearInterval(config.trainingStopwatch);
                };

                /*
                * Event Handler
                  */
                onChangeCurrentClassifier = function (e) {
                    let formerClassifierName = ctrl.currentClassifierName;
                    if (!(formerClassifierName === null)) {
                        // save config of former classifier
                        ctrl.configs[formerClassifierName] = ctrl.currentConfig;
                    }
                    ctrl.currentClassifierName = e.val();
                    ctrl.currentConfig = ctrl.configs[ctrl.currentClassifierName];
                    //$('#train-test-ratio').value = ctrl.currentConfig.trainingRatio;
                    ctrl.updateCurrentClassifier();
                };

                onClickHistoryData = function () {
                    let config = ctrl.currentConfig;
                    config.trainWithXESFile = false;
                    config.xesFile = null;
                    if (config.trainWithHistoryData || config.trainWithHistoryDataAndTimeFrame) {
                        config.trainWithHistoryData = false;
                        config.trainingDataSpecified = false;
                    } else {
                        config.trainWithHistoryData = true;
                        config.trainingDataSpecified = true;
                    }
                    config.trainWithHistoryDataAndTimeFrame = false;
                    //ctrl.currentConfig = {...config};
                    // dirty-check for view update
                    ctrl.$digest();
                };

                onClickSpecifyTimeFrame = function () {
                    let config = {...ctrl.currentConfig};
                    config.trainWithXESFile = false;
                    config.xesFile = null;
                    config.trainWithHistoryData = config.trainWithHistoryDataAndTimeFrame;
                    config.trainingDataSpecified = true;
                    config.disableDataSelection = false;
                    ctrl.currentConfig = {...config};
                    // dirty-check for view update
                    ctrl.$digest();
                };

                onSelectXES = function () {
                    // TODO: catch if file not of .xes format
                    let xesFile = document.getElementById("xes-file").files[0];
                    if (xesFile === null) {
                        return;
                    }
                    let trainingButton = document.getElementById("training-button");
                    trainingButton.disabled = false;
                    ctrl.currentConfig.xesFile = xesFile;
                    ctrl.currentConfig.trainWithXESFile = true;
                    ctrl.currentConfig.trainWithHistoryData = false;
                    ctrl.currentConfig.trainWithHistoryDataAndTimeFrame = false;
                    ctrl.currentConfig.trainingDataSpecified = true;
                    ctrl.currentConfig.disableDataSelection = false;
                    ctrl.$digest();
                };


                onClickRevertClassifier = function () {
                    ctrl.targetVersion = +$('#revert-versions').val();
                    $('#confirmRevertModal').modal();
                };

                confirmRevert = function () {
                    if (ctrl.targetVersion !== null) {
                        ApiService.revertClassifier(ctrl.resourceName, ctrl.currentClassifierName, ctrl.targetVersion).then(
                            function successCallback(response) {
                                ctrl.updateCurrentClassifier();
                                ctrl.updateTrainableClassifiers();
                                ctrl.currentClassifierMeta = response.data;
                            }
                        )
                    }
                };

                onClickDeleteClassifier = function () {
                    $('#confirmDeleteModal').modal();
                };

                confirmDelete = function () {
                    ApiService.deleteClassifier(ctrl.resourceName, ctrl.currentClassifierName).then(
                        function successCallback() {
                            ctrl.updateTrainableClassifiers();
                            ctrl.clearCurrentConfig();
                        }
                    )
                };

                onClickRenameClassifier = function () {
                    $('#renameModal').modal();
                    setTimeout(
                        () => $('#rename-input').focus(),
                        500
                    )
                };

                onClickCopyClassifier = function () {
                    $('#confirmCopyModal').modal();
                    setTimeout(
                        () => $('#copy-given-name').focus(),
                        500
                    )
                };

                confirmRename = function() {
                    let oldName = ctrl.currentConfig.classifierName
                    let newName = $('#rename-input').val();
                    ctrl.currentClassifierName = newName
                    ctrl.currentConfig.classifierName = newName
                    ctrl.trainableClassifiers.find( tc => tc.name === oldName).name = newName
                    ctrl.$digest()
                    // remember config of renamed classifier
                    ApiService.renameClassifier(ctrl.resourceName, oldName, newName).then(
                        function successCallback(response){
                            ctrl.updateTrainableClassifiers()
                            ctrl.updateCurrentClassifier()
                        }
                    )
                }

                confirmCopy = function () {
                    let copyGivenName = $('#copy-given-name').val();
                    ApiService.copyClassifier(ctrl.resourceName, ctrl.currentClassifierName, copyGivenName).then(
                        function successCallback(response) {
                            ctrl.updateTrainableClassifiers();
                            ctrl.configs[copyGivenName] = AdminService.initializeClassifierTrainingConfig(ctrl.resourceName, copyGivenName);
                        }
                    )
                };

                updateTrainTestRatio = function () {
                    let ratio = $('#train-test-ratio').val();
                    $('#train-ratio').html(ratio + '%');
                    $('#test-ratio').html((100 - ratio) + '%');
                };

                toggleClassifierElem = function (e) {
                    ctrl.activeClassifierElem = e.data('classifier-name');
                    $('.list-label-wrapper').removeClass('active');
                    e.closest('.list-label-wrapper').addClass('active');
                };

                onClickTraining = function () {
                    let config = ctrl.currentConfig;
                    if (config.trainingDataSpecified && !config.isTrainingRunning
                        && !config.isTrainingCanceled && !config.isTrainingFinished) {
                        config.trainingState = "waiting";
                        config.disableDataSelection = true;
                        ctrl.startCountingTrainingDuration(config);
                        ctrl.startTraining();
                        ctrl.$digest();
                        return;
                    }
                    if (config.trainingDataSpecified && config.isTrainingRunning) {
                        ctrl.stopCountingTrainingDuration(config);
                        config.isTrainingRunning = false;
                        config.isTrainingCanceled = true;
                        config.trainingState = "finished";
                        ctrl.stopTraining();
                        ctrl.$digest();
                        return;
                    }
                    // else: reset view
                    config.trainingState = "enabled";
                    config.disableDataSelection = false;
                    ctrl.resetTrainingForms()
                    ctrl.setTrainingDuration();
                    ctrl.$digest();
                };

                ctrl.resetTrainingForms = function(){
                    // remember acurracy
                    let acurracy = ctrl.currentConfig.acurracy
                    ctrl.currentConfig = AdminService.initializeClassifierTrainingConfig(ctrl.resourceName, ctrl.currentClassifierName);
                    ctrl.currentConfig.acurracy = acurracy
                }

                ctrl.startTraining = function () {
                    ctrl.manageTrainingDurationListener()
                    let givenName = ctrl.currentClassifierName;
                    let config = ctrl.currentConfig;
                    let ratio = $('#train-test-ratio').val() / 100;
                    config.isTrainingRunning = true;
                    if (config.trainWithHistoryData) {
                        ApiService.trainClassifier(ctrl.resourceName, givenName, ratio,
                            ctrl.successTrainingCallback,
                            ctrl.errorTrainingCallback);
                        return;
                    }
                    if (config.trainWithHistoryDataAndTimeFrame) {
                        ApiService.trainClassifierWithTimeframe(ctrl.resourceName, givenName, ratio,
                            ctrl.currentConfig.historyDataFrom,
                            ctrl.currentConfig.historyDataUntil,
                            ctrl.successTrainingCallback,
                            ctrl.errorTrainingCallback
                        )
                        return;
                    }
                    ApiService.trainClassifierWithFile(ctrl.resourceName,
                        ctrl.currentClassifierName, ctrl.currentConfig.xesFile, ratio,
                        ctrl.successTrainingCallback,
                        ctrl.errorTrainingCallback)
                };

                ctrl.successTrainingCallback = function (config) {
                    ctrl.updateCurrentClassifier();
                    ctrl.updateTrainableClassifiers();
                    ctrl.trainingCallback(config);
                };

                ctrl.errorTrainingCallback = function (config) {
                    alert("The training of classifier " + config.classifierName +
                        " was not successful. For further information, please examine the server log of your Camunda application.")
                    ctrl.trainingCallback(config);
                };

                ctrl.trainingCallback = function (config) {
                    if(config.classifierName == ctrl.currentClassifierName) {
                        config = ctrl.currentConfig;
                    }
                    config.trainingState = "finished";
                    config.isTrainingRunning = false;
                    config.isTrainingFinished = true;
                    ctrl.stopCountingTrainingDuration(config);
                    ctrl.updateCurrentClassifier();
                    ctrl.updateTrainableClassifiers();
                };

                ctrl.stopTraining = function () {
                    ApiService.stopTraining(ctrl.resourceName, ctrl.currentClassifierName);
                    ctrl.updateTrainableClassifiers();
                };

                $('.info-collapse-label').on('click', function () {
                    $(this).next('.info-collapse-list').slideToggle(300);
                    $(this).toggleClass('active');
                });

                /*
                * Executed Part
                */
                ctrl.load();
                /* bugfixes */
                $('.info-collapse-list').hide();
                $('.sub-modal').appendTo('body');
            }
        ]
    )
});