package com.andbutso.largo

import javax.sound.midi.{MidiSystem, MidiChannel}
import java.util.concurrent.{ConcurrentLinkedQueue, Executors}

// MIDI_note_number    Name in music    Notes
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

object Note {
//  val all = IndexedSeq(
//    C,
//
//  )

  val C  = Note(60) // Middle C
  val D  = Note(62)
  val E  = Note(64)
  val F  = Note(65)
  val G  = Note(67)
  val A  = Note(69)
  val B  = Note(71)
}
//
//import Note._
//
//object ChromaticScale extends Scale(
//  Seq(C, C.sharp, D, D.sharp, E, F, F.sharp, G, G.sharp, A, A.sharp, B)
//) {
//
//}

// TODO Note should include duration; this is currently just a Pitch
case class Note(number: Int) {
  def sharp = this + 1
  def s     = sharp
  def flat  = this - 1
  def b     = flat

  def moveOctave(offset: Int) = {
    this + (PitchInterval.octave * offset)
  }

  def ++(noteOffset: PitchInterval) = {
    noteOffset.offsets.foldLeft(Seq(this)) { case (notes, offset) =>
      notes :+ (notes.last + offset.interval)
    }
  }

  def +(offset: Int) = {
    Note(number + offset)
  }

  def -(offset: Int) = {
    Note(number - offset)
  }

//  def majorChord = {
//    majorScale.chord
//  }
//
//  def majorScale = {
//    Scale(this ++ PitchInterval.majorScale)
//  }
//
//  def minorScale = {
//    Scale(this ++ PitchInterval.minorScale)
//  }
//
//  def minorChord = {
//    minorScale.chord
//  }
}

// TODO DiatonicScale
//case class Scale(notes: Seq[Note]) {
//  def chord = {
//    val scale = notes.toIndexedSeq
//    Chord(PitchInterval.triad map { index => scale(index) })
//  }
//}

class PlayerPiano {
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
  def play(notes: Seq[Note], duration: Int = defaultDuration): Unit = {
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

//  val triad = Seq(0, 2, 4)
  object Triad {
    val major = Seq()
    val minor = Seq()
    val diminished = Seq()
    val augmented = Seq()
  }

  val majorScale = PitchInterval(
    Seq(
      wholeTone, // Supertonic
      wholeTone, // Mediant
      halfTone,  // Subdominant
      wholeTone, // Dominant
      wholeTone, // Submediant
      wholeTone, // Leading tone
      halfTone   // Tonic
    )
  )
  // TODO Natural minor scale vs harmonic minor scale (https://en.wikipedia.org/wiki/Minor_scale)
  val minorScale = PitchInterval(Seq(wholeTone, halfTone, wholeTone, wholeTone, halfTone, wholeTone, wholeTone))
}

case class Chord(notes: Seq[Note])

object Accidental extends Enumeration {
  val Natural, Sharp, Flat, DoubleSharp, DoubleFlat = Value
}

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

case class MajorScale(tonicKeyNote: Pitch) {
  import PitchInterval.{halfTone, wholeTone}

  val supertonic  = tonicKeyNote + wholeTone
  val mediant     = supertonic   + wholeTone
  val subdominant = mediant      + halfTone
  val dominant    = subdominant  + wholeTone
  val submediant  = dominant     + wholeTone
  val leadingTone = submediant   + wholeTone
  val tonic       = leadingTone  + halfTone

  val pitches = Seq(
    tonicKeyNote, supertonic, mediant, subdominant, dominant, submediant, leadingTone, tonic
  )
}

case class Pitch(
  letter: Symbol,
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
}

object Triad extends Enumeration {
  val Major, Minor, Diminished, Augmented = Value
}

object Pitch {
  val C = Pitch('C)
  val D = Pitch('D)
  val E = Pitch('E)
  val F = Pitch('F)
  val G = Pitch('G)
  val A = Pitch('A)
  val B = Pitch('B)
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

  def indexOf(pitch: Pitch) = {
    pitch.accidental match {
      case Flat =>
        indexes(pitch♮) - 1 // TODO Doesn't work for C♭
      case _ =>
        indexes(pitch)
    }
  }
}

case class SemiTone(interval: Int)