package org.opennms.netmgt.syslogd;

/**
 * An individual stage of the token parser. A parser is composed of
 * a sequence of {@link ParserStage} objects.
 */
public interface ParserStage {

	public static enum AcceptResult {
		/**
		 * Continue the parsing process.
		 */
		CONTINUE,
		/**
		 * Complete the parsing stage and consider the current
		 * character as already consumed.
		 */
		COMPLETE_AFTER_CONSUMING,
		/**
		 * Complete the parsing stage and reset the position of the
		 * buffer so that the next stage can consume the current
		 * character.
		 */
		COMPLETE_WITHOUT_CONSUMING,
		/**
		 * Cancel the parsing process due to a failure to parse
		 * based on the current rules for this stage.
		 */
		CANCEL
	}

	/**
	 * Mark the stage as optional.
	 * 
	 * @param optional
	 */
	void setOptional(boolean optional);

	/**
	 * Mark the stage as terminal, ie. it handles a buffer
	 * underflow as successful completion instead of failure.
	 * 
	 * @param terminal
	 */
	void setTerminal(boolean terminal);

	/**
	 * Process the state for this stage and return it so
	 * that the next stage can continue processing.
	 */
	ParserState apply(ParserState state);
}