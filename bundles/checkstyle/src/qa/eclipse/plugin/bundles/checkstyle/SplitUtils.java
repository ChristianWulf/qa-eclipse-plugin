/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.bundles.checkstyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Wulf
 *
 */
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
	public static ISplitResult split(final String text) {
		return new SplitResultImpl(text);
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
	static List<String> splitFromTheRight(final String text, final SplitQuantity splitQuantity, final char separator) {
		final List<String> textParts = new ArrayList<>();
		int endIndex = text.length();

		for (int i = text.length() - 1; i >= 0; i--) {
			final char character = text.charAt(i);
			if (character == separator) {
				final int beginIndex = i + 1; // +1: don't include the separator

				final String textPart = text.substring(beginIndex, endIndex);
				textParts.add(textPart);

				endIndex = i;

				if (splitQuantity == SplitQuantity.ONCE) {
					break;
				}
			}
		}

		final String textPart = text.substring(0, endIndex);
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
	static List<String> splitFromTheLeft(final String text, final SplitQuantity splitQuantity, final char separator) {
		final List<String> textParts = new ArrayList<>();
		int beginIndex = 0;

		for (int i = 0; i < text.length(); i++) {
			final char character = text.charAt(i);
			if (character == separator) {
				final int endIndex = i;

				final String textPart = text.substring(beginIndex, endIndex);
				textParts.add(textPart);

				beginIndex = i + 1; // +1: don't include the separator

				if (splitQuantity == SplitQuantity.ONCE) {
					break;
				}
			}
		}

		final String textPart = text.substring(beginIndex, text.length());
		textParts.add(textPart);

		return textParts;
	}

	public static interface ISplitResult {

		public IQuantityResult once();

		public IQuantityResult always();
	}

	static class SplitResultImpl implements ISplitResult, IQuantityResult, ISeparatorResult {

		private final String text;
		private SplitQuantity splitQuantity;
		private char separator;

		public SplitResultImpl(final String text) {
			this.text = text;
		}

		@Override
		public IQuantityResult once() {
			// return new QuantityResultImpl(text, SplitQuantity.ONCE);
			this.splitQuantity = SplitQuantity.ONCE;
			return this;
		}

		@Override
		public IQuantityResult always() {
			// return new QuantityResultImpl(text, SplitQuantity.ALWAYS);
			this.splitQuantity = SplitQuantity.ALWAYS;
			return this;
		}

		@Override
		public ISeparatorResult at(final char separator) { // NOCS works like a setter, NOPMD internal DSL
			this.separator = separator;
			return this;
		}

		@Override
		public List<String> fromTheRight() {
			return SplitUtils.splitFromTheRight(this.text, this.splitQuantity, this.separator);
		}

		@Override
		public List<String> fromTheLeft() {
			return SplitUtils.splitFromTheLeft(this.text, this.splitQuantity, this.separator);
		}

	}

	/**
	 *
	 * @author Christian Wulf
	 *
	 */
	public static interface IQuantityResult {

		public ISeparatorResult at(char separator); // NOPMD internal DSL
	}

	/**
	 *
	 * @author Christian Wulf
	 *
	 */
	public static enum SplitQuantity {
		ONCE, ALWAYS
	}

	static class QuantityResultImpl implements IQuantityResult {

		private final String text;
		private final SplitQuantity splitQuantity;

		public QuantityResultImpl(final String text, final SplitQuantity splitQuantity) {
			this.text = text;
			this.splitQuantity = splitQuantity;
		}

		@Override
		public ISeparatorResult at(final char separator) { // NOPMD internal DSL
			return new SeparatorResultImpl(this.text, this.splitQuantity, separator);
		}
	}

	public static interface ISeparatorResult {
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

	static class SeparatorResultImpl implements ISeparatorResult {

		private final String text;
		private final SplitQuantity splitQuantity;
		private final char separator;

		public SeparatorResultImpl(final String text, final SplitQuantity splitQuantity, final char separator) {
			this.text = text;
			this.splitQuantity = splitQuantity;
			this.separator = separator;
		}

		@Override
		public List<String> fromTheRight() {
			return SplitUtils.splitFromTheRight(this.text, this.splitQuantity, this.separator);
		}

		@Override
		public List<String> fromTheLeft() {
			return SplitUtils.splitFromTheLeft(this.text, this.splitQuantity, this.separator);
		}

	}

}
