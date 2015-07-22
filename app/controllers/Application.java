package controllers;

import java.util.List;
import java.util.Properties;

import org.h2.engine.Mode;

import play.db.*;
import models.Rating;
import play.Logger;
import play.data.Form;
import play.mvc.*;
import play.twirl.api.Content;
import util.KafkaHelper;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class Application extends Controller {

	final static Form<Rating> rating = Form.form(Rating.class);

	public Result submit() {
		//Bind Directly from the request content
		Rating r = Form.form(Rating.class).bindFromRequest().get();
//		//Get Values from Webpage
//		Integer user = r.getUser();
//		Integer product =r.getProduct();
//		Double rating = r.getRating();
		Logger.info("Rating hit received");
		Logger.info("dispatching to kafka: "+r.toString());
		
		KafkaHelper.kafka("product_rating", r);
		//kafka(user,product,rating); //alternative
		return ok("Values Successfully sent to Kafka Topic");
	}
	
	public static void kafka(Integer user, Integer product, Double rating) {
		Properties props = new Properties();
		props.put("metadata.broker.list","10.153.7.113:9094,10.153.7.113:9096");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("request.required.acks", "1");
		ProducerConfig config = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(config);
		KeyedMessage<String, String> data = new KeyedMessage<String, String>(
				"product_rating", user + " " + product + " "+ rating);
		producer.send(data);
		producer.close();
	}
	
}
