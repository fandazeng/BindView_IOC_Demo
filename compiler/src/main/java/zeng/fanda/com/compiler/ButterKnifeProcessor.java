package zeng.fanda.com.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import zeng.fanda.com.annotation.BindView;
import zeng.fanda.com.annotation.OnClick;

/**
 * @author 曾凡达
 * @date 2019/6/28
 */
public class ButterKnifeProcessor extends AbstractProcessor {

    private Elements mElementsUtils;
    private Filer mFiler;
    private Map<TypeElement, Set<Element>> mElems;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        mElementsUtils = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mElems = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        boolean hasError = verifyGeneratedCode(roundEnvironment.getElementsAnnotatedWith(BindView.class));
        if (!hasError) {
            initBindElems(roundEnvironment.getElementsAnnotatedWith(BindView.class));
            generateJavaClass();
        }
        return true;
    }

    /**
     * public final class MainActivity$ViewBinder {
     * public static void bindView(MainActivity target) {
     * target.mIOC = (android.widget.TextView)target.findViewById(2131165325);
     * target.mOtherIOC = (android.widget.TextView)target.findViewById(2131165326);
     * }
     * }
     */
    private void generateJavaClass() {
        for (TypeElement enclosedElem : mElems.keySet()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.get(enclosedElem.asType()), "target")
                    .returns(TypeName.VOID);
            for (Element bindElem : mElems.get(enclosedElem)) {
                methodSpecBuilder.addStatement(String.format("target.%s = (%s)target.findViewById(%d)", bindElem.getSimpleName(), bindElem.asType(), bindElem.getAnnotation(BindView.class).value()));
            }
            TypeSpec typeSpec = TypeSpec.classBuilder(enclosedElem.getSimpleName() + "$ViewBinder")
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                    .addMethod(methodSpecBuilder.build())
                    .build();
            JavaFile file = JavaFile.builder(getPackageName(enclosedElem), typeSpec).build();
            try {
                file.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  获取包路径
     */
     private String getPackageName(TypeElement enclosedElem) {
        return mElementsUtils.getPackageOf(enclosedElem).getQualifiedName().toString();
    }

    /**
     * 初始化绑定元素
     */
    private void initBindElems(Set<? extends Element> bindElems) {
        for (Element bindElem : bindElems) {
            TypeElement enclosedElem = (TypeElement) bindElem.getEnclosingElement();
            Set<Element> elems = mElems.get(enclosedElem);
            if (elems == null) {
                elems = new HashSet<>();
                mElems.put(enclosedElem, elems);
            }
            elems.add(bindElem);
        }
    }

    /**
     * 校验代码生成限制
     */
    private boolean verifyGeneratedCode(Set<? extends Element> bindElems) {
        boolean hasError = false;
        for (Element bindElem : bindElems) {
            TypeElement enclosingElement = (TypeElement) bindElem.getEnclosingElement();

            // 校验修饰符，不能是静态和私有
            Set<Modifier> modifiers = bindElem.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
                hasError = true;
            }

            //检验容器类型(接口、类等等)，只能用于类类型
            if (enclosingElement.getKind() != ElementKind.CLASS) {
                hasError = true;
            }

            // 校验容器类的修饰符，不能是私有类
            if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
                hasError = true;
            }
        }

        return hasError;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(BindView.class.getCanonicalName());
        set.add(OnClick.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    
}
