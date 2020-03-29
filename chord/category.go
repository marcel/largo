package chord

type Category uint8

const (
	// Secundal chords can be decomposed into a series of (major or minor) seconds.
	// For example, the chord C-D-E♭ is a series of seconds, containing a major second (C-D) and a minor second (D-E♭)
	Secundal Category = iota + 2 // 2nd's: major 2nd, minor 2nd
	// Tertian chords can be decomposed into a series of (major or minor) thirds.
	// For example, the C major triad (C-E-G) is defined by a sequence of two intervals, the first (C-E) being
	// a major third and the second (E-G) being a minor third. Most common chords are tertian.
	Tertian // 3rd's: major 3rd, minor 3rd
	// Quartal chords can be decomposed into a series of (perfect or augmented) fourths.
	// Quartal harmony normally works with a combination of perfect and augmented fourths.
	// Diminished fourths are enharmonically equivalent to major thirds, so they are uncommon.
	// For example, the chord C-F-B is a series of fourths, containing a perfect fourth (C-F) and
	// an augmented fourth/tritone (F-B).
	Quartal // 4th's: perfect 4th, augmented 4th
	Quintal // 5th's
)
