package com.andbutso.largo

import javax.sound.midi.{MidiSystem, MidiChannel}
import java.util.concurrent.{ConcurrentLinkedQueue, Executors}

import com.andbutso.largo

import scala.annotation.tailrec

trait PitchInterval {
  def intervals: Seq[SemiTone]
  def apply(note: Note): Seq[Note]
}

class AbsoluteInterval(val intervals: Seq[SemiTone]) extends PitchInterval {
  def apply(note: Note) = {
    intervals.foldLeft(Seq(note)) { case (notes, interval) =>
      notes :+ (note + interval)
    }
  }
}

object AbsoluteInterval {
  def apply(semiTones: SemiTone*) = new AbsoluteInterval(semiTones)
}

class StackedInterval(val intervals: Seq[SemiTone]) extends PitchInterval {
  def apply(note: Note) = {
    intervals.foldLeft(Seq(note)) { case (notes, interval) =>
      notes :+ (notes.last + interval)
    }
  }
}

object StackedInterval {
  def apply(semiTones: SemiTone*) = new StackedInterval(semiTones)
}

object PitchInterval {
  // Intervals of extended meantone temperament
  // Name                 Interval          Pitch (from C)   Roman No.
  val unison            = SemiTone(0)       // C             I
  val diminishedSecond  = unison            // D♭♭
  val chromaticSemiTone = SemiTone(1)       // C♯
  val minorSecond       = chromaticSemiTone // D♭
  val halfTone          = minorSecond
  val wholeTone         = SemiTone(2)       // D             II
  val diminishedThird   = wholeTone         // E♭♭
  val augmentedSecond   = SemiTone(3)       // D♯
  val minorThird        = augmentedSecond   // E♭
  val majorThird        = SemiTone(4)       // E             III
  val diminishedFourth  = majorThird        // F♭
  val augmentedThird    = SemiTone(5)       // E♯
  val perfectFourth     = augmentedThird    // F             IV
  val augmentedFourth   = SemiTone(6)       // F♯
  val diminishedFifth   = augmentedFourth   // G♭
  val perfectFifth      = SemiTone(7)       // G             V
  val diminishedSixth   = perfectFifth      // A♭♭
  val augmentedFifth    = SemiTone(8)       // G♯
  val minorSixth        = augmentedFifth    // A♭
  val majorSixth        = SemiTone(9)       // A             VI
  val diminishedSeventh = majorSixth        // B♭♭
  val augmentedSixth    = SemiTone(10)      // A♯
  val minorSeventh      = augmentedSixth    // B♭
  val majorSeventh      = SemiTone(11)      // B             VII
  val diminishedOctave  = majorSeventh      // C♭
  val augmentedSeventh  = SemiTone(12)      // B♯
  val octave            = augmentedSeventh  // C             VIII
}

object Accidental extends Enumeration {
  val Natural, Sharp, Flat, DoubleSharp, DoubleFlat = Value
}

// TODO DiatonicScale

object Scale {
  import PitchInterval._

  val major = StackedInterval(
    wholeTone, // Supertonic
    wholeTone, // Mediant
    halfTone,  // Subdominant
    wholeTone, // Dominant
    wholeTone, // Submediant
    wholeTone, // Leading tone
    halfTone   // Tonic / Octave
  )
  // TODO Natural minor scale vs harmonic minor scale (https://en.wikipedia.org/wiki/Minor_scale)
  val minor = StackedInterval(
    wholeTone,
    halfTone,
    wholeTone,
    wholeTone,
    halfTone,
    wholeTone,
    wholeTone
  )

  object Degree extends Enumeration {
    val Tonic       = Value("I")
    val Supertonic  = Value("ii")
    val Mediant     = Value("iii")
    val Subdominant = Value("IV")
    val Dominant    = Value("V")
    val Submediant  = Value("vi")
    val Subtonic    = Value("vii")
  }
}

