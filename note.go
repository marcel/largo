package largo

import (
	"fmt"

	"github.com/marcel/largo/chord"
)

const (
	ConcertPitch       = 440.0
	SemiToneCents      = 1200
	SemiTonesPerOctave = 12
)

type SemiTone uint8

const (
	// Intervals of extended meantone temperament
	// Name                 Interval          Pitch (from C)   Roman No.
	Unison            = SemiTone(0)       // C             I
	DiminishedSecond  = Unison            // D♭♭
	ChromaticSemiTone = SemiTone(1)       // C♯
	MinorSecond       = ChromaticSemiTone // D♭
	HalfTone          = MinorSecond
	WholeTone         = SemiTone(2)      // D             II
	DiminishedThird   = WholeTone        // E♭♭
	AugmentedSecond   = SemiTone(3)      // D♯
	MinorThird        = AugmentedSecond  // E♭
	MajorThird        = SemiTone(4)      // E             III
	DiminishedFourth  = MajorThird       // F♭
	AugmentedThird    = SemiTone(5)      // E♯
	PerfectFourth     = AugmentedThird   // F             IV
	AugmentedFourth   = SemiTone(6)      // F♯
	DiminishedFifth   = AugmentedFourth  // G♭
	PerfectFifth      = SemiTone(7)      // G             V
	DiminishedSixth   = PerfectFifth     // A♭♭
	AugmentedFifth    = SemiTone(8)      // G♯
	MinorSixth        = AugmentedFifth   // A♭
	MajorSixth        = SemiTone(9)      // A             VI
	DiminishedSeventh = MajorSixth       // B♭♭
	AugmentedSixth    = SemiTone(10)     // A♯
	MinorSeventh      = AugmentedSixth   // B♭
	MajorSeventh      = SemiTone(11)     // B             VII
	DiminishedOctave  = MajorSeventh     // C♭
	AugmentedSeventh  = SemiTone(12)     // B♯
	OctaveTone        = AugmentedSeventh // C             VIII
)

type PitchInterval interface {
	FromNote(Note) Notes
}

type StackedInterval []SemiTone

func (s StackedInterval) FromNote(note Note) Notes {
	// intervals.foldLeft(Seq(note)) { case (notes, interval) =>
	//   notes :+ (notes.last + interval)
	// }
	panic("not implemented")
}

type AbsoluteInterval []SemiTone

func (a AbsoluteInterval) FromNote(note Note) Notes {
	// intervals.foldLeft(Seq(note)) { case (notes, interval) =>
	//   notes :+ (note + interval)
	// }
	panic("not implemented")
}

// TODO implement sort.Interface
type Note struct {
	Pitch  Pitch
	Octave Octave
}

func NewNote(pitch Pitch, octave Octave) Note {
	return Note{
		Pitch:  pitch,
		Octave: octave,
	}
}

func (n Note) Frequency() {
	// val intervalFromConcertPitch = Note.ConcertPitch.interval(this)
	// Pitch.Frequency.ConcertPitch * Math.pow(Octave.TwelfthRootOf2, intervalFromConcertPitch.interval)
	panic("not implemented")
}

func (n Note) Index() int {
	// CircularPitchClass.indexOf(pitch) + (octave.value * SemiTone.perOctave)
	panic("not implemented")
}

// override func (n Note) equals(obj: Any) = {
//   obj match {
//     case o: Note => o.index.equals(index)
//     case _ => false
//   }
// }

func (n Note) Interval(otherNote Note) SemiTone {
	// val octaveInterval = octave.interval(otherNote.octave) * SemiTone.perOctave
	// SemiTone(pitch.interval(otherNote.pitch).interval + octaveInterval)
	panic("not implemented")
}

// func (n Note) compare(otherNote: Note) = {
//   0.compare(interval(otherNote).interval)
// }

func (n Note) Raise(interval SemiTone) Note {
	return PitchSpace[n.Index()+int(interval)]
}

