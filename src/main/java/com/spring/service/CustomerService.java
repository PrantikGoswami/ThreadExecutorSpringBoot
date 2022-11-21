package com.spring.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.model.Customer;
import com.spring.repository.CustomerRepository;



@Service
public class CustomerService {
	
	Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
	
	@Autowired
	private CustomerRepository repository;
	
	@Async
	public CompletableFuture<List<Customer>> findAllCustomers(){
		LOGGER.info("CustomerService.findAllCustomers by thread {}", Thread.currentThread().getName());
		List<Customer> customers = (List<Customer>) repository.findAll();
		return CompletableFuture.completedFuture(customers);
	}
	
	public List<Customer> findAllCustomersNonAsync(){
		LOGGER.info("CustomerService.findAllCustomersNonAsync by thread {}", Thread.currentThread().getName());
		List<Customer> customers = (List<Customer>) repository.findAll();
		return customers;
	}
	
	@Async
	public CompletableFuture<List<Customer>> saveAll(MultipartFile file) throws Exception{
		long start = System.currentTimeMillis();
		List<Customer> customers = generateCustomersFromCSV(file);
		LOGGER.info("Saving customers of Size {} - with Thread {}", customers.size(), Thread.currentThread().getName());
		customers = (List<Customer>) repository.saveAll(customers);
		long end = System.currentTimeMillis();
		LOGGER.info("Total time to save {}", end-start);
		return CompletableFuture.completedFuture(customers);
	}
	
	private List<Customer> generateCustomersFromCSV(final MultipartFile file) throws Exception{
		final List<Customer> customers = new ArrayList<>();
		
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
			String line;
			while((line = reader.readLine()) != null) {
				final String[] data = line.split(",");
				final Customer customer = new Customer(null, data[0], data[1], data[2], data[3]);
				customers.add(customer);
			}
			return customers;
		} catch (final IOException e) {
			LOGGER.info("Failed to parse CSV {}", e);
			throw new Exception("Failed to parse CSV {}", e);
		}
		
	}
	
	

}
