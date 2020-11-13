package uni_ko.bpm.cockpit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uni_ko.bpm.cockpit.Simulation.SimulationEnvironment;



@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
        boolean debug = false;

        String dir = "Camunda_Starter/process-definitions";
        if (Application.isEclipse()) {
            dir =  System.getProperty("user.dir") + "/process-definitions";
        }
        
        SimulationEnvironment se = new SimulationEnvironment();
        if (!se.existsSetup("CoronaSupermarkt") || debug) {
            se.buildDefaultSetup(dir, "CoronaSupermarkt", 400, 100, 0.20);
        }
        if (!se.existsSetup("sid-11167e06-c45d-4370-b356-4921b2398414") || debug) {
            se.buildDefaultSetup(dir, "sid-11167e06-c45d-4370-b356-4921b2398414", 400, 100, 0.20);
        }
    }

    public static boolean isEclipse() {
        boolean isEclipse = true;
        if (System.getenv("eclipse") == null) {
            isEclipse = false;
        }
        return isEclipse;
    }

}
