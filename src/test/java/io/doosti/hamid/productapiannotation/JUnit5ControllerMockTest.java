package io.doosti.hamid.productapiannotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.doosti.hamid.productapiannotation.controller.ProductController;
import io.doosti.hamid.productapiannotation.model.Product;
import io.doosti.hamid.productapiannotation.model.ProductEvent;
import io.doosti.hamid.productapiannotation.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


@ExtendWith(SpringExtension.class)
public class JUnit5ControllerMockTest {

	
	private WebTestClient client;
	private List<Product> expectedList;
	
	@MockBean
	private ProductRepository repository;
	
	@BeforeEach
	void beforeEach() {
		this.client = 
				WebTestClient.bindToController(new ProductController(repository))
				.configureClient()
				.baseUrl("/products")
				.build();
		
		this.expectedList = repository.findAll().collectList().block();
	}
	
	@Test
	void testGetAllProducts() {
		
		when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList)); 
		
		client.get()
			.uri("/")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBodyList(Product.class)
			.isEqualTo(expectedList);
	}
	
	@Test
	void testProductInvalidIdNotFound() {
		
		when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
		
		client.get()
			.uri("/aaa")
			.exchange()
			.expectStatus()
			.isNotFound();
	}
	
	
	@Test
	void testProductIdFound() {
		when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
		Product expProduct = expectedList.get(0);
		
		client.get()
			.uri("/{id}",expProduct.getId())
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(Product.class)
			.isEqualTo(expProduct);
		
	}
	
	@Test
	void testProductEvents() {
		when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
			ProductEvent expectedEvent = new ProductEvent(0L,"Product event");
			
			FluxExchangeResult<ProductEvent> result = 
					client.get().uri("/events")
						.accept(MediaType.TEXT_EVENT_STREAM)
						.exchange()
						.expectStatus()
						.isOk()
						.returnResult(ProductEvent.class);
			
			StepVerifier.create(result.getResponseBody())
				.expectNext(expectedEvent)
				.expectNextCount(2)
				.consumeNextWith(event -> assertEquals(Long.valueOf(3), event.getEventId()))
				.thenCancel()
				.verify();
	}

}
