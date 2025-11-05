package com.ecobazaar.ecobazaar.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecobazaar.ecobazaar.dto.FeedbackRequest;
import com.ecobazaar.ecobazaar.model.Feedback;
import com.ecobazaar.ecobazaar.repository.FeedbackRepository;
import com.ecobazaar.ecobazaar.repository.ProductRepository;

@Service
public class FeedbackService {
	
	private final FeedbackRepository feedbackRepository;
	private final ProductRepository productRepository;
	
	public FeedbackService(FeedbackRepository feedbackRepository, ProductRepository productRepository) {
		this.feedbackRepository = feedbackRepository;
		this.productRepository = productRepository;
	}
	
	public Feedback addFeedback(Long productId, Long consumerId, FeedbackRequest feedback) {
		
		// Check product exists
		productRepository.findById(productId)
		.orElseThrow(()-> new RuntimeException("Product is not available"));
		
		// Rating validation
		if(feedback.getRating()<1||feedback.getRating()>5) {
			throw new RuntimeException("Rating must be 1 to 5");
		}
		
		// Restrict one feedback per consumer per product
		if(feedbackRepository.findByProductIdAndConsumerId(productId, consumerId).isPresent()) {
			throw new RuntimeException("You have already submitted the feedback for this product");
		}
		
		Feedback f = new Feedback();
		
		f.setProductId(productId);
		f.setConsumerId(consumerId);
		f.setRating(feedback.getRating());
		f.setComment(feedback.getComment());
		
		return feedbackRepository.save(f);
			
	}

	public List<Feedback> getFeedbackForProduct(Long productId){
		return feedbackRepository.findByProductId(productId);
	}
}
