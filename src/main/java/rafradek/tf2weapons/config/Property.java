package rafradek.tf2weapons.config;

import java.util.Arrays;

public class Property {
	public enum Type {
		STRING, INTEGER, BOOLEAN, DOUBLE
	}

	private final String name;
	private Type type;
	private String[] values;
	private String[] validValues;
	private String comment = "";
	private final String[] defaults;

	public Property(String name, String value, Type type) {
		this(name, new String[] { value }, type);
	}

	public Property(String name, String[] values, Type type) {
		this.name = name;
		this.values = values;
		this.defaults = Arrays.copyOf(values, values.length);
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public Type getType() {
		return this.type;
	}

	public Property setValidValues(String[] validValues) {
		this.validValues = validValues;
		return this;
	}

	public String[] getValidValues() {
		return this.validValues;
	}

	public String getString() {
		return this.values.length == 0 ? "" : this.values[0];
	}

	public String[] getStringList() {
		return this.values;
	}

	public int getInt() {
		return Integer.parseInt(getString());
	}

	public int[] getIntList() {
		int[] out = new int[this.values.length];
		for (int i = 0; i < this.values.length; i++) {
			out[i] = Integer.parseInt(this.values[i]);
		}
		return out;
	}

	public boolean getBoolean() {
		return Boolean.parseBoolean(getString());
	}

	public double getDouble() {
		return Double.parseDouble(getString());
	}

	public boolean isList() {
		return this.values.length > 1;
	}

	public Property set(String[] values) {
		this.values = values;
		return this;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public void setValue(String value) {
		this.values = new String[] { value };
	}

	public void setToDefault() {
		this.values = Arrays.copyOf(this.defaults, this.defaults.length);
	}

	public Property setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public String getComment() {
		return this.comment;
	}

	public Property setRequiresMcRestart(boolean value) {
		return this;
	}

	public Property setRequiresWorldRestart(boolean value) {
		return this;
	}
}
