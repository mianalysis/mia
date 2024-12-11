package io.github.mianalysis.mia.gui.svg;

import com.github.weisj.jsvg.parser.DefaultParserProvider;
import com.github.weisj.jsvg.parser.DomProcessor;

public class CustomParserProvider extends DefaultParserProvider {
    private CustomColorsProcessor processor;

    public CustomParserProvider(CustomColorsProcessor processor) {
        this.processor = processor;
    }

    @Override
    public DomProcessor createPreProcessor() {
        return processor;
    }
}