// TODO this was called ++ in Scala, so I'm  not sure if the name I chose is
// correct
func (n Note) RaiseAccordingTo(intervals PitchInterval) Notes {
	return intervals.FromNote(n)
}

func (n Note) MajorScale() Notes {
	return n.RaiseAccordingTo(MajorScale)
}

func (n Note) MinorScale() Notes {
	return n.RaiseAccordingTo(MinorScale)
}

func (n Note) String() string {
	return fmt.Sprintf("%s%d", n.Pitch.String(), n.Octave)
}

type Notes []Note

func (s SemiTone) IntervalBetweenFrequencies() {
	panic("not implemented yet")
}

type Accidental uint8

const (
	Natural Accidental = iota + 1
	Sharp
	Flat
	DoubleSharp
	DoubleFlat
)

type Degree string

const (
	Tonic       = Degree("I")
	Supertonic  = Degree("ii")
	Mediant     = Degree("iii")
	Subdominant = Degree("IV")
	Dominant    = Degree("V")
	Submediant  = Degree("vi")
	Subtonic    = Degree("vii")
)

var (
	MajorScale = AbsoluteInterval{
		WholeTone, // Supertonic
		WholeTone, // Mediant
		HalfTone,  // Subdominant
		WholeTone, // Dominant
		WholeTone, // Submediant
		WholeTone, // Leading tone
		HalfTone,  // Tonic / Octave
	}

	// TODO Natural minor scale vs harmonic minor scale (https://en.wikipedia.org/wiki/Minor_scale)
	MinorScale = AbsoluteInterval{
		WholeTone,
		HalfTone,
		WholeTone,
		WholeTone,
		HalfTone,
		WholeTone,
		WholeTone,
	}
)

type Scale struct {
	TonicKeyNote              Note
	IntervalsFromTonicKeyNote PitchInterval
}

func (s Scale) Degrees() Notes    { return s.TonicKeyNote.RaiseAccordingTo(s.IntervalsFromTonicKeyNote) }
func (s Scale) Supertonic() Note  { return s.Degrees()[1] }
func (s Scale) Mediant() Note     { return s.Degrees()[2] }
func (s Scale) Subdominant() Note { return s.Degrees()[3] }
func (s Scale) Dominant() Note    { return s.Degrees()[4] }
func (s Scale) Submediant() Note  { return s.Degrees()[5] }
func (s Scale) LeadingTone() Note { return s.Degrees()[6] }
func (s Scale) Octave() Note      { return s.Degrees()[7] }

type Letter rune

func (l Letter) Pitch(optionalAccidental ...Accidental) Pitch {
	accidental := Natural

	if optionalAccidental != nil {
		accidental = optionalAccidental[0]
	}

	return Pitch{
		Letter:     l,
		Accidental: accidental,
	}
}

type Pitch struct {
	Letter     Letter
	Accidental Accidental
}

func (p Pitch) AtOctave(octave Octave) Note {
	return NewNote(p, octave)
}

func (p Pitch) Sharp() Pitch {
	return p.Letter.Pitch(Sharp)
}

func (p Pitch) Flat() Pitch {
	return p.Letter.Pitch(Flat)
}

func (p Pitch) Natural() Pitch {
	return p.Letter.Pitch(Natural)
}

func (p Pitch) Note(octave Octave) Note {
	// return NewNote(p, octave)
	panic("not implemented")
}

func (p Pitch) RaiseBy(number int) Pitch {
	// this + SemiTone(number)
	panic("not implemented")
}

func (p Pitch) MoveUpFrom(semiTone SemiTone) Pitch {
	// CircularPitchClass.moveUpFrom(this, semiTone)
	panic("not implemented")
}

func (p Pitch) LowerBy(number int) Pitch {
	// this - SemiTone(number)
	panic("not implemented")
}

func (p Pitch) MoveDownFrom(semiTone SemiTone) Pitch {
	// CircularPitchClass.moveDownFrom(this, semiTone)
	panic("not implemented")
}

func (p Pitch) Interval(pitch Pitch) {
	// SemiTone(CircularPitchClass.indexOf(pitch) - CircularPitchClass.indexOf(this))
	panic("not implemented, figure out return type")
}

