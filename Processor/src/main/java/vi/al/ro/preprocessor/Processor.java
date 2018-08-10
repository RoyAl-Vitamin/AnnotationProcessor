package vi.al.ro.preprocessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//@SupportedSourceVersion(SourceVersion.latestSupported())
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "vi.al.ro.preprocessor.Index"
})
@SupportedOptions(value = {
        Processor.OPTION_INDEX_CLASS_PACKAGE,
        Processor.OPTION_INDEX_CLASS_NAME
})
public class Processor extends AbstractProcessor {

    public static final String OPTION_INDEX_CLASS_PACKAGE = "typeIndexClassPackage";

    public static final String OPTION_INDEX_CLASS_NAME = "typeIndexClassName";

    private String indexClassPackage;

    private String indexClassName;

    private boolean processed;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Map<String, String> options = processingEnv.getOptions();

        indexClassPackage = options.get(OPTION_INDEX_CLASS_PACKAGE);
        indexClassName = options.get(OPTION_INDEX_CLASS_NAME);

        processed = false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (processed) {
            return true;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Index.class);

        if (elements.size() == 0) {
            return false;
        }

        IndexGenerator generator = new IndexGenerator();

        String indexClassPackage = processingEnv.getOptions().get(OPTION_INDEX_CLASS_PACKAGE);

        if (indexClassPackage != null) {
            generator.setTargetPackage(indexClassPackage);
        }

        String indexClassName = processingEnv.getOptions().get(OPTION_INDEX_CLASS_NAME);

        if (indexClassPackage != null) {
            generator.setTargetClassName(indexClassName);
        }

        for (Element item : elements) {
            Index index = item.getAnnotation(Index.class);
            generator.add(index.tag(), ((TypeElement) item).getQualifiedName().toString());
        }

        try {
            generator.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error("Save generated source error [{0}]", e.getMessage());
        }

        processed = true;

        return true;
    }

    private void error(String format, Object... params) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, MessageFormat.format(format, params)
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
