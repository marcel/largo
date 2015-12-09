package com.andbutso.largo.midi

import java.util.concurrent.{ConcurrentLinkedQueue, Executors}
import javax.sound.midi.{MidiChannel, MidiSystem}

import com.andbutso.largo.midi

case class Chord(notes: Seq[midi.Note])

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
  def play(notes: Seq[midi.Note], duration: Int = defaultDuration): Unit = {
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
