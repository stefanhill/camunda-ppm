ngDefine('cockpit.plugin.cockpit-plugin.components', [
	'./diagram-overlay/diagramOverlayCtrl',
	'./process-prediction/processPredictionCtrl',
	'module:cockpit.plugin.cockpit-plugin.diagram-overlay:./diagram-overlay/diagramOverlayModule',
	'module:cockpit.plugin.cockpit-plugin.process-prediction:./process-prediction/processPredictionModule',
	'module:cockpit.plugin.cockpit-plugin.admin-globals:./admin-globals/adminGlobalsModule',
	'module:cockpit.plugin.cockpit-plugin.hyper-opt:./hyper-opt/hyperOptModule',
	'module:cockpit.plugin.cockpit-plugin.ppm-creation:./ppm-creation/ppmCreationModule',
	'module:cockpit.plugin.cockpit-plugin.ppm-training:./ppm-training/ppmTrainingModule',
	'module:cockpit.plugin.cockpit-plugin.ppm-runtime:./ppm-runtime/ppmRuntimeModule',
], function(module) {

	var Configuration = function Configuration(ViewsProvider) {

		/* components in process instance diagram overlay view */
		ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.overlay', {
			id: 'ppm-diagram-overlay',
			priority: 20,
			url: 'plugin://cockpit-plugin/static/app/components/diagram-overlay/diagramOverlayView.html',
			controller: 'diagramOverlayCtrl'
		});

		/* components in tab of process instance view */
		ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
			id: 'ppm-process-prediction',
			priority: 21,
			label: 'Process Prediction',
			url: 'plugin://cockpit-plugin/static/app/components/process-prediction/processPredictionView.html',
			controller: 'processPredictionCtrl'
		});
		
		ViewsProvider.registerDefaultView('cockpit.repository.resource.action', {
			id: 'admin-globals',
			priority: 22,
			label: 'Admin Globals',
			url: 'plugin://cockpit-plugin/static/app/components/admin-globals/adminGlobalsView.html',
			controller: 'adminGlobalsCtrl'
		})
		
		/* components in cockpit deployments */
		ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
			id: 'ppm-creation',
			priority: 24,
			label: 'PPM Creation',
			url: 'plugin://cockpit-plugin/static/app/components/ppm-creation/ppmCreationView.html',
			controller: 'ppmCreationCtrl'
		})
		ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
			id: 'ppm-training',
			priority: 23,
			label: 'PPM Training',
			url: 'plugin://cockpit-plugin/static/app/components/ppm-training/ppmTrainingView.html',
			controller: 'ppmTrainingCtrl'
		})
		ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
			id: 'ppm-runtime',
			priority: 22,
			label: 'PPM Runtime',
			url: 'plugin://cockpit-plugin/static/app/components/ppm-runtime/ppmRuntimeView.html',
			controller: 'ppmRuntimeCtrl'
		})
		ViewsProvider.registerDefaultView('cockpit.repository.resource.detail', {
			id: 'hyper-opt',
			priority: 21,
			label: 'PPM Find',
			url: 'plugin://cockpit-plugin/static/app/components/hyper-opt/hyperOptView.html',
			controller: 'hyperOptCtrl'
		})
	};

	Configuration.$inject = ['ViewsProvider'];

	module.config(Configuration);

	return module;
	
});