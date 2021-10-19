/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mapstruct.control.MappingControl;
import org.mapstruct.factory.Mappers;

import static org.mapstruct.NullValueCheckStrategy.ON_IMPLICIT_CONVERSION;
import static org.mapstruct.SubclassExhaustiveStrategy.COMPILE_ERROR;

/**
 * Marks an interface or abstract class as a mapper and activates the generation of a implementation of that type via
 * MapStruct.
 *
 * <p>
 * <strong>Example 1:</strong> Creating mapper
 * </p>
 * <pre><code class='java'>
 * &#64;Mapper
 * public interface CarMapper {
 *     CarDto toCarDto(Car source);
 * }
 * </code></pre>
 * <p>
 * <strong>Example 2:</strong> Use additional mappers with parameters {@link #uses()}, {@link #componentModel()}
 * and {@link #injectionStrategy()}
 * </p>
 * <pre><code class='java'>
 * // we have MarkMapper (map field "mark" to field "name" to upper case)
 * &#64;Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
 * public class MarkMapper {
 *     public String mapMark(String mark) {
 *         return mark.toUpperCase();
 *     }
 * }
 * // we have CarMapper
 * &#64;Mapper(
 *      componentModel = MappingConstants.ComponentModel.SPRING,
 *      uses = MarkMapper.class,
 *      injectionStrategy = InjectionStrategy.CONSTRUCTOR)
 * public interface CarMapper {
 *     &#64;Mapping(target = "name", source = "mark")
 *     CarDto convertMap(CarEntity carEntity);
 * }
 * </code></pre>
 * <pre><code class='java'>
 * // generates
 * &#64;Component
 * public class CarMapperImpl implements CarMapper {
 *     private final MarkMapper markMapper;
 *     &#64;Autowired
 *     public CarMapperImpl(MarkMapper markMapper) {
 *         this.markMapper = markMapper;
 *     }
 *     &#64;Override
 *     public CarDto convertMap(CarEntity carEntity) {
 *         if ( carEntity == null ) {
 *             return null;
 *         }
 *         CarDto carDto = new CarDto();
 *         carDto.setName( markMapper.mapMark( carEntity.getMark() ) );
 *         return carDto;
 *     }
 * }
 * </code></pre>
 *
 * @author Gunnar Morling
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Mapper {

    /**
     * Other mapper types used by this mapper. May be hand-written classes or other mappers generated by MapStruct. No
     * cycle between generated mapper classes must be created.
     *
     * @return The mapper types used by this mapper.
     */
    Class<?>[] uses() default { };

    /**
     * Additional types for which an import statement is to be added to the generated mapper implementation class.
     * This allows to refer to those types from within mapping expressions given via {@link Mapping#expression()},
     * {@link Mapping#defaultExpression()} or using
     * their simple name rather than their fully-qualified name.
     *
     * @return classes to add in the imports of the generated implementation.
     */
    Class<?>[] imports() default { };

    /**
     * How unmapped properties of the source type of a mapping should be
     * reported. The method overrides an unmappedSourcePolicy set in a central
     * configuration set by {@link #config() }
     *
     * @return The reporting policy for unmapped source properties.
     *
     * @since 1.3
     */
    ReportingPolicy unmappedSourcePolicy() default ReportingPolicy.IGNORE;

    /**
     * How unmapped properties of the target type of a mapping should be
     * reported. The method overrides an unmappedTargetPolicy set in a central
     * configuration set by {@link #config() }
     *
     * @return The reporting policy for unmapped target properties.
     */
    ReportingPolicy unmappedTargetPolicy() default ReportingPolicy.WARN;

    /**
     * How lossy (narrowing) conversion, for instance long to integer should be
     * reported. The method overrides an typeConversionPolicy set in a central
     * configuration set by {@link #config() }
     *
     * @since 1.3
     *
     * @return The reporting policy for unmapped target properties.
     */
    ReportingPolicy typeConversionPolicy() default ReportingPolicy.IGNORE;

    /**
     * Specifies the component model to which the generated mapper should
     * adhere. Supported values are
     * <ul>
     * <li> {@code default}: the mapper uses no component model, instances are
     * typically retrieved via {@link Mappers#getMapper(Class)}</li>
     * <li>
     * {@code cdi}: the generated mapper is an application-scoped CDI bean and
     * can be retrieved via {@code @Inject}</li>
     * <li>
     * {@code spring}: the generated mapper is a Spring bean and
     * can be retrieved via {@code @Autowired}</li>
     * <li>
     * {@code jsr330}: the generated mapper is annotated with {@code @javax.inject.Named} and
     * {@code @Singleton}, and can be retrieved via {@code @Inject}</li>
     * </ul>
     * The method overrides a componentModel set in a central configuration set
     * by {@link #config() }
     *
     * @return The component model for the generated mapper.
     */
    String componentModel() default MappingConstants.ComponentModel.DEFAULT;

    /**
     * Specifies the name of the implementation class. The {@code <CLASS_NAME>} will be replaced by the
     * interface/abstract class name.
     * <p>
     * Defaults to postfixing the name with {@code Impl}: {@code <CLASS_NAME>Impl}
     *
     * @return The implementation name.
     * @see #implementationPackage()
     */
    String implementationName() default "<CLASS_NAME>Impl";

    /**
     * Specifies the target package for the generated implementation. The {@code <PACKAGE_NAME>} will be replaced by the
     * interface's or abstract class' package.
     * <p>
     * Defaults to using the same package as the mapper interface/abstract class
     *
     * @return the implementation package.
     * @see #implementationName()
     */
    String implementationPackage() default "<PACKAGE_NAME>";

    /**
     * A class annotated with {@link MapperConfig} which should be used as configuration template. Any settings given
     * via {@link Mapper} will take precedence over the settings from the referenced configuration source. The list of
     * referenced mappers will contain all mappers given via {@link Mapper#uses()} and {@link MapperConfig#uses()}.
     *
     * @return A class which should be used as configuration template.
     */
    Class<?> config() default void.class;

    /**
     * The strategy to be applied when propagating the value of collection-typed properties. By default, only JavaBeans
     * accessor methods (setters or getters) will be used, but it is also possible to invoke a corresponding adder
     * method for each element of the source collection (e.g. {@code orderDto.addOrderLine()}).
     * <p>
     * Any setting given for this attribute will take precedence over {@link MapperConfig#collectionMappingStrategy()},
     * if present.
     *
     * @return The strategy applied when propagating the value of collection-typed properties.
     */
    CollectionMappingStrategy collectionMappingStrategy() default CollectionMappingStrategy.ACCESSOR_ONLY;

    /**
     * The strategy to be applied when {@code null} is passed as source argument value to the methods of this mapper.
     * If no strategy is configured, the strategy given via {@link MapperConfig#nullValueMappingStrategy()} will be
     * applied, using {@link NullValueMappingStrategy#RETURN_NULL} by default.
     *
     * @return The strategy to be applied when {@code null} is passed as source value to the methods of this mapper.
     */
    NullValueMappingStrategy nullValueMappingStrategy() default NullValueMappingStrategy.RETURN_NULL;

    /**
     * The strategy to be applied when a source bean property is {@code null} or not present. If no strategy is
     * configured, the strategy given via {@link MapperConfig#nullValuePropertyMappingStrategy()} will be applied,
     * {@link NullValuePropertyMappingStrategy#SET_TO_NULL} will be used by default.
     *
     * @since 1.3
     *
     * @return The strategy to be applied when {@code null} is passed as source property value or the source property
     * is not present.
     */
    NullValuePropertyMappingStrategy nullValuePropertyMappingStrategy() default
        NullValuePropertyMappingStrategy.SET_TO_NULL;

    /**
     * The strategy to use for applying method-level configuration annotations of prototype methods in the interface
     * specified with {@link #config()}. Annotations that can be inherited are for example {@link Mapping},
     * {@link IterableMapping}, {@link MapMapping}, or {@link BeanMapping}.
     * <p>
     * If no strategy is configured, the strategy given via {@link MapperConfig#mappingInheritanceStrategy()} will be
     * applied, using {@link MappingInheritanceStrategy#EXPLICIT} as default.
     *
     * @return The strategy to use for applying {@code @Mapping} configurations of prototype methods in the interface
     * specified with {@link #config()}.
     */
    MappingInheritanceStrategy mappingInheritanceStrategy() default MappingInheritanceStrategy.EXPLICIT;

    /**
     * Determines when to include a null check on the source property value of a bean mapping.
     *
     * Can be overridden by the one on {@link MapperConfig}, {@link BeanMapping}  or {@link Mapping}.
     *
     * @return strategy how to do null checking
     */
    NullValueCheckStrategy nullValueCheckStrategy() default ON_IMPLICIT_CONVERSION;

    /**
     * Determines how to handle missing implementation for super classes when using the {@link SubclassMapping}.
     *
     * Can be overridden by the one on {@link BeanMapping}, but overrides {@link MapperConfig}.
     *
     * @return strategy to handle missing implementation combined with {@link SubclassMappings}.
     *
     * @since 1.5
     */
    SubclassExhaustiveStrategy subclassExhaustiveStrategy() default COMPILE_ERROR;

    /**
     * Determines whether to use field or constructor injection. This is only used on annotated based component models
     * such as CDI, Spring and JSR 330.
     *
     * If no strategy is configured, {@link InjectionStrategy#FIELD} will be used as default.
     *
     * @return strategy how to inject
     */
    InjectionStrategy injectionStrategy() default InjectionStrategy.FIELD;

    /**
     * If MapStruct could not find another mapping method or apply an automatic conversion it will try to generate a
     * sub-mapping method between the two beans. If this property is set to {@code true} MapStruct will not try to
     * automatically generate sub-mapping methods.
     * <p>
     * Can be configured by the {@link MapperConfig#disableSubMappingMethodsGeneration()} as well.
     * <p>
     * Note: If you need to use {@code disableSubMappingMethodsGeneration} please contact the MapStruct team at
     * <a href="http://mapstruct.org">mapstruct.org</a> or
     * <a href="https://github.com/mapstruct/mapstruct">github.com/mapstruct/mapstruct</a> to share what problem you
     * are facing with the automatic sub-mapping generation.
     *
     * @return whether the automatic generation of sub-mapping methods is disabled
     *
     * @since 1.2
     */
    boolean disableSubMappingMethodsGeneration() default false;

    /**
     * The information that should be used for the builder mappings. This can be used to define custom build methods
     * for the builder strategy that one uses.
     *
     * If no builder is defined the builder given via {@link MapperConfig#builder()} will be applied.
     *
     * <p>
     * NOTE: In case no builder is defined here, in {@link BeanMapping} or {@link MapperConfig} and there is a single
     * build method, then that method would be used.
     * <p>
     * If the builder is defined and there is a single method that does not match the name of the build method then
     * a compile error will occur
     *
     * @return the builder information
     *
     * @since 1.3
     */
    Builder builder() default @Builder;

    /**
     * Allows detailed control over the mapping process.
     *
     * @return the mapping control
     *
     * @since 1.4
     *
     * @see org.mapstruct.control.DeepClone
     * @see org.mapstruct.control.NoComplexMapping
     * @see org.mapstruct.control.MappingControl
     */
    Class<? extends Annotation> mappingControl() default MappingControl.class;

    /**
     * Exception that should be thrown by the generated code if no mapping matches for enums.
     * If no exception is configured, the exception given via {@link MapperConfig#unexpectedValueMappingException()}
     * will be used, using {@link IllegalArgumentException} by default.
     *
     * <p>
     * Note:
     * <ul>
     *     <li>
     *      The defined exception should at least have a constructor with a {@link String} parameter.
     *     </li>
     *     <li>
     *      If the defined exception is a checked exception then the enum mapping methods should have that exception
     *      in the throws clause.
     *     </li>
     * </ul>
     *
     * @return the exception that should be used in the generated code
     *
     * @since 1.4
     */
    Class<? extends Exception> unexpectedValueMappingException() default IllegalArgumentException.class;
}
