package ij.macro;

import wbif.sjx.MIA.MIA;

/**
 * Provides the same functionality as the standard Interpreter, except for error handling.  This class will store the
 * error and not display a dialog.  This can be detected by the calling method and handled accordingly.
 */
public class CustomInterpreter extends Interpreter {
    private String errorMessage = "";

    @Override
    void error(String message){
        wasError = true;
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}