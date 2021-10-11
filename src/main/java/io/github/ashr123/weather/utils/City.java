package io.github.ashr123.weather.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class City
{
	@JsonProperty
	private int id;
	@JsonProperty
	private String name;
	@JsonProperty
	private String state;
	@JsonProperty
	private String country;
	@JsonProperty
	private Coordinates coord;

	private City()
	{
	}

	public City(
		int id,
		String name,
		String state,
		String country,
		Coordinates coord
	)
	{
		this.id = id;
		this.name = name;
		this.state = state;
		this.country = country;
		this.coord = coord;
	}

	public int id()
	{
		return id;
	}

	public String name()
	{
		return name;
	}

	public String state()
	{
		return state;
	}

	public String country()
	{
		return country;
	}

	public Coordinates coord()
	{
		return coord;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (City) obj;
		return this.id == that.id &&
			Objects.equals(this.name, that.name) &&
			Objects.equals(this.state, that.state) &&
			Objects.equals(this.country, that.country) &&
			Objects.equals(this.coord, that.coord);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, state, country, coord);
	}

	@Override
	public String toString()
	{
		return "City[" +
			"id=" + id + ", " +
			"name=" + name + ", " +
			"state=" + state + ", " +
			"country=" + country + ", " +
			"coord=" + coord + ']';
	}

}