func (p Pitch) String() string {
	display := func(s string) string {
		return string(p.Letter) + string(p.Accidental)
	}
	switch p.Accidental {
	case Sharp:
		return display("♯")
	case Flat:
		return display("♭")
	case Natural:
		return display("")
	case DoubleFlat:
		return display("♭♭") // N.B. Double flat unicode work?
	case DoubleSharp:
		return display("♯♯") // N.B. Double sharp unicode work?
	}

	return "<unknown pitch>"
}

// TODO replace with whatever resolves the TODO for Key
type Pitches []Pitch

// TODO These should be chords rather than pitches
// (in other words, the perfect fifth interval should be calculated from the tonic of each chord)
type Key Pitch

func (k Key) Pitch() Pitch { return Pitch(k) }
func (k Key) IV() Pitch    { return k.Pitch().MoveDownFrom(PerfectFifth) }
func (k Key) I() Pitch     { return Pitch(k) }
func (k Key) V() Pitch     { return k.Pitch().MoveUpFrom(PerfectFifth) }

// N.B. II - VII should be lower case in accordance with proper musical
// notation but we want these methods to be exported to we have capitalized
// them
func (k Key) II() Pitch  { return k.V().MoveUpFrom(PerfectFifth) }
func (k Key) VI() Pitch  { return k.II().MoveUpFrom(PerfectFifth) }
func (k Key) III() Pitch { return k.VI().MoveUpFrom(PerfectFifth) }
func (k Key) VII() Pitch { return k.III().MoveUpFrom(PerfectFifth) }

func (k Key) Major() Pitches      { return Pitches{k.IV(), k.I(), k.V()} }
func (k Key) Minor() Pitches      { return Pitches{k.II(), k.VI(), k.III()} }
func (k Key) Diminished() Pitches { return Pitches{k.VII()} }

func (k Key) Chords() Pitches { return append(k.Major(), append(k.Minor(), k.Diminished()...)...) }

var PitchSpace = func() []Note {
	// Octave.Min.to(Octave.Max).flatMap { octave =>
	//   CircularPitchClass.pitches.map { pitch =>
	//     Note(pitch, octave)
	//   }
	// }
	panic("not implemented")
}()

var (
	C = Letter('C').Pitch()
	D = Letter('D').Pitch()
	E = Letter('E').Pitch()
	F = Letter('F').Pitch()
	G = Letter('G').Pitch()
	A = Letter('A').Pitch()
	B = Letter('B').Pitch()
)

// https://en.wikipedia.org/wiki/Seventh_chord
//
// Tertian:
// Cmaj⁷   — Major seventh (major/major)
// Cmin⁷   — Minor seventh (minor/minor)
// C⁷      — Dominant seventh (major/minor)
// C°⁷     — Diminished seventh
// Cm⁷     — Half-diminished seventh (diminished/minor)
// Cm maj⁷ - Minor major seventh
// Cmaj⁷⁽⁵⁾  – Augmented major seventh
//
// Non-tertian:
// Caug⁷   — Augmented seventh (augmented/minor)
// CmM7♭5 — Diminished major seventh
// C7♭5   — Dominant seventh flat five
type Seventh struct {
	Triad Triad
	Note  Note
}

var (
	DominantSeventh = AbsoluteInterval{
		MajorThird,
		PerfectFifth,
		MinorSeventh,
	}

	MajorSeventhSeventh = AbsoluteInterval{
		MajorThird,
		PerfectFifth,
		MajorSeventh,
	}

	MinorMajorSeventh = AbsoluteInterval{
		MinorThird,
		PerfectFifth,
		MajorSeventh,
	}

	MinorSeventhSeventh = AbsoluteInterval{
		MinorThird,
		PerfectFifth,
		MinorSeventh,
	}

	AugmentedMajorSeventh = AbsoluteInterval{
		MajorThird,
		AugmentedFifth,
		MajorSeventh,
	}

	AugmentedSeventhSeventh = AbsoluteInterval{
		MajorThird,
		AugmentedFifth,
		MinorSeventh,
	}

	HalfDiminishedSeventh = AbsoluteInterval{
		MinorThird,
		DiminishedFifth,
		MinorSeventh,
	}

	DiminishedSeventhSeventh = AbsoluteInterval{
		MinorThird,
		DiminishedFifth,
		DiminishedSeventh,
	}

	DominantFlatFiveSeventh = AbsoluteInterval{
		MajorThird,
		DiminishedFifth,
		MinorSeventh,
	}
)

