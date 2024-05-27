package main;

import java.util.Objects;

public class ChestKey {
	private final String key;

	ChestKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChestKey chestKey = (ChestKey) o;
		return Objects.equals(key, chestKey.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public String toString() {
		return key;
	}
}