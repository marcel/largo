package com.andbutso.largo

import javax.sound.midi.{MidiSystem, MidiChannel}
import java.util.concurrent.{ConcurrentLinkedQueue, Executors}

import scala.annotation.tailrec

// TODO DiatonicScale

case class PitchInterval(offsets: Seq[SemiTone])

// Major chord
//  major triad
//  major seventh
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

object Scale {
  import PitchInterval._

  val major = PitchInterval(
    Seq(
      wholeTone, // Supertonic
      wholeTone, // Mediant
      halfTone,  // Subdominant
      wholeTone, // Dominant
      wholeTone, // Submediant
      wholeTone, // Leading tone
      halfTone   // Tonic / Octave
    )
  )
  // TODO Natural minor scale vs harmonic minor scale (https://en.wikipedia.org/wiki/Minor_scale)
  val minor = PitchInterval(
    Seq(
      wholeTone,
      halfTone,
      wholeTone,
      wholeTone,
      halfTone,
      wholeTone,
      wholeTone
    )
  )
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

object Triad {
  import PitchInterval._

  val major = PitchInterval(
    Seq(
      majorThird, // 4 semitones from root
      minorThird  // perfect fifth, 7 semitones from root
    )
  )

  val minor = PitchInterval(
    Seq(
      minorThird, // 3 semitones from root
      majorThird  // perfect fifth, 7 semitones from root
    )
  )

  val diminished = PitchInterval(
    Seq(
      minorThird, // 3 semitones from root
      minorThird  // diminished fifth, 6 semitones from root
    )
  )

  val augmented = PitchInterval(
    Seq(
      majorThird, // 4 semitones from root
      majorThird  // augmented fifth, 8 semitones from root
    )
  )
}

case class Triad(notes: IndexedSeq[Note]) {
  def root  = notes(0)
  def third = notes(1)
  def fifth = notes(2)
}

// TODO Implement all
// https://en.wikipedia.org/wiki/Seventh_chord
//
// Tertian:
// Cmaj⁷   — Major seventh
// Cmin⁷   — Minor seventh
// C⁷      — Dominant seventh
// C°⁷     — Diminished seventh
// Cm⁷     — Half-diminished seventh
// Cm maj⁷ - Minor major seventh
// Cmaj⁷⁽⁵⁾  – Augmented major seventh
//
// Non-tertian:
// Caug⁷   — Augmented seventh
// CmM7♭5 — Diminished major seventh
// C7♭5   — Dominant seventh flat five
case class Seventh(triad: Triad, seventh: Note)

//case class MajorScale(tonicKeyNote: Pitch) {
//  import PitchInterval.{halfTone, wholeTone}
//
//  val supertonic  = tonicKeyNote + wholeTone
//  val mediant     = supertonic   + wholeTone
//  val subdominant = mediant      + halfTone
//  val dominant    = subdominant  + wholeTone
//  val submediant  = dominant     + wholeTone
//  val leadingTone = submediant   + wholeTone
//  val tonic       = leadingTone  + halfTone
//
//  val pitches = Seq(
//    tonicKeyNote, supertonic, mediant, subdominant, dominant, submediant, leadingTone, tonic
//  )
//}

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

  def |(octave: Octave) = {
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

// Octave is two notes that have a frequency ration of 2:1
// Octave spans 12 semitones or 1200 cents

// TODO Extract these to locations that suit each as their umbrella abstractions are implemented
object MiscConversion {

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

// MIDI note number    Name in music    Notes
// ----------------    -------------    --------------------------
//  21                 A0               lowest note on an 88-key piano
//   .                 .                .
//   .                 .                .
//   .                 .                .
//  57                 A3               has a frequency of 220 Hertz
//  58                 A3# = B3b
//  59                 B3
//  60                 C4               "middle C"; start of 4th octave
//  61                 C4# = D4b
//  62                 D4
//  63                 D4# = E4b
//  64                 E4
//  65                 F4
//  66                 F4# = G4b
//  67                 G4
//  68                 G4# = A4b
//  69                 A4               "concert A"; has a frequency of 440 Hertz
//  70                 A4# = B4b
//  71                 B4
//  72                 C5               "concert C"; start of 5th octave
//  73                 C5# = D5b
//   .                 .                .
//   .                 .                .
//   .                 .                .
// 108                 C8               highest note on an 88-key piano
object Midi {
  object Note {
    val ZeroInHertz = 8.1758
    
    val ConcertPitch = 69 // A4

    def octavesFromConcertPitch(frequency: Hertz) = {
      Frequency.logarithmicRatio(Pitch.Frequency.ConcertPitch, frequency)
    }

    def fromFrequency(frequency: Hertz) = {
      Note(
        Math.round(
          ConcertPitch + (SemiTone.perOctave * octavesFromConcertPitch(frequency))
        ).toInt
      )
    }
  }

  case class Note(number: Int) {
    def ♯ = Note(number + 1)
    def ♭ = Note(number - 1)

    def toFrequency = {
      val deltaFromConcertPitch = number - Note.ConcertPitch
      val rawHertz = Math.pow(2, deltaFromConcertPitch / SemiTone.perOctave.toFloat) * Pitch.Frequency.ConcertPitch
      Math.round(rawHertz * 100) / 100.0 // Round to 2 places
    }

    //  def majorChord = {
    //    majorScale.chord
    //  }
    //
    //
    //  def minorChord = {
    //    minorScale.chord
    //  }
  }

  case class Chord(notes: Seq[Midi.Note])

  class Player {
    val synth = MidiSystem.getSynthesizer
    synth.open()

    val channels = new ConcurrentLinkedQueue[MidiChannel]
    synth.getChannels.take(8) foreach { channel =>
      channels.offer(channel)
    }

    val pool = Executors.newFixedThreadPool(channels.size())

    val defaultVolume = 80
    val defaultDuration = 200 // Milliseconds

    def onChannel[T](handler: MidiChannel => T): Unit = {
      pool.execute(
        new Runnable {
          override def run(): Unit = {
            val channel = channels.poll()
            handler(channel)
            channels.offer(channel)
          }
        }
      )
    }
    def play(notes: Seq[Midi.Note], duration: Int = defaultDuration): Unit = {
      onChannel { channel =>
        notes foreach { note =>
          channel.noteOn(note.number, defaultVolume)
          Thread.sleep(duration)
          channel.noteOff(note.number)
        }
      }
    }

    def play(chord: Chord, duration: Int): Unit = {
      onChannel { channel =>
        chord.notes foreach { note =>
          channel.noteOn(note.number, defaultVolume)
        }

        Thread.sleep(duration)
        channel.allNotesOff()
      }
    }
  }
}

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
    Pitch.Frequency.ConcertPitch * Math.pow(Octave.TwelthRootOf2, intervalFromConcertPitch.interval)
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
    intervals.offsets.foldLeft(IndexedSeq(this)) { case (notes, offset) =>
      notes :+ (notes.last + offset.interval)
    }
  }

  def majorScale = {
    this ++ PitchInterval.majorScale
  }

  def minorScale = {
    this ++ PitchInterval.minorScale
  }

  override def toString = {
    s"$pitch${octave.value}"
  }
}

object Octave {
  val TwelthRootOf2 = Math.pow(2, 1/12.0)
  val Min = 0
  val Max = 8

  implicit def fromOctaveToInt(octave: Octave): Int = octave.value
  implicit def fromIntToOctave(int: Int): Octave    = Octave(int)
}

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
