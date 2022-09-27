package com.parvin.midi_analysis.chromaticism;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a basic note name corresponding to MIDI note numbers 0-11 (C-B).<br>
 * Excludes double sharps and double flats.<br>
 * Does not consider octaves of notes.
 * @author dparvin
 *
 */
public enum BasicNoteName {
	B_SHARP(0), C(0), C_SHARP(1), D_FLAT(1), D(2), D_SHARP(3), E_FLAT(3), E(4), F_FLAT(4), E_SHARP(5), F(5), F_SHARP(6),
	G_FLAT(6), G(7), G_SHARP(8), A_FLAT(8), A(9), A_SHARP(10), B_FLAT(10), B(11), C_FLAT(11);
	
	private static final Map<String, BasicNoteName> stringToEnum = Stream.of(values()).collect(
			Collectors.toMap(Object::toString, e -> e));
	
	public static Optional<BasicNoteName> fromString(String noteName) {
		return Optional.ofNullable(stringToEnum.get(noteName));
	}
	
	public static Optional<BasicNoteName> fromMidiNoteNumber(int midiNoteNumber) {
		return Optional.empty(); // TODO Make flat and sharp variants?
	}
	
	private final int midiNoteNumber;
	
	BasicNoteName(int midiNoteNumber) {
		this.midiNoteNumber = midiNoteNumber;
	}

	/**
	 * Get the MIDI note number (0-11) of this note.
	 * @return MIDI note number.
	 */
	public int getMidiNoteNumber() {
		return midiNoteNumber;
	}
	
	@Override
	public String toString() {
		String originalString = super.toString();
		if (originalString.length() == 1) {
			return originalString;
		}
		
		String noteLetter = originalString.substring(0, 1);
		if (originalString.toLowerCase().endsWith("_sharp")) {
			return noteLetter + "#";
		} else {
			return noteLetter + "b";
		}
	}
}
