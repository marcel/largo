package com.andbutso.largo.midi

import com.andbutso.largo.{SemiTone, Pitch, Frequency, Hertz}

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
}