package com.andbutso.largo.midi

import com.andbutso.largo.{SemiTone, Pitch, Frequency, Hertz}

import scala.collection.immutable.SortedSet

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

  def on(channel: Int, velocity: Int) = {

  }

  def off(channel: Int, velocity: Int) = {

  }
}

trait Message {
  def status: Status
  def bytes: Seq[Byte]
}


object Function extends Enumeration {
  val LowerBound = 127

  // Channel Functions
  val Off                  = Value(0)
  val On                   = Value(1)
  val PolyphonicAftertouch = Value(2)
  val ControlModeChange    = Value(3)
  val ProgramChange        = Value(4)
  val ChannelAftertouch    = Value(5)
  val PitchBendChange      = Value(6)

  // Non-Channel Functions
  val SystemExclusive      = Value(240)
  val MIDITimeCodeQtrFrame = Value(241)
  val SongPositionPointer  = Value(242)
  val SongSelect           = Value(243)
  val TuneRequest          = Value(246)
  val EndOfSysEx           = Value(247)
  val TimingClock          = Value(248)
  val Start                = Value(250)
  val Continue             = Value(251)
  val Stop                 = Value(252)
  val ActiveSensing        = Value(254)
  val SystemReset          = Value(255)

  val NoteFunctions = SortedSet(
    Off,
    On,
    PolyphonicAftertouch
  )

  val ChannelFunctions = SortedSet(
    Off,
    On,
    PolyphonicAftertouch,
    ControlModeChange,
    ProgramChange,
    ChannelAftertouch,
    PitchBendChange
  )

  val NonChannelFunctions = SortedSet(
    SystemExclusive,
    MIDITimeCodeQtrFrame,
    SongPositionPointer,
    SongSelect,
    TuneRequest,
    EndOfSysEx,
    TimingClock,
    Start,
    Continue,
    Stop,
    ActiveSensing,
    SystemReset
  )

  val All = ChannelFunctions ++ NonChannelFunctions

  implicit def functionToRichFunction(function: Function.Value): RichFunction = {
    RichFunction(function)
  }

  case class RichFunction(function: Value) {
    def statusByteOffset = {
      Function.LowerBound + (Channel.Max * function.id)
    }

    def isChannelFunction = {
      ChannelFunctions.contains(function)
    }
  }
}

object Channel {
  val Min = 1
  val Max = 16
}

case class Channel(number: Int) {
  require(number >= Channel.Min && number <= Channel.Max)
  def apply(function: Function.Value) = {
    ChannelStatus(this, function)
  }
}

object Status {
  val MinNonChannelFunctionStatusValue = Function.NonChannelFunctions.head.id

  def isChannelStatus(value: Int) = {
    value < MinNonChannelFunctionStatusValue
  }

  def fromByte(byte: Byte): Status = {
    val asInt = byte & 0xFF
    if (isChannelStatus(asInt)) {
      val minusOffset = asInt - Function.LowerBound
      val channel     = Channel(minusOffset % Channel.Max)
      val function    = Function.ChannelFunctions.toIndexedSeq((minusOffset - channel.number) / Channel.Max)

      ChannelStatus(channel, function)
    } else {
      NonChannelStatus(Function(asInt))
    }
  }
}

trait Status {
  def function: Function.Value
  def toByte: Byte
}

case class ChannelStatus(channel: Channel, function: Function.Value) extends Status {
  def toByte = {
    (channel.number + function.statusByteOffset).toByte
  }
}

case class NonChannelStatus(function: Function.Value) extends Status {
  def toByte = {
    function.id.toByte
  }
}