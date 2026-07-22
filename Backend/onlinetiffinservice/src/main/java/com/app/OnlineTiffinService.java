package com.app;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineTiffinService {

	public static void main(String[] args) {
		SpringApplication.run(OnlineTiffinService.class, args);
	}

	@Bean // equvalent to <bean id ..../> in xml file
	public ModelMapper configureMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
		.setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;//method rets bean instance to SC
	}
}
