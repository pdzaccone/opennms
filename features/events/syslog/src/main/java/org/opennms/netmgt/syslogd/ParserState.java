package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * The state of the entire parse operation. This state
 * should include all of the finished tokens generated
 * by {@link ParserStage} operations.
 */
public class ParserState {
	private final ByteBuffer buffer;

	// TODO: Replace with a strategy
	public final EventBuilder builder;

	public ParserState(ByteBuffer input) {
		this(input, new EventBuilder("uei.opennms.org/test", ParserState.class.getSimpleName()));
	}

	public ParserState(ByteBuffer input, EventBuilder builder) {
		this.buffer = input;
		this.builder = builder;
	}

	public ByteBuffer getBuffer() {
		// TODO: See if this slows anything down
		return buffer.asReadOnlyBuffer();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("builder", builder)
			.toString();
	}
}