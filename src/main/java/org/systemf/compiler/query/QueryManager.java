package org.systemf.compiler.query;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class QueryManager {
	private final HashSet<Object> entities = new HashSet<>();
	private final HashMap<Object, HashSet<Object>> attributes = new HashMap<>();
	private final HashSet<EntityProviderInfo> entityProviders = new HashSet<>();
	private final HashSet<AttributeProviderInfo> attributeProviders = new HashSet<>();

	public void registerProvider(EntityProvider<?> provider) {
		var entityClass = Arrays.stream(provider.getClass().getGenericInterfaces())
				.filter(type -> type instanceof ParameterizedType).map(type -> (ParameterizedType) type)
				.filter(type -> EntityProvider.class == type.getRawType())
				.map(ParameterizedType::getActualTypeArguments).filter(args -> args.length == 1).map(args -> args[0])
				.filter(arg -> arg instanceof Class).map(arg -> (Class<?>) arg).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Cannot find the entity class of the provider"));
		entityProviders.add(new EntityProviderInfo(entityClass, provider));
	}

	public void registerProvider(AttributeProvider<?, ?> provider) {
		var params = Arrays.stream(provider.getClass().getGenericInterfaces())
				.filter(type -> type instanceof ParameterizedType).map(type -> (ParameterizedType) type)
				.filter(type -> AttributeProvider.class == type.getRawType())
				.map(ParameterizedType::getActualTypeArguments).filter(args -> args.length == 2)
				.filter(args -> Arrays.stream(args).allMatch(arg -> arg instanceof Class)).findFirst().orElseThrow(
						() -> new IllegalArgumentException(
								"Cannot find the entity and the attribute class of the provider"));
		attributeProviders.add(new AttributeProviderInfo((Class<?>) params[0], (Class<?>) params[1], provider));
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> findInSet(Set<Object> set, Class<T> clazz) {
		return set.stream().filter(obj -> clazz.isAssignableFrom(obj.getClass())).findFirst().map(o -> (T) o);
	}

	@SuppressWarnings("unchecked")
	private <T> T produce(Class<T> entityClass) {
		var provider = (EntityProvider<? extends T>) entityProviders.stream()
				.filter(info -> entityClass.isAssignableFrom(info.entityClass)).findFirst()
				.orElseThrow(() -> new NoSuchElementException("Cannot find a suitable entity nor provider")).provider;
		var res = provider.produce();
		entities.add(res);
		return res;
	}

	public <T> T get(Class<T> entityClass) {
		return findInSet(entities, entityClass).orElseGet(() -> produce(entityClass));
	}

	@SuppressWarnings("unchecked")
	private <T, U> U produceAttribute(Set<Object> set, T entity, Class<U> attributeClass) {
		var entityClass = entity.getClass();
		var provider = (AttributeProvider<? super T, ? extends U>) attributeProviders.stream()
				.filter(info -> info.entityClass.isAssignableFrom(entityClass) &&
				                attributeClass.isAssignableFrom(info.attributeClass)).findFirst().orElseThrow(
						() -> new NoSuchElementException("Cannot find a suitable attribute nor provider")).provider;
		var res = provider.getAttribute(entity);
		set.add(res);
		return res;
	}

	public <T, U> U getAttribute(T entity, Class<U> attributeClass) {
		var set = attributes.computeIfAbsent(entity, _ -> new HashSet<>());
		return findInSet(set, attributeClass).orElseGet(() -> produceAttribute(set, entity, attributeClass));
	}

	public void invalidate(Object entity) {
		attributes.remove(entity);
		entities.remove(entity);
	}

	public void invalidateAllAttributes(Object entity) {
		attributes.remove(entity);
	}

	public void invalidateAttributes(Object entity, Class<?> attributeClass) {
		if (!attributes.containsKey(entity)) return;
		attributes.get(entity).removeIf(attribute -> attributeClass.isAssignableFrom(attribute.getClass()));
	}

	private record EntityProviderInfo(Class<?> entityClass, EntityProvider<?> provider) {
	}

	private record AttributeProviderInfo(Class<?> entityClass, Class<?> attributeClass,
	                                     AttributeProvider<?, ?> provider) {
	}
}