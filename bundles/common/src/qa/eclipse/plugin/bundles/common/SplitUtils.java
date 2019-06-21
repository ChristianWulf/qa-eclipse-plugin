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
package qa.eclipse.plugin.bundles.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Internal DSL to split strings in a more convenient way.
 *
 * Sequence split(text),once|always,at(separator),fromTheEnd|fromTheBeginning
 *
 * * split = sets the text to split
 * * once = split only at the first occurrence (results on success in 2 strings)
 * * always = causes to continue splitting (results on success in multiple strings)
 * * at = set the separator character
 * * fromTheEnd = starts scanning of the text from end of the text
 * * fromTheBeginning = starts scanning of the text from beginning of the text
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
	private static List<String> splitFromTheEnd(final String text, final SplitQuantity splitQuantity, final char separator) {
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
	private static List<String> splitFromTheBeginning(final String text, final SplitQuantity splitQuantity, final char separator) {
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

	/**
	 * Interface expressing the number of splits.
	 *
	 * @author Christian Wulf
	 *
	 */
	public interface ISplitResult {

		/**
		 * Only split once.
		 *
		 * @return returns a quantity result object
		 */
		IQuantityResult once();

		/**
		 * Only split at every occurrence of the separator.
		 *
		 * @return returns a quantity result object
		 */
		IQuantityResult always();
	}

	/**
	 *
	 * @author Christian Wulf
	 *
	 */
	/* default */ static class SplitResultImpl implements ISplitResult, IQuantityResult, ISeparatorResult {

		private final String text;
		private SplitQuantity splitQuantity;
		private char separator;

		public SplitResultImpl(final String text) {
			this.text = text;
		}

		@Override
		public IQuantityResult once() {
			this.splitQuantity = SplitQuantity.ONCE;
			return this;
		}

		@Override
		public IQuantityResult always() {
			this.splitQuantity = SplitQuantity.ALWAYS;
			return this;
		}

		@Override
		public ISeparatorResult at(final char separator) { // NOCS works like a setter, NOPMD internal DSL
			this.separator = separator;
			return this;
		}

		@Override
		public List<String> fromTheEnd() {
			return SplitUtils.splitFromTheEnd(this.text, this.splitQuantity, this.separator); // NOPMD
			// Ignore warning on calling parent class static methods
		}

		@Override
		public List<String> fromTheBeginning() {
			return SplitUtils.splitFromTheBeginning(this.text, this.splitQuantity, this.separator); // NOPMD
			// Ignore warning on calling parent class static methods
		}

	}

	/**
	 * Defines the separator keyword.
	 *
	 * @author Christian Wulf
	 *
	 */
	public interface IQuantityResult {

		/**
		 * Set the separator.
		 *
		 * @param separator
		 *            separator character.
		 *
		 * @return result after separation
		 */
		ISeparatorResult at(char separator); // NOPMD internal DSL
	}

	/**
	 * How often shall we split the string.
	 *
	 * @author Christian Wulf
	 *
	 */
	public enum SplitQuantity {
		ONCE, ALWAYS
	}

	/**
	 * Class to implement the separator.
	 *
	 * @author Christian Wulf
	 *
	 */
	/* default */ static class QuantityResultImpl implements IQuantityResult {

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

	/**
	 * Traversing the result.
	 *
	 * @author Reiner Jung
	 *
	 */
	public interface ISeparatorResult {
		/**
		 * This is a terminal operation.
		 *
		 * @return the text parts separated at the separator position(s); or a newly
		 *         allocated empty list otherwise. The text parts are sorted according
		 *         to the order of occurrence in passed the text (from left to right).
		 */
		List<String> fromTheEnd();

		/**
		 * This is a terminal operation.
		 *
		 * @return the text parts separated at the separator position(s); or a newly
		 *         allocated empty list otherwise. The text parts are sorted according
		 *         to the order of occurrence in the passed text (from left to right).
		 */
		List<String> fromTheBeginning();
	}

	/**
	 * Implementation of the string parsing direction.
	 *
	 * @author Christian Wulf
	 *
	 */
	/* default */static class SeparatorResultImpl implements ISeparatorResult {

		private final String text;
		private final SplitQuantity splitQuantity;
		private final char separator;

		public SeparatorResultImpl(final String text, final SplitQuantity splitQuantity, final char separator) {
			this.text = text;
			this.splitQuantity = splitQuantity;
			this.separator = separator;
		}

		@Override
		public List<String> fromTheEnd() {
			return SplitUtils.splitFromTheEnd(this.text, this.splitQuantity, this.separator); // NOPMD
			// Ignore warning on calling parent class static methods
		}

		@Override
		public List<String> fromTheBeginning() {
			return SplitUtils.splitFromTheBeginning(this.text, this.splitQuantity, this.separator); // NOPMD
			// Ignore warning on calling parent class static methods
		}

	}

}
