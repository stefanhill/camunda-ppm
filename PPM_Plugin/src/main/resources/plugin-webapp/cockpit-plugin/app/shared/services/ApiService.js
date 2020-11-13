ngDefine('cockpit.plugin.cockpit-plugin.shared-services', function (module) {
    module.factory('ApiService', ['$http', 'Uri', '$rootScope', 'AdminService',
        function ($http, Uri, $rootScope, AdminService) {

            var ApiService = {};
            ApiService.runningTrainings = [],
                ApiService.trainingListener = null,
                ApiService.trainingStatusRequestBalancer = {};

            ApiService.getPublicClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/process-prediction/" + resourceName + "/classifier",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getClassifierPredictionTypes = function (processDefinitionId, givenName) {
                // hotfix to encode special chars
                let givenNameEscaped = encodeURIComponent(encodeURIComponent(givenName))
                let uri = "plugin://cockpit-plugin/:engine/prediction/" + processDefinitionId + "/classifier-prediction-types/" + givenNameEscaped,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getPrediction = function (processDefinitionId, givenName, processInstanceId) {
                let uri = "plugin://cockpit-plugin/:engine/prediction/classify",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        processDefinitionId: processDefinitionId,
                        processInstanceId: processInstanceId
                    };
                return $http({method: 'POST', url: url, data: data})
            };

            ApiService.getTrainableClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/trainable-classifiers",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getClassifierMeta = function (resourceName, givenName) {

                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/classifier-meta/"
                url = Uri.appUri(uri);
                data = {
                    givenName: givenName,
                };
                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.getAvailableClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/available-classifiers",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getClassifierParams = function (resourceName, classifierName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/classifier-params/" + classifierName,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getClassifierInstanceParams = function (resourceName, givenName) {
                // hotfix to encode special chars
                let givenNameEscaped = encodeURIComponent(encodeURIComponent(givenName))
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/classifier-instance-params/" + givenNameEscaped,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.createClassifier = function (resourceName, classifierName, classifierParams, givenName, author) {
                let uri = "plugin://cockpit-plugin/:engine/create-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        classifierParams: classifierParams,
                        author: author,
                        resourceName: resourceName,
                        classifierName: classifierName
                    };

                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.trainClassifier = function (resourceName, givenName, ratio,
                                                   successCallback, errorCallback) {
                let uri = "plugin://cockpit-plugin/:engine/admin/train-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        ratio: ratio,
                        resourceName: resourceName,
                    };
                $http({method: 'PUT', url: url, data: data}).then(
                    function successInitializeCallback(response) {
                        ApiService.addTrainingListener(resourceName, givenName,
                            successCallback, errorCallback);
                        return true;
                    }, function errorInitializeCallback(response) {
                        return false;
                    }
                )
            };

            ApiService.trainClassifierWithTimeframe = function (resourceName, givenName, ratio, startDate, endDate,
                                                                successCallback, errorCallback) {
                let uri = "plugin://cockpit-plugin/:engine/admin/train-classifier-with-timeframe",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        ratio: ratio,
                        resourceName: resourceName,
                        startDate: startDate,
                        endDate: endDate
                    };
                return $http({method: 'PUT', url: url, data: data}).then(
                    function successInitializeCallback(response) {
                        ApiService.addTrainingListener(resourceName, givenName,
                            successCallback, errorCallback);
                        return true;
                    }, function errorInitializeCallback(response) {
                        return false;
                    }
                )
            };

            ApiService.trainClassifierWithFile = function (resourceName, givenName, file, ratio,
                                                           successCallback, errorCallback) {
                const readUploadedFileAsText = (inputFile) => {
                    const temporaryFileReader = new FileReader();
                    return new Promise((resolve, reject) => {
                        temporaryFileReader.onerror = () => {
                            temporaryFileReader.abort();
                            reject(new DOMException("Problem parsing input file."));
                        };
                        temporaryFileReader.onload = () => {
                            resolve(temporaryFileReader.result);
                        };
                        temporaryFileReader.readAsText(inputFile);
                    });
                }
                readUploadedFileAsText(file).then(fileContent => {
                    let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/trainWithFile/",
                        url = Uri.appUri(uri);
                    let data = {
                        givenName: givenName,
                        ratio: ratio,
                        fileContent: fileContent
                    }
                    return $http({method: 'POST', url: url, data: data}).then(
                        function successInitializeCallback(response) {
                            ApiService.addTrainingListener(resourceName, givenName,
                                successCallback, errorCallback);
                            return true;
                        }, function errorInitializeCallback(response) {
                            return false;
                        }
                    )
                })

            };


            ApiService.stopTraining = function (resourceName, givenName) {
                let trainingItemIndex = ApiService.runningTrainings.findIndex(trainingItem => {
                    return trainingItem.resourceName === resourceName && trainingItem.givenName === givenName
                })
                ApiService.runningTrainings.splice(trainingItemIndex, 1)
                // hotfix to encode special chars
                ApiService.finishTrainingStatusListener()
                let givenNameEscaped = encodeURIComponent(encodeURIComponent(givenName))
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/stopTraining/" + givenNameEscaped,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.copyClassifier = function (resourceName, givenName, copyGivenName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/copy-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        resourceName: resourceName,
                        copyGivenName: copyGivenName
                    };
                return $http({method: 'POST', url: url, data: data})
            };

            ApiService.revertClassifier = function (resourceName, givenName, targetVersion) {
                let uri = "plugin://cockpit-plugin/:engine/admin/revert-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        resourceName: resourceName,
                        targetVersion: targetVersion
                    };
                return $http({method: 'POST', url: url, data: data})
            };

            ApiService.renameClassifier = function (resourceName, oldName, newName) {
                let oldNameEscaped = encodeURIComponent(encodeURIComponent(oldName))
                let newNameEscaped = encodeURIComponent(encodeURIComponent(newName))
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName +
                    "/rename-classifier/" + oldNameEscaped + "/" + newNameEscaped,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            }

            ApiService.getClassifierVersions = function (resourceName, classifierName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/classifier-versions/" + classifierName,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getTrainedClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/trained-classifiers",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };


            ApiService.getAtomicClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/atomic-classifiers",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.getMergedClassifiers = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/merged-classifiers",
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.mergeClassifier = function (resourceName, classifierList, givenName) {
                let uri = "plugin://cockpit-plugin/:engine/merge-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        author: "Bernd",
                        resourceName: resourceName,
                        classifierList: classifierList
                    };
                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.deleteClassifier = function (resourceName, givenName) {
                let uri = "plugin://cockpit-plugin/:engine/delete-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        resourceName: resourceName,
                    };
                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.togglePubliclyClassifier = function (resourceName, givenName, publicly) {
                let uri = "plugin://cockpit-plugin/:engine/toggle-publicly-classifier",
                    url = Uri.appUri(uri),

                    data = {
                        givenName: givenName,
                        resourceName: resourceName,
                        publicly: publicly
                    };
                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.setDefaultClassifier = function (resourceName, givenName) {
                let uri = "plugin://cockpit-plugin/:engine/default-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        givenName: givenName,
                        resourceName: resourceName,
                    };
                return $http({method: 'PUT', url: url, data: data})
            };

            ApiService.getTrainingStatus = function (resourceName, givenName) {
                // hotfix to encode special chars
                let givenNameEscaped = encodeURIComponent(encodeURIComponent(givenName))
                let uri = "plugin://cockpit-plugin/:engine/admin/" + resourceName + "/training-status/"
                    + givenNameEscaped,
                    url = Uri.appUri(uri);
                return $http({method: 'GET', url: url})
            };

            ApiService.addTrainingListener = function (resourceName, givenName,
                                                       trainingSuccessCallback, trainingFailedCallback) {
                if (ApiService.runningTrainings.length === 0) {
                    ApiService.initializeTrainingListening();
                }
                let training = {}
                training.resourceName = resourceName;
                training.givenName = givenName;
                training.trainingSuccessCallback = trainingSuccessCallback;
                training.trainingFailedCallback = trainingFailedCallback;
                ApiService.runningTrainings.push(training);
            };

            ApiService.initializeTrainingListening = function () {
                ApiService.trainingListener = setInterval(function () {
                    // periodically check status of all running training processes
                    let runningTrainingsCopy = [...ApiService.runningTrainings]
                    runningTrainingsCopy.forEach(function (trainingItem) {
                        if (!ApiService.shouldGetTrainingStatus(trainingItem.resourceName, trainingItem.givenName)) {
                            // reduce request frequency if training takes very long
                            return
                        }
                        ApiService.getTrainingStatus(trainingItem.resourceName, trainingItem.givenName)
                            .then(function successCallback(response) {
                                let trainingStatus = response.data
                                let config = AdminService.resourceConfigs[trainingItem.resourceName]
                                    .ppmTrainingConfig.classifierConfigs[trainingItem.givenName];
                                config.acurracy = Math.floor(trainingStatus.accuracy * 10000) / 100 + " %";
                                if (trainingStatus.done) {
                                    ApiService.finishTrainingStatusListener(trainingItem)
                                    if (trainingStatus.success) {
                                        trainingItem.trainingSuccessCallback(config)
                                        return
                                    }
                                    trainingItem.trainingFailedCallback(config)
                                }
                            }, function errorCallback(response) {
                                console.log(response);
                            })
                    })
                }, 1000);
            };

            ApiService.finishTrainingStatusListener = function (trainingItem) {
                let index = ApiService.runningTrainings.indexOf(trainingItem);
                ApiService.runningTrainings.splice(index, 1);
                if (ApiService.runningTrainings.length === 0) {
                    clearInterval(ApiService.trainingListener);
                }
            };

            ApiService.shouldGetTrainingStatus = function (resourceName, givenName) {
                let balancer = ApiService.trainingStatusRequestBalancer;
                if (balancer[resourceName] === undefined) {
                    balancer[resourceName] = {}
                }
                let balance;
                if (balancer[resourceName][givenName] === undefined) {
                    balance = 0
                } else {
                    balance = balancer[resourceName][givenName]
                }
                ApiService.trainingStatusRequestBalancer[resourceName][givenName] = balance + 1
                if (balance < 15) {
                    return true
                }
                if (balance <= 60) {
                    return balance % 5 === 0
                }
                return balance % 10 === 0
            }

            /*
             * Hyper parameter optimization
             */

            ApiService.getTestList = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/test-list",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.getTestInformation = function (resourceName, testName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/test-information",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName,
                        testName: testName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.getTestResults = function (resourceName, testName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/test-results",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName,
                        testName: testName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.getPossibleMetrics = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/possible-metrics",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.getPossibleOptimizer = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/possible-optimizer",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.getPossibleClassifierParameterRange = function (resourceName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/possible-classifier-parameter-range",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName
                    };
                return $http({method: 'POST', url: url, data: data});
            };

            ApiService.deleteTest = function (resourceName, testName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/delete-test",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName,
                        testName: testName
                    };
                return $http({method: 'PUT', url: url, data: data});
            };

            ApiService.pauseTest = function (resourceName, testName) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/pause-test",
                    url = Uri.appUri(uri),
                    data = {
                        resourceName: resourceName,
                        testName: testName
                    };
                return $http({method: 'PUT', url: url, data: data});
            };

            ApiService.createTest = function (resourceName, testName, ratio, classifiers, optimizer, metric, saveNumber) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/create-test",
                    url = Uri.appUri(uri),
                    data = {
                        testName: testName,
                        ratio: ratio,
                        resourceName: resourceName,
                        classifiers: classifiers,
                        optimizer: optimizer,
                        metric: metric,
                        saveNumber: saveNumber
                    };
                return $http({method: 'PUT', url: url, data: data});
            };

            ApiService.createTestWithTimeframe = function (resourceName, testName, ratio, startDate, endDate,
                                                           classifiers, optimizer, metric, saveNumber) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/create-test-with-timeframe",
                    url = Uri.appUri(uri),
                    data = {
                        testName: testName,
                        ratio: ratio,
                        resourceName: resourceName,
                        startDate: startDate,
                        endDate: endDate,
                        classifiers: classifiers,
                        optimizer: optimizer,
                        metric: metric,
                        saveNumber: saveNumber
                    };
                return $http({method: 'PUT', url: url, data: data});
            };

            ApiService.createTestWithFile = function (resourceName, testName, file, ratio, classifiers, optimizer, metric, saveNumber) {
                const readUploadedFileAsText = (inputFile) => {
                    const temporaryFileReader = new FileReader();
                    return new Promise((resolve, reject) => {
                        temporaryFileReader.onerror = () => {
                            temporaryFileReader.abort();
                            reject(new DOMException("Problem parsing input file."));
                        };
                        temporaryFileReader.onload = () => {
                            resolve(temporaryFileReader.result);
                        };
                        temporaryFileReader.readAsText(inputFile);
                    });
                }
                readUploadedFileAsText(file).then(fileContent => {
                    let uri = "plugin://cockpit-plugin/:engine/hyper-opt/create-test-with-file",
                        url = Uri.appUri(uri);
                    let data = {
                        testName: testName,
                        ratio: ratio,
                        fileContent: fileContent,
                        resourceName: resourceName,
                        classifiers: classifiers,
                        optimizer: optimizer,
                        metric: metric,
                        saveNumber: saveNumber
                    }
                    return $http({method: 'POST', url: url, data: data});
                })
            };

            ApiService.createClassifierFromResult = function (resourceName, testName, classifierId, givenName, trained, author) {
                let uri = "plugin://cockpit-plugin/:engine/hyper-opt/create-classifier",
                    url = Uri.appUri(uri),
                    data = {
                        testName: testName,
                        resourceName: resourceName,
                        classifierId: classifierId,
                        givenName: givenName,
                        trained: trained,
                        author: author
                    };
                return $http({method: 'PUT', url: url, data: data});
            };

            return ApiService;

        }

    ]);

});