abstract class Scale(tonicKeyNote: Note, intervalsFromTonicKeyNote: PitchInterval) {
  val degrees = tonicKeyNote ++ intervalsFromTonicKeyNote
  def supertonic  = degrees(1)
  def mediant     = degrees(2)
  def subdominant = degrees(3)
  def dominant    = degrees(4)
  def submediant  = degrees(5)
  def leadingTone = degrees(6)
  def octave      = degrees(7)
}

case class MajorScale(tonicKeyNote: Note) extends Scale(tonicKeyNote, Scale.major)
case class MinorScale(tonicKeyNote: Note) extends Scale(tonicKeyNote, Scale.minor)

import Accidental._

// TODO These should be chords rather than pitches
// (in other words, the perfect fifth interval should be calculated from the tonic of each chord)
case class Key(pitch: Pitch) {
  import PitchInterval.perfectFifth

  val IV  = pitch - perfectFifth
  val I   = pitch
  val V   = pitch + perfectFifth
  val ii  = V     + perfectFifth
  val vi  = ii    + perfectFifth
  val iii = vi    + perfectFifth
  val vii = iii   + perfectFifth

  val major      = Seq(IV, I, V)
  val minor      = Seq(ii, vi, iii)
  val diminished = Seq(vii)

  val chords = major ++ minor ++ diminished
}

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
case class Seventh(triad: Triad, seventh: Note)

// TODO Given a set of notes be able to detect based on the interval between them
// if they are any of the defined Triads or Sevenths or scales

object Seventh {
  import PitchInterval._

  val dominant = AbsoluteInterval(
    majorThird,
    perfectFifth,
    minorSeventh
  )

  val major = AbsoluteInterval(
    majorThird,
    perfectFifth,
    majorSeventh
  )

  val minorMajor = AbsoluteInterval(
    minorThird,
    perfectFifth,
    majorSeventh
  )

  val minor = AbsoluteInterval(
    minorThird,
    perfectFifth,
    minorSeventh
  )

  val augmentedMajor = AbsoluteInterval(
    majorThird,
    augmentedFifth,
    majorSeventh
  )

  val augmented = AbsoluteInterval(
    majorThird,
    augmentedFifth,
    minorSeventh
  )

  val halfDiminished = AbsoluteInterval(
    minorThird,
    diminishedFifth,
    minorSeventh
  )

  val diminished = AbsoluteInterval(
    minorThird,
    diminishedFifth,
    diminishedSeventh
  )

  val dominantFlatFive = AbsoluteInterval(
    majorThird,
    diminishedFifth,
    minorSeventh
  )
}

// TODO Altered chords (https://en.wikipedia.org/wiki/Chord_(music)#Altered_chords)
// TODO Suspended chords (https://en.wikipedia.org/wiki/Chord_(music)#Suspended_chords)

object Chord {
  object Quality extends Enumeration {
    val Major          = Value("maj") // Δ, M
    val Minor          = Value("min") // -
    val Diminished     = Value("dim") // o, °
    val HalfDiminished = Value("ø")   // ø, Ø
    val Dominant       = Value("dom") // 7
    val Augmented      = Value("aug") // +
  }

  object IntervalNumber extends Enumeration {
    val Third         = Value
    val Triad         = Third
    val Fifth         = Value("⁵")
    val Indeterminate = Fifth
    val Neutral       = Fifth
    val Sixth         = Value("⁶")
    val Seventh       = Value("⁷")
    val Ninth         = Value("⁹")
    val Eleventh      = Value("¹¹")
    val Thirteenth    = Value("¹³")
  }

