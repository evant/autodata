package me.tatarka.autodata.compiler.internal;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.squareup.javapoet.JavaFile;

import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.compiler.model.AutoDataGetterMethod;
import me.tatarka.autodata.plugins.AutoBuilder;
import me.tatarka.autodata.plugins.AutoEquals;
import me.tatarka.autodata.plugins.AutoToString;

/**
 * Created by evan on 4/20/15.
 */
// Use a wildcard to allow user-defined AutoData annotations by putting them in the same package.
@SupportedAnnotationTypes("me.tatarka.autodata.base.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutoDataAnnotationProcessor extends AbstractProcessor {
    private static final String PREFIX = "AutoData_";

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private Map<String, AutoDataProcessor> processors = Maps.newLinkedHashMap();
    private AutoDataProcessor<AutoData> baseProcessor = new AutoDataBaseProcessor();
    private Set<Element> processedElements = Sets.newHashSet();

    private static final List<Class> DEFAULTS = Arrays.asList(new Class[]{
            AutoEquals.class, AutoToString.class, AutoBuilder.class
    });

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();

        baseProcessor.init(processingEnv);

        ResourceFinder resourceFinder = new ResourceFinder("META-INF/services/", getClass().getClassLoader());
        List<Class<? extends AutoDataProcessor>> processorClasses;

        try {
            processorClasses = resourceFinder.findAllImplementations(AutoDataProcessor.class);
        } catch (IOException | ClassNotFoundException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return;
        }

        if (processorClasses.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Did not find any AutoDataProcessors");
            return;
        }

        for (Class<? extends AutoDataProcessor> processorClass : processorClasses) {
            AutoDataProcessor processor;
            try {
                processor = processorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                continue;
            }
            processor.init(processingEnv);
            String annotationName = getAnnotationNameForProcessor(processor);
            if (annotationName != null) {
                processors.put(annotationName, processor);
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "AutoDataProcessor " + processor + " does not provide required annotation generic type parameter.");
            }
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
                                if (getAnnotationName(processAnnotationClass).equals(AutoData.class.getName())) {
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
                            // May not be a valid user annotation after all (example: @AutoData.Builder)
                            if (userAnnotation == null) {
                                continue;
                            }

                            autoData = userAnnotation.getAnnotation(AutoData.class);

                            if (autoData == null) {
                                messager.printMessage(Diagnostic.Kind.ERROR, "Custom AutoData annotation " + userAnnotation + " is not annotated with @AutoData.");
                                continue;
                            }

                            Set<Annotation> annotationSet = Sets.newHashSet();
                            for (Annotation processAnnotation : userAnnotation.getAnnotations()) {
                                if (getAnnotationName(processAnnotation).equals(AutoData.class.getName())) {
                                    // Skip adding AutoData processor, as it's already hard-coded to always run.
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

        Map<String, AutoDataField> fieldMap = Maps.newLinkedHashMap();

        boolean wasError = false; // Set this so we can show as many errors as we can before bailing out.

        List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
        boolean allGetters = allGetters(methods);

        for (ExecutableElement element : methods) {
            if (!element.getModifiers().contains(Modifier.ABSTRACT) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                continue;
            }

            if (!element.getParameters().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Abstract method " + element.getSimpleName() + " in class " + classElement.getQualifiedName() + " must not take any arguments.", element);
                wasError = true;
            }

            TypeMirror returnType = element.getReturnType();
            if (returnType.getKind() == TypeKind.VOID) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Abstract method " + element.getSimpleName() + " in class " + classElement.getQualifiedName() + " must have a non-void return type.", element);
                wasError = true;
            }

            if (returnType instanceof ArrayType && !((ArrayType) returnType).getComponentType().getKind().isPrimitive()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method " + element.getSimpleName() + " cannot return a non-primitive array in class " + classElement.getQualifiedName() + ".", element);
                wasError = true;
            }

            String methodName = element.getSimpleName().toString();
            String fieldName = allGetters ? nameWithoutPrefix(methodName) : methodName;
            AutoDataGetterMethod method = new AutoDataGetterMethod(element);
            AutoDataField field = new AutoDataField(fieldName, method);

            AutoDataField previousField;
            if ((previousField = fieldMap.get(field.getName())) != null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "More than one AutoData field called " + fieldName + " in class " + classElement.getQualifiedName() + " (" + methodName + " and " + previousField.getGetterMethod().getName() + ").", element);
                wasError = true;
            }

            fieldMap.put(fieldName, field);
        }

        if (wasError) {
            return;
        }

        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        String className = PREFIX + buildClassName(classElement);
        List<ExecutableElement> declaredMethods = ElementFilter.methodsIn(classElement.getEnclosedElements());


        AutoDataClass autoDataClass = new AutoDataClass(className, classElement, fieldMap.values());
        AutoDataClassBuilderImpl genClassBuilder = new AutoDataClassBuilderImpl(className, autoDataClass, declaredMethods, messager, typeUtils);

        Iterable<Annotation> pluginAnnotations;
        if (autoData.defaults()) {
            for (Class<?> annotation : DEFAULTS) {
                String name = getAnnotationName(annotation);
                genClassBuilder.setCurrentPluginName(name);
                AutoDataProcessor processor = processors.get(name);
                if (processor != null) {
                    processor.process(null, autoDataClass, genClassBuilder);
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Missing AutoDataProcessor for annotation " + annotation + ".");
                }
            }
            pluginAnnotations = removeDefaults(processAnnotations);
        } else {
            pluginAnnotations = processAnnotations;
        }

        for (Annotation annotation : pluginAnnotations) {
            String name = getAnnotationName(annotation);
            genClassBuilder.setCurrentPluginName(name);
            AutoDataProcessor<Annotation> processor = processors.get(name);
            if (processor == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Missing AutoDataProcessor for annotation " + annotation + ".");
                continue;
            }
            processor.process(annotation, autoDataClass, genClassBuilder);
        }

        baseProcessor.process(autoData, autoDataClass, genClassBuilder);

        String qualifiedClassName;
        if (Strings.isNullOrEmpty(packageName)) {
            qualifiedClassName = className;
        } else {
            qualifiedClassName = packageName + "." + className;
        }

        Writer writer = null;
        boolean threw = true;
        try {
            JavaFileObject jfo = filer.createSourceFile(qualifiedClassName);
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

    private <T extends Annotation> String getAnnotationName(Class<?> annotationClass) {
        return annotationClass.getName();
    }

    private <T extends Annotation> String getAnnotationName(T annotation) {
        return annotation.getClass().getInterfaces()[0].getName();
    }

    private static Iterable<Annotation> removeDefaults(Iterable<Annotation> annotations) {
        return Iterables.filter(annotations, new Predicate<Annotation>() {
            @Override
            public boolean apply(Annotation input) {
                return !DEFAULTS.contains(input.getClass());
            }
        });
    }

    private static boolean allGetters(Iterable<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            String name = method.getSimpleName().toString();
            boolean get = name.startsWith("get") && !name.equals("get");
            boolean is = name.startsWith("is") && !name.equals("is")
                    && method.getReturnType().getKind() == TypeKind.BOOLEAN;
            if (!get && !is) {
                return false;
            }
        }
        return true;
    }

    private static String nameWithoutPrefix(String name) {
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }
        return Introspector.decapitalize(name);
    }

    @Nullable
    private static Class<Annotation> getDeclaredAnnotationClass(AnnotationMirror mirror) throws ClassNotFoundException {
        TypeElement element = (TypeElement) mirror.getAnnotationType().asElement();
        // Ensure the annotation has the correct retention and targets.
        Retention retention = element.getAnnotation(Retention.class);
        if (retention != null && retention.value() != RetentionPolicy.RUNTIME) {
            return null;
        }
        Target target = element.getAnnotation(Target.class);
        if (target != null) {
            if (target.value().length < 2) {
                return null;
            }
            List<ElementType> targets = Arrays.asList(target.value());
            if (!(targets.contains(ElementType.TYPE) && targets.contains(ElementType.ANNOTATION_TYPE))) {
                return null;
            }
        }
        return (Class<Annotation>) Class.forName(element.getQualifiedName().toString());
    }

    @Nullable
    private static String getAnnotationNameForProcessor(AutoDataProcessor processor) {
        for (Type iface : processor.getClass().getGenericInterfaces()) {
            String name = iface.getTypeName();
            if (name.startsWith(AutoDataProcessor.class.getName())) {
                if (iface instanceof ParameterizedType) {
                    return ((ParameterizedType) iface).getActualTypeArguments()[0].getTypeName();
                }

            }
        }
        return null;
    }

    private static String buildClassName(Element classElement) {
        String name = classElement.getSimpleName().toString();
        Element enclosing = classElement.getEnclosingElement();
        if (enclosing instanceof PackageElement) {
            return name;
        } else {
            return buildClassName(enclosing) + "_" + name;
        }
    }
}
