package io.doosti.hamid.productapiannotation.repository;

import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import io.doosti.hamid.productapiannotation.model.Product;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product,String>{

	Flux<Product> findByNameOrderByPrice(Publisher<String> name);
}