  object Category extends Enumeration {
    // Secundal chords can be decomposed into a series of (major or minor) seconds.
    // For example, the chord C-D-E♭ is a series of seconds, containing a major second (C-D) and a minor second (D-E♭)
    val Secundal = Value // 2nd's: major 2nd, minor 2nd
    // Tertian chords can be decomposed into a series of (major or minor) thirds.
    // For example, the C major triad (C-E-G) is defined by a sequence of two intervals, the first (C-E) being
    // a major third and the second (E-G) being a minor third. Most common chords are tertian.
    val Tertian  = Value // 3rd's: major 3rd, minor 3rd
    // Quartal chords can be decomposed into a series of (perfect or augmented) fourths.
    // Quartal harmony normally works with a combination of perfect and augmented fourths.
    // Diminished fourths are enharmonically equivalent to major thirds, so they are uncommon.
    // For example, the chord C-F-B is a series of fourths, containing a perfect fourth (C-F) and
    // an augmented fourth/tritone (F-B).
    val Quartal  = Value // 4th's: perfect 4th, augmented 4th
    val Quintal  = Value // 5th's
  }
}
// "A chord is a combination of three or more tones sounded simultaneously"
case class Chord(
  root: Note,
  quality: Chord.Quality.Value,
  number: Chord.IntervalNumber.Value
) {
  // TODO Implement inversion
  def inversion(number: Int) = ???
}

object Triad {
  import PitchInterval._

  val major = StackedInterval(
    majorThird, // 4 semitones from root
    minorThird  // perfect fifth, 7 semitones from root
  )

  val minor = StackedInterval(
    minorThird, // 3 semitones from root
    majorThird  // perfect fifth, 7 semitones from root
  )

  val diminished = StackedInterval(
    minorThird, // 3 semitones from root
    minorThird  // diminished fifth, 6 semitones from root
  )

  val augmented = StackedInterval(
    majorThird, // 4 semitones from root
    majorThird  // augmented fifth, 8 semitones from root
  )
}

case class Triad(notes: IndexedSeq[Note]) {
  def root  = notes(0)
  def third = notes(1)
  def fifth = notes(2)

  // TODO Implement
  def inversion(number: Int) = ???
}

case class Pitch(
  letter: Char,
  accidental: Accidental.Value = Natural
) {
  def ♯ = {
    copy(accidental = Sharp)
  }

  def ♭ = {
    copy(accidental = Flat)
  }

  def ♮ = {
    copy(accidental = Natural)
  }

  def apply(octave: Octave) = {
    Note(this, octave)
  }

  def +(number: Int): Pitch = {
    this + SemiTone(number)
  }

  def +(semiTone: SemiTone): Pitch = {
    PitchClass.moveUpFrom(this, semiTone)
  }

  def -(number: Int): Pitch = {
    this - SemiTone(number)
  }

  def -(semiTone: SemiTone): Pitch = {
    PitchClass.moveDownFrom(this, semiTone)
  }

  def interval(pitch: Pitch) = {
    SemiTone(PitchClass.indexOf(pitch) - PitchClass.indexOf(this))
  }

  override def toString = {
    val accidentalString = accidental match {
      case Sharp       => "♯"
      case Flat        => "♭"
      case Natural     => ""
      case DoubleFlat  => "♭♭" // N.B. Double flat unicode doesn't work in IntelliJ
      case DoubleSharp => "♯♯" // N.B. Double sharp unicode doesn't work either
    }

    s"$letter$accidentalString"
  }
}

object Pitch {
  val C = Pitch('C')
  val D = Pitch('D')
  val E = Pitch('E')
  val F = Pitch('F')
  val G = Pitch('G')
  val A = Pitch('A')
  val B = Pitch('B')

  object Frequency {
    val C0 = 16.35D
    val A4 = 440.0D
    val ConcertPitch = A4
    val B8 = 7902.13D
  }
}

import Pitch._

object PitchClass extends PitchClass(
  IndexedSeq(
    C,
    C♯,
    D,
    D♯,
    E,
    F,
    F♯,
    G,
    G♯,
    A,
    A♯,
    B
  )
)

