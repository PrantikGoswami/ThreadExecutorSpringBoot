package com.spring.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.model.Customer;
import com.spring.service.CustomerService;

@RestController
@RequestMapping(value = "customer")
public class CustomerController {
	
	@Autowired
	private CustomerService service;
	
	Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);
	
	@PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> saveCustomers(@RequestParam(value = "files") MultipartFile[] files) throws Exception{
		LOGGER.info("CustomerController.saveCustomers by thread {}", Thread.currentThread().getName());
		for (MultipartFile file:files) {
			service.saveAll(file);
		}
		return ResponseEntity.status(HttpStatus.CREATED).build();
		
	}
	
	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody CompletableFuture<ResponseEntity<List<Customer>>> getAllCustomers() throws InterruptedException{
		LOGGER.info("CustomerController.getAllCustomers by thread {}", Thread.currentThread().getName());
		return service.findAllCustomers().thenApply(ResponseEntity::ok).exceptionally(handleGetCarFailure);
	}
	
	@GetMapping(value = "/justGetAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Customer> getAllCustomersNonAsync() throws InterruptedException{
		LOGGER.info("CustomerController.getAllCustomersNonAsync by thread {}", Thread.currentThread().getName());
		System.out.println(service.findAllCustomersNonAsync().get(0));
		return service.findAllCustomersNonAsync();
	}
	
	@GetMapping(value = "/checkAllThread", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getCustomers(){
		CompletableFuture<List<Customer>> customer_list1 =  service.findAllCustomers();
		CompletableFuture<List<Customer>> customer_list2 =  service.findAllCustomers();
		CompletableFuture<List<Customer>> customer_list3 =  service.findAllCustomers();
		
		CompletableFuture.allOf(customer_list1, customer_list2, customer_list3).join();
		
		return ResponseEntity.status(HttpStatus.OK).build();
		
	}
	
	private Function<Throwable, ResponseEntity<List<Customer>>> handleGetCarFailure = throwable -> {
		LOGGER.error("Failed to read records: {}", throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    };

}
