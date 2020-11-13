ngDefine('cockpit.plugin.cockpit-plugin.shared-services', function(module) {
	module.factory('AdminService', [ '$rootScope', function($rootScope) {
		
		let AdminService = {};

		AdminService.currentResourceName = null;
		AdminService.resourceConfigs = {};
		AdminService.userName = "";

		// ppm creation config does not depend on resource (all resources use the same classifiers)
		AdminService.ppmCreationConfig = {
			classifiersLoaded: false,
			availableClassifiers: null,
			availableClassifierObjects: null,
			classifierParams: null,
		}

		AdminService.initResource = function (resource) {
			let resourceConfig = {};
			let resourceName = resource.name;
			resourceConfig.deploymentId = resource.deploymentId;
			resourceConfig.resourceId = resource.id;
			resourceConfig.ppmTrainingConfig = {
				initialized: false,
				// classifierConfigs: { "classifier_abc" : { trainingRunning : false, ... }, ... }
				classifierConfigs: {},
				currentClassifierName : null,
			};
			AdminService.resourceConfigs[resourceName] = resourceConfig;
		};


		AdminService.setCurrentClassifierName = function(givenName){
			let resourceConfig = AdminService.resourceConfigs[AdminService.currentResourceName];
			resourceConfig.ppmTrainingConfig.currentClassifierName = givenName;
			console.log(givenName);
		}

		AdminService.updatePpmTrainingConfigs = function (resourceName, trainableClassifierNames) {
			let ppmTrainingConfig = AdminService.resourceConfigs[resourceName].ppmTrainingConfig;
			for (let classifierName of trainableClassifierNames) {
				if(!ppmTrainingConfig.classifierConfigs[classifierName]){
					ppmTrainingConfig.classifierConfigs[classifierName] = AdminService.initializeClassifierTrainingConfig(resourceName, classifierName);
				}
			}
		};

		AdminService.callTabByName = function(tabName, timeout){
			let tabs_bar = document.querySelector(".nav-tabs")
			if(tabs_bar === null){
				return
			}
			let tabs = Array.prototype.slice.call( tabs_bar.children )
			let tab = tabs.find( tab => tabName.includes(tab.innerText))
			let component_link = tab.children[0]
			setTimeout(function(){
				component_link.click(); },
				timeout
			);
		}

		AdminService.initializeClassifierTrainingConfig = function (resourceName, classifierName) {
			let config = {};
			config.classifierName = classifierName;
			config.xesFile = null;
			config.trainingDataSpecified = false;
			config.trainWithXESFile = false;
			config.trainWithHistoryData = false;
			config.trainWithHistoryDataAndTimeFrame = false;
			config.disableDataSelection = false;
			config.isTrainingRunning = false;
			config.runningTrainingAccuracy = null;
			config.isTrainingFinished = false;
			config.trainingState = "enabled";
			config.trainingStopwatch = null;
			config.trainingDuration = "00:00:000";
			config.trainingRatio = 80;
			config.acurracy = "0 %";
			config.historyDataFrom = moment().startOf('minute').subtract(24, 'hour');
			config.historyDataUntil = moment().startOf('minute');
			config.timeFrameOptions = {
				singleDatePicker: true,
				locale: {
					format: 'D-MMMM-YY hh:mm A',
				},
				opens: 'left',
				drops: 'up'
			};
			return config;
		};

		return AdminService;
		
	}]);
});
