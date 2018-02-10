package qa.eclipse.plugin.bundles.checkstyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SplitUtils {

	private SplitUtils() {
		// utility class
	}

	/**
	 * This is builder operation.
	 * 
	 * @param text
	 *            to be split.
	 * @return the next builder step to define how often the text should be split.
	 */
	public static SplitResult split(String text) {
		return new SplitResultImpl(text);
	}

	public static interface SplitResult {

		public QuantityResult once();

		public QuantityResult always();
	}

	static class SplitResultImpl implements SplitResult, QuantityResult, SeparatorResult {

		private final String text;
		private SplitQuantity splitQuantity;
		private char separator;

		public SplitResultImpl(String text) {
			this.text = text;
		}

		@Override
		public QuantityResult once() {
			// return new QuantityResultImpl(text, SplitQuantity.ONCE);
			splitQuantity = SplitQuantity.ONCE;
			return this;
		}

		@Override
		public QuantityResult always() {
			// return new QuantityResultImpl(text, SplitQuantity.ALWAYS);
			splitQuantity = SplitQuantity.ALWAYS;
			return this;
		}

		@Override
		public SeparatorResult at(char separator) {
			this.separator = separator;
			return this;
		}

		@Override
		public List<String> fromTheRight() {
			return splitFromTheRight(text, splitQuantity, separator);
		}

		@Override
		public List<String> fromTheLeft() {
			return splitFromTheLeft(text, splitQuantity, separator);
		}

	}

	public static interface QuantityResult {

		public SeparatorResult at(char separator);
	}

	public static enum SplitQuantity {
		ONCE, ALWAYS
	}

	static class QuantityResultImpl implements QuantityResult {

		private String text;
		private SplitQuantity splitQuantity;

		public QuantityResultImpl(String text, SplitQuantity splitQuantity) {
			this.text = text;
			this.splitQuantity = splitQuantity;
		}

		@Override
		public SeparatorResult at(char separator) {
			return new SeparatorResultImpl(text, splitQuantity, separator);
		}
	}

	public static interface SeparatorResult {
		/**
		 * This is a terminal operation.
		 * 
		 * @return the text parts separated at the separator position(s); or a newly
		 *         allocated empty list otherwise. The text parts are sorted according
		 *         to the order of occurrence in passed the text (from left to right).
		 */
		public List<String> fromTheRight();

		/**
		 * This is a terminal operation.
		 * 
		 * @return the text parts separated at the separator position(s); or a newly
		 *         allocated empty list otherwise. The text parts are sorted according
		 *         to the order of occurrence in the passed text (from left to right).
		 */
		public List<String> fromTheLeft();
	}

	static class SeparatorResultImpl implements SeparatorResult {

		private String text;
		private SplitQuantity splitQuantity;
		private char separator;

		public SeparatorResultImpl(String text, SplitQuantity splitQuantity, char separator) {
			this.text = text;
			this.splitQuantity = splitQuantity;
			this.separator = separator;
		}

		@Override
		public List<String> fromTheRight() {
			return splitFromTheRight(text, splitQuantity, separator);
		}

		@Override
		public List<String> fromTheLeft() {
			return splitFromTheLeft(text, splitQuantity, separator);
		}

	}

	/**
	 * @param text
	 *            to be split.
	 * @param splitQuantity
	 *            how often the text should be split.
	 * @param separator
	 * @return the text parts separated at the separator position(s); or a newly
	 *         allocated empty list otherwise. The text parts are sorted according
	 *         to the order of occurrence in passed the text (from left to right).
	 */
	static List<String> splitFromTheRight(String text, SplitQuantity splitQuantity, char separator) {
		List<String> textParts = new ArrayList<>();
		int endIndex = text.length();

		for (int i = text.length() - 1; i >= 0; i--) {
			char character = text.charAt(i);
			if (character == separator) {
				int beginIndex = i + 1; // +1: don't include the separator

				String textPart = text.substring(beginIndex, endIndex);
				textParts.add(textPart);

				endIndex = i;

				if (splitQuantity == SplitQuantity.ONCE) {
					break;
				}
			}
		}

		String textPart = text.substring(0, endIndex);
		textParts.add(textPart);

		// According to the javadoc, the text parts should be sorted according
		// to the order of occurrence in passed the text (from left to right).
		// Hence, we need to reverse the order.
		Collections.reverse(textParts);

		return textParts;
	}

	/**
	 * @param text
	 *            to be split.
	 * @param splitQuantity
	 *            how often the text should be split.
	 * @param separator
	 * @return the text parts separated at the separator position(s); or a newly
	 *         allocated empty list otherwise. The text parts are sorted according
	 *         to the order of occurrence in passed the text (from left to right).
	 */
	static List<String> splitFromTheLeft(String text, SplitQuantity splitQuantity, char separator) {
		List<String> textParts = new ArrayList<>();
		int beginIndex = 0;

		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);
			if (character == separator) {
				int endIndex = i;

				String textPart = text.substring(beginIndex, endIndex);
				textParts.add(textPart);

				beginIndex = i + 1; // +1: don't include the separator

				if (splitQuantity == SplitQuantity.ONCE) {
					break;
				}
			}
		}

		String textPart = text.substring(beginIndex, text.length());
		textParts.add(textPart);

		return textParts;
	}
}
