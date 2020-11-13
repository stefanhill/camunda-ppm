ngDefine('cockpit.plugin.cockpit-plugin.hyper-opt', function (module) {

    module.controller('hyperOptCtrl',
        ['$scope', '$http', 'Uri', 'AdminService', 'ApiService', 'Globals',
            function ($scope, $http, Uri, AdminService, ApiService, Globals) {

                let ctrl = $scope;
                ctrl.testList = [];
                ctrl.possibleClassifier = [];
                ctrl.possibleMetrics = [];
                ctrl.possibleOptimizer = [];
                ctrl.currentOptimizer = null;
                ctrl.currentOptimizerParameter = {
                    multi: [],
                    option: [],
                    single: [],
                    show: false
                };
                ctrl.config = {
                    xesFile: null,
                    timeFrameOptions: {
                        singleDatePicker: true,
                        locale: {
                            format: 'D-MMMM-YY hh:mm A',
                        },
                        opens: 'left',
                        drops: 'up'
                    },
                    historyDataFrom: null,
                    historyDataUntil: null
                }
                ctrl.createButton = $('#add-test');
                ctrl.testCounter = 0;

                ctrl.loadCreatePanel = function () {
                    /*
                     * classifier: {
                     *  name,
                     *  parameter: {
                    *           text,
                    *           type: single|list,
                    *           from,
                    *           to,
                    *           steps,
                    *           options
                    *       }
                     */

                    ApiService.getPossibleClassifierParameterRange(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            for (let elem in response.data) {
                                if (response.data.hasOwnProperty(elem)) {
                                    ctrl.possibleClassifier.push({
                                        name: elem,
                                        parameter: {
                                            multi: response.data[elem].filter(x => x['from'] == null),
                                            single: response.data[elem].filter(x => x['from'] != null)
                                        }
                                    });
                                }
                            }
                        });
                    ApiService.getPossibleMetrics(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.possibleMetrics = response.data;
                        });
                    ApiService.getPossibleOptimizer(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            for (let elem in response.data) {
                                if (response.data.hasOwnProperty(elem)) {
                                    ctrl.possibleOptimizer.push({
                                        name: elem,
                                        parameter: response.data[elem]
                                    });
                                }
                            }
                        });
                };


                ctrl.loadTests = function () {
                    ctrl.testList = [];
                    ApiService.getTestList(AdminService.currentResourceName)
                        .then(function successCallback(response) {
                            ctrl.testCounter = 0;
                            for (let test of response.data) {
                                ctrl.testCounter++;
                                let t = {};
                                t.name = test.first;
                                t.numFinished = test.third;
                                t.state = test.second;
                                if (t.state === 'success') {
                                    ApiService.getTestInformation(AdminService.currentResourceName, t.name)
                                        .then(function informationCallback(informationResponse) {
                                            t.classifiers = [];
                                            for (let classifier in informationResponse.data) {
                                                if (informationResponse.data.hasOwnProperty(classifier)) {
                                                    let parameter = informationResponse.data[classifier]['second'],
                                                        c = {
                                                            name: classifier.split('.').pop(),
                                                            parameter: parameter.map(x => {
                                                                return {
                                                                    text: x.text,
                                                                    options: '[' + x.options.reduce((x, y) => {
                                                                        return x.toString() + '], [' + y.toString()
                                                                    }) + ']'
                                                                }
                                                            })
                                                        };
                                                    t.classifiers.push(c);
                                                }
                                            }
                                            ApiService.getTestResults(AdminService.currentResourceName, t.name)
                                                .then(function resultCallback(resultResponse) {
                                                    t.results = [];
                                                    for (let entry of resultResponse.data) {
                                                        let r = {
                                                                name: entry['classifierName'],
                                                                metric: entry['metric']['key'],
                                                                metricValue: Math.round(entry['metric']['value'] * 100) / 100,
                                                                id: entry['runID'],
                                                                runTime: Math.round(entry['runTime']),
                                                                config: []
                                                            },
                                                            configuration = entry['configuration'];
                                                        for (let c in configuration) {
                                                            if (configuration.hasOwnProperty(c))
                                                                r.config.push({
                                                                    text: c,
                                                                    value: configuration[c]
                                                                });
                                                        }
                                                        t.results.push(r);
                                                    }
                                                    ctrl.testList.push(t);
                                                });
                                        });
                                } else {
                                    ctrl.testList.push(t);
                                }
                            }
                        });
                };

                uploadXES = function () {
                    ctrl.config.xesFile = document.getElementById("opt-xes-file").files[0];
                }

                selectTimeFrame = function () {
                    ctrl.config.xesFile = null;
                    let history = $('#opt-history-data')
                    history.prop('checked', true);
                    history.trigger('click');
                }

                updateTrainTestRatio = function () {
                    let ratio = $('#opt-train-test-ratio').val();
                    $('#opt-train-ratio').html(ratio + '%');
                    $('#opt-test-ratio').html((100 - ratio) + '%');
                };


                showOptimizerParameter = function (e) {
                    ctrl.currentOptimizer = e.val();
                    let cop = ctrl.possibleOptimizer.filter(x => x.name === ctrl.currentOptimizer)[0]['parameter'];
                    ctrl.currentOptimizerParameter.multi = cop.filter(x => x['possible_value'] != null && x['allow_multi_select']);
                    ctrl.currentOptimizerParameter.option = cop.filter(x => x['possible_value'] != null && !x['allow_multi_select']);
                    ctrl.currentOptimizerParameter.single = cop.filter(x => x['suggested_value'] != null);
                    ctrl.currentOptimizerParameter.show = cop.length > 0;
                    ctrl.$digest();
                };

                toggleClassifierParams = function (e) {
                    e.parent().next('.opt-classifier-params-wrapper').toggle();
                }

                toggleTooltip = function (e) {
                    e.siblings('.parameter-tooltip').toggleClass('active');
                };

                ctrl.getPredictionIconClass = function (predictionType) {
                    return Globals.getPredictionTypeIconClass(predictionType)
                }

                ctrl.getInnerParameter = function (parameterID, classifierName) {
                    return ctrl.possibleClassifier.filter(x => x.name === classifierName)[0]
                        .parameter.multi.filter(x => x['parameter_id'] === parameterID)[0].options[0];
                }

                ctrl.getInnerConfiguration = function (testName, classifierName) {
                    return ctrl.testList.filter(x => x.name === testName)[0]
                        .classifiers.filter(x => x.name === classifierName)[0].parameter;
                }

                ctrl.getInnerResult = function (testName, id) {
                    return ctrl.testList.filter(x => x.name === testName)[0]
                        .results.filter(x => x.id === id)[0].config;
                }

                deleteTest = function (e) {
                    ApiService.deleteTest(AdminService.currentResourceName, e.data('delete'))
                        .then(function successCallback() {
                            ctrl.loadTests();
                        });
                }

                pauseTest = function (e) {
                    ApiService.pauseTest(AdminService.currentResourceName, e.data('pause'))
                        .then(function successCallback() {
                            ctrl.loadTests();
                        });
                }

                ctrl.checkAndSetOptParameters = function () {
                    let testName = $('#test-name').val(),
                        saveNumber = $('#save-number').val(),
                        metric = $('.possible-metrics input[name="possible-metric"]:checked').val(),
                        trainingMode = $('#history-data-with-timeframe').prop('checked') ? 'timeframe' : $('input[name="training-mode"]:checked').val(),
                        trainingConfig = {
                            historyFrom: ctrl.config.historyDataFrom,
                            historyUntil: ctrl.config.historyDataUntil,
                            xesFile: ctrl.config.xesFile,
                        },
                        classifiers = {},
                        optimizer = {},
                        valid = true;

                    for (let classifier of ctrl.possibleClassifier) {
                        let parameters = [];
                        if ($('#possible-' + classifier.name).prop('checked')) {
                            for (let parameter of classifier.parameter.multi) {
                                parameter.options[0] = $('*[data-classifier="' + classifier.name + '"] *[data-parameter-id="' + parameter.parameter_id + '"] input:checked').map(function () {
                                    return this.value
                                }).get();
                                parameters.push(parameter);
                                valid = valid && parameter.options.length > 0;
                            }
                            for (let parameter of classifier.parameter.single) {
                                parameter.from = $('*[data-classifier="' + classifier.name + '"] .from-param[data-parameter="' + parameter.parameter_id + '"]').val();
                                parameter.to = $('*[data-classifier="' + classifier.name + '"] .to-param[data-parameter="' + parameter.parameter_id + '"]').val();
                                parameter.stepping = $('*[data-classifier="' + classifier.name + '"] .stepping-param[data-parameter="' + parameter.parameter_id + '"]').val();
                                parameters.push(parameter);
                                valid = valid && parameter.from !== "" && parameter.to !== "" && parameter.stepping !== "";
                            }
                            classifiers[classifier.name] = parameters;
                        }
                    }

                    let optimizerParams = [];
                    for (let parameter of ctrl.currentOptimizerParameter.multi) {
                        parameter.current_value = $('#optimizer-params .multi-param input[data-parameter="' + parameter.parameter_id + '"]:checked').map(function () {
                            return this.value;
                        }).get();
                        optimizerParams.push(parameter);
                        valid = valid && parameter.current_value.length > 0;
                    }
                    for (let parameter of ctrl.currentOptimizerParameter.option) {
                        parameter.current_value = $('#optimizer-params .option-param input[data-parameter="' + parameter.parameter_id + '"]:checked').map(function () {
                            return this.value;
                        }).get();
                        optimizerParams.push(parameter);
                        valid = valid && parameter.current_value.length > 0;
                    }
                    for (let parameter of ctrl.currentOptimizerParameter.single) {
                        parameter.current_value = $('#optimizer-params .single-param input[data-parameter="' + parameter.parameter_id + '"]').val();
                        optimizerParams.push(parameter);
                        valid = valid && parameter.current_value !== "";
                    }
                    optimizer[ctrl.currentOptimizer] = optimizerParams;

                    valid = valid && trainingMode != null && testName !== "" && saveNumber !== "" && metric != null && ctrl.currentOptimizer != null;
                    valid = valid && Object.keys(optimizer).length > 0 && Object.keys(classifiers).length > 0;

                    if (trainingMode === 'timeframe') {
                        valid = valid && trainingConfig.historyUntil != null && trainingConfig.historyFrom != null;
                    }
                    if (trainingMode === 'xes') {
                        valid = valid && trainingConfig.xesFile !== "";
                    }

                    return {
                        valid: valid,
                        testName: testName,
                        trainingRatio: +$('#opt-train-test-ratio').val() / 100,
                        metric: metric,
                        saveNumber: saveNumber,
                        trainingConfig: trainingConfig,
                        trainingMode: trainingMode,
                        classifiers: classifiers,
                        optimizer: optimizer
                    }
                }

                checkOptParameters = function () {
                    let csp = ctrl.checkAndSetOptParameters();
                    if (csp.valid && ctrl.createButton.attr('data-state') === 'disabled') {
                        ctrl.createButton.attr('data-state', 'enabled');
                    } else if (!csp.valid) {
                        ctrl.createButton.attr('data-state', 'disabled');
                    }
                }

                resetCreatePanel = function () {
                    $('#test-name').val('');
                    $('.history-data-form input').val('');
                    $('#opt-history-data').prop('checked', false);
                    $('#opt-xes-file-proxy').prop('checked', false);
                    $('input[name="possible-metric"]').prop('checked', false);
                    $('input[name="possible-classifier"]:checked').each(function () {
                        $(this).prop('checked', false);
                        toggleClassifierParams($(this));
                    });
                    $('input[name="optimizer"]').prop('checked', false);
                    ctrl.currentOptimizerParameter.show = false;
                    ctrl.createButton.attr('data-state', 'disabled');
                }

                reloadTests = function () {
                    ctrl.loadTests();
                };

                toggleAddTest = function () {
                    $('#add-test-wrapper').toggle();
                };

                toggleTestInformation = function (e) {
                    e.next('.test-information-wrapper').toggle(300);
                    e.toggleClass('active');
                }

                toggleResultInformation = function (e) {
                    e.next('.result-body').toggle(300);
                    e.toggleClass('active');
                }

                toggleElementList = function (e) {
                    e.next('.info-collapse-list').toggle(300);
                    e.toggleClass('active');
                }

                ctrl.clearTestForm = function () {
                    $('#add-test-wrapper input[type="text"]').val('');
                    $('#add-test-wrapper input[type="radio"]:checked').prop('checked', false);
                    $('#add-test-wrapper input[type="checkbox"]:checked').prop('checked', false);
                }

                createTest = function () {
                    let csp = ctrl.checkAndSetOptParameters();
                    if (csp.valid && ctrl.createButton.attr('data-state') === 'enabled') {
                        //ctrl.clearTestForm();
                        ctrl.createButton.attr('data-state', 'waiting');
                        let successCallback = function () {
                            ctrl.createButton.attr('data-state', 'success');
                            setTimeout(function () {
                                $('#add-test-wrapper').hide();
                                resetCreatePanel();
                                ctrl.loadTests();
                            }, 1000);
                        };
                        let errorCallback = function () {
                            ctrl.createButton.attr('data-state', 'fail');
                        };
                        switch (csp.trainingMode) {
                            case 'history':
                                ApiService.createTest(AdminService.currentResourceName, csp.testName, csp.trainingRatio,
                                    csp.classifiers, csp.optimizer, csp.metric, csp.saveNumber)
                                    .then(successCallback, errorCallback);
                                break;
                            case 'xes':
                                ApiService.createTestWithFile(AdminService.currentResourceName, csp.testName,
                                    csp.trainingConfig.xesFile, csp.trainingRatio, csp.classifiers, csp.optimizer,
                                    csp.metric, csp.saveNumber)
                                    .then(successCallback, errorCallback);
                                break;
                            case 'timeframe':
                                ApiService.createTestWithTimeframe(AdminService.currentResourceName, csp.testName,
                                    csp.trainingRatio, csp.trainingConfig.historyFrom, csp.trainingConfig.historyUntil,
                                    csp.classifiers, csp.optimizer, csp.metric, csp.saveNumber)
                                    .then(successCallback, errorCallback);
                                break;
                            default:
                                break;
                        }
                    }
                };

                ctrl.checkAndSetClsParameters = function (e) {
                    let givenName = e.find('.given-name').val(),
                        id = e.data('create-id'),
                        trained = e.find('.create-trained').prop('checked');

                    return {
                        valid: givenName !== "",
                        givenName: givenName,
                        id: id,
                        trained: trained
                    }
                }

                checkClsParameters = function (e) {
                    let csp = ctrl.checkAndSetClsParameters(e),
                        button = e.find('.create-classifier-from-result');
                    if (csp.valid && button.data('state') === 'disabled') {
                        button.attr('data-state', 'enabled');
                    } else if (!csp.valid) {
                        button.attr('data-state', 'disabled');
                    }
                };

                createClassifierFromResult = function (e) {
                    let csp = ctrl.checkAndSetClsParameters(e.parent('form')),
                        testName = e.data('test-name');
                    if (csp.valid) {
                        e.attr('data-state', 'waiting');
                        ApiService.createClassifierFromResult(AdminService.currentResourceName, testName, csp.id,
                            csp.givenName, csp.trained, AdminService.userName)
                            .then(function successCallback() {
                                    e.attr('data-state', 'success');

                                },
                                function errorCallback() {
                                    e.attr('data-state', 'fail');

                                });
                    }
                }

                updateTrainTestRatio();
                ctrl.loadTests();
                //setInterval(ctrl.loadTests, 10000);
                ctrl.loadCreatePanel();

            }
        ]
    )
});