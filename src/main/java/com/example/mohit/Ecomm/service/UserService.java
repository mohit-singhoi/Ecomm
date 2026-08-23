package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	public User registerUser(User user) {
		try {
			User newUser = userRepository.save(user);
			System.out.println("user Successfully Register");
			return newUser;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	

	public User loginUser(String email, String password) {
		
		// Check if user is valid  or not
		User user = userRepository.findByEmail(email);
		if(user != null && user.getPassword().equals(password))
		{
			return user;
		}
		
		return null; // invalid credentials
	}


	
	public List<User> getAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}

}
