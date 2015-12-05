package com.andbutso.largo

import javax.sound.midi._
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
  val C  = Note(60)
  val Cs = C.sharp
  val Db = Cs
  val D  = Note(62)
  val Ds = D.sharp
  val Eb = Ds
  val E  = Note(64)
  val F  = Note(65)
  val Fs = F.sharp
  val Gb = Fs
  val G  = Note(67)
  val Gs = G.sharp
  val Ab = Gs
  val A  = Note(69)
  val As = A.sharp
  val Bb = As
  val B  = Note(71)
}

case class Note(number: Int) {
  def sharp = this + 1
  def s     = sharp
  def flat  = this - 1
  def b     = flat

  def moveOctave(offset: Int) = {
    this + (NoteOffset.octave * offset)
  }

  def ++(noteOffset: NoteOffset) = {
    noteOffset.offsets.foldLeft(Seq(this)) { case (notes, offset) =>
      notes :+ (notes.last + offset)
    }
  }

  def +(offset: Int) = {
    Note(number + offset)
  }

  def -(offset: Int) = {
    Note(number - offset)
  }

  def majorChord = {
    majorScale.chord
  }

  def majorScale = {
    Scale(this ++ NoteOffset.majorScale)
  }

  def minorScale = {
    Scale(this ++ NoteOffset.minorScale)
  }

  def minorChord = {
    minorScale.chord
  }
}

case class Scale(notes: Seq[Note]) {
  def chord = {
    val scale = notes.toIndexedSeq
    Chord(NoteOffset.triad map { index => scale(index) })
  }
}

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

case class NoteOffset(offsets: Seq[Int])

object NoteOffset {
  val triad = Seq(0, 2, 4)
  val octave = 12
  val majorScale = NoteOffset(Seq(2, 2, 1, 2, 2, 2, 1))
  val minorScale = NoteOffset(Seq(2, 1, 2, 2, 1, 2, 2))
}

case class Chord(notes: Seq[Note])
