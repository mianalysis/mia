package io.github.mianalysis.mia.module;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.Logic;

public abstract class ModuleTest {

    @Test
    public abstract void testGetHelp();

    public static Stream<Arguments> dimensionLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Logic logic : Logic.values())
                argumentBuilder.add(Arguments.of(dimension, logic));
            
        return argumentBuilder.build();
    }
}