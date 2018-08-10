package vi.al.ro.preprocessor;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Генератор исходного кода класса индексов
 */
public class IndexGenerator {

    private final Map<String, Set<String>> map = new TreeMap<>();

    private String targetPackage = "";

    private String targetClass = "TypeIndex";

    public IndexGenerator() {
    }

    public IndexGenerator(String targetPackage, String targetClass) {
        this.targetPackage = targetPackage;
        this.targetClass = targetClass;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public void setTargetClassName(String targetClass) {
        this.targetClass = targetClass;
    }

    public void add(String tag, String className) {

        if (tag == null) {
            tag = "";
        } else {
            tag = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, tag);
        }

        Set<String> index = map.get(tag);

        if (index == null) {
            map.put(tag, index = new TreeSet<>());
        }

        index.add(className);
    }

    @Override
    public String toString() {
        return render().toString();
    }

    public void writeTo(Filer filer) throws IOException {
        render().writeTo(filer);
    }

    public void writeTo(File file) throws IOException {
        render().writeTo(file);
    }

    public void writeTo(Path path) throws IOException {
        render().writeTo(path);
    }

    public void writeTo(Appendable appendable) throws IOException {
        render().writeTo(appendable);
    }

    private JavaFile render() {
        TypeSpec.Builder typeIndexBuilder = TypeSpec.classBuilder(targetClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (String item : map.keySet()) {
            typeIndexBuilder.addField(buildTagIndex(item));
        }

        TypeSpec typeIndex = typeIndexBuilder.build();

        return JavaFile
                .builder(targetPackage, typeIndex)
                .skipJavaLangImports(true)
                .build();
    }

    private FieldSpec buildTagIndex(String name) {

        String fieldName;

        if (name == null || name.length() == 0) {
            fieldName = "TYPES";
        } else {
            fieldName = "TYPES_" + name;
        }

        FieldSpec.Builder b = FieldSpec
                .builder(ArrayTypeName.of(Class.class), fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        CodeBlock.Builder indexValues = CodeBlock.builder();

        indexValues.add("{\n").indent();

        boolean first = true;

        for (String item : map.get(name)) {
            if (!first) {
                indexValues.add(",\n");
            } else {
                first = false;
            }
            indexValues.add(item + ".class");
        }

        indexValues.add("\n").unindent().add("}");

        return b.initializer(indexValues.build()).build();
    }

}
