package wbif.sjx.MIA.Process;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 * Created by Stephen Cross on 14/06/2019.
 */
public class ConsoleFetcher implements Command {
    private static UIService accessibleService = null;

    @Parameter
    private static UIService uiService;

    @Override
    public void run() {
        System.out.println("From ConsoleFetcher "+uiService+"_"+accessibleService);
        accessibleService = uiService;
        System.out.println("From ConsoleFetcher 2 "+uiService+"_"+accessibleService);
    }

    public static UIService getService() {
        System.out.println("From ConsoleFetcher getter"+uiService+"_"+accessibleService);
        return accessibleService;
    }
}