type Triad [3]Note

func (t Triad) Root() Note  { return t[0] }
func (t Triad) Third() Note { return t[1] }
func (t Triad) Fifth() Note { return t[2] }

func (t Triad) Inversion(number int) {
	panic("not implemented")
}

var (
	MajorTriad = StackedInterval{
		MajorThird, // 4 semitones from root
		MinorThird, // perfect fifth, 7 semitones from root
	}

	MinorTriad = StackedInterval{
		MinorThird, // 3 semitones from root
		MajorThird, // perfect fifth, 7 semitones from root
	}

	DiminishedTriad = StackedInterval{
		MinorThird, // 3 semitones from root
		MinorThird, // diminished fifth, 6 semitones from root
	}

	AugmentedTriad = StackedInterval{
		MajorThird, // 4 semitones from root
		MajorThird, // augmented fifth, 8 semitones from root
	}
)

const (
	TwelfthRootOf2 = 1.0594630943592953 // 2^(1/12)
	LowestOctave   = 0
	HighestOctave  = 8
)

// Octave is two notes that have a frequency ratio of 2:1. An octave spans 12
// semitones or 1200 cents.
type Octave uint8

func (o Octave) Interval(otherOctave Octave) Octave {
	return otherOctave - o
}

func (o Octave) Note(pitch Pitch) Note {
	return NewNote(pitch, o)
}

// TODO Altered chords (https://en.wikipedia.org/wiki/Chord_(music)#Altered_chords)
// TODO Suspended chords (https://en.wikipedia.org/wiki/Chord_(music)#Suspended_chords)
// a Chord is a combination of three or more tones sounded simultaneously
type Chord struct {
	Root    Note
	Quality chord.Quality
	Number  chord.IntervalNumber
}

var CircularPitchClass = Pitches{
	C,
	C.Sharp(),
	D,
	D.Sharp(),
	E,
	F,
	F.Sharp(),
	G,
	G.Sharp(),
	A,
	A.Sharp(),
	B,
}

func (p Pitches) Indexes() map[Pitch]int {
	// p.zipWithIndex.toMap
	panic("not implemented")
}

func (p Pitches) MoveUpFrom(pitch Pitch, semiTone SemiTone) Pitch {
	index := p.indexOf(pitch)
	indexToMoveTo := (index + int(semiTone)) % 12
	return p[indexToMoveTo]
}

func (p Pitches) MoveDownFrom(pitch Pitch, semiTone SemiTone) Pitch {
	index := p.indexOf(pitch)
	indexToMoveTo := (index - int(semiTone)) % 12
	destination := indexToMoveTo
	if indexToMoveTo < 0 {
		destination = len(p) + indexToMoveTo
	}
	return p[destination]
}

func (p Pitches) MoveFrom(pitch Pitch, semiTone SemiTone) Pitch {
	if semiTone == 0 {
		return pitch
	}

	if semiTone > 0 {
		return p.MoveUpFrom(pitch, semiTone)
	}

	return p.MoveDownFrom(pitch, SemiTone(-semiTone))
}

func (p Pitches) indexOf(pitch Pitch) int {
	// switch pitch.Accidental {
	// case Flat :
	//     val natural = pitch.♮
	//     val index   = indexes(natural)
	//     if (index != 0) {
	//       indexes(natural) - 1
	//     } else {
	//       indexOf(B)
	//     }
	// default:
	//     return indexes(pitch)
	// }
	panic("not implmeented")
}