case class PitchClass(pitches: IndexedSeq[Pitch]) {
  val indexes = pitches.zipWithIndex.toMap

  def moveUpFrom(pitch: Pitch, semiTone: SemiTone) = {
    val index         = indexOf(pitch)
    val indexToMoveTo = (index + semiTone.interval) % 12
    pitches(indexToMoveTo)
  }

  def moveDownFrom(pitch: Pitch, semiTone: SemiTone) = {
    val index         = indexOf(pitch)
    val indexToMoveTo = (index - semiTone.interval) % 12
    val destination = if (indexToMoveTo < 0) pitches.size + indexToMoveTo else indexToMoveTo
    pitches(destination)
  }

  def moveFrom(pitch: Pitch, semiTone: SemiTone) = {
    semiTone.interval match {
      case 0 =>
        pitch
      case interval if interval > 0 =>
        moveUpFrom(pitch, semiTone)
      case interval =>
        moveDownFrom(pitch, SemiTone(-semiTone.interval))
    }
  }

  @tailrec
  final def indexOf(pitch: Pitch): Int = {
    pitch.accidental match {
      case Flat =>
        val natural = pitch.♮
        val index   = indexes(natural)
        if (index != 0) {
          indexes(natural) - 1
        } else {
          indexOf(B)
        }
      case _ =>
        indexes(pitch)
    }
  }
}

object SemiTone {
  val cents = 1200
  val perOctave = 12

  // e.g.
  //  val freqA = 130.81 (C3)
  //  val freqB = 261.63 (C4)
  //  intervalBetweenFrequencies(freqA, freqB)
  //  => SemiTone(12)
  def intervalBetweenFrequencies(freqA: Hertz, freqB: Hertz) = {
    SemiTone(Math.round(cents * Frequency.logarithmicRatio(freqA, freqB) / 100).toInt)
  }

  implicit def fromSemiToneToInt(semiTone: SemiTone): Int = semiTone.interval
  implicit def fromIntToSemiTone(int: Int): SemiTone      = SemiTone(int)
}

case class SemiTone(interval: Int)

object Note {
  val all = Octave.Min.to(Octave.Max).flatMap { octave =>
    PitchClass.pitches.map { pitch =>
      Note (pitch, octave)
    }
  }

  val ConcertPitch = Note(A, 4)

  def fromFrequency(frequency: Hertz) = {

  }
}

case class Note(pitch: Pitch, octave: Octave) extends Ordered[Note] {
  def toFrequency = {
    val intervalFromConcertPitch = Note.ConcertPitch.interval(this)
    Pitch.Frequency.ConcertPitch * Math.pow(Octave.TwelfthRootOf2, intervalFromConcertPitch.interval)
  }

  def index = {
    PitchClass.indexOf(pitch) + (octave.value * SemiTone.perOctave)
  }

  override def equals(obj: Any) = {
    obj match {
      case o: Note => o.index.equals(index)
      case _ => false
    }
  }

  override def hashCode = index.hashCode()

  def interval(otherNote: Note) = {
    val octaveInterval = octave.interval(otherNote.octave) * SemiTone.perOctave
    SemiTone(pitch.interval(otherNote.pitch).interval + octaveInterval)
  }

  def compare(otherNote: Note) = {
    0.compare(interval(otherNote).interval)
  }

  def +(interval: SemiTone) = {
    Note.all(index + interval.interval)
  }

  def ++(intervals: PitchInterval) = {
    intervals(this)
  }

  def majorScale = {
    this ++ Scale.major
  }

  def minorScale = {
    this ++ Scale.minor
  }

  override def toString = {
    s"$pitch${octave.value}"
  }
}

object Octave {
  val TwelfthRootOf2 = Math.pow(2, 1/12.0)
  val Min = 0
  val Max = 8

  implicit def fromOctaveToInt(octave: Octave): Int = octave.value
  implicit def fromIntToOctave(int: Int): Octave    = Octave(int)
}

// Octave is two notes that have a frequency ration of 2:1
// Octave spans 12 semitones or 1200 cents
case class Octave(value: Int) {
  require(value >= Octave.Min && value <= Octave.Max, s"$value out of bounds")

  def interval(octave: Octave) = {
    octave.value - value
  }

  def apply(pitch: Pitch) = {
    Note(pitch, this)
  }
}

// TODO
// KeySignature
// TimeSignature
