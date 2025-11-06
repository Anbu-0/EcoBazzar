package com.ecobazaar.ecobazaar.dto;

public class AdminOverview {
	
	private long totalUsers;
    private long totalProducts;
    private long totalTransactions;
    private long totalFeedbacks;
    
    public AdminOverview() {}

    public AdminOverview(long totalUsers, long totalProducts, long totalTransactions, long totalFeedbacks) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalTransactions = totalTransactions;
        this.totalFeedbacks = totalFeedbacks;
    }

	public long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(long totalUsers) {
		this.totalUsers = totalUsers;
	}

	public long getTotalProducts() {
		return totalProducts;
	}

	public void setTotalProducts(long totalProducts) {
		this.totalProducts = totalProducts;
	}

	public long getTotalTransactions() {
		return totalTransactions;
	}

	public void setTotalTransactions(long totalTransactions) {
		this.totalTransactions = totalTransactions;
	}

	public long getTotalFeedbacks() {
		return totalFeedbacks;
	}

	public void setTotalFeedbacks(long totalFeedbacks) {
		this.totalFeedbacks = totalFeedbacks;
	}

}
