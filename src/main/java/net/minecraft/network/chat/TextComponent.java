package net.minecraft.network.chat;

public class TextComponent {
	private final String text;

	public TextComponent(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
