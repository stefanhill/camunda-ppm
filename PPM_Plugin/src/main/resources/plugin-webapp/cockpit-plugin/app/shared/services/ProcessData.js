ngDefine('cockpit.plugin.cockpit-plugin.shared-services', function(module) {
	module.factory('ProcessData', [ '$rootScope', function($rootScope) {
		
		let ProcessData = {};

		ProcessData.processDefinitionId = null;
		ProcessData.processInstanceId = null;
		ProcessData.bpmnModel = null;
		ProcessData.currentClassifier = null;
		
		ProcessData.nextActivityCandidates = [];
		ProcessData.activityPredictions = {};
		ProcessData.bpmnElementNames = {};

  	  	ProcessData.initializeProcessInstanceData = function(processInstance){
			ProcessData.processInstanceId = processInstance.id;
			ProcessData.processDefinitionId = processInstance.definitionId;
			// Process Key: Split Process Definition ID by second last ":" (cut off deploymentId) 
			ProcessData.processKey = ProcessData.processDefinitionId.split(":").slice(0,-2).join("")
		};

  	  	ProcessData.initializeProcessDefinitionData = function(deployment) {
			ProcessData.processDefinitionId = deployment.id;
		};
		
		ProcessData.setResourceName = function(resourceName){
			ProcessData.resourceName = resourceName;
		};
  	  			
		ProcessData.setNextActivityCandidates = function( list ){
			ProcessData.nextActivityCandidates = list
			$rootScope.$broadcast("nextActivityCandidatesChanged")
		}
		
		ProcessData.setActivityPredictions = function( map ){
			ProcessData.activityPredictions = map
			$rootScope.$broadcast("activityPredictionsChanged")
		}

		ProcessData.setBpmnElementName = function( elementId, elementName ){
			ProcessData.bpmnElementNames[elementId] = elementName;
		}

		return ProcessData;
		
	}]);
});
