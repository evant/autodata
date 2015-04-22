package me.tatarka.autodata.compiler.internal;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.io.Closeables;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.compiler.model.AutoDataGetterMethod;
import me.tatarka.autodata.plugins.AutoEquals;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by evan on 4/20/15.
 */
// Use a wildcard to allow user-defined AutoData annotations by putting them in the same package.
@SupportedAnnotationTypes("me.tatarka.autodata.base.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoDataAnnotationProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private Map<String, AutoDataProcessor> processors = Maps.newLinkedHashMap();
    private Set<Element> processedElements = Sets.newHashSet();

    private static final List<Class> DEFAULTS = Arrays.asList(new Class[]{
            AutoEquals.class
    });

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        ServiceLoader<AutoDataProcessor> serviceLoader = ServiceLoader.load(AutoDataProcessor.class);
        for (AutoDataProcessor processor : serviceLoader) {
            processors.put(processor.forAnnotation().getName(), processor);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                AutoData autoData = element.getAnnotation(AutoData.class);
                if (element.getKind() == ElementKind.CLASS) {
                    // We are likely to hit the same element multiple times because multiple of the 
                    // annotations that we are listening to may be on it.
                    if (processedElements.contains(element)) {
                        continue;
                    }

                    processedElements.add(element);

                    try {
                        if (autoData != null) {
                            Set<Annotation> annotationSet = Sets.newHashSet();
                            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                                Class<Annotation> processAnnotationClass = getDeclaredAnnotationClass(annotationMirror);
                                if (processAnnotationClass.getName().equals(AutoData.class.getName())) {
                                    // Skip adding AutoData processor, as it's already hard-coded to always run.
                                    continue;
                                }
                                annotationSet.add(element.getAnnotation(processAnnotationClass));
                            }

                            processAutoData(autoData, annotationSet, (TypeElement) element);
                        } else {
                            // Is a user-defined annotation, look for it and retrieve it's annotations.
                            AnnotationMirror annotationMirror = element.getAnnotationMirrors().iterator().next();
                            Class<Annotation> userAnnotation = getDeclaredAnnotationClass(annotationMirror);
                            autoData = userAnnotation.getAnnotation(AutoData.class);

                            if (autoData == null) {
                                messager.printMessage(Diagnostic.Kind.ERROR, "Custom AutoData annotation " + userAnnotation + " is not annotated with @AutoData.");
                                continue;
                            }

                            Set<Annotation> annotationSet = Sets.newHashSet();
                            for (Annotation processAnnotation : userAnnotation.getAnnotations()) {
                                if (processAnnotation.getClass().getName().equals(AutoData.class.getName())) {
                                    // Skipp adding AutoData processor, as it's already hard-coded to always run.
                                    continue;
                                }
                                annotationSet.add(processAnnotation);
                            }

                            processAutoData(autoData, annotationSet, (TypeElement) element);
                        }

                    } catch (IOException | ClassNotFoundException | RuntimeException e) {
                        // Don't propagate this exception, which will confusingly crash the compiler.
                        // Instead, report a compiler error with the stack trace.
                        String trace = Throwables.getStackTraceAsString(e);
                        messager.printMessage(Diagnostic.Kind.ERROR, "AutoData threw an exception: " + trace, element);
                    }
                }
            }
        }
        return false;
    }

    private void processAutoData(AutoData autoData, Set<Annotation> processAnnotations, TypeElement classElement) throws IOException {
        if (!classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Class " + classElement.getQualifiedName() + " must be abstract.", classElement);
            return;
        }

        BiMap<AutoDataField, AutoDataGetterMethod> fields = HashBiMap.create();

        boolean wasError = false; // Set this so we can show as many errors as we can before bailing out.
        for (Element element : classElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD || !element.getModifiers().contains(Modifier.ABSTRACT) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) element;
            if (!methodElement.getParameters().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Abstract method " + methodElement.getSimpleName() + " in class " + classElement.getQualifiedName() + " must not take any arguments.", methodElement);
                wasError = true;
            }

            TypeMirror returnType = methodElement.getReturnType();
            if (returnType.getKind() == TypeKind.VOID) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Abstract method " + methodElement.getSimpleName() + " in class " + classElement.getQualifiedName() + " must have a non-void return type.", methodElement);
                wasError = true;
            }

            String methodName = element.getSimpleName().toString();
            TypeName methodReturnType = TypeName.get(methodElement.getReturnType());

            String fieldName = nameWithoutPrefix(methodName);

            AutoDataField field = new AutoDataField(fieldName, methodReturnType, false);
            AutoDataGetterMethod previousMethod;
            if ((previousMethod = fields.get(field)) != null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "More than one AutoData field called " + fieldName + " in class " + classElement.getQualifiedName() + " (" + methodName + " and " + previousMethod.getName() + ").", methodElement);
                wasError = true;
            }

            AutoDataGetterMethod method = new AutoDataGetterMethod(methodName, methodReturnType);
            fields.put(field, method);
        }

        if (wasError) {
            return;
        }

        String packageName = packageName(classElement.getQualifiedName().toString());
        TypeName className = TypeName.get(classElement.asType());

        AutoDataClass autoDataClass = new AutoDataClass(packageName, className, fields, null);
        TypeSpec.Builder genClassBuilder = TypeSpec.classBuilder(autoDataClass.getGenSimpleClassName());

        if (autoData.defaults()) {
            getProcessor(AutoEquals.class).process(null, autoDataClass, genClassBuilder);
            for (Annotation annotation : removeDefaults(processAnnotations)) {
                getProcessor(annotation).process(annotation, autoDataClass, genClassBuilder);
            }
        } else {
            for (Annotation annotation : processAnnotations) {
                AutoDataProcessor<Annotation> processor = getProcessor(annotation);
                if (processor == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Missing AutoDataProcessor for annotation " + annotation + ".");
                    continue;
                }
                processor.process(annotation, autoDataClass, genClassBuilder);
            }
        }
        getProcessor(AutoData.class).process(autoData, autoDataClass, genClassBuilder);

        Writer writer = null;
        boolean threw = true;
        try {
            JavaFileObject jfo = filer.createSourceFile(autoDataClass.getGenQualifiedClassName());
            writer = jfo.openWriter();
            JavaFile javaFile = JavaFile.builder(packageName, genClassBuilder.build())
                    .skipJavaLangImports(true)
                    .build();
            javaFile.writeTo(writer);
            threw = false;
        } finally {
            Closeables.close(writer, threw);
        }
    }

    private <T extends Annotation> AutoDataProcessor<T> getProcessor(Class<T> annotationClass) {
        return processors.get(annotationClass.getName());
    }

    private <T extends Annotation> AutoDataProcessor<T> getProcessor(T annotation) {
        return processors.get(annotation.getClass().getInterfaces()[0].getName());
    }

    private static Iterable<Annotation> removeDefaults(Iterable<Annotation> annotations) {
        return Iterables.filter(annotations, new Predicate<Annotation>() {
            @Override
            public boolean apply(Annotation input) {
                return !DEFAULTS.contains(input.getClass());
            }
        });
    }

    private static String packageName(String className) {
        if (className == null || className.isEmpty() || !className.contains(".")) {
            return "";
        }
        return className.substring(0, className.lastIndexOf("."));
    }

    private static String nameWithoutPrefix(String name) {
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }
        return Introspector.decapitalize(name);
    }

    private static Class<Annotation> getDeclaredAnnotationClass(AnnotationMirror mirror) throws ClassNotFoundException {
        TypeElement element = (TypeElement) mirror.getAnnotationType().asElement();
        return (Class<Annotation>) Class.forName(element.getQualifiedName().toString());
    }
}
