package wbif.sjx.MIA.Plugins;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.ConsoleRenderer;
import wbif.sjx.MIA.Process.Logging.ErrorLog;
import wbif.sjx.MIA.Process.Logging.MessageLog;

import java.io.PrintStream;
import java.util.Random;

@Plugin(type = Command.class, menuPath = "Plugins>Bristol WBIF>Logger")
public class Logger_  implements Command {
    @Parameter
    private UIService uiService;

    @Override
    public void run() {
        // Wait until the Console has been initialised
        while (uiService.getDefaultUI().getConsolePane().getComponent() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Setting the MIA log
        MIA.setLog(new ConsoleRenderer(uiService));

        // Redirecting the standard output and error streams, so they are formatted by for the console
        System.setOut(new PrintStream(MessageLog.getInstance()));
        System.setErr(new PrintStream(ErrorLog.getInstance()));

    }
}
