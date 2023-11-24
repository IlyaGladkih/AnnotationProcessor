package example.processors;

import example.annotations.Builder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("example.annotations.Builder")
public class BuilderAnnotationProcessor extends AbstractProcessor {

    HashMap<Element, List<? extends Element>> map = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements =
                    roundEnv.getElementsAnnotatedWith(Builder.class);

        for(Element e:elements){
            if (e.getKind().isClass()){
                map.put(e,e.getEnclosedElements());
            }
        }

        for (Element e:map.keySet()){
            List<? extends Element> elementsInClass = map.get(e);
            List<String> methods = new ArrayList<>();

            String className = e.getSimpleName().toString()+"Builder";

            for (Element elem:elementsInClass){
                if (elem.getKind().isField()){
                    String fieldClass = elem.getSimpleName().toString();
                    Set<Modifier> modifiers = elem.getModifiers();
                    StringBuilder camelCaseName = new StringBuilder(fieldClass);
                    camelCaseName.setCharAt(0,fieldClass.substring(0,1).toUpperCase().charAt(0));
                    if(!modifiers.contains(Modifier.STATIC)){
                        String method = "public " + className + " set" + camelCaseName + "("+elem.asType()+" "+fieldClass.toLowerCase()+")"+
                                "{object.set"+camelCaseName+"(" + fieldClass.toLowerCase()+");"+
                                "return this;}";
                        System.out.println(method);
                        methods.add(method);
                    }

                }

            }


            String classHead = "public class " + className+"{private "+ e.getSimpleName().toString() + " object = new " +
                    e.getSimpleName().toString()+"();";

            String buildMethod = "public "+ e.getSimpleName().toString() + " build(){return object;}";
            writeBuilderFile(methods,classHead,className,buildMethod);
        }

        return true;
    }

    public void writeBuilderFile(List<String> methods,
                                 String classHead,String className, String buildMethod){



        try{
            JavaFileObject file = processingEnv.getFiler().createSourceFile(className);
            PrintWriter writer = new PrintWriter(file.openWriter());

            writer.write(classHead);

            for (String method:methods){
                writer.write(method);
            }

            writer.write(buildMethod);
            writer.write("}");
            writer.close();

        } catch (IOException e) {
            System.out.println("File already exist");
        }
    }
}
