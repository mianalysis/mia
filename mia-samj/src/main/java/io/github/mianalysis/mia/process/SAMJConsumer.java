package io.github.mianalysis.mia.process;

import java.util.function.Consumer;

import io.github.mianalysis.mia.MIA;

public class SAMJConsumer implements Consumer<String> {

    @Override
    public void accept(String t) {
        if (!t.equals(""))
            MIA.log.writeDebug(t);
    }
    
}
