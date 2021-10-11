package io.github.ashr123.weather.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class Coordinates
{
	@JsonProperty
	private double lon;
	@JsonProperty
	private double lat;

	private Coordinates()
	{
	}

	public Coordinates(
		double lon,
		double lat
	)
	{
		this.lon = lon;
		this.lat = lat;
	}

	public double lon()
	{
		return lon;
	}

	public double lat()
	{
		return lat;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Coordinates) obj;
		return Double.doubleToLongBits(this.lon) == Double.doubleToLongBits(that.lon) &&
			Double.doubleToLongBits(this.lat) == Double.doubleToLongBits(that.lat);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(lon, lat);
	}

	@Override
	public String toString()
	{
		return "Coordinates[" +
			"lon=" + lon + ", " +
			"lat=" + lat + ']';
	}

}
