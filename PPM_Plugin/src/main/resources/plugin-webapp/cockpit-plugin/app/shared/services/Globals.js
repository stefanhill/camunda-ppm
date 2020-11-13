ngDefine('cockpit.plugin.cockpit-plugin.shared-services', function (module) {
    module.factory('Globals', function () {

        var Globals = {};

        Globals.init = function () {
            for (let child in document.querySelector(".nav-tabs").children) {
                console.log(document.querySelector(".nav-tabs").children.item(0).inner_text)
            }
        };

        Globals.getPredictionTypeIconClass = function (predictionType) {
            switch (predictionType) {
                case "ActivityPrediction":
                    return "glyphicon-step-forward";
                case "TimePrediction":
                    return "glyphicon-hourglass";
                case "RiskPrediction":
                    return "glyphicon-flash";
            }
            return "";
        };

        Globals.msToHMS = function (ms) {
            ms = Math.round(ms);
            let seconds = ms / 1000,
                hours = parseInt(seconds / 3600);
            seconds = seconds % 3600;
            let minutes = parseInt(seconds / 60);
            seconds = seconds % 60;

            hours = hours < 10 ? '0' + hours : hours;
            minutes = minutes < 10 ? '0' + minutes : minutes;
            seconds = seconds < 10 ? '0' + seconds : seconds;

            return hours + ':' + minutes + ':' + seconds;
        }

        return Globals;

    });
});
