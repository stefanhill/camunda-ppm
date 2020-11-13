package uni_ko.bpm.cockpit.PPM_Plugin.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import uni_ko.bpm.cockpit.PPM_Plugin.CockpitPlugin;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt.*;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.*;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.creation.CreateClassifierResource;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.runtime.*;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training.*;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction.PredictionResource;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction.ClassifierPredictionTypesResource;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction.PublicClassifiersResource;


@Path("plugin/" + CockpitPlugin.ID)
public class CockpitPluginRootResource extends AbstractCockpitPluginRootResource {

    public CockpitPluginRootResource() {
        super(CockpitPlugin.ID);
    }

    @Path("{engineName}/process-prediction/{processDefinitionId}/classifier")
    public PublicClassifiersResource getPublicClassifiers(@PathParam("engineName") String engineName,
                                                          @PathParam("processDefinitionId") String processDefinitionId) {
        return subResource(new PublicClassifiersResource(engineName, processDefinitionId), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/classifier-meta/")
    public ClassifierMetaResource getClassifierMeta(@PathParam("engineName") String engineName,
                                                    @PathParam("resourceName") String resourceName) {
        return subResource(new ClassifierMetaResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/prediction/classify")
    public PredictionResource getPrediction(@PathParam("engineName") String engineName) {
        return subResource(new PredictionResource(engineName), engineName);
    }

    @Path("{engineName}/prediction/{processDefinitionId}/classifier-prediction-types/{givenName}")
    public ClassifierPredictionTypesResource getClassifierPredictionTypes(@PathParam("engineName") String engineName,
                                                                          @PathParam("processDefinitionId") String processDefinitionId,
                                                                          @PathParam("givenName") String givenName) {
        return subResource(new ClassifierPredictionTypesResource(engineName, processDefinitionId, givenName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/available-classifiers")
    public AvailableClassifiersResource getAvailableClassifiers(@PathParam("engineName") String engineName,
                                                                @PathParam("resourceName") String resourceName) throws Exception {
        return subResource(new AvailableClassifiersResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/classifier-params/{classifierName}")
    public ClassifierParamsResource getClassifierParams(@PathParam("engineName") String engineName,
                                                        @PathParam("resourceName") String resourceName,
                                                        @PathParam("classifierName") String classifierName) throws Exception {
        return subResource(new ClassifierParamsResource(engineName, resourceName, classifierName), engineName);
    }

    @Path("{engineName}/create-classifier")
    public CreateClassifierResource createClassifier(@PathParam("engineName") String engineName) throws Exception {
        return subResource(new CreateClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/classifier-instance-params/{givenName}")
    public ClassifierInstanceParamsResource getClassifierInstanceParams(@PathParam("engineName") String engineName,
                                                                        @PathParam("resourceName") String resourceName,
                                                                        @PathParam("givenName") String givenName) throws Exception {
        return subResource(new ClassifierInstanceParamsResource(engineName, resourceName, givenName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/trainable-classifiers")
    public TrainableClassifiersResource getTrainableClassifiers(@PathParam("engineName") String engineName,
                                                                @PathParam("resourceName") String resourceName) {
        return subResource(new TrainableClassifiersResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/admin/train-classifier")
    public TrainClassifierResource trainClassifier(@PathParam("engineName") String engineName) {
        return subResource(new TrainClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/trained-classifiers")
    public TrainedClassifierResource getTrainedClassifiers(@PathParam("engineName") String engineName,
                                                           @PathParam("resourceName") String resourceName) {
        return subResource(new TrainedClassifierResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/atomic-classifiers")
    public AtomicClassifiersResource getAtomicClassifiers(@PathParam("engineName") String engineName,
                                                          @PathParam("resourceName") String resourceName) {
        return subResource(new AtomicClassifiersResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/merged-classifiers")
    public MergedClassifiersResource getMergedClassifiers(@PathParam("engineName") String engineName,
                                                          @PathParam("resourceName") String resourceName) {
        return subResource(new MergedClassifiersResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/merge-classifier")
    public MergeClassifierResource mergeClassifier(@PathParam("engineName") String engineName) {
        return subResource(new MergeClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/delete-classifier")
    public DeleteClassifierResource deleteClassifier(@PathParam("engineName") String engineName) {
        return subResource(new DeleteClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/rename-classifier/{oldName}/{newName}")
    public RenameClassifierResource renameClassifier(@PathParam("engineName") String engineName, @PathParam("resourceName") String resourceName,
                                                     @PathParam("oldName") String oldName, @PathParam("newName") String newName) {
        return subResource(new RenameClassifierResource(engineName, resourceName, oldName, newName), engineName);
    }

    @Path("{engineName}/toggle-publicly-classifier")
    public TogglePubliclyClassifierResource togglePubliclyClassifier(@PathParam("engineName") String engineName) {
        return subResource(new TogglePubliclyClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/default-classifier")
    public SetDefaultClassifierResource setDefaultClassifier(@PathParam("engineName") String engineName) {
        return subResource(new SetDefaultClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/admin/train-classifier-with-timeframe")
    public TrainClassifierWithTimeframeResource trainClassifierWithTimeframe(@PathParam("engineName") String engineName) {
        return subResource(new TrainClassifierWithTimeframeResource(engineName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/trainWithFile")
    public TrainClassifierWithFileResource trainClassifierWithFile(@PathParam("engineName") String engineName, @PathParam("resourceName") String resourceName) {
        return subResource(new TrainClassifierWithFileResource(engineName, resourceName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/training-status/{givenName}")
    public TrainingStatusResource getTrainingStatus(@PathParam("engineName") String engineName,
                                                    @PathParam("resourceName") String resourceName,
                                                    @PathParam("givenName") String givenName) {
        return subResource(new TrainingStatusResource(engineName, resourceName, givenName), engineName);
    }

    @Path("{engineName}/admin/{resourceName}/stopTraining/{givenName}")
    public StopTrainingResource stopTraining(@PathParam("engineName") String engineName,
                                             @PathParam("resourceName") String resourceName,
                                             @PathParam("givenName") String givenName) {
        return subResource(new StopTrainingResource(engineName, resourceName, givenName), engineName);
    }

    @Path("{engineName}/admin/revert-classifier")
    public RevertClassifierResource revertClassifier(@PathParam("engineName") String engineName) {
        return subResource(new RevertClassifierResource(engineName), engineName);
    }

    @Path("{engineName}/admin/copy-classifier")
    public CopyClassifierResource copyClassifier(@PathParam("engineName") String engineName) {
        return subResource(new CopyClassifierResource(engineName), engineName);
    }

    /*
     * Hyper parameter optimization
     */

    @Path("{engineName}/hyper-opt/create-classifier")
    public CreateClassifierFromResultResource createClassifierFromResult(@PathParam("engineName") String engineName) {
        return subResource(new CreateClassifierFromResultResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/create-test")
    public CreateTestResource createTest(@PathParam("engineName") String engineName) {
        return subResource(new CreateTestResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/create-test-with-timeframe")
    public CreateTestWithTimeframeResource createTestWithTimeframe(@PathParam("engineName") String engineName) {
        return subResource(new CreateTestWithTimeframeResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/create-test-with-file")
    public CreateTestWithFileResource createTestWithFile(@PathParam("engineName") String engineName) {
        return subResource(new CreateTestWithFileResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/possible-classifier-parameter-range")
    public PossibleClassifierParameterRangeResource getPossibleClassifier(@PathParam("engineName") String engineName) {
        return subResource(new PossibleClassifierParameterRangeResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/possible-metrics")
    public PossibleMetricsResource getPossibleMetrics(@PathParam("engineName") String engineName) {
        return subResource(new PossibleMetricsResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/possible-optimizer")
    public PossibleOptimizerResource getPossibleOptimizer(@PathParam("engineName") String engineName) {
        return subResource(new PossibleOptimizerResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/test-information")
    public TestInformationResource getTestInformation(@PathParam("engineName") String engineName) {
        return subResource(new TestInformationResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/test-results")
    public TestResultsResource getTestResults(@PathParam("engineName") String engineName) {
        return subResource(new TestResultsResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/test-list")
    public TestListResource getTestList(@PathParam("engineName") String engineName) {
        return subResource(new TestListResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/delete-test")
    public DeleteTestResource deleteTest(@PathParam("engineName") String engineName) {
        return subResource(new DeleteTestResource(engineName), engineName);
    }

    @Path("{engineName}/hyper-opt/pause-test")
    public PauseTestResource pauseTest(@PathParam("engineName") String engineName) {
        return subResource(new PauseTestResource(engineName), engineName);
    }

}
