ngDefine('cockpit.plugin.cockpit-plugin.diagram-overlay', function (module) {

    module.controller('diagramOverlayCtrl',
        ['$scope', '$http', 'Uri', 'ProcessData',
            function ($scope, $http, Uri, ProcessData) {

                let elementId = $scope.bpmnElement.id,
                    elementName = $scope.bpmnElement.name;
                $scope.bubbleStyle = {
                    'margin': '0',
                    'color': '#000',
                    'right': '0',
                    'font-size': '12px',
                    'position': 'absolute',
                    'padding': '4px',
                    'border-radius': '10px',
                    'line-height': '1',
                    'border': '1px #000 solid'
                };
                if($scope.bpmnElement.$type == "bpmn:EndEvent"){
                    ProcessData.setBpmnElementName(elementId, "PROCESS_END");
                } else {
                    ProcessData.setBpmnElementName(elementId, elementName);
                }

                $scope.$on("nextActivityCandidatesChanged", function (event, args) {
                    if (ProcessData.nextActivityCandidates.includes(elementId)) {
                        let p = ProcessData.activityPredictions[elementId];
                        $scope.probability =  p + '%';
                        $scope.bubbleStyle['background-color'] = 'hsl('+p+',80%,50%)';
                        $scope.show = true;
                    }
                    else {
                        $scope.show = false;
                    }
                });
            }]);
});
