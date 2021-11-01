package at.kaindorf.pojo;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A POJOs Memory object to store query results from our main measurement {@code black_ice}.
 * 
 * @author Nico Baumann
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "black_ice")
public class MemBlackIce{
	
	@Column(name = "time")
	private Instant time;
	
	@Column(name = "temperature")
	private Float temperature;
	
	@Column(name = "humidity")
	private Float humidity;
	
	@Column(name = "air_pressure")
	private Float air_pressure;
	
	@Column(name = "light_level")
	private Float light_level;
	
}
