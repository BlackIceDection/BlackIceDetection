package at.kaindorf.beans;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A Memory object to store query results from the main measurement {@code black_ice}.
 * 
 * @author Nico Baumann
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "black_ice") // Defines measurement to use. Since we only have one we will just hardcode it.
public class MemBlackIce{
	
	@Column(name = "time")
	private Instant time; // InfluxDB timestamp of entry.
	
	@Column(name = "temperature")
	private Float temperature;
	
	@Column(name = "humidity")
	private Float humidity;
	
	@Column(name = "air_pressure")
	private Float air_pressure;
	
	@Column(name = "light_level")
	private Float light_level;
	
	@Column(name = "buffer")
	private Long buffer; // Buffer to read/write data with.
	
}
