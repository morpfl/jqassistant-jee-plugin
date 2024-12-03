package org.jqassistant.plugin.jee.injection.test;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import org.jqassistant.plugin.jee.injection.test.set.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class InjectionIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "jee-injection:BeanProducer".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void beanProducerIdentification() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class);
        Result<Concept> conceptResult = applyConcept("jee-injection:BeanProducer");
        store.beginTransaction();
        assertThat(conceptResult.getStatus(), equalTo(Result.Status.SUCCESS));
        assertThat(conceptResult.getRows().size(), equalTo(3));
        assertThat(conceptResult.getRows().get(0).getColumns().get("Injectable").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(conceptResult.getRows().get(0).getColumns().get("BeanProducers").getLabel(), endsWith("beanProducerA()"));
        assertThat(conceptResult.getRows().get(1).getColumns().get("Injectable").getLabel(), endsWith("test.set.InjectableB"));
        assertThat(conceptResult.getRows().get(1).getColumns().get("BeanProducers").getLabel(), endsWith("beanProducerB()"));
        assertThat(conceptResult.getRows().get(2).getColumns().get("Injectable").getLabel(), endsWith("java.lang.String"));
        assertThat(conceptResult.getRows().get(2).getColumns().get("BeanProducers").getLabel(), endsWith("beanProducerC()"));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "jee-injection:InjectionPoint".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void injectionPointIdentification() throws Exception {
        scanClasses(BeanProducer.class);
        Result<Concept> conceptResult = applyConcept("jee-injection:InjectionPoint");
        store.beginTransaction();
        assertThat(conceptResult.getStatus(), equalTo(Result.Status.SUCCESS));
        assertThat(conceptResult.getRows().size(), equalTo(3));
        assertThat(conceptResult.getRows().get(0).getColumns().get("InjectionPoint").getLabel(), endsWith("LocalEjb ejb"));
        assertThat(conceptResult.getRows().get(1).getColumns().get("InjectionPoint").getLabel(), endsWith("java.lang.Object injectionPointField"));
        assertThat(conceptResult.getRows().get(2).getColumns().get("InjectionPoint").getLabel(), endsWith("void test()"));


        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:BeanProducerMustNotBeInvokedDirectly".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void beanProducerAccess() throws Exception {
        scanClasses(BeanProducer.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:BeanProducerMustNotBeInvokedDirectly");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.BeanProducer"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Invocation").getLabel(), endsWith("void beanProducerAccessor()"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:FieldsOfInjectablesMustNotBeManipulated".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void injectableFieldManipulation() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, InjectableB.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:FieldsOfInjectablesMustNotBeManipulated");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(2));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Injectable").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("WriteToInjectableField").getLabel(), endsWith("void manipulateField()"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable1"));
        assertThat(constraintResult.getRows().get(1).getColumns().get("Injectable").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(1).getColumns().get("WriteToInjectableField").getLabel(), endsWith("void accessFieldStatically()"));
        assertThat(constraintResult.getRows().get(1).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable2"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:InjectablesShouldBeHeldInFinalFields".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void finalFieldsForInjectables() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, InjectableB.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:InjectablesShouldBeHeldInFinalFields");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(2));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable1"));
        assertThat(constraintResult.getRows().get(1).getColumns().get("Type").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(1).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable2"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:InjectablesMustNotBeAccessedStatically".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void staticAccessOfInjectables() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, InjectableB.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:InjectablesMustNotBeAccessedStatically");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Method").getLabel(), endsWith("void accessFieldStatically()"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable2"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:InjectablesMustNotBeHeldInStaticVariables".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void staticVariablesForInjectables() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, InjectableB.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:InjectablesMustNotBeHeldInStaticVariables");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Field").getLabel(), endsWith("InjectableB fieldOfInjectable2"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:InjectablesMustNotBeInstantiated".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void injectableInstantiation() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, InjectableB.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:InjectablesMustNotBeInstantiated");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.InjectableA"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Method").getLabel(), endsWith("void injectableInstantiation()"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Injectable").getLabel(), endsWith("test.set.InjectableB"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:InjectablesMustOnlyBeHeldInInjectables".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void injectableOfNonInjectable() throws Exception {
        scanClasses(BeanProducer.class, InjectableA.class, NonInjectableType.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:InjectablesMustOnlyBeHeldInInjectables");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("NonInjectableHavingInjectablesAsField").getLabel(), endsWith("test.set.NonInjectableType"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Fields").getLabel(), endsWith("test.set.InjectableA"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:JdkClassesMustNotBeInjectables".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void jdkClassesAsInjectables() throws Exception {
        scanClasses(BeanProducer.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:JdkClassesMustNotBeInjectables");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Injectable").getLabel(), endsWith("java.lang.String"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:NoCombinationOfBeanProducersAndApplicationCode".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void combinationOfBeanProducersAndApplicationCode() throws Exception {
        scanClasses(BeanProducer.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:NoCombinationOfBeanProducersAndApplicationCode");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Java-Klasse").getLabel(), endsWith("test.set.BeanProducer"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jee-injection:FieldInjectionIsNotAllowed".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void fieldInjection() throws Exception {
        scanClasses(BeanProducer.class, LocalEjb.class);
        Result<Constraint> constraintResult = validateConstraint("jee-injection:FieldInjectionIsNotAllowed");
        store.beginTransaction();
        assertThat(constraintResult.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(constraintResult.getRows().size(), equalTo(1));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel(), endsWith("test.set.BeanProducer"));
        assertThat(constraintResult.getRows().get(0).getColumns().get("Field").getLabel(), endsWith("Object injectionPointField"));
        store.commitTransaction();
    }
}