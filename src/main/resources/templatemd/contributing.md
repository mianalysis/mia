We welcome any contributions to the MIA project.  If you'd like to get involved, here are a few ways you could do so:
- **Adding a new MIA module**.  We'd love it if you'd consider contributing a new module to MIA.  Such a module could allow an existing ImageJ plugin to be used as part of MIA workflows or add totally new functionality.  Adding new modules to MIA and the module format is detailed in an example module on our [mia-examples](https://github.com/mianalysis/mia-examples/tree/main/DevelopmentExamples/DevEx1_CustomModule) repository.  The repository also contains a template module that can be used as a starting point for new modules.
- **Contributing new example workflows**.  If you've got a workflow you'd like to share, please consider uploading it to the [mia-examples](https://github.com/mianalysis/mia-examples) repository.
- **Adding automated tests**.  Automated testing for MIA is currently incomplete.  We'd be very grateful for any tests that were added to the automated test suite.
- **Developing existing MIA modules**.  We're in the process of transitioning from using ImageJ's ImagePlus format to the [ImgLib2](https://imagej.net/libs/imglib2/) format.  Amongst other benefits, this will allow MIA to make use ImgLib2's disk-cached image formats (loading images directly from storage). Any modules which could be rewritten to take advantage of ImgLib2 would help us reach this goal faster!
- **Providing feedback**.  Any feedback, feature suggestions or comments are very welcome.  If you'd like to tell us how you're getting on with MIA, please contact us via the [Issues](https://github.com/mianalysis/mia/issues) board or send an email to stephen.cross@bristol.ac.uk

If you'd like to include the latest version of MIA in your project, you can add the following dependency to your pom.xml file:

```
<dependency>
    <groupId>io.github.mianalysis</groupId>
    <artifactId>mia</artifactId>
    <version>${MIAVERSION}</version>
</dependency>
```

The JavaDoc for MIA is available [here](https://javadoc.io/doc/io.github.mianalysis/mia).