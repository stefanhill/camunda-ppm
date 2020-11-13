ngDefine('cockpit.plugin.cockpit-plugin.process-prediction', function (module) {

    module.controller('processPredictionCtrl',
        ['$scope', '$http', 'Uri', 'ProcessData', 'ApiService', 'Globals',
            function ($scope, $http, Uri, ProcessData, ApiService, Globals) {

                let ctrl = $scope;
                ctrl.publicClassifiers = [];
                ctrl.activityPredictions = [];
                ctrl.timePrediction = null;
                ctrl.riskPrediction = null;
                ctrl.currentClassifier = null;
                ctrl.predictionException = {};
                ctrl.noClassifierWarning = true;

                ProcessData.initializeProcessInstanceData(ctrl.processInstance);


                loadPredictions = function (name) {
                    if(name === undefined){
                        let classifierQuery = $('#public-classifiers input:checked');
                        ProcessData.currentClassifier = null;

                        if (classifierQuery) {
                            ProcessData.currentClassifier = classifierQuery[0].value;
                        } else {
                            ProcessData.currentClassifier = ctrl.publicClassifiers.filter(x => x.def).map(x => x.name)[0];
                        }
                    } else {
                        ProcessData.currentClassifier = name
                    }
                    let predictionException = {},
                        activityPredictions = [],
                        activityMap = {},
                        timePrediction = null,
                        riskPrediction = null;
                    ApiService.getPrediction(ProcessData.processDefinitionId, ProcessData.currentClassifier, ProcessData.processInstanceId)
                            .then(function successCallback(response) {
                                let activityResponse = response.data.filter(function (e) {
                                    return e.type === "ActivityPrediction"
                                })[0];
                                if (activityResponse) {
                                    for (let activity in activityResponse.evals) {
                                        if (activityResponse.evals.hasOwnProperty(activity)) {
                                            let rounded_probability = Math.round(activityResponse.evals[activity] * 100);
                                            if (rounded_probability > 0) {
                                                activityPredictions.push({
                                                    activity: activity,
                                                    name: ProcessData.bpmnElementNames[activity],
                                                    probability: rounded_probability,
                                                });
                                                activityMap[activity] = rounded_probability;
                                            }
                                        }
                                    }

                                    activityPredictions.sort(function (a, b) {
                                        return b.probability > a.probability;
                                    });
                                    ProcessData.setActivityPredictions(activityMap);
                                    ProcessData.setNextActivityCandidates(Object.keys(activityMap));
                                } else {
                                    predictionException['ActivityPrediction'] = true;
                                }

                                let timeResponse = response.data.filter(function (e) {
                                    return e.type === 'TimePrediction'
                                })[0];
                                if (timeResponse) {
                                    timePrediction = Globals.msToHMS(timeResponse.evals['0.0']);
                                } else {
                                    predictionException['TimePrediction'] = true;
                                }

                                riskPrediction = null;
                                let riskResponse = response.data.filter(function (e) {
                                    return e.type === 'RiskPrediction'
                                })[0];
                                if (riskResponse) {
                                    riskPrediction = Math.floor(riskResponse.evals['0.0'] * 100);
                                } else {
                                    predictionException['RiskPrediction'] = true;
                                }

                                ctrl.predictionException = predictionException;
                                ctrl.activityPredictions = activityPredictions;
                                ctrl.timePrediction = timePrediction;
                                ctrl.riskPrediction = riskPrediction;

                            }, function errorCallback(response) {
                                console.log('Fetching activity prediction failed with response ', response);
                            });
                };

                ApiService.getPublicClassifiers(ProcessData.processDefinitionId)
                    .then(function successCallback(response) {
                            ctrl.publicClassifiers = [];
                            for (let elem of response.data) {
                                const name = elem.key['given_name'];
                                ApiService.getClassifierPredictionTypes(ProcessData.processDefinitionId, name)
                                    .then(function successCallback(ptResponse) {
                                        ctrl.publicClassifiers.push({
                                            name: name,
                                            version: elem.key['version'],
                                            def: elem.value,
                                            predictionTypeIcons: ptResponse.data.map(x => Globals.getPredictionTypeIconClass(x))
                                        });
                                        ctrl.publicClassifiers.sort((a, b) => {
                                            return a.name > b.name;
                                        });
                                        if (elem.value) {
                                            ProcessData.currentClassifier = name;
                                            loadPredictions(name);
                                        }
                                        ctrl.noClassifierWarning = ctrl.publicClassifiers.length === 0;
                                    })
                            }
                            // set default classifier if no one is specified in backend
                            if (ProcessData.currentClassifier == null && ctrl.publicClassifiers.length > 0) {
                                ProcessData.currentClassifier = ctrl.publicClassifiers[0].name;
                                ctrl.publicClassifiers[0].def = true;
                            }
                        },
                        function errorCallback(response) {
                            console.log("Initializing Defaults failed with response ", response)
                        });

                /* bugfixes */
                $('.sub-modal').appendTo('body');
            }
        ]
    )
});