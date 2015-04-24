**Warning!** This is a work in progress, not everything described below is complete.

# AutoData
An extensable alternative to [AutoValue](https://github.com/google/auto/tree/master/value)

## Goals
- Be a drop-in replacement for AutoValue, with all it's features.
- Allow mix-and-match usage of features, and write your own code-gen extensions easily.

## Basic Usage
You should look at the documentation for [AutoValue](https://github.com/google/auto/tree/master/value) to see how to use it, since the base feature set is the same. Just replace `@com.google.auto.value.AutoValue` with `@me.tatarka.base.AutoData` and `@com.google.auto.value.AutoValue.Builder` with `@me.tatarka.base.AutoData`.

## Applying a plugin

You can apply addional plugins by simply adding more annotations. By default, the plugins `@AutoDataEquals`, `@AutoDataToString`, and `@AutoDataBuilder` are applied. You can disable the defaults by setting `@AutoData(defaults = false)`. Therefore, to simulate the default settings you can do:
```java
@AutoData(defaults=false) @AutoDataEquals @AutoDataToString @AutoDataBuilder
public abstract class MyClass {}
```

However, this may become cumbersome to apply additional annations to all of your classes. Instead, and actually prefered, you can create your own annotation on which you can apply the plugins to. This annotation **must** be in the package `me.tatarka.autovalue.base` (or a subpackage of that) and **must** be annotated with at least `@AutoData`.
```java
package me.tatarka.autovalue.base.com.example;

@AutoData(defaults=false) @AutoDataEquals @AutoDataToString @AutoDataBuilder
public @interface @Data {}
```

You can now simply apply that annotation on your classes instead.
```java
@Data
public abstract class MyClass {}
```

## Writing your own plugin
In order to write your own plugin, you should create 2 projects: one to include your annotation at runtime, and one for the annotation processor. You also want the processor-part of your plugin to depend on `autodata-compiler`.

You can name your annotation whatever you want, below is an example of the `@AutoEquals` plugin that is provided by default. Both a retention of `RUNTIME` and a target of `ANNOTATION_TYPE` are required to use your annotation with the user-defined annotation feature described above.
```java
// No package requirements here!
package me.tatarka.autodata.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface AutoEquals {
}
```

Then create your processor in the other project, implementing `AutoDataProcessor`. This is a [Service Provider](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) and you configure it as such. However, `autodata-compiler` includes [AutoService](https://github.com/google/auto/tree/master/service) to make this super easy (Yeah an annotation-processor used for an annotation-processor, who would have thunk!).
```java
package me.tatarka.autodata.compiler.plugins;

import com.google.auto.service.AutoService;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.plugins.AutoEquals;
import javax.annotation.processing.ProcessingEnvironment;

@AutoService(AutoDataProcessor.class)
public class AutoEqualsProcessor implements AutoDataProcessor<AutoEquals> {
  @Override
  public Class<AutoEquals> forAnnotation() {
    return AutoEquals.class;
  }
  
  @Override
  public void init(ProcessingEnvironment env) {
  }

  @Override
  public void process(AutoEquals annotation, final AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilder) {
    ...
  }
}
```
You need to provied your annotation that you wrote earlier.

The `init()` method gives you the annotation processor's environment. This allows you to report error messages, use the type and element utils, and write your own addiontal classes if you so choose.

The meat of the implemetation goes in `process`. This takes an `AutoDataClass` that provieds information about the class you are processing, and `AutoDataClassBuilder` where you output anything you want to generate. The code geneator uses [JavaPoet](https://github.com/square/javapoet) and you should check out their documentation for how to use that api. It also takes an instance of the annotation you defined. Feel free to declare any annotation arguments if you want to use them to customize your processing.
