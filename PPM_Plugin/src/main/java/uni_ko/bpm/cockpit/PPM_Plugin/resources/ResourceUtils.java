package uni_ko.bpm.cockpit.PPM_Plugin.resources;

import java.util.*;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.List_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;

public class ResourceUtils {

    public static Pair<String, HashMap<Integer, Object>> parseOptimizerConfig(Object optimizerIn) throws ClassNotFoundException {
        LinkedHashMap<String, Object> optimizer = (LinkedHashMap<String, Object>) optimizerIn;
        Map.Entry<String, Object> entry = optimizer.entrySet().iterator().next();

        String optimizerName = entry.getKey();
        List<LinkedHashMap<String, Object>> params = (ArrayList<LinkedHashMap<String, Object>>) entry.getValue();
        HashMap<Integer, Object> optimizerParams = new HashMap<>();

        for (LinkedHashMap<String, Object> param :
                params) {
            Class<?> dataType = Class.forName((String) param.get("data_type"));
            Integer parameterID = (Integer) param.get("parameter_id");
            Object obj;
            if (param.containsKey("suggested_value")) {
                if (Integer.class.isAssignableFrom(dataType)) {
                    obj = Integer.valueOf((String) param.get("current_value"));
                } else if (Double.class.isAssignableFrom(dataType)) {
                    obj = Double.valueOf((String) param.get("current_value"));
                } else {
                    obj = param.get("current_value");
                }
            } else {
                obj = (ArrayList<?>) param.get("current_value");
            }
            optimizerParams.put(parameterID, obj);
        }

        return new Pair<>(optimizerName, optimizerParams);
    }

    public static HashMap<String, HashMap<Integer, Range>> parseClassifierConfig(Object classifierIn) throws Exception {
        LinkedHashMap<String, Object> classifiers = (LinkedHashMap<String, Object>) classifierIn;
        HashMap<String, HashMap<Integer, Range>> classifierConfig = new HashMap<>();
        for (Map.Entry<String, Object> entry :
                classifiers.entrySet()) {
            String classifierName = entry.getKey();
            List<LinkedHashMap<String, Object>> values = (ArrayList<LinkedHashMap<String, Object>>) entry.getValue();
            HashMap<Integer, Range> ranges = new HashMap<>();
            for (LinkedHashMap<String, Object> rangeParams :
                    values) {
                Range range;
                Class<?> dataType = Class.forName((String) rangeParams.get("data_type"));
                Integer parameterID = (Integer) rangeParams.get("parameter_id");
                String text = (String) rangeParams.get("text");
                String infoText = (String) rangeParams.get("info_text");
                if (rangeParams.containsKey("from")) {
                    if (Integer.class.isAssignableFrom(dataType)) {
                        Integer from = Integer.valueOf((String) rangeParams.get("from"));
                        Integer to = Integer.valueOf((String) rangeParams.get("to"));
                        Integer stepping = Integer.valueOf((String) rangeParams.get("stepping"));
                        range = new Numeric_Range(parameterID, from, to, stepping, text, infoText, dataType);
                    } else if (Double.class.isAssignableFrom(dataType)) {
                        Double from = Double.valueOf((String) rangeParams.get("from"));
                        Double to = Double.valueOf((String) rangeParams.get("to"));
                        Double stepping = Double.valueOf((String) rangeParams.get("stepping"));
                        range = new Numeric_Range(parameterID, from, to, stepping, text, infoText, dataType);
                    } else {
                        List<?> options = (ArrayList<?>) rangeParams.get("options");
                        range = new Range(options, parameterID, text, infoText, dataType);
                    }
                } else {
                    List<?> options = (ArrayList<?>) ((ArrayList<?>) rangeParams.get("options")).get(0);
                    boolean allow_multi_select = (Boolean) rangeParams.get("allow_multi_select");
                    range = new List_Range(parameterID, options, allow_multi_select, text, infoText, dataType);
                }
                ranges.put((Integer) rangeParams.get("parameter_id"), range);
            }
            classifierConfig.put(classifierName, ranges);
        }
        return classifierConfig;
    }


}